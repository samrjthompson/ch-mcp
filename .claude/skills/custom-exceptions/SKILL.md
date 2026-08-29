---
name: custom-exceptions
description: Sam's rule for exceptions — eight types in common/exception/, each a bare RuntimeException with two constructors, thrown by CompaniesHouseResponseHandler and translated at the @Tool boundary by ToolExceptionAspect and ToolExceptionMapper. Load before writing, throwing or catching an exception in this repo.
---

# Custom exceptions

Eight exception types in `common/exception/`, one throw site, one translation boundary.

## The eight

| Type | Meaning |
|---|---|
| `BadRequestException` | Companies House rejected the request as malformed (400) |
| `UnauthorizedException` | The Companies House API key was rejected (401) |
| `ForbiddenException` | Access to the requested resource is forbidden (403) |
| `NotFoundException` | Companies House has no record matching the request (404) |
| `TooManyRequestsException` | The Companies House rate limit has been exceeded (429) |
| `InternalServerErrorException` | Companies House reported its own internal error (500), or returned a status this app does not recognise |
| `BadGatewayException` | Companies House was unreachable or unavailable (502 or 503) |
| `ToolException` | The one type that actually leaves a `@Tool` method — everything above is translated into this before it reaches an MCP client |

Seven of the eight — everything but `ToolException` — exist to classify a single thing: the HTTP status
Companies House returned. That list isn't closed. Right now an unrecognised status falls back to
`InternalServerErrorException`, which is correct for the statuses nothing distinguishes yet — but if
Companies House starts returning one worth telling apart, add a ninth: a new class in `common/exception/`
with the same two-constructor shape, a new arm in `CompaniesHouseResponseHandler`'s switch, and a new case
in `ToolExceptionMapper`. The rule this skill actually enforces is the *shape* every type takes and the
single throw site, not a ceiling on how many there can be.

## The shape

All eight are identical but for the name:

```java
package io.github.samrjthompson.chmcp.common.exception;

public class BadGatewayException extends RuntimeException {

    public BadGatewayException(final String message) {
        super(message);
    }

    public BadGatewayException(final String message, Throwable cause) {
        super(message, cause);
    }
}
```

- `extends RuntimeException` specifically. Never `Exception`, never a checked exception, never a shared
  project base class.
- Exactly two constructors, both delegating straight to `super`. No more, no fewer.
- No fields. No status code, no error code, no context map, no builder. The type *is* the payload — the
  mapper reads the class and knows what message to produce.
- `final String message`, bare `Throwable cause`, per CLAUDE.md's `final` rule.

No shared base class, even though all eight are identical. A `BaseException` invites
`catch (BaseException exception)`, which collapses them back into one and forces anything downstream to
reopen the type to find out what happened. The duplication is eight short files and it never changes.

## Where they're thrown

There is exactly one throw site: `CompaniesHouseResponseHandler.checkStatus()`, in `client/`. It inspects
the HTTP status on every non-2xx response from Companies House and throws the matching type, one status to
one exception:

```java
switch (status) {
    case BAD_REQUEST -> throw new BadRequestException(msg);
    case UNAUTHORIZED -> throw new UnauthorizedException(msg);
    case FORBIDDEN -> throw new ForbiddenException(msg);
    case NOT_FOUND -> throw new NotFoundException(msg);
    case TOO_MANY_REQUESTS -> throw new TooManyRequestsException(msg);
    case INTERNAL_SERVER_ERROR -> throw new InternalServerErrorException(msg);
    case BAD_GATEWAY, SERVICE_UNAVAILABLE -> throw new BadGatewayException(msg);
    default -> throw new InternalServerErrorException(unexpectedStatusMessage);
}
```

502 and 503 both become `BadGatewayException` — both mean Companies House itself is unreachable or
unavailable, which is the distinction that matters here, not the exact code. Any status this switch
doesn't recognise also becomes `InternalServerErrorException`, on the basis that an unclassifiable upstream
response is closer to "something broke that we didn't plan for" than any of the named cases.

This is a single-upstream client: everything these seven types classify is Companies House's response.
There is no repository layer and no second backend, so there is no "our code broke" case among them —
that distinction doesn't arise in this codebase.

A `NotFoundException` here is not swallowed into an empty result. It propagates like any other
`RuntimeException`, reaches the `@Tool` boundary, and becomes an MCP tool error via the aspect below — an
MCP client asking for a specific company that doesn't exist gets told so, not handed a silent `null`.

Never catch one of the seven to steer logic elsewhere in the app. They exist to reach
`ToolExceptionAspect`; catching one to decide what to do next means the condition was expected, so it
should have been a return value instead.

## Throwing

Wrap the cause whenever you are inside a `catch`. The two-argument constructor exists for exactly this,
and dropping the cause throws away the only part of the stack trace that says what actually went wrong.
Messages name the operation and bracket the identifiers, built with `formatted` — the same style the rest
of the codebase logs in. Never put an API key, a credential or personal data in a message.

## `ToolExceptionMapper`

`@Component`, one method, `toErrorMessage(RuntimeException exception)`. A type-switch over the exception's
runtime type produces a fixed, safe message per type:

- Each of the seven domain exceptions above maps to its own constant message (e.g.
  `BadGatewayException` → `"Companies House could not be reached"`).
- `ConstraintViolationException` (Jakarta validation) maps to a dynamic message built by joining each
  violation's own message.
- Anything else falls back to a generic `"An unexpected error occurred while executing the tool"`.

It never throws. It only translates an exception to the `String` an MCP client will see.

## `ToolExceptionAspect`

`@Aspect @Component`, constructor-injected with `ToolExceptionMapper`. One piece of advice:

```java
@Around("@annotation(org.springframework.ai.tool.annotation.Tool)")
public Object translateToolExceptions(ProceedingJoinPoint joinPoint) throws Throwable {
    try {
        return joinPoint.proceed();
    } catch (RuntimeException exception) {
        throw new ToolException(toolExceptionMapper.toErrorMessage(exception), exception);
    }
}
```

The pointcut is annotation-based — it wraps every `@Tool`-annotated method in the app, in whichever class
declares it, not a package or naming convention. This is the sole exception-translation boundary in the
codebase; no `@Tool` method does its own try/catch (see the `tool-pattern` skill).

## Catching broadly, throwing narrowly

"Never use generic error types" governs what you *throw*, not what you *catch* — it's about the type you
construct, not the type in a catch clause.

`ToolExceptionAspect` above relies on this deliberately: `catch (RuntimeException exception)` is a broad
catch, on purpose, at a genuine boundary that funnels every possible failure into one safe outward signal.
An earlier version of this aspect named all seven domain types plus `ConstraintViolationException` in a
multi-catch instead — that added nothing, since the aspect never branches on the caught type itself (only
`ToolExceptionMapper`'s switch does), and it meant any *other* `RuntimeException` — a real bug, not a
classified failure — skipped translation entirely and leaked its raw message instead of the mapper's safe
fallback.

Catch broadly only at a boundary like this one — something wrapping every invocation of a given kind. Not
mid-flow, and never as a way to skip deciding what a failure actually is before it reaches that boundary.
The exception types themselves stay specific either way — this only changes what a *handler* is allowed to
catch, never what anything is allowed to throw.

## Tests

**The seven domain exception classes get no test.** Two constructors delegating to `super` is exactly the
trivial code the `unit-test-pattern` skill rules out.

**`ToolExceptionAspectTest`** covers both branches of the advice:

```java
@ExtendWith(MockitoExtension.class)
class ToolExceptionAspectTest {

    @Mock
    private ToolExceptionMapper toolExceptionMapper;

    @InjectMocks
    private ToolExceptionAspect toolExceptionAspect;

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    @Test
    void shouldReturnJoinPointResultWhenNoExceptionIsThrown() throws Throwable {
        // given
        when(proceedingJoinPoint.proceed()).thenReturn(RESULT);

        // when
        final Object actual = toolExceptionAspect.translateToolExceptions(proceedingJoinPoint);

        // then
        assertEquals(RESULT, actual);
    }

    @Test
    void shouldRethrowToolExceptionWithMappedMessageWhenJoinPointThrowsRuntimeException() throws Throwable {
        // given
        RuntimeException thrownException = new RuntimeException(MESSAGE);
        when(proceedingJoinPoint.proceed()).thenThrow(thrownException);
        when(toolExceptionMapper.toErrorMessage(any())).thenReturn(MAPPED_MESSAGE);

        // when
        final ToolException actual = assertThrows(ToolException.class,
                () -> toolExceptionAspect.translateToolExceptions(proceedingJoinPoint));

        // then
        assertEquals(MAPPED_MESSAGE, actual.getMessage());
        assertEquals(thrownException, actual.getCause());
        verify(toolExceptionMapper).toErrorMessage(thrownException);
    }
}
```

**`ToolExceptionMapperTest`** gets one test per switch arm — each mapped exception type is its own branch,
per `unit-test-pattern`'s branch-coverage rule.

**`CompaniesHouseResponseHandlerTest`** gets one test per status code it classifies, asserting the right
exception type is thrown with the expected message.

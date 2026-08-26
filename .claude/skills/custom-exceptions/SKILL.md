---
name: custom-exceptions
description: Sam's rule for exceptions — three custom types only (InternalServerErrorException 500, BadGatewayException 502, NotFoundException 404), each a bare RuntimeException with two constructors, every status code owned by ControllerExceptionHandler. Load before writing, throwing or catching an exception in this repo.
---

# Custom exceptions

Three exception types, one handler, and nothing anywhere else that knows a status code.

## The three

| Type | Status | Thrown when |
|---|---|---|
| `InternalServerErrorException` | 500 | Something inside the app broke |
| `BadGatewayException` | 502 | Something outside the app broke |
| `NotFoundException` | 404 | A resource the code had already established must exist is gone |

The list is closed. A new failure picks one of the three — it does not get a fourth type. If none of the
three fits, the honest answer is almost always that the condition is not exceptional and should not be an
exception at all.

## The shape

All three are identical but for the name:

```java
package io.github.samrjthompson.chmcp.common.exceptions;

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
  handler reads the class and knows the status.
- `final String message`, bare `Throwable cause`, per the `final-only-for-values` skill.

No shared base class, even though the three are identical. A `BaseException` invites
`catch (BaseException exception)`, which collapses the three back into one and forces the handler to
reopen the type to find the status. The duplication is three short files and it never changes.

## Inside or outside

The 500/502 split is about *where* the failure happened, not how bad it was.

**Inside → `InternalServerErrorException`.** Our own code broke: a mapping produced something impossible,
an invariant we maintain does not hold, a value we constructed was rejected by our own rules.

**Outside → `BadGatewayException`.** The failure crossed a process boundary: the Companies House API,
Mongo, Postgres, the filesystem, any other HTTP service. In practice this is anything you learn about by
catching a library's exception — `IOException`, a driver exception, a Jackson failure parsing somebody
else's payload.

The test: if the cause came back from something reached over a socket or a disk, it is 502.

### An upstream 404 is not a 502

A `404` from Companies House is not a gateway failure. The upstream worked perfectly and gave a correct
answer — the company does not exist. Only 5xx, timeouts, connection failures and unparseable bodies are
`BadGatewayException`. An upstream `404` becomes an empty `Optional`, per the next section.

## Exceptions are exceptional

Never throw for a condition the caller expects. This is the rule that decides most cases, and it is the
one most often got wrong.

If absence is a possible correct answer for the input, the method returns `Optional<T>` or an empty
collection, and the caller decides what to say about it:

```java
public Optional<CompanyProfile> getCompany(final String companyNumber) {
```

A search that matches nothing returns an empty list. A lookup on a number a user typed returns an empty
`Optional`. Neither is a `NotFoundException` — the caller asked a reasonable question and got a truthful
answer.

Turning that into a 404 response is one call, with no branching in the controller:

```java
@GetMapping("/companies/{companyNumber}")
public ResponseEntity<CompanyProfile> getCompany(@PathVariable("companyNumber") final String companyNumber) {
    LOGGER.info("Getting company [{}]", companyNumber);

    return ResponseEntity.of(service.getCompany(companyNumber));
}
```

`ResponseEntity.of` gives 200 with the body when the `Optional` is present and an empty 404 when it is
not.

`NotFoundException` is for the other case: a resource the code has already established must exist. An id
read out of a parent record, a document written moments ago, a foreign key we own. Its absence means
something upstream of here is broken, which is why it is worth a stack trace and an ERROR log.

Two more consequences:

- **Never catch one of the three to steer logic.** They exist to reach the handler. Catching one to
  decide what to do next means the condition was expected, so it should have been a return value.
- **Never use them for validation.** Rejecting bad input is Jakarta's job — `@NotBlank` and friends on the
  request record. The handler maps the resulting `ConstraintViolationException` to 400.

## Throwing

Wrap the cause whenever you are inside a `catch`. The two-argument constructor exists for exactly this,
and dropping the cause throws away the only part of the stack trace that says what actually went wrong:

```java
} catch (MongoException ex) {
    final String msg = "Failed to read company [%s] from Mongo".formatted(companyNumber);
    LOGGER.error(msg);
    throw new BadGatewayException(msg, ex);
}
```

Not this:

```java
} catch (MongoException ex) {
    throw new BadGatewayException("Mongo read failed");
}
```

Messages name the operation and bracket the identifiers, built with `formatted` — the same style the
rest of the codebase logs in. Never put an API key, a credential or personal data in a message: the
handler logs it.

## `ControllerExceptionHandler`

Lives in `common/exceptions/` beside the three types, so a throw and the status it produces are read side
by side. It owns every status code in the application.

```java
package io.github.samrjthompson.chmcp.common.exceptions;

import io.github.samrjthompson.chmcp.common.exception.BadGatewayException;
import io.github.samrjthompson.chmcp.common.exception.InternalServerErrorException;

@RestControllerAdvice
public class ControllerExceptionHandler {

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<Void> handleNotFoundException() {
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler(BadGatewayException.class)
  public ResponseEntity<Void> handleBadGatewayException() {
    return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Void> handleConstraintViolationException() {
    return ResponseEntity.badRequest().build();
  }

  @ExceptionHandler({InternalServerErrorException.class, Exception.class})
  public ResponseEntity<Void> handleInternalServerErrorException() {
    return ResponseEntity.internalServerError().build();
  }
}
```

**`ResponseEntity<Void>` everywhere.** Status only, no body. Nothing an attacker can read, nothing about
our internals on the wire. The detail lives in the log, where it belongs.

**Anything unhandled is a 500.** `Exception.class` shares a method with
`InternalServerErrorException` because they mean the same thing: the app broke and we did not see it
coming. Spring resolves to the closest matching handler, so the fallback only fires when nothing more
specific matches. Method order in the file does not affect resolution.

## Tests

**The three exception classes get no test.** Two constructors delegating to `super` is exactly the
trivial code the `unit-test-pattern` skill rules out.

**`ControllerExceptionHandlerTest`** gets one test per handler method — each is its own branch:

```java
@ExtendWith(MockitoExtension.class)
public class ControllerExceptionHandlerTest {

    private static final String MESSAGE = "Failed to read company [00445790] from Mongo";

    @InjectMocks
    private ControllerExceptionHandler controllerExceptionHandler;

    @Test
    void shouldReturnBadGatewayWhenBadGatewayExceptionIsHandled() {
        // given / when
        final ResponseEntity<Void> actual =
                controllerExceptionHandler.handleBadGatewayException(new BadGatewayException(MESSAGE));

        // then
        assertEquals(HttpStatusCode.valueOf(HttpStatus.BAD_GATEWAY.value()), actual.getStatusCode());
    }
}
```

**At least one path through a `ControllerIT`.** The unit test proves the method returns the right status;
only `MockMvc` against the real context proves the advice is registered and actually intercepts.

## Today's codebase

`CompaniesHouseApiException` predates this rule. It carries a `statusCode` field and takes the status as
its first constructor argument, which is the shape this skill exists to replace — it is a
`BadGatewayException`, with the upstream `404` becoming an empty `Optional` rather than an exception. Do
not copy it, and do not add a second exception in its style. `ControllerExceptionHandler` is not written
yet either.

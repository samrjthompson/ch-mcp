---
name: unit-test-pattern
description: Sam's shape for JUnit Jupiter and Mockito unit tests — how many tests to write, mock field ordering, given/when/then phases, matchers in when() and real values in verify(). Load before writing or editing any *Test.java in this repo, and before proposing how many tests a class needs.
---

# Unit test pattern

Every unit test in this repo copies the shape of `ControllerTest.java`. JUnit Jupiter for assertions,
Mockito for collaborators, three named phases, one mock field block in a fixed order.

This skill governs `*Test.java` only. `*IT.java` runs under failsafe against Testcontainers and is a
different animal — nothing here applies to it.

## The reference test

`../../../src/test/java/io/github/samrjthompson/chmcp/chmcp/company/api/ControllerTest.java`:

```java
@ExtendWith(MockitoExtension.class)
public class ControllerTest {

    @Mock
    private Service service;

    @InjectMocks
    private Controller controller;

    @Test
    void shouldRetrieveCompaniesWithinFiveMileRadiusOfPostcode() {
        // given
        final String postcode = "G12 8AU";
        final long radius = 5L;

        when(service.getCompanies(anyString(), anyLong())).thenReturn(List.of());

        // when
        final ResponseEntity<List<String>> actual = controller.getCompanies(postcode, radius);

        // then
        assertEquals(HttpStatusCode.valueOf(HttpStatus.OK.value()), actual.getStatusCode());
        assertEquals(List.of(), actual.getBody());

        verify(service).getCompanies(postcode, radius);
    }
}
```

## How many tests

Every test after the first must reach a line or branch the existing tests do not. Before adding one, name the
line/branch it covers. If there is no answer, do not write it.

`RepositoryTest` is the worked example. `Repository.findWithinRadius` is straight-line code — it takes a
query and a row mapper from `SqlHelper`, hands over two named parameters, and returns the list. One test
walks all of it, so one test is the whole class. Proposing a second for the empty-result case, or a third
for a different postcode, adds nothing: both re-walk the same statements and assert that Mockito returns
what it was told to return.

This governs the tests *after* the first, not the first itself. Branchless code reports 100% branch
coverage before a single test exists, so coverage delta can never justify the opening test — write that
one whenever the method is worth testing at all, then apply the rule from the second onwards.

Not reasons to add a test:

- A second set of input values down the same path. Values do not move coverage.
- An empty list, an empty `Optional`, or a null that a stub was told to hand back. That tests Mockito.
- A collaborator's behaviour. That belongs in the collaborator's own test class.
- Symmetry with another test class, or a feeling that one test looks thin.

Reasons to add one:

- A branch not yet taken — `if`, `else`, ternary, `switch` arm, `catch`, or a short-circuiting `&&`/`||`.
- A thrown exception the method is expected to propagate or translate.

When the extra case is real behaviour but sits in branchless code — a row mapper's column names, say —
that is a job for the `*IT`, not for a second unit test.

## Field ordering

Three groups in this order, separated by a single blank line. Never put a blank line *inside* a group.

```java
@Mock
private Service service;
@Mock
private Repository repository;

@InjectMocks
private Controller controller;

@Mock
private CompanyProfile companyProfile;
```

1. **Constructor dependencies** of the class under test.
2. **The class under test.**
3. **General mocks** — every mock that is not a constructor dependency: a mocked argument passed to the
   method under test, or a mocked object a stub hands back.

Not this — a blank line between every `@Mock` loses the grouping the layout exists to show:

```java
@Mock
private Service service;

@Mock
private Repository repository;

@InjectMocks
private Controller controller;

@Mock
private CompanyProfile companyProfile;
```

Constants go above all three groups, as in any Java class.

The grouping is for the reader. `@InjectMocks` injects by type and ignores field position, so a general
mock whose type happens to match a constructor parameter is still injected — position does not prevent
it.

## When `@InjectMocks` will not do

If the constructor takes a `String` or another plain value Mockito cannot supply, drop `@InjectMocks`.
Declare the field bare in the same middle position and build it in `@BeforeEach void setUp()`. The real
dependencies keep their `@Mock`:

```java
@ExtendWith(MockitoExtension.class)
public class ServiceTest {

    private static final String API_KEY = "api-key";

    @Mock
    private Client client;
    @Mock
    private Repository repository;

    private Service service;

    @Mock
    private CompanyProfile companyProfile;

    @BeforeEach
    void setUp() {
        service = new Service(client, repository, API_KEY);
    }
}
```

## The three phases

Every test body is `// given`, `// when`, `// then`, each marker preceded by a blank line. This is the
normal case, and most tests look exactly like it.

These markers are the sole exception to the `no-code-comments` skill. They mark structure rather than
explain code, so they cannot rot. Nothing else in a test file may carry a comment — if a step needs
explaining, rename the variable or the test method until it does not.

### Folding two phases together

Where a phase would stand completely empty, fold it into its neighbour and combine the two markers.

Fold `given` into `when` only when there is nothing whatever to arrange — the method under test takes no
arguments, or takes only mocks already declared at class level. Declaring a local is arranging, so a
test carrying even one `final String postcode = "G12 8AU";` keeps its own `// given`:

```java
@Test
void shouldPassSuppliedCompanyToService() {
    // given / when
    controller.save(companyProfile);

    // then
    verify(service).save(companyProfile);
}
```

Fold `when` into `then` when the call under test happens inside the assertion, leaving no separate
invocation to mark:

```java
@Test
void shouldPropagateExceptionWhenPostcodeIsUnknown() {
    // given
    final String postcode = "ZZ99 9ZZ";
    final long radius = 5L;

    when(service.getCompanies(anyString(), anyLong())).thenThrow(new PostcodeNotFoundException());

    // when / then
    assertThrows(PostcodeNotFoundException.class, () -> controller.getCompanies(postcode, radius));
}
```

`assertDoesNotThrow(() -> controller.getCompanies())` folds the same way.

Three separate markers stay the default. Reach for a combined marker because the test came out that
shape, not to tighten a test that already reads fine.

## Stubbing and verification

Real values never appear inside `when()`. They appear in the `verify(…)` arguments in the `then` step,
which is what pins the contract:

```java
// given
final String postcode = "G12 8AU";
final long radius = 5L;

when(service.getCompanies(anyString(), anyLong())).thenReturn(List.of());

// then
verify(service).getCompanies(postcode, radius);
```

Never `when(service.getCompanies("G12 8AU", 5L))`.

Reach for the type-specific matcher first — `anyString()`, `anyLong()`, `anyInt()`, `anyList()`. Use
`any(Foo.class)` or `eq(Foo.class)` only where the extra specificity is genuinely required, such as
picking between overloads or matching a class-literal argument:

```java
when(restClient.get(anyString(), eq(CompanyProfile.class))).thenReturn(companyProfile);
```

Mockito is all-or-nothing about matchers: once one argument is a matcher, every argument must be. That
is the only reason `eq()` exists — not as a way to smuggle a real value into a stub.

## Assertions

JUnit Jupiter only, statically imported from `org.junit.jupiter.api.Assertions` — `assertEquals`,
`assertThrows`, `assertTrue`, and the rest. AssertJ and Hamcrest are not on the classpath and adding
either needs Sam's agreement first.

Static imports throughout: `assertEquals`, not `Assertions.assertEquals`; `when` and `verify`, not
`Mockito.when` and `Mockito.verify`.

## Naming and modifiers

- Class is `public`, named `<ClassUnderTest>Test`, annotated `@ExtendWith(MockitoExtension.class)`.
- Test methods are package-private and `void`. No `public`.
- Method names start `should` and carry both the behaviour and the condition:
  `shouldRetrieveCompaniesWithinFiveMileRadiusOfPostcode`, not `testGetCompanies`.
- Locals are `final`.
- The result of the call under test is named `actual`.

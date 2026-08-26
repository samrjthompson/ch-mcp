---
name: final-only-for-values
description: Sam's rule for the `final` modifier on locals and parameters — only primitives, their boxed types and `String` take it, every other type goes bare, and `actual`/`expected` in tests always take it. Fields are unaffected. Load before writing or editing any Java file in this repo, and whenever deciding whether a declaration takes `final`.
---

# `final` only for values

A local variable or a method parameter takes `final` only when its type is a primitive, a boxed
primitive, or `String`. Every other type — records, DTOs, entities, collections, `Class<T>`,
`HttpRequest`, mocks, builders, anything from a library — is declared bare.

Fields are outside this rule and do not change. `private static final Logger LOGGER` and
`private final CompaniesHouseClient companiesHouseClient` stay exactly as they are. Constructor
injection still means `private final` fields, as the `controller-pattern` skill requires.

There is exactly one exception, and it lives in tests: `actual` and `expected` always take `final`,
whatever their type. The last section covers it.

## The list

Takes `final`:

`int` · `long` · `short` · `byte` · `char` · `float` · `double` · `boolean`
`Integer` · `Long` · `Short` · `Byte` · `Character` · `Float` · `Double` · `Boolean`
`String`

Everything else goes bare. The list is closed. Do not extend it by reasoning about which library classes
happen to be immutable — `LocalDate`, `BigDecimal`, `UUID`, `Duration` and enum types are objects, so
they are declared bare like every other object. The value of the rule is that it applies mechanically,
and a per-type immutability audit destroys that.

## Why

On a `String` or an `int`, `final` tells the truth. The binding and the value are the same thing, so
"will not be reassigned" and "will not change" mean the same thing to the reader.

On an object it is a half-promise. `final HttpRequest request` freezes the reference and says nothing
whatever about the object, which is the part a reader actually cares about. It reads as a guarantee of
immutability that Java never made. Dropping it removes the false signal, and takes six characters of
noise off every declaration in the method.

## Worked example

```java
public <T> T get(final String path, Map<String, String> queryParameters, Class<T> responseType) {
    LOGGER.info("Sending GET request to Companies House path [{}]", path);

    HttpRequest request = HttpRequest.newBuilder()
            .uri(buildUri(path, queryParameters))
            .GET()
            .build();

    HttpResponse<String> response = send(request, path);

    return deserialise(response.body(), responseType, path);
}
```

A mixed signature is the expected outcome, not an inconsistency to tidy away. `final String path` sitting
beside a bare `Map<String, String> queryParameters` is the rule working.

## The other binding forms

| Form | Rule |
|---|---|
| Enhanced-for variable | Same rule — `for (final String name : names)`, `for (CompanySearchResult result : results)` |
| `catch` parameter | Always an exception type, so always bare — `catch (IOException exception)` |
| Try-with-resources | Already implicitly final. Never write the word |
| Lambda parameter | Bare, whatever the type |

## Where appropriate

`final` on a value local or parameter is the default, not an obligation. Drop it where the code genuinely
reassigns the variable — an accumulator built up in a loop, a value narrowed through a chain of
conditions. Reassigning a *parameter* is its own defect: fix that rather than dropping the `final` to
accommodate it.

## In tests

The same rule, with one exception.

### `actual` and `expected` are always `final`

Whatever their type. `final CompanySearchResponse actual`, `final ResponseEntity<List<String>> actual`,
`final CompaniesHouseApiException actual` — all correct, even though none of those types is on the list.

These two variables carry the outcome the test exists to check, and they are the ones a later line must
never quietly rebind. `final` there is deliberate redundancy: it costs nothing, and it rules out the one
reassignment that would silently invalidate every assertion below it. Nothing else in the test gets the
treatment — a `CompanySearchRequest request` in the `// given` block is an ordinary local and goes bare.

```java
@Test
void shouldRetrieveCompaniesWithinFiveMileRadiusOfPostcode() {
    // given
    final String postcode = "G12 8AU";
    final long radius = 5L;

    when(service.getCompanies(anyString(), anyLong())).thenReturn(List.of());

    // when
    final ResponseEntity<List<String>> actual = controller.getCompanies(postcode, radius);

    // then
    assertEquals(List.of(), actual.getBody());
}
```

The exception is the name, not the position. `final CompaniesHouseApiException actual =
assertThrows(...)` in a folded `// when / then` is the same variable doing the same job.

Everything else in a test follows the ordinary rule. `// given` locals are usually `String` and
primitives, so they keep `final` anyway; a request object or a stubbed collaborator goes bare.

`private static final` constants at the top of a test class are fields, so they are untouched —
`private static final Map<String, String> QUERY_PARAMETERS = Map.of("q", "tesco");` keeps its `final`.

---
name: tool-pattern
description: Sam's shape for @Tool classes — one @Tool method per class in the owning domain's tool/ package, @Component with constructor-injected service, no exception handling, maps to a response record, registered in McpToolConfig. Load before writing or editing any @Tool class.
---

# Tool pattern

A `@Tool` method logs the call, builds a request from its parameters, and delegates to one service method.
That is the whole job.

## The reference tool

```java
@Component
public class SearchTool {

    public static final String NAME = "search";
    public static final String DESCRIPTION = "Search Companies House for companies matching a free text query";

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchTool.class);

    private final CompaniesService companiesService;

    public SearchTool(CompaniesService companiesService) {
        this.companiesService = companiesService;
    }

    @Tool(name = NAME, description = DESCRIPTION)
    public CompanySearchResponse search(@ToolParam(description = "Free text company search query") final String query,
            @ToolParam(description = "Number of results per page", required = false) final Integer itemsPerPage,
            @ToolParam(description = "Zero-based index of the first result", required = false) final Integer startIndex,
            @ToolParam(description = "Companies House search restriction filter",
                    required = false) final String restrictions) {
        LogContext.get().toolName(NAME);
        LOGGER.info("Dispatching tool call");

        CompanySearchRequest request = CompanySearchRequest.builder()
                .query(query)
                .itemsPerPage(itemsPerPage)
                .startIndex(startIndex)
                .restrictions(restrictions)
                .build();

        return companiesService.searchCompanies(request);
    }
}
```

## One `@Tool` method per class, in the owning domain's `tool/` package

`@Component`, and exactly one method carrying `@Tool`. Placement is the domain's own `tool/` package —
`company/tool/`, not a shared `mcp` package. Both are enforced by `ArchitectureTest`:
`toolClassesExposeAtMostOneTool` and `toolClassesLiveInADomainToolPackage`.

`NAME` and `DESCRIPTION` are `public static final String` constants, used both in the `@Tool` annotation
and to identify the call elsewhere — `LogContext.get().toolName(NAME)` above.

## Constructor injection, one collaborator

`private final` field for the feature's service, set via the constructor — never the client directly. A
tool reaching past its service into `client/` would break `ArchitectureTest`'s `featuresTalkThroughServices`
rule (only a feature's `service` or `client` package may depend on `client`).

## Parameters

`@ToolParam(description = ..., required = ...)` on each, `required = false` for anything optional.
Parameter types follow CLAUDE.md's `final` rule: `final String`, `final Integer` — both are on the closed
list, so both take `final`.

## No logic

Log, build a request, call one service method, return its result. No branching, no mapping beyond
constructing the request, no assembling a response from two calls. A method that needs a decision is a
method whose decision belongs in the service.

## Maps to a response record, never raw upstream JSON

The return type is a response record the service produces, per CLAUDE.md — here `CompanySearchResponse`,
never the Companies House API's own JSON shape passed through unchanged.

## Exceptions are somebody else's job

No try/catch. `ToolExceptionAspect` wraps every `@Tool`-annotated method with an `@Around` advice keyed on
the annotation itself, catches any `RuntimeException` that escapes, and rethrows it as a `ToolException`
with a safe, mapped message. A tool class never imports anything from `common.exception` — enforced by
`ArchitectureTest`'s `toolClassesDoNotHandleExceptionsThemselves`. See the `custom-exceptions` skill for
the full mechanism.

## Registration

Register the tool bean in `McpToolConfig`, per CLAUDE.md.

## Log messages stay short

One INFO line naming the operation — "Dispatching tool call" above. Do not mechanically grow the message
as the parameter list grows; a parameter does not earn a place in the log just by existing.

## Tests

One class, `SearchToolTest`, derived directly from the reference tool above:

```java
@ExtendWith(MockitoExtension.class)
class SearchToolTest {

    private static final String QUERY = "tesco";
    private static final Integer ITEMS_PER_PAGE = 20;
    private static final Integer START_INDEX = 0;
    private static final String RESTRICTIONS = "active";

    @Mock
    private CompaniesService companiesService;

    @InjectMocks
    private SearchTool searchTool;

    @Mock
    private CompanySearchResponse companySearchResponse;

    @Test
    void shouldSearchCompaniesWhenArgumentsAreProvided() {
        // given
        CompanySearchRequest expectedRequest = CompanySearchRequest.builder()
                .query(QUERY)
                .itemsPerPage(ITEMS_PER_PAGE)
                .startIndex(START_INDEX)
                .restrictions(RESTRICTIONS)
                .build();
        when(companiesService.searchCompanies(any())).thenReturn(companySearchResponse);

        // when
        final CompanySearchResponse actual = searchTool.search(QUERY, ITEMS_PER_PAGE, START_INDEX, RESTRICTIONS);

        // then
        assertEquals(companySearchResponse, actual);
        verify(companiesService).searchCompanies(expectedRequest);
    }
}
```

Field order follows `unit-test-pattern`: the service collaborator, then the tool under test, then the
unrelated return-value mock. One test covers the method's single straight-line path — there is no branch
to justify a second, per `unit-test-pattern`'s how-many-tests rule. `when()` takes a loose matcher
(`any()`); `verify()` takes the concrete request the tool is expected to have built.

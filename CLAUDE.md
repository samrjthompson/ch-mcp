# Companies House MCP Server

Read-only MCP server over the Companies House Public Data API. Personal/portfolio project.

## Stack

Java 25, Spring Boot 4, Spring AI (MCP server, WebMVC), Maven, JUnit Jupiter, Mockito, WireMock, ArchUnit.

Planned but not yet present: Docker Compose, PostgreSQL, React (static). Nothing in the repo depends on
these today — do not write code, config or tests that assume they exist.

## Commands

- `make test` — the verification command. Runs `spotless:check`, then `mvn clean verify` (surefire for
  `*Test`, failsafe for `*IT`). A green run is the whole proof; do not add further checks on top of it.
- `make unit-test` — inner loop only, never the final proof.
- `make format` — apply formatting in place.

Do not run `mvn verify` directly. Spotless is not bound to a lifecycle phase, so that path silently skips
the format check.

## Enforced rules

`ArchitectureTest` and `TestArchitectureTest` are authoritative for structural rules and are not restated
here. Between them they cover outbound HTTP placement, which layers may reach the client, `@Tool` class
shape and location, generic exception construction, and unit-vs-integration test separation. Read them
before designing anything that touches those areas — they will fail the build, so guessing is expensive.

## Domain facts

**Two hosts, do not mix.**

- Public data: <https://api.company-information.service.gov.uk>
- Documents: <https://document-api.company-information.service.gov.uk>

**Reference specs.** Swagger 2.0, fetchable with `curl`. The root spec is an index only — every path is a
`$ref`.

- Public data: <https://developer-specs.company-information.service.gov.uk/api.ch.gov.uk-specifications/swagger-2.0/spec/swagger.json>
- Documents: <https://developer-specs.company-information.service.gov.uk/document.api.ch.gov.uk-specifications/swagger-2.0/spec/swagger.json>

Refs are baked with a `http://127.0.0.1:10000` host and do not resolve as written. Swap that prefix for
`https://developer-specs.company-information.service.gov.uk` to fetch a fragment, e.g.
`.../api.ch.gov.uk-specifications/swagger-2.0/spec/companyProfile.json`.

Take request and response shapes from these specs, not from memory.

**Auth** is Basic: key as username, empty password. The key comes from `CH_API_KEY` only. Never a literal,
never logged, never in a fixture.

**Rate limit** is 600 requests per 5 minutes per key, shared across every endpoint.

**Company numbers** are 8 characters, uppercase, zero-padded — `01234567`, `SC012345`.

## Code conventions

- No code comments and no Javadoc, in any file type. Three exceptions: `application.properties` and
  `pom.xml`; machine-read directives that happen to use comment syntax (`# noqa`,
  `# shellcheck disable=...`); and the `// given`, `// when`, `// then` markers in tests. Leave existing
  comments alone unless a change makes one factually wrong.
- `final` on locals and parameters only where the type is a primitive, a boxed primitive, or `String`.
  Everything else is declared bare, including `LocalDate`, `BigDecimal`, `UUID` and enum types. Fields are
  unaffected — constructor-injected fields stay `private final`. In tests, `actual` and `expected` always
  take `final` whatever their type.
- Never return raw upstream JSON. Map to a response record.
- Register tools in `McpToolConfig`.
- Unit tests are `*Test.java`, integration tests `*IT.java`. The suffix decides which runner picks the
  class up, so a misnamed test silently never runs.
- Integration tests set application properties with `@DynamicPropertySource`.
- Nothing in the test suite hits the live API.

## Skills

Skills live in `.claude/skills/`. Load them per their descriptions.

If we implement something and I ask you to promote the pattern as a skill, add it to `.claude/skills/`
with YAML frontmatter.
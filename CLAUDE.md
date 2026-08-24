# Companies House MCP Server

Read-only MCP server over the CH Public Data API. Intended as a personal/portfolio project, the infrastructure will run
on Docker locally and be orchestrated by a `docker-compose.yaml`.

## Stack

Java 25, Spring Boot 4+, Maven 3.9+, Docker Compose, React (static), PostgreSQL, JUnit Jupiter.

## Skills

Claude skills can be found in `./.claude/skills`.

## Commands

- Build + test: `mvn clean verify`

## Hosts — two, do not mix

- Public data: https://api.company-information.service.gov.uk
- Documents:   https://developer-specs.company-information.service.gov.uk/companies-house-public-data-api/reference

## Reference specs

Swagger 2.0, fetchable with `curl`. The root spec is an index only — every path is a `$ref`.

- Public data: https://developer-specs.company-information.service.gov.uk/api.ch.gov.uk-specifications/swagger-2.0/spec/swagger.json
- Documents:   https://developer-specs.company-information.service.gov.uk/document.api.ch.gov.uk-specifications/swagger-2.0/spec/swagger.json

Refs are baked with a `http://127.0.0.1:10000` host and do not resolve as written. Swap that prefix for
`https://developer-specs.company-information.service.gov.uk` to fetch a fragment, e.g.
`.../api.ch.gov.uk-specifications/swagger-2.0/spec/companyProfile.json`.

Take request and response shapes from these specs, not from memory.

## Rules

- Auth is Basic: key as username, empty password. Key comes from CH_API_KEY only. Never a literal, never logged, never
  in a fixture.
- Rate limit is 600 req / 5 min per key, shared across all endpoints. Every outbound call goes through a client.
- Company numbers are 8 chars, uppercase, zero-padded (`01234567`, `SC012345`).
- One tool per class in `tools/`, registered in `ToolConfig`.
- Tests run against fixtures in `src/test/resources/`. Nothing hits live.
- Never return raw upstream JSON. Map to a response record.
- Use Java net HTTP rather than Spring Rest.
- Integration tests (itests/Itests) should end in `*IT.java` and unit tests `*Test.java`.
- Never use `@SpringBootTest`, `@Testcontainers`, or `WireMock` in a unit test, only itests.
- In itests, use `@DynamicPropertySource` to set app properties.
- Do not leave code comments (except in `application.properties` and `pom.xml`) or java docs.

## Skill promotion

If we implement code and I ask you to promote the pattern as a skill, please add it to the `./claude/skills`
directory with YAML frontmatter.
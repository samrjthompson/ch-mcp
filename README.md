# Companies House MCP

## Code style

Java formatting is enforced by [Spotless](https://github.com/diffplug/spotless), using the Eclipse JDT
formatter against the profile in `eclipse-java-formatter.xml` (4-space indent, 120-column line length).

- `make format` — reformat the codebase in place.
- Spotless is bound to the `compile` phase, so `make test`, `make unit-test`, or a bare `mvn verify` all
  fail on unformatted code without a separate check step.

## Architecture rules

Structural rules are enforced by [ArchUnit](https://www.archunit.org/), split across two test classes:

- `ArchitectureTest` — production code: outbound HTTP stays inside `client`/`config`, only a feature's
  service layer may talk to `client` directly, `@Tool`-annotated classes expose at most one such method,
  live in their owning domain's `tool` package rather than a shared `mcp` package, and never handle
  exceptions themselves (that's centralised in `ToolExceptionAspect`), and no class throws a generic
  `RuntimeException`/`Exception` directly.
- `TestArchitectureTest` — test code: `*Test` classes never use `@SpringBootTest`, `@Testcontainers`, or
  WireMock; anything that does must be named `*IT` and run under failsafe.

Both run automatically as part of `make test` — no separate command.

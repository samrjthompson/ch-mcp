# ch-mcp

## Code style

Java formatting is enforced by [Spotless](https://github.com/diffplug/spotless), using the Eclipse JDT
formatter against the profile in `eclipse-java-formatter.xml` (4-space indent, 120-column line length).

- `make format` — reformat the codebase in place.
- `make test` (`mvn verify`) also runs `spotless:check`, so the build fails if any file is unformatted.

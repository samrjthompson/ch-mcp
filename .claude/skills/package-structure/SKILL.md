---
name: package-structure
description: Where a new class goes — package by feature under the base package, common/ for unowned classes including the exception hierarchy, config/ for configuration, logging/ for the structured-logging package. Load before creating any new Java file or package in this repo.
---

# Package structure

Package by feature. One package per business feature, directly under
`io.github.samrjthompson.chmcp`, named with the **singular** domain noun. Layers live inside the
feature, never above it — there is no top-level `service/` or `tool/`.

This is the default for every new class. It holds unless Sam says otherwise for a specific case.

```
chmcp/
├── Main.java
├── client/
├── common/
│   └── exception/
├── company/
│   ├── model/
│   ├── service/
│   └── tool/
├── config/
└── logging/
```

`client/` holds the outbound Companies House HTTP client and its supporting classes
(`CompaniesHouseClient`, `CompaniesHouseResponseHandler`, `HttpRequestBuilder`, `ResponseBodySanitiser`,
`UriBuilder`) — see `ArchitectureTest`'s `onlyClientAndConfigTalkHttpDirectly` rule, which confines
`java.net.http` usage to `client/` and `config/`.

## Choosing the package

Ask what the class is *about*, not what kind of class it is. A new service for company data goes in
`company/service/`, not in a top-level service package. If a new class does not fit an existing feature,
it is telling you a new feature package is needed.

## `common/`

For classes with no clear owning feature. The test is ownership, not reuse: if one feature uses it, it
belongs to that feature even if a second feature might want it later. Move it to `common/` when it
genuinely has no single owner.

`common/` currently holds `ToolExceptionAspect` and `ToolExceptionMapper` — the cross-cutting
exception-translation boundary for every `@Tool` method — plus the `exception/` subpackage with the
exception hierarchy they translate. See the `custom-exceptions` skill.

## `config/`

Spring configuration classes, top-level and sibling to the features — `CompaniesHouseProperties`,
`HttpClientConfig`, `McpToolConfig`.

## `logging/`

Top-level and sibling to the features, same reasoning as `config/` — a cross-cutting capability rather
than something one feature owns. See the `structured-logging` skill.

## Features talk through services

A feature calls another feature's service. It never reaches into another feature's `tool/` or `model/`
directly. This repo currently has one feature package, `company`; the rule is enforced regardless by
`ArchitectureTest`'s `featuresTalkThroughServices`, which allows only a `..service..` or `..client..`
class to depend on `..client..` — a `tool` class can reach its own feature's service, never the client.

## Tests

Unit tests mirror main, package for package: `company/tool/SearchTool.java` is tested by
`company/tool/SearchToolTest.java`.

Integration tests do not mirror per production class here. There are exactly two, both living at the root
`chmcp` test package rather than inside a feature: `MainIT` and `McpServerIT`, each exercising the whole
Spring context through `MockMvc`. A new feature does not automatically need its own `*IT` — see the
`running-tests` skill for what the two existing ones cover.

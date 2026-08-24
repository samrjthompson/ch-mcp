---
name: package-structure
description: Where a new class goes — package by feature under the base package, common/ for unowned classes and exceptions, config/ for configuration. Load before creating any new Java file or package in this repo.
---

# Package structure

Package by feature. One package per business feature, directly under
`io.github.samrjthompson.chmcp`, named with the **singular** domain noun. Layers live inside the
feature, never above it — there is no top-level `controller/`, `service/` or `repository/`.

This is the default for every new class. It holds unless Sam says otherwise for a specific case.

```
chmcp/
├── company/
│   ├── api/
│   └── service/
├── companieshouse/
├── geocoding/
├── common/
│   └── exceptions/
└── config/
```

## Choosing the package

Ask what the class is *about*, not what kind of class it is. A repository for company records goes in
`company/`, not in a repository package. If a new class does not fit an existing feature, it is telling
you a new feature package is needed.

## `common/`

For classes with no clear owning feature. The test is ownership, not reuse: if one feature uses it, it
belongs to that feature even if a second feature might want it later. Move it to `common/` when it
genuinely has no single owner.

`common/exceptions/` holds the exception types and `ControllerExceptionHandler`, so a throw and the
status code it produces are read side by side.

## `config/`

Spring configuration classes, top-level and sibling to the features.

## Features talk through services

A feature calls another feature's service. It never reaches into another feature's `api/` or its data
access. If `geocoding` needs company records, it calls the `company` service.

## Tests mirror main

A test lives in the same package as its subject: `company/api/Controller.java` is tested by
`company/api/ControllerTest.java` and `company/api/ControllerIT.java`.

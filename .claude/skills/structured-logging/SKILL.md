---
name: structured-logging
description: Sam's shape for request-correlated structured logging — LogContext wraps SLF4J's MDC, Spring Boot's native structured console logging serialises it automatically, no call site needs to pass context explicitly. Load before adding a new LogContext field, writing a class that logs, or touching a logging.* property.
---

# Structured logging

Correlation context (a `requestId`, and whatever else a request discovers about itself) travels through
every log line via `LogContext`, a thin wrapper around SLF4J's `MDC`. Nothing about how `LOGGER.info(...)`
is called changes anywhere in the codebase — the context rides along automatically.

## `LogContext`

`io.github.samrjthompson.chmcp.logging.LogContext`:

```java
public final class LogContext {

    private static final String REQUEST_ID_KEY = "requestId";
    private static final String QUERY_KEY = "query";
    private static final String UNINITIALISED = "uninitialised";
    private static final LogContext INSTANCE = new LogContext();

    private LogContext() {
    }

    public static void initialise(final String requestId) {
        MDC.put(REQUEST_ID_KEY, requestId);
    }

    public static LogContext get() {
        return INSTANCE;
    }

    public LogContext query(final String query) {
        MDC.put(QUERY_KEY, query);
        return this;
    }

    public static String getRequestId() {
        return Objects.requireNonNullElse(MDC.get(REQUEST_ID_KEY), UNINITIALISED);
    }

    public static Map<String, String> getLogMap() {
        return Objects.requireNonNullElseGet(MDC.getCopyOfContextMap(), Map::of);
    }

    public static void clear() {
        MDC.clear();
    }
}
```

`initialise()` is called once, at the request boundary. `.get().<field>(value)` chains on additional
fields as a request discovers them. `clear()` must run in a `finally` at that same boundary — Tomcat
reuses pooled threads across requests, so a path that skips it leaks one request's context into the next.

## Why MDC, not a hand-rolled map

Spring Boot's native structured logging (`logging.structured.format.console`, since 3.4) serialises the
current MDC map into every JSON log line automatically — no extra dependency, no
`logstash-logback-encoder`, no `logback-spring.xml`. That means every `LOGGER.info(...)`/`LOGGER.error(...)`
call in the codebase needs zero changes to carry correlation data. Never pass `LogContext.getLogMap()` as
a trailing log argument, and never reach for `net.logstash.logback.argument.StructuredArguments` —
`getLogMap()` exists only for the rare case something needs the raw map directly, not for routine logging.

## `logging.structured.format.console=ecs` in `application.properties`

The one property that turns console output into JSON, ECS-shaped, with the MDC context included as
top-level fields. `service.name` is populated from `spring.application.name` for free. This is a global
switch — every log line becomes JSON, including local `mvn spring-boot:run` output, not just
request-scoped ones.

## Two null-safety guards worth knowing about

- `getRequestId()` never returns `null` — raw `MDC.get(...)` does, both before `initialise()` and after
  `clear()`. `Objects.requireNonNullElse(..., UNINITIALISED)` reproduces the never-null contract every
  caller relies on.
- `getLogMap()` never returns `null` — `MDC.getCopyOfContextMap()` does when nothing has been set yet, a
  well-known SLF4J footgun. `Objects.requireNonNullElseGet(..., Map::of)` guards it.

## Adding a field

`LogContext` only carries fields with a real call site today (`requestId`, `query`) — it is not pre-built
with fields for concepts that don't exist yet in the codebase. Named methods, not a generic
`field(key, value)` escape hatch: the method list on this one class is the entire schema of what this app
ever attaches to a log line, and a typo in a bare string key would silently create a second, disconnected
field instead of failing.

When a feature needs a new one — a `companyNumber` once a company-lookup tool exists, say — add a
same-shaped chainable method here at that point, not before:

```java
public LogContext companyNumber(final String companyNumber) {
    MDC.put(COMPANY_NUMBER_KEY, companyNumber);
    return this;
}
```

## `CorrelationIdFilter`

`io.github.samrjthompson.chmcp.logging.CorrelationIdFilter` is the one place `initialise()`/`clear()` are
called — a `@Component`-registered `OncePerRequestFilter` that Spring wires into the chain automatically,
no `FilterRegistrationBean` needed:

```java
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final String headerValue = request.getHeader(REQUEST_ID_HEADER);
        final String requestId = headerValue == null || headerValue.isBlank()
                ? UUID.randomUUID().toString()
                : headerValue;

        try {
            LogContext.initialise(requestId);
            response.setHeader(REQUEST_ID_HEADER, requestId);
            filterChain.doFilter(request, response);
        } finally {
            LogContext.clear();
        }
    }
}
```

It honours an inbound `X-Request-Id` header when present (useful once this service sits behind a gateway
or an MCP client that already mints its own correlation id), falls back to a generated UUID otherwise, and
echoes whichever one it used back as a response header. Never extend this filter to log or MDC-ify headers
wholesale — it stays scoped to the one id, in keeping with the rule that the Companies House API key must
never be logged.

## Package

Top-level `logging/`, sibling to `config/` — see the `package-structure` skill's `logging/` section.

## Tests

This package currently ships without unit or integration tests, at Sam's explicit instruction. Don't add
them speculatively — ask first if the pattern grows more complex than what's here today.

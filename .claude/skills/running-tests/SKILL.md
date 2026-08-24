---
name: running-tests
description: Which test command to run here and when — `make test` is the default and covers unit tests and integration tests; `make unit-test` is an inner-loop tool only. Load before running any test command, and before proposing how a change will be verified.
---

# Running tests

`make test` is the command. It is `mvn clean verify` — surefire runs the unit tests, failsafe runs the
integration tests. Run it once, at the end, and report what it says.

## A green `make test` is the whole verification

Do not bolt extra proof steps onto a passing suite. No `javap` over the compiled classes to confirm a
generated method exists, no `curl` against a running app, no grepping build output for a warning. Sam does the
final verification of the code. The job is to get the suite green and say so plainly.

If the suite is red, say that too, with the failing output. Never describe a change as working on the strength
of a clean compile.

## Never run both test verbs

`make test` already runs the unit tests. Running `make unit-test` first and `make test` after runs the unit
suite twice, for no new information. The same goes for a separate `mvn compile` beforehand — `make test`
compiles.

One test command per change.

## `make unit-test` is for the inner loop only

`make unit-test` is `mvn clean test`. Reach for it while iterating on a unit test in progress, when the
feedback speed matters and the integration tests cannot tell you anything. It is never the final proof that a
change works, and never a step on the way to `make test`.

## Testcontainers needs no permission

The integration tests start containers through Testcontainers. That is expected and requires no confirmation —
just run `make test`. This is separate from the compose stack, which is Sam's to start and never something to
initiate. See the `ask-before-starting-containers` skill.

Testcontainers does need the Docker daemon running. If it is not, say so and stop. Do not quietly fall back to
`make unit-test` and report the change verified — that hides untested integration code behind a green tick.

## When not to run tests at all

Markdown-only changes — README, CLAUDE.md, skills, ADRs — are verified by reading them back. The suite proves
nothing about prose, and may be red from work happening in parallel.

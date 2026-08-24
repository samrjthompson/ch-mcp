---
name: no-code-comments
description: Sam's rule that code ships without comments. Load before writing or editing any file in this repo — Java, YAML, SQL, Makefile, Dockerfile, shell, or CI config — and before adding a comment for any reason.
---

# No code comments

Do not write comments in code. Not block comments, not trailing comments, not section banners, not
Javadoc, not "explain the why" comments. Sam considers them bad practice and has asked for them to stop.
The narrow exceptions below are the entire list.

## Why

A comment is a second thing to keep true. It rots silently while the code around it changes, and the next
reader cannot tell whether it still describes reality. Code that needs prose alongside it to be understood
is code that has not been made clear enough yet.

## What to do instead

When the urge to write a comment appears, it is pointing at a defect in the code. Fix the code:

| Urge | Fix |
|---|---|
| Explaining what a block does | Extract it into a method whose name says that |
| Explaining a value | Give it a named constant or variable |
| Explaining a condition | Extract it into a well-named boolean |
| Explaining a workaround | Name the method or variable after the constraint it works around |
| Explaining a whole file | Put it in the README or an ADR under `docs/adr/`, not in the source |

Prose belongs in files that exist to hold prose: `README.md`, `docs/adr/`, commit message bodies, and pull
request descriptions. A commit message is the right home for "why I did it this way" — it is
version-anchored, so it cannot rot.

## The narrow exceptions

Only these, and nothing else:

- **Machine-read directives** that happen to use comment syntax and change tool behaviour, such as
  `# shellcheck disable=SC2086`, `# noqa`, or a `<!-- prettier-ignore -->`. These are instructions, not
  explanations. Java annotations like `@SuppressWarnings` are not comments and are unaffected.
- **The `// given`, `// when`, `// then` phase markers in a test.** They are structure, not explanation,
  and the `unit-test-pattern` skill requires them. This covers integration tests as much as unit tests.
  Nothing else in a test file may carry a comment.
- **A comment Sam explicitly asks for** in that specific instance.

## Existing comments

Leave comments already in the repo alone. They are Sam's. Delete one only when the change makes it
factually wrong — a stale comment is worse than none — or when he asks.

## Checking your own work

Before reporting a file as done, reread it and confirm no comment was introduced. This applies to config
and CI files as readily as to Java: a `#` line in a YAML workflow or a Makefile is still a code comment.

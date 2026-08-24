---
name: ask-before-starting-containers
description: Sam starts the compose stack, not me — never run `docker compose up`, `make start`, `docker run` or `docker build` on my own initiative. Testcontainers under `make test` is exempt and needs no permission. Load before running a docker command or a Makefile verb that touches the compose stack.
---

# Sam starts the containers

The compose stack is Sam's to bring up. Do not start it, and do not offer to — it is not a thing to ask
permission for, it is a thing not to initiate. If a change genuinely cannot be verified without a running
stack, finish everything else, then say what is left unverified and why.

## Never run

- `docker compose up` in any form, including `-d` and `--build`
- `make start`, which is `docker compose up --build --force-recreate -d`
- `docker run`, including throwaway `--rm` tool containers such as linters
- `docker build`, and anything else that pulls an image
- `docker compose down`, `stop`, `restart`, `rm`, and any other command that tears down or recreates what is
  already running — `make docker-down` included

## Free to run without asking

**`make test`.** The integration tests start containers through Testcontainers. That is ordinary test
execution, not stack management, and needs no confirmation. See the `running-tests` skill.

Read-only inspection carries no risk and needs no confirmation either:

- `docker ps`, `docker images`, `docker inspect`
- `docker compose ps`, `docker compose config`
- `docker compose logs` — never with `-f`, which follows forever and will hang the session

## Why

Starting containers takes ports, memory and disk Sam may be using for something else; `--force-recreate`
throws away a stack he is mid-way through debugging; and pulling images spends bandwidth and time without
warning. Bringing the stack up re-applies the schema and reseeds ~150k rows, destroying any state he had.

Testcontainers is different in kind: it manages its own short-lived containers on random ports, tears them
down at the end of the run, and touches nothing he is using.

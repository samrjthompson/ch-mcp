---
name: ask-before-starting-containers
description: Never run docker build, docker run, or docker compose in any form on my own initiative — read-only docker inspection is fine. Load before running a docker command.
---

# Sam starts the containers

Docker Compose, and anything it would bring up, is not part of this repo today — see CLAUDE.md's list of
planned-but-not-present infrastructure. This skill is a standing rule for when a docker command comes up
regardless: starting containers is Sam's to do, not mine to initiate.

## Never run

- `docker compose up` in any form, including `-d` and `--build`
- `docker run`, including throwaway `--rm` tool containers such as linters
- `docker build`, and anything else that pulls or builds an image
- `docker compose down`, `stop`, `restart`, `rm`, or any other command that tears down or recreates
  something already running

## Free to run without asking

Read-only inspection carries no risk and needs no confirmation:

- `docker ps`, `docker images`, `docker inspect`
- `docker compose ps`, `docker compose config`
- `docker compose logs` — never with `-f`, which follows forever and will hang the session

## Why

Starting containers takes ports, memory and disk Sam may be using for something else; `--force-recreate`
throws away a stack he is mid-way through debugging; and pulling images spends bandwidth and time without
warning. None of that is a decision to make on his behalf, even for something that looks routine.

This skill will expand once the compose stack described in CLAUDE.md is actually built, to cover what
running it does to any state it holds.

# Dora

Bootstrap a local project from a reviewed local Dora source with `dora bootstrap`.
Choose `--starter blank` for a neutral skeleton, `--starter spring-vue` for empty
backend and frontend roots, or `--starter spring-vue-buildable` for a domain-free
Spring and Vue technical application with real setup, test, and build commands.
Starters do not create product behavior.

Dora is a portable delivery-control package for implementation planning,
verification, documentation contracts, and controlled project extensions.

## Start a new project

For a new product, complete `templates/project-new.yaml` with the product brief,
domain library, agent profile, and first bounded work item. Then use the one public
entrypoint. Dora writes only neutral control artifacts, declared knowledge, project
memory, and the first work declaration; it does not invent product behavior or code.

```text
/absolute/path/to/dora/bin/dora new my-app --answers project-new.yaml
```

For a project-local Dora package, reviewed immutable source, technical starter, or
CI pack, use `dora bootstrap`. Dora never downloads code during bootstrap. The
complete beginner path is in [`docs/new-project.md`](docs/new-project.md).

It owns reusable protocols and command mechanics. A consuming project owns its
domain behavior, source code, tests, runtime evidence, documentation, and
extension commands.

## Project adapter

A project declares its paths, control commands, retained evidence locations,
and local extensions in a `dora_project_adapter` document. Validate one with:

```text
dora/bin/dora validate-adapter path/to/project-adapter.yaml
```

The adapter schema is `dora/project-adapter.schema.yaml`. Dora does not infer
product paths or completion status from an adapter.

## Package boundary

`bin/`, `lib/`, and `templates/` contain portable Dora behavior. `fixtures/`
contains consumer examples and is deliberately outside the portable package
boundary audit.

For a complete beginner and operator workflow—including plugins, reports,
upgrades, and the boundary between Dora and a product—read
[`docs/operator-guide.md`](docs/operator-guide.md).

For the Codex-first route from an idea to verified application work, read
[`docs/agent-first-application-guide.md`](docs/agent-first-application-guide.md).

## Public agent command surface

Use these commands from the Dora package that controls the project. Every output is
declared guidance; only project work verification records completion evidence.

```text
dora agent-context <adapter-path> <work-plan-path> <task-id>
dora agent-next <adapter-path> <execution-inventory-path>
dora status <adapter-path> <execution-inventory-path>
dora impact <adapter-path> <node-id>...
dora agent-closeout <adapter-path> <work-plan-path> <task-id> <change-impact-path> <changed-path>...
```

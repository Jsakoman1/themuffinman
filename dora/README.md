# Dora

Bootstrap a local project from a reviewed local Dora source with `dora bootstrap`.
Choose `--starter blank` for a neutral skeleton, `--starter spring-vue` for empty
backend and frontend roots, or `--starter spring-vue-buildable` for a domain-free
Spring and Vue technical application with real setup, test, and build commands.
Starters do not create product behavior.

Dora is a portable delivery-control package for implementation planning,
verification, documentation contracts, and controlled project extensions.

## Start a new project

Use an explicit local Dora source. Dora never downloads code during bootstrap and
never invents product behavior. The complete beginner path is in
[`docs/new-project.md`](docs/new-project.md).

```text
/absolute/path/to/dora/bin/dora bootstrap my-app --project my-app --source bootstrap-source.yaml --starter blank
cd my-app
./bin/dora doctor .dora/project.yaml
```

The `blank` starter creates a neutral technical skeleton. The `spring-vue` starter
creates only empty backend and frontend roots. The `spring-vue-buildable` starter
adds a compilable Spring application and a dependency-free Vue browser surface so
the declared project commands can run immediately. All starters leave product
domains, authentication, data schemas, and user-facing features for deliberate
project work.

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

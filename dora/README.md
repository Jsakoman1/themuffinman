# Dora

Bootstrap a local project from a reviewed local Dora source with `dora bootstrap`.
Choose `--starter blank` for a neutral skeleton, `--starter spring-vue` for empty
backend and frontend roots, or `--starter spring-vue-buildable` for a domain-free
Spring and Vue technical application with real setup, test, and build commands.
Starters do not create product behavior.

Dora is a portable delivery-control package for implementation planning,
verification, documentation contracts, and controlled project extensions.

## Start a new project

For a new product, the primary Codex and beginner route is a confirmed idea
interview plus a reviewed local Dora source. Codex records only user-confirmed
answers and explicit open decisions in `idea-interview.yaml`; Dora then creates
neutral project context, a first capability package, a first work declaration,
and a local Git baseline. It does not invent product rules or implementation.

```text
/absolute/path/to/dora/bin/dora create-app my-app \
  --interview idea-interview.yaml \
  --source bootstrap-source.yaml \
  --starter spring-vue-buildable \
  --codex-integrate
```

The starter is optional and technical only. `--source` must be a reviewed local
source descriptor with an immutable ref and checksum. `--codex-integrate` adds
project-local Dora navigation without overwriting `AGENTS.md`.

`dora new <destination> --answers project-new.yaml` remains available for
explicit compatibility workflows that already own complete product knowledge.

For a project-local Dora package, reviewed immutable source, technical starter, or
CI pack, use `dora bootstrap`. Dora never downloads code during bootstrap. The
complete beginner path is in [`docs/new-project.md`](docs/new-project.md).

Before starting controlled work, inspect baseline readiness with
`dora readiness <project-root>`. It never changes Git by default. Use
`--initialize-git` only when you explicitly want Dora to create the local initial
commit for a new project.

After project creation, read the generated product/domain knowledge, run
`dora diagnose .dora/project.yaml`, use `dora next` for the declared inventory,
and start only one bounded work task at a time. A generated capability package is
starting context, not implementation or release proof.

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

## Guided authoring and technical proof

Codex can begin with a resumable, user-confirmed (`user_confirmed`) interview rather than hand-writing
YAML. Use `dora interview-start`, `dora interview-next`, and
`dora interview-answer`; then use `dora authoring-next` for one declared next
action. Check local prerequisites with `dora app-readiness`, obtain a read-only
proposal with `dora vertical-slice`, and append cited choices with
`dora decision-record`. These commands never infer business rules or create
product source code.

The optional `dora runtime-profile-apply <destination> --dry-run|--apply` profile
contains only a neutral technical health check. Browser installation and runtime
execution require explicit user approval; the profile is not authentication,
authorization, backup, deployment, or production readiness.

## v1.5 controlled application assembly

After a complete confirmed interview session, `dora session-create-app` can preview
or create a new empty project without manually assembling a create-app bundle:

```text
dora session-create-app my-app --input session-create-app.yaml --preview
dora session-create-app my-app --input session-create-app.yaml --apply
```

For one explicitly confirmed capability, `dora feature-skeleton` proposes migration,
backend, API, Vue, test, runtime-evidence, and documentation paths without writing
project files. The Ruby apply API writes only review-required placeholders, rejects
path traversal and collisions, and is not implementation or acceptance proof.

v1.5 also supplies neutral auth/permission, data-safety/demo-data, UI/accessibility,
operational-readiness, migration/API-workflow safety, architecture-decision, and
capability-acceptance contracts. They require explicit consumer declarations. They
never create credentials, real data, secrets, deployments, or passing evidence.

## v1.6 compiled feature route

For the declared `spring-vue-postgres-buildable` starter, v1.6 can turn a fully
confirmed compiled-feature document into a deterministic Spring JDBC, Flyway, API,
Vue, test, documentation, and evidence output set. First inspect the exact output:

```text
dora compiled-feature-preview confirmed-compiled-feature.yaml --format yaml
```

The preview includes the migration version, output paths, template hashes, and
unresolved obligations. A Codex agent must review it, render the declared source,
and use the explicit `Dora::CompiledFeatureApply` API with the exact manifest and
rendered-file map. Apply rejects a collision, traversal, historic migration target,
or a map that differs from the manifest.

Generation is not completion. Run the generated safety inspection; obtain approval
before compile work that may download dependencies; keep database and browser proof
as separate approved tasks; and record project-owned business acceptance evidence.
The full route is in [`docs/compiled-feature-route.md`](docs/compiled-feature-route.md).

# Dora operator guide

Dora is a local, portable delivery-control package. It gives a project a repeatable
way to initialize technical structure, declare implementation work, validate that
work, configure local controls, and collect diagnostic plugin reports. It does not
choose or implement a product's domain, permissions, data model, runtime evidence,
or release decision.

## First project from a reviewed source

Keep a reviewed Dora directory locally and record its immutable 40-character
revision in a source descriptor. From an empty parent directory:

```text
/absolute/path/to/dora/bin/dora bootstrap my-app --project my-app --source bootstrap-source.yaml --starter spring-vue-buildable --ci github-actions
cd my-app
./bin/dora doctor .dora/project.yaml
```

`bootstrap-source.yaml` must identify a local Dora path and its reviewed commit:

```yaml
kind: dora_bootstrap_source
version: 1
source:
  path: /absolute/path/to/reviewed/dora
  ref: 0123456789abcdef0123456789abcdef01234567
```

Bootstrap copies Dora into `my-app/dora`, creates the project-local `./bin/dora`
launcher, records the source in `.dora/bootstrap-source.yaml`, creates the control
files, and generates the selected starter. No network download occurs during this
step.

## Choose a starter and run it

Use `blank` for an empty technical skeleton and `spring-vue` for empty backend and
frontend roots. Use `spring-vue-buildable` for a domain-free Spring application and
a minimal Vue browser surface whose commands already run. Use
`spring-vue-postgres-buildable` when a new application explicitly needs a neutral
Spring Boot, Vue, PostgreSQL, Flyway, health, and Compose foundation:

```text
/absolute/path/to/dora/bin/dora bootstrap my-app --project my-app --source bootstrap-source.yaml --starter spring-vue-buildable --ci github-actions
cd my-app
npm --prefix frontend install --ignore-scripts
mvn -q -f backend/pom.xml test && npm --prefix frontend run test
mvn -q -f backend/pom.xml package && npm --prefix frontend run build
```

For the PostgreSQL foundation, choose the starter explicitly through `create-app` or
`bootstrap`, then create a local `.env` from the example before starting the local
database. Do not commit `.env`.

```text
/absolute/path/to/dora/bin/dora create-app my-app --interview idea-interview.yaml --source bootstrap-source.yaml --starter spring-vue-postgres-buildable --codex-integrate
cd my-app
cp .env.example .env
# Set a local POSTGRES_PASSWORD in .env.
docker compose --env-file .env up -d postgres
npm --prefix frontend install --ignore-scripts
mvn -q -f backend/pom.xml test && npm --prefix frontend run test
mvn -q -f backend/pom.xml package && npm --prefix frontend run build
# With the backend running locally: curl --fail http://localhost:8080/actuator/health
```

This foundation provides technical wiring only. It does not choose authentication,
product permissions, data retention, backups, schema fields, domain entities, API
resources, production hosting, or a release process. Confirm those decisions in the
product before asking Codex to plan implementation.

## Review a first vertical slice before implementation

After the idea interview, Codex can form a confirmed capability context and use
Dora's vertical-slice proposal and readiness checks. The proposal lists only the
candidate migration, backend, API, frontend, test, runtime-evidence, documentation,
and atomic-work paths. It is not source code, SQL, a generated database change, or an
implementation command.

If data safety, workflow, permission, or technical decisions are missing, the
readiness result is blocked and names each missing decision. Confirm it with the
product owner first; do not let Codex fill it in by inference.

Maintainers can repeat the independent proof from the Dora package with:

```text
ruby test/independent_postgres_starter_consumer_test.rb
ruby test/independent_postgres_starter_runtime_test.rb
ruby test/independent_vertical_slice_consumer_test.rb
```

`independent_postgres_starter_runtime_test.rb` creates a fresh temporary consumer,
starts its own scoped Compose PostgreSQL container, starts the generated backend, and
executes the declared health command. Its cleanup removes only that temporary Compose
project and its volume. Docker Desktop must be running; this test does not use a
shared database or generate product data.

The same commands are declared in `.dora/project-commands.yaml`; the generated CI
workflow uses them. Before adding any feature, replace or extend those commands only
when the project genuinely needs different technical validation.

## Day-to-day control loop

Run `./bin/dora help` to see the project-local commands. The usual loop is:

1. Create a narrow work plan with one observable task at a time.
2. Start it with `./bin/dora work-start .dora/project.yaml plan=<path> task=<id>`.
3. Implement the bounded change and run the task's leaf validation.
4. Record passing evidence with `./bin/dora work-verify .dora/project.yaml plan=<path> task=<id>`.
5. Run `./bin/dora doctor .dora/project.yaml` before handoff to confirm the local control declarations remain healthy.

`work-verify` records command output and changed-path evidence. It is a delivery
control, not proof that a product feature is safe to release.

## Codex-first guided authoring

For a new idea, start `dora interview-start <session.yaml> --project <id>` and let
Codex use `dora interview-next` to ask one required question at a time. Record an
answer only with `dora interview-answer`; its provenance is `user_confirmed`.
`dora authoring-next` returns one deterministic next action and never fills an open
decision by inference.

Use `dora app-readiness <starter-compatibility.yaml>` before a starter to inspect
only declared local tool versions. `dora vertical-slice <context.yaml>` returns a
read-only proposal plus decision blockers. `dora decision-record <log.yaml> ...`
appends a cited record and preserves earlier records. None of these commands
implements product code or grants release approval.

`dora runtime-profile-apply <destination> --dry-run` previews the neutral
Playwright technical-health profile; `--apply` requires an empty destination and
does not install a browser. A browser installation or runtime execution is an
explicit-approval action. Its evidence covers only the temporary local technical
health marker, never a consumer flow, authorization, data safety, deployment, or
production readiness.

## Add a portable plugin and read its report

Declare a plugin in `.dora/plugins.yaml`. A plugin has a project-relative entrypoint,
explicit source roots and inputs, and a report destination under `docs/audit-output/`.

```yaml
kind: dora_plugin_manifest
version: 1
plugins:
  - id: source-shape
    entrypoint: plugins/source_shape.rb
    source_roots:
      - id: application
        path: src
    inputs:
      scope: technical
    output:
      kind: source-shape-report
      path: docs/audit-output/source-shape.json
```

Run it with:

```text
./bin/dora plugin-contract .dora/plugins.yaml
./bin/dora plugin-run .dora/plugins.yaml source-shape
```

Dora writes the structured report to the declared JSON file. Plugin findings are
diagnostic evidence only; they do not replace domain tests, runtime acceptance, or
release approval.

## Upgrade Dora safely

Dora is deliberately copied into each project so a project stays reproducible.
To upgrade, obtain a newer reviewed local Dora source, compare its immutable commit
and release notes, make a reversible project backup or branch, then bootstrap a
fresh temporary project with the same starter. Run its setup, test, build, doctor,
and plugin checks before copying the reviewed Dora package and compatible control
changes into the existing project. Update `.dora/bootstrap-source.yaml` only after
that review. Dora does not silently upgrade an existing project.

## What Dora owns and what the product retains

Dora owns reusable mechanics: starter generation, reviewable vertical-slice
proposals, readiness gates, work-plan contracts, serial verification, adapters,
control schemas, portable plugins, report writing, and CI templates. A consuming
application owns its product meaning: entities, permissions, workflows, API behavior,
user-facing copy, runtime and browser evidence, production operations, and release
approval.

For MuffinMan specifically, `docs/dora-muffinman-tool-ownership.yaml` classifies
each local audit as delegated or extracted reusable mechanics, or as product-retained
authority. The catalog is a MuffinMan decision record, not a Dora requirement for
other applications. When creating a new product, make the same ownership decision
for its local audits rather than copying MuffinMan product checks blindly.

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
a minimal Vue browser surface whose commands already run:

```text
/absolute/path/to/dora/bin/dora bootstrap my-app --project my-app --source bootstrap-source.yaml --starter spring-vue-buildable --ci github-actions
cd my-app
npm --prefix frontend install --ignore-scripts
mvn -q -f backend/pom.xml test && npm --prefix frontend run test
mvn -q -f backend/pom.xml package && npm --prefix frontend run build
```

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

Dora owns reusable mechanics: starter generation, work-plan contracts, serial
verification, adapters, control schemas, portable plugins, report writing, and CI
templates. A consuming application owns its product meaning: entities, permissions,
workflows, API behavior, user-facing copy, runtime and browser evidence, production
operations, and release approval.

For MuffinMan specifically, `docs/dora-muffinman-tool-ownership.yaml` classifies
each local audit as delegated or extracted reusable mechanics, or as product-retained
authority. The catalog is a MuffinMan decision record, not a Dora requirement for
other applications. When creating a new product, make the same ownership decision
for its local audits rather than copying MuffinMan product checks blindly.

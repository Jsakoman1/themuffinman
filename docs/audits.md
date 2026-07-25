# Local Audits

Audits are optional diagnostics. They never decide whether a work plan is complete; `make work-verify` owns that
decision.

All audit reports are disposable and are written under `docs/audit-output/`. They are not a second source of truth.

## Catalog

Run the compact retained control set with:

```text
make control-check
```

Run the broad diagnostic set only when needed:

```text
make audit-all
```

Focused groups:

- `make audit-backend` — API drift, read surfaces, repository fetches, mapper usage, mutation safety
- `make audit-frontend` — endpoint links, route surfaces, stale surfaces, duplicated frontend logic, permission rules
- `make audit-docs` — documentation contracts and docs-as-tests
- `make audit-truth-registry` — canonical truth-registry metadata and path integrity
- `make audit-interface-evidence` — endpoint consumer-evidence classification over the static linker
- `make audit-data-workflow-impact` — migration, data ownership, and workflow coverage integrity
- `make audit-capability-evidence` — runtime artifact references and capability/evidence separation
- `make audit-delivery-provenance` — build, generated-contract, dependency, and release-provenance integrity
- `make system-map-impact` — advisory changed-file relationships to system-map registries and evidence sources
- `make audit-atomic-task-hardening` — atomic task and execution-inventory shape for the retained System Map control queue
- `make repository-map` — validates the frontend Babel AST index and JDK Java Compiler AST/source map without retaining generated output
- `make audit-tool-catalog` — detects local helpers that are no longer referenced by the repository control surface
- `make audit-runtime-tools` — checks retained Chromium/runtime scripts, Playwright availability, references, and syntax without claiming live runtime proof
- `make tool-self-test` — tests helper syntax, YAML parsing, AST indexing, repository mapping, catalog integrity, and bounded search
- `make clean-generated` — remove disposable audit output, frontend build output, and temporary files

Use a focused audit in a work plan when it is relevant to that change. A passing audit is evidence for that task, not a
completion signal by itself.

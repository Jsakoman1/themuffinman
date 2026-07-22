# Local Audits

Audits are optional diagnostics. They never decide whether a work plan is complete; `make work-verify` owns that
decision.

All audit reports are disposable and are written under `docs/audit-output/`. They are not a second source of truth.

## Catalog

Run the complete practical set with:

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
- `make audit-tests` — contract-test gaps and fixture duplication
- `make audit-impact` — changed-file impact analysis and changeset risk

Use a focused audit in a work plan when it is relevant to that change. A passing audit is evidence for that task, not a
completion signal by itself.

# Validation Memory Phase Three Plan

Purpose: connect validation-memory directly into feature closeout enforcement and add a dedicated drift audit between the human and machine-readable validation memory layers.

## Goal

Keep closeout checks and validation-memory docs synchronized by sourcing canonical command expectations from `docs/validation-memory.json` and auditing drift between `docs/validation-memory.md` and the JSON contract.

## Scope

- `scripts/feature-closeout-audit.sh`
- `scripts/audits/audit-validation-memory-drift.rb`
- `scripts/audits/local_tooling_extended_tools.rb`
- `Makefile`
- `docs/tooling/codex-local-audits.md`
- `docs/validation-memory.md`

## Checklist

- [x] Make feature closeout audit read canonical command expectations from validation-memory JSON.
- [x] Add a dedicated validation-memory drift audit.
- [x] Register the new audit in local tooling surfaces.
- [x] Validate closeout, docs, and the new drift audit.

## Validation

- `ruby scripts/audits/audit-validation-memory-drift.rb`
- `make feature-closeout-audit manifest=.agents/feature-manifests/vision-voice-surface-manifest.yaml`
- `make audit-documentation`
- `make audit-todo`

## Completion Evidence

- Status: complete
- Execution status: complete
- Validation:
  - `ruby scripts/audits/audit-validation-memory-drift.rb`
  - `make feature-closeout-audit manifest=.agents/feature-manifests/vision-voice-surface-manifest.yaml`
  - `make generate-audit-registry-artifacts`
  - `./mvnw test -Dtest=AgentOperatingModelValidationTest`
  - `make audit-documentation`
  - `make audit-todo`
- Notes:
  - `feature-closeout-audit` now reads `docs/validation-memory.json` for canonical frontend, backend, agent, workflow, and closeout command expectations instead of duplicating those strings ad hoc.
  - Added `scripts/audits/audit-validation-memory-drift.rb` so the human and machine-readable validation memory layers can drift-check each other explicitly.
  - The closeout integration stays conservative: validation-memory strengthens exact command matching, but it does not replace the stronger deterministic manifest, schema, or backend validation rules.
- Persistent backlog item: none

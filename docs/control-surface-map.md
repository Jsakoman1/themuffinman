# Control Surface Map

This map separates live truth from generated control outputs and historical archive material.

For active operational state and automation-facing facts, prefer machine-readable files first. Treat markdown as the
explanatory layer unless the markdown file is explicitly listed as the canonical business, domain, or workflow source
of truth.

## Live Truth

These files should be treated as the primary editable source for current behavior:

- `docs/agent-operating-model.yaml`
- `docs/agent-operating-model/sections/*.yaml`
- `docs/business-logic.md`
- `docs/domain-technical.md`
- `docs/regression-scenario-catalog.yaml`
- `docs/regression-scenario-catalog.md`
- `docs/validation-memory.md`
- `docs/validation-memory.json`
- `docs/source-of-truth-inventory.md`
- `docs/implementation-backlog.md`
- `docs/agent-improvement-backlog.md`
- active `.agents/feature-manifests/*.yaml`

## Generated Control

These outputs are useful for audits, validation, and local tooling, but they are not the primary source of truth:

- `docs/generated/agent-endpoint-inventory.json`
- `docs/generated/automation-read-model-inventory.json`
- `docs/generated/backend-audit-inventory.json`
- `docs/generated/source-of-truth-audit.json`
- `docs/generated/local-tooling/audit-summary-index.*`
- `docs/generated/local-tooling/context-packs/*`
- `docs/generated/local-tooling/domain-packs/*`
- `docs/generated/local-tooling/endpoint-contract-packs/*`
- `docs/generated/local-tooling/docs-as-tests.*`
- `docs/generated/local-tooling/diagnostics/*`
- `docs/generated/local-tooling/workflow-slices/*`
- `docs/generated/local-tooling/plan-code-maps/*`

## Historical Archive

These are useful for traceability, but they should not be treated as active behavior definitions or current control state:

- completed `.agents/*.md` plans
- completed `.agents/feature-manifests/*.yaml`
- `docs/generated/local-tooling/.history/*`
- `docs/generated/local-tooling/.cache/*`
- long-form retrospective and summary outputs under `docs/generated/local-tooling/`

## Rule Of Thumb

- If a change affects current product behavior, update the live truth first.
- If a change only affects reporting or automation summaries, update the generated control layer.
- If a file only preserves history, leave it as archive unless it is actively confusing current work.
- If a generated review artifact starts to look like a second source of truth, compact it back into a clearer review surface.
- If a state can be represented machine-readably, store the operational version in YAML, JSON, or manifest form first
  and use markdown for explanation, review, or curated context.

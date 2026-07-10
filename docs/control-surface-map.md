# Control Surface Map

This map separates live truth from generated control outputs and historical archive material.

For active operational state and automation-facing facts, prefer machine-readable files first. Treat markdown as the
explanatory layer unless the markdown file is explicitly listed as the canonical business, domain, or workflow source
of truth.

## Live Truth

These files should be treated as the primary editable source for current behavior:

- `docs/agent-operating-model.yaml`
- `docs/agent-operating-model/sections/*.yaml`
- `docs/program-planning-model.md`
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
- `docs/batch-shell-model.md`

### Batch Shell Model

Use these files as the live inventory for the new batch-shell model:

- `apps/themuffinman/frontend/src/modules/app-shell/shellDefinitions.ts`
- `apps/themuffinman/frontend/src/modules/app-shell/shellRouteRegistry.ts`
- `apps/themuffinman/frontend/src/modules/app-shell/shellSurfaceData.ts`
- `apps/themuffinman/frontend/src/modules/app-shell/visionHandoff.ts`
- `apps/themuffinman/frontend/src/router.ts`

The batch-shell model treats `batch-plan.md` files as the durable planning surface for in-flight work when such plans
exist. At the moment there are no active batch-plan files, so the live planning surface is effectively empty.
Older planning artifacts may still exist in historical outputs, but they are not the live operational source of truth.

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
- `.agents/templates/master-plan.template.md`
- `.agents/templates/feature-implementation-plan.template.md`

Treat these as generated operational aids. They may drive routing, audits, or closeout checks, but they do not replace
the live truth and they are not all equally important during day-to-day work.

### Operator-Core Generated Control

These are the default compact entry surfaces for everyday work:

- `docs/generated/local-tooling/audit-summary-index.*`
- `docs/generated/local-tooling/codex-context/latest.*`
- `docs/generated/local-tooling/targeted-tests.*`

### Focused Review Packs

Open these only when the compact operator-core surfaces do not answer the question:

- `docs/generated/local-tooling/context-packs/*`
- `docs/generated/local-tooling/domain-packs/*`
- `docs/generated/local-tooling/endpoint-contract-packs/*`
- `docs/generated/local-tooling/workflow-slices/*`
- `docs/generated/local-tooling/dto-usage-packs/*`
- `docs/generated/local-tooling/symbol-test-links/*`

## Historical Archive

These are useful for traceability, but they should not be treated as active behavior definitions or current control state:

- completed `.agents/*.md` plans
- completed `.agents/feature-manifests/*.yaml`
- `docs/generated/local-tooling/.history/*`
- `docs/generated/local-tooling/.cache/*`
- long-form retrospective and summary outputs under `docs/generated/local-tooling/`

History and cache outputs are archive-only support material. They should not be part of the default operator read path
or the default commit-review signal.

## Rule Of Thumb

- If a change affects current product behavior, update the live truth first.
- If a change only affects reporting or automation summaries, update the generated control layer.
- If a file only preserves history, leave it as archive unless it is actively confusing current work.
- If a generated review artifact starts to look like a second source of truth, compact it back into a clearer review surface.
- If a state can be represented machine-readably, store the operational version in YAML, JSON, or manifest form first
  and use markdown for explanation, review, or curated context.
- If a generated file only preserves prior snapshots or cache state, treat it as archive noise unless a specific audit
  or retrospective task is using it on purpose.

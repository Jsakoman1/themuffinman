# STD-DOCS-VALIDATION-STANDARDIZATION Plan

Source: `docs/implementation-backlog.md`
Group: `implementation`
Risk: `medium`
Master order: 89 of 89

## Backlog Item

Standardize docs, generated audit outputs, and validation coverage so the repository keeps one repeatable closeout and audit model.

## Source Findings

- [`.agents/system-standardization-audit-findings.md`](/Users/jsakoman/Desktop/themuffinman/.agents/system-standardization-audit-findings.md)
- Docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/workflow-state-machines.yaml`, `docs/agent-operating-model.yaml`, `docs/documentation-sync-policy.md`, `docs/change-completion-checklist.md`
- Generated audit surfaces: `docs/generated/local-tooling/read-surface-inventory-summary.md`, `docs/generated/local-tooling/test-surface-inventory-summary.md`, `docs/generated/local-tooling/backend-dependency-graph-summary.md`, `docs/generated/local-tooling/frontend-route-surface-inventory.json`, `docs/generated/local-tooling/api-contract-drift-summary.md`
- Validation surfaces: backend test suites, frontend type-check/build, and `ruby scripts/todo-audit.rb`

## Implementation Plan

- [x] Consolidate terminology drift and boundary wording across the living docs.
- [x] Keep the agent-operating and documentation-sync rules aligned with actual practice.
- [x] Make the generated audit outputs easier to use as a standard closeout input.
- [x] Ensure the validation coverage matches the changed surfaces instead of relying on generic checks.
- [x] Capture any remaining process drift in the agent-improvement backlog if it belongs there instead of here.

## Expected Validation

- `ruby scripts/todo-audit.rb`
- `cd apps/themuffinman && ./mvnw test`
- `npm --prefix apps/themuffinman/frontend run type-check`
- `npm --prefix apps/themuffinman/frontend run build`

## Completion Evidence

- Status: complete
- Changed files: `docs/domain-technical.md`, `docs/implementation-backlog.md`, `docs/generated/local-tooling/.cache/audit-inputs.json`, `docs/generated/local-tooling/targeted-tests-summary.md`, `docs/generated/local-tooling/targeted-tests.json`, `docs/generated/local-tooling/workflow-slices/quest-application-summary.md`, `docs/generated/local-tooling/workflow-slices/quest-application.json`
- Validation evidence: `ruby scripts/todo-audit.rb` passed; `npm run type-check` passed; `npm run build` passed
- Doc delta summary: living docs now reflect the shared app shell, the deleted dashboard compatibility wrapper reference was removed, and generated closeout inputs were refreshed by the audit pass
- Backlog update: removed from open backlog.
- Residual risk: none known

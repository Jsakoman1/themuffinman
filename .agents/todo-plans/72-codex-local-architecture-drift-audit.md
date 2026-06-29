# CODEX-LOCAL-ARCHITECTURE-DRIFT-AUDIT Plan

Source: `docs/codex-local-tooling-todo.md`
Group: `local-tooling`
Risk: `medium`
Master order: 72 of 82

## Backlog Item

Flag oversized classes, controllers with business logic, Vue views with product logic, repeated permission checks, and services that mix query, mutation, policy, and mapping responsibilities.

Source notes:
  Proposed entrypoints:
  - `ruby scripts/audits/audit-architecture-drift.rb`
  - `make audit-architecture-drift`
  Proposed outputs:
  - `docs/generated/local-tooling/architecture-drift.json`
  - `docs/generated/local-tooling/architecture-drift-summary.md`
  Notes:
  - Keep it report-first and threshold-based to avoid noisy rewrites.
  - Link findings to implementation backlog IDs when possible.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make audit-documentation`
- [x] `make audit-local-tooling-incremental`
- [x] `cd apps/themuffinman && ./mvnw test`
- [x] `cd apps/themuffinman/frontend && npm run type-check`
- [x] `cd apps/themuffinman/frontend && npm run build`

## Completion Evidence

- Status: complete
- Changed files: added `scripts/audits/audit-architecture-drift.rb`; extended `scripts/audits/local_tooling_batch_audits.rb`, `scripts/audits/local_tooling_extended_tools.rb`, `Makefile`, `docs/codex-local-tooling-todo.md`, `docs/change-completion-checklist.md`, `docs/domain-technical.md`, and generated `docs/generated/local-tooling/architecture-drift.json`, `docs/generated/local-tooling/architecture-drift-summary.md`, and refreshed audit registry/index artifacts.
- Validation evidence: `make audit-architecture-drift` generated a report-first architecture drift report with 26 findings; `ruby scripts/audits/generate-audit-registry-artifacts.rb`, `ruby scripts/audits/audit-make-target-index.rb`, `ruby scripts/todo-audit.rb`, `make audit-documentation`, `make audit-agent-safety`, `make audit-local-tooling-incremental`, `ruby scripts/audits/audit-generated-artifact-freshness.rb`, `cd apps/themuffinman/frontend && npm run type-check`, `cd apps/themuffinman/frontend && npm run build`, and `cd apps/themuffinman && ./mvnw test` all passed.
- Backlog update: removed `CODEX-LOCAL-ARCHITECTURE-DRIFT-AUDIT` from open local tooling items and listed `make audit-architecture-drift` under available local audits.
- Residual risk: findings are threshold-based review signals, so the report may include false positives and does not enforce architectural limits yet.

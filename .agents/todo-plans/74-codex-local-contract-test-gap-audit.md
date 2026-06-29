# CODEX-LOCAL-CONTRACT-TEST-GAP-AUDIT Plan

Source: `docs/codex-local-tooling-todo.md`
Group: `local-tooling`
Risk: `medium`
Master order: 74 of 82

## Backlog Item

Map endpoints and DTOs to backend tests, frontend contract usage, generated contracts, and documented behavior to find missing contract checks.

Source notes:
  Proposed entrypoints:
  - `ruby scripts/audits/audit-contract-test-gaps.rb`
  - `make audit-contract-test-gaps`
  Proposed outputs:
  - `docs/generated/local-tooling/contract-test-gaps.json`
  - `docs/generated/local-tooling/contract-test-gaps-summary.md`
  Notes:
  - Prioritize automation-relevant and planner-visible DTOs first.
  - This should reduce silent backend/frontend drift.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make audit-agent-safety`
- [x] `make audit-local-tooling-incremental`
- [x] `cd apps/themuffinman && ./mvnw test`
- [x] `cd apps/themuffinman/frontend && npm run type-check`
- [x] `cd apps/themuffinman/frontend && npm run build`

## Completion Evidence

- Status: complete
- Changed files: `scripts/audits/audit-contract-test-gaps.rb`, `scripts/audits/local_tooling_extended_tools.rb`, `Makefile`, `docs/codex-local-tooling-todo.md`, `docs/domain-technical.md`, generated local-tooling artifacts
- Validation evidence: `ruby -c scripts/audits/audit-contract-test-gaps.rb`; `make audit-contract-test-gaps`; `make generate-audit-registry-artifacts`; `ruby scripts/todo-audit.rb`; `make audit-agent-safety`; `make audit-local-tooling-incremental`; `cd apps/themuffinman && ./mvnw test` (273 tests, 0 failures); `cd apps/themuffinman/frontend && npm run type-check`; `cd apps/themuffinman/frontend && npm run build`
- Backlog update: `CODEX-LOCAL-CONTRACT-TEST-GAP-AUDIT` marked complete in `docs/codex-local-tooling-todo.md`
- Residual risk: Signal matching is conservative and may miss tests or docs that use different terminology.

# IMPL-ADMIN-SANDBOX-SEPARATION Plan

Source: `docs/implementation-backlog.md`
Group: `implementation`
Risk: `high`
Master order: 21 of 82

## Backlog Item

Make production admin flows, sandbox generation flows, and synthetic data helpers structurally separate packages with clear naming and tests.

Source notes:
  Goal: preserve safety boundaries and make automation-related changes easier to audit.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `cd apps/themuffinman && ./mvnw test`

## Completion Evidence

- Status: complete
- Changed files: added `agent/sandbox` planner classes and tests, wired `AdminAgentPlaygroundService` to delegate sandbox-generation planning, updated agent docs, living docs, source-of-truth coverage, backend audit classification, and generated artifacts.
- Validation evidence: targeted agent tests passed with 38 tests; `./mvnw test` passed with 269 tests; `npm run type-check` passed; `npm run build` passed; `make audit-agent-safety` passed; `make audit-sandbox-generation-coverage` passed; `make audit-documentation` passed; `make audit-generated-artifact-freshness` passed; `ruby scripts/todo-audit.rb` passed.
- Backlog update: removed `IMPL-ADMIN-SANDBOX-SEPARATION` from `docs/implementation-backlog.md`.
- Residual risk: sandbox generation remains planning-only; no synthetic data execution endpoint or production mutation path was added.

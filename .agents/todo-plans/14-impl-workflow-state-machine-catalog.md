# IMPL-WORKFLOW-STATE-MACHINE-CATALOG Plan

Source: `docs/implementation-backlog.md`
Group: `implementation`
Risk: `high`
Master order: 14 of 82

## Backlog Item

Standardize workflow transitions for quests, applications, circle requests, chat conversations, and future bookings as named state-machine contracts.

Source notes:
  Goal: make state transitions auditable, easier to test, and easier for Codex to extend without missing edge cases.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make audit-agent-safety`
- [x] `cd apps/themuffinman && ./mvnw test`

## Completion Evidence

- Status: complete
- Changed files: `docs/workflow-state-machines.yaml`, `docs/workflow-state-machines.md`, `WorkflowStateMachineCatalogTest.java`, `docs/business-logic.md`, `docs/domain-technical.md`, `docs/implementation-backlog.md`
- Validation evidence: `make audit-agent-safety` passed; `cd apps/themuffinman && ./mvnw test` passed with 260 tests; `make audit-documentation` passed; `ruby scripts/todo-audit.rb` passed.
- Backlog update: removed `IMPL-WORKFLOW-STATE-MACHINE-CATALOG` from `docs/implementation-backlog.md`.
- Residual risk: the catalog is a validation-backed contract, not a runtime state-machine engine; runtime services remain the execution owners.

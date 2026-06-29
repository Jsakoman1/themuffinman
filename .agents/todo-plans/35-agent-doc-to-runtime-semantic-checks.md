# AGENT-DOC-TO-RUNTIME-SEMANTIC-CHECKS Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `low`
Master order: 35 of 82

## Backlog Item

Add stronger semantic checks between documented rules and runtime behavior.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make audit-agent-safety`

## Completion Evidence

- Status: complete
- Changed files:
  - `apps/themuffinman/src/test/java/com/themuffinman/app/docs/WorkflowStateMachineCatalogTest.java`
  - `docs/agent-improvement-backlog.md`
  - `docs/agent-operating-model.md`
  - `docs/domain-technical.md`
  - `.agents/todo-master-plan.md`
  - `.agents/todo-plans/35-agent-doc-to-runtime-semantic-checks.md`
- Validation evidence:
  - `./mvnw test -Dtest=WorkflowStateMachineCatalogTest`
  - `make audit-agent-safety`
  - `ruby scripts/todo-audit.rb`
- Backlog update: removed `AGENT-DOC-TO-RUNTIME-SEMANTIC-CHECKS` from `docs/agent-improvement-backlog.md`.
- Residual risk: the new semantic check verifies workflow transition intent ids against known mutating intents; deeper source-level proof that every transition implementation enforces every documented guard remains deferred to existing use-case and scenario contract tests.

# AGENT-CONTEXT-FIRST-WORKFLOW Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `medium`
Master order: 39 of 82

## Backlog Item

Standardize a session-start workflow where Codex reads a compact codebase capsule, diff summary, audit summary index, and topic context pack before broad repository exploration.

Source notes:
  Purpose: reduce token usage by making compact local summaries the default entrypoint for implementation work.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make audit-documentation`
- [x] `make audit-agent-safety`
- [x] `make audit-local-tooling-incremental`
- [x] `cd apps/themuffinman && ./mvnw test`

## Completion Evidence

- Status: complete
- Changed files:
  - `scripts/audits/local_tooling_batch_audits.rb`
  - `docs/tooling/codex-local-audits.md`
  - `docs/agent-operating-model.md`
  - `docs/domain-technical.md`
  - `docs/agent-improvement-backlog.md`
  - `.agents/todo-master-plan.md`
  - `.agents/todo-plans/39-agent-context-first-workflow.md`
- Validation evidence:
  - `make audit-documentation`
  - `make audit-agent-safety`
  - `make audit-local-tooling-incremental`
  - `./mvnw test`
  - `ruby scripts/todo-audit.rb`
- Backlog update: removed `AGENT-CONTEXT-FIRST-WORKFLOW` from `docs/agent-improvement-backlog.md`.
- Residual risk: the context-first workflow is documented and generated into the local-audit guide; future task-context budget tooling can add size limits and hard evidence checks.

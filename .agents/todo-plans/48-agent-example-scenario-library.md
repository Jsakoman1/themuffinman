# AGENT-EXAMPLE-SCENARIO-LIBRARY Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `medium`
Master order: 48 of 82

## Backlog Item

Maintain compact examples for common implementation patterns such as adding an endpoint, changing a workflow transition, adding a DTO, adding a migration, and updating docs.

Source notes:
  Purpose: give Codex canonical patterns to follow instead of inferring style from many files.

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
- [x] `cd apps/themuffinman && ./mvnw test`

## Completion Evidence

- Status: complete
- Changed files: `docs/example-scenario-library.md`, `docs/agent-operating-model.md`, `docs/change-completion-checklist.md`, `docs/domain-technical.md`, `docs/agent-improvement-backlog.md`, `docs/agent-operating-model/sections/documentation_sync.yaml`, `docs/agent-operating-model.yaml`, `apps/themuffinman/src/test/java/com/themuffinman/app/docs/AgentOperatingModelValidationTest.java`
- Validation evidence: `ruby scripts/generate-agent-operating-model.rb` passed; `ruby scripts/todo-audit.rb` passed; `make audit-documentation` passed; `make audit-agent-safety` passed; `cd apps/themuffinman && ./mvnw test` passed with 273 tests.
- Backlog update: removed `AGENT-EXAMPLE-SCENARIO-LIBRARY` from `docs/agent-improvement-backlog.md`.
- Residual risk: examples are intentionally compact pattern guidance; they do not replace per-change plans, docs templates, or validation evidence.

# AGENT-IMPLEMENTATION-SLICE-CHECKPOINTS Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `medium`
Master order: 40 of 82

## Backlog Item

Extend the operating model with explicit small-slice checkpoints for broad changes: plan, first backend slice, first frontend slice, docs/artifacts sync, validation, and commit boundary.

Source notes:
  Purpose: avoid oversized implementation turns and make partial progress safer to resume.

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
- [x] `cd apps/themuffinman/frontend && npm run type-check`
- [x] `cd apps/themuffinman/frontend && npm run build`

## Completion Evidence

- Status: complete
- Changed files: `docs/agent-operating-model/sections/policies.yaml`, `docs/agent-operating-model.yaml`, `docs/agent-operating-model.schema.json`, `docs/agent-operating-model.md`, `docs/change-completion-checklist.md`, `docs/documentation-sync-policy.md`, `docs/domain-technical.md`, `docs/agent-improvement-backlog.md`, `apps/themuffinman/src/test/java/com/themuffinman/app/docs/AgentOperatingModelValidationTest.java`
- Validation evidence: `ruby scripts/todo-audit.rb` passed; `make audit-documentation` passed; `./mvnw test -Dtest=AgentOperatingModelValidationTest` passed; `make audit-agent-safety` passed; `cd apps/themuffinman && ./mvnw test` passed with 273 tests; `cd apps/themuffinman/frontend && npm run type-check` passed; `cd apps/themuffinman/frontend && npm run build` passed.
- Backlog update: removed `AGENT-IMPLEMENTATION-SLICE-CHECKPOINTS` from `docs/agent-improvement-backlog.md`
- Residual risk: checkpoint rules are validated for presence and order, but later closeout-enforcement backlog items still own hard failure for incomplete slice evidence.

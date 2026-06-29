# AGENT-BROADER-PLANNER-DTO-REGISTRATION Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `medium`
Master order: 36 of 82

## Backlog Item

Extend source-of-truth registration audits to broader planner-visible DTO surfaces.

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
- Changed files:
  - `docs/agent-operating-model/sections/backend_audit_coverage.yaml`
  - `docs/agent-operating-model/sections/source_of_truth.yaml`
  - `docs/agent-operating-model/sections/documentation_coverage.yaml`
  - `docs/agent-operating-model.yaml`
  - `docs/generated/backend-audit-inventory.json`
  - `docs/generated/source-of-truth-audit.json`
  - `scripts/generate-source-of-truth-audit.rb`
  - `docs/agent-improvement-backlog.md`
  - `docs/agent-operating-model.md`
  - `docs/domain-technical.md`
  - `.agents/todo-master-plan.md`
  - `.agents/todo-plans/36-agent-broader-planner-dto-registration.md`
- Validation evidence:
  - `ruby scripts/generate-agent-operating-model.rb`
  - `ruby scripts/generate-backend-audit-inventory.rb`
  - `ruby scripts/generate-source-of-truth-audit.rb`
  - `./mvnw test -Dtest=AgentOperatingModelValidationTest`
  - `make audit-agent-safety`
  - `make audit-local-tooling-incremental`
  - `./mvnw test`
  - `npm run type-check`
  - `npm run build`
  - `ruby scripts/todo-audit.rb`
- Backlog update: removed `AGENT-BROADER-PLANNER-DTO-REGISTRATION` from `docs/agent-improvement-backlog.md`.
- Residual risk: common action/navigation primitives are now strict; remaining automation-relevant service, event, API-error, review DTO, and admin-update DTO gaps stay report-only for later smaller slices.

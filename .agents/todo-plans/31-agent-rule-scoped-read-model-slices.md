# AGENT-RULE-SCOPED-READ-MODEL-SLICES Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `low`
Master order: 31 of 82

## Backlog Item

Add more rule-scoped strict slices for high-value automation read models before considering wider tier promotion.

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
- Changed files: promoted the workmarket news read-model DTO slice into strict `automation_relevant` source-of-truth and documentation coverage, added source refs for the news item DTO and destination enum, and updated operating docs/current state.
- Validation evidence: `./mvnw test -Dtest=AgentOperatingModelValidationTest` passed; `make audit-agent-safety` passed with 96 backend subset tests, frontend type-check, admin-agent UI validation, frontend contract validation, frontend build, and TODO audit; TODO audit reports 29 open backlog items remaining.
- Backlog update: removed `AGENT-RULE-SCOPED-READ-MODEL-SLICES`; remaining broader tightening continues through existing specific backlog items rather than this general guardrail.
- Residual risk: no runtime behavior changed; validation must confirm generated artifacts and strict backend audit coverage are fresh.

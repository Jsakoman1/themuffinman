# AGENT-AUTOMATION-RELEVANT-SERVICE-COVERAGE Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `medium`
Master order: 30 of 82

## Backlog Item

Keep broader `automation_relevant` service coverage report-first until DTO contract drift is much lower.

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

## Completion Evidence

- Status: complete
- Changed files: documented that the broad `automation_relevant` service catch-all remains report-first, narrowed future tightening to small rule-scoped DTO/read-model/service slices, updated the persistent backlog current state, and removed the completed guardrail backlog item.
- Validation evidence: `./mvnw test -Dtest=AgentOperatingModelValidationTest` passed; `make audit-agent-safety` passed; `make audit-local-tooling-incremental` passed; `cd apps/themuffinman && ./mvnw test` passed with 269 tests; `ruby scripts/todo-audit.rb` passed with 30 open backlog items remaining.
- Backlog update: removed `AGENT-AUTOMATION-RELEVANT-SERVICE-COVERAGE`; the intentionally deferred strictening path remains represented by `AGENT-RULE-SCOPED-READ-MODEL-SLICES` and later rule-scoped audit-tightening items.
- Residual risk: no runtime behavior changed; risk is limited to documentation/model drift if generated artifacts are not refreshed.

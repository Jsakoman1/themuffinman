# AGENT-WORKMARKET-APPLICATION-DETAIL-DTO Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `medium`
Master order: 28 of 82

## Backlog Item

Tighten workmarket application-detail DTOs after the quest-detail slice is stable.

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
- Changed files: `docs/agent-operating-model/sections/backend_audit_coverage.yaml`, `docs/agent-operating-model/sections/source_of_truth.yaml`, `docs/agent-operating-model/sections/documentation_coverage.yaml`, generated agent/audit artifacts, living docs, persistent backlog, and master plan.
- Validation evidence: `cd apps/themuffinman && ./mvnw test -Dtest=AgentOperatingModelValidationTest`, `make audit-agent-safety`, `cd apps/themuffinman && ./mvnw test`, `make audit-documentation`, `make audit-generated-artifact-freshness`, and `ruby scripts/todo-audit.rb` passed.
- Backlog update: Removed `AGENT-WORKMARKET-APPLICATION-DETAIL-DTO` from `docs/agent-improvement-backlog.md` after adding application-detail DTOs to strict source and documentation coverage.
- Residual risk: Low; this was an audit/docs/generated-artifact tightening slice with no Java runtime behavior change.

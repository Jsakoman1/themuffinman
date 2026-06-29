# AGENT-REGRESSION-SCENARIO-CATALOG Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `medium`
Master order: 51 of 82

## Backlog Item

Maintain a catalog of critical regression scenarios per domain and map them to test classes and commands.

Source notes:
  Purpose: help Codex choose targeted tests without scanning the whole test tree.

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

## Completion Evidence

- Status: complete
- Changed files: `docs/regression-scenario-catalog.yaml`, `docs/regression-scenario-catalog.md`, `docs/agent-operating-model/sections/documentation_sync.yaml`, `docs/agent-operating-model.yaml`, `apps/themuffinman/src/test/java/com/themuffinman/app/docs/AgentOperatingModelValidationTest.java`, `docs/change-completion-checklist.md`, `docs/documentation-sync-policy.md`, `docs/domain-technical.md`, `docs/agent-improvement-backlog.md`, `docs/generated/source-of-truth-audit.json`
- Validation evidence: `ruby scripts/generate-agent-operating-model.rb` passed; `./mvnw test -Dtest=AgentOperatingModelValidationTest` passed; `ruby scripts/todo-audit.rb` passed; `make audit-documentation` passed; `make audit-agent-safety` passed; `ruby scripts/audits/audit-generated-artifact-freshness.rb` passed with `stale: 0`; `cd apps/themuffinman && ./mvnw test` passed with 273 tests.
- Backlog update: removed `AGENT-REGRESSION-SCENARIO-CATALOG` from `docs/agent-improvement-backlog.md`.
- Residual risk: the catalog maps current critical scenarios to focused backend commands; future new edge cases must update the catalog when introduced.

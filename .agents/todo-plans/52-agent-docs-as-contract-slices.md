# AGENT-DOCS-AS-CONTRACT-SLICES Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `medium`
Master order: 52 of 82

## Backlog Item

Promote selected business/domain documentation sections into contract-like slices that must have corresponding runtime tests or audit checks.

Source notes:
  Purpose: make important documented behavior executable instead of purely narrative.

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
- Changed files: `docs/docs-as-contract-slices.yaml`, `docs/docs-as-contract-slices.md`, `docs/agent-operating-model/sections/documentation_sync.yaml`, `docs/agent-operating-model.yaml`, `apps/themuffinman/src/test/java/com/themuffinman/app/docs/AgentOperatingModelValidationTest.java`, `docs/change-completion-checklist.md`, `docs/documentation-sync-policy.md`, `docs/domain-technical.md`, `docs/agent-improvement-backlog.md`, `docs/generated/source-of-truth-audit.json`, generated local-tooling reports refreshed by `make audit-local-tooling-incremental`
- Validation evidence: `ruby scripts/generate-agent-operating-model.rb` passed; `./mvnw test -Dtest=AgentOperatingModelValidationTest` passed; `ruby scripts/todo-audit.rb` passed; `make audit-documentation` passed; `make audit-agent-safety` passed; `make audit-local-tooling-incremental` exited 0; `ruby scripts/generate-source-of-truth-audit.rb` passed; `ruby scripts/audits/audit-generated-artifact-freshness.rb` passed with `stale: 0`; `cd apps/themuffinman && ./mvnw test` passed with 273 tests.
- Backlog update: removed `AGENT-DOCS-AS-CONTRACT-SLICES` from `docs/agent-improvement-backlog.md`.
- Residual risk: the first docs-as-contract set covers selected high-value sections; future protected documentation sections must be added to the slice catalog when they become contract-like.

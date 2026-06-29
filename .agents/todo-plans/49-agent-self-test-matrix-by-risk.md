# AGENT-SELF-TEST-MATRIX-BY-RISK Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `medium`
Master order: 49 of 82

## Backlog Item

Extend the operating model with explicit self-test tiers: syntax-only, targeted unit, domain scenario, contract/type-check, generated-artifact validation, and full validation.

Source notes:
  Purpose: make validation cheaper for low-risk changes while still fail-hard for high-risk ones.

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
- Changed files: `docs/agent-operating-model/sections/policies.yaml`, `docs/agent-operating-model.yaml`, `docs/agent-operating-model.schema.json`, `apps/themuffinman/src/test/java/com/themuffinman/app/docs/AgentOperatingModelValidationTest.java`, `docs/agent-operating-model.md`, `docs/change-completion-checklist.md`, `docs/domain-technical.md`, `docs/agent-improvement-backlog.md`, `docs/generated/source-of-truth-audit.json`
- Validation evidence: `ruby scripts/generate-agent-operating-model.rb` passed; `ruby scripts/todo-audit.rb` passed; `make audit-documentation` passed; `make audit-agent-safety` passed; `make audit-local-tooling-incremental` passed; `ruby scripts/generate-source-of-truth-audit.rb` passed; `ruby scripts/audits/audit-generated-artifact-freshness.rb` passed with `stale: 0`; `cd apps/themuffinman && ./mvnw test` passed with 273 tests.
- Backlog update: removed `AGENT-SELF-TEST-MATRIX-BY-RISK` from `docs/agent-improvement-backlog.md`.
- Residual risk: self-test tiers are now defined and structurally validated; hard-fail evidence quality enforcement remains covered by later backlog items.

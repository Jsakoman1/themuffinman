# AGENT-TEST-EVIDENCE-QUALITY-GATES Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `medium`
Master order: 50 of 82

## Backlog Item

Add validation evidence quality checks that reject vague closeouts such as "tests not run" without a reason, or "build passed" without command and scope.

Source notes:
  Purpose: preserve reliable implementation process across sessions.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make audit-agent-safety`
- [x] `cd apps/themuffinman/frontend && npm run type-check`
- [x] `cd apps/themuffinman/frontend && npm run build`

## Completion Evidence

- Status: complete
- Changed files: `scripts/audits/audit-validation-evidence-quality.rb`, `Makefile`, `docs/agent-operating-model/sections/policies.yaml`, `docs/agent-operating-model.yaml`, `docs/agent-operating-model.schema.json`, `apps/themuffinman/src/test/java/com/themuffinman/app/docs/AgentOperatingModelValidationTest.java`, `docs/change-completion-checklist.md`, `docs/documentation-sync-policy.md`, `docs/domain-technical.md`, `docs/agent-improvement-backlog.md`, `docs/generated/source-of-truth-audit.json`, `docs/generated/local-tooling/make-target-index.json`, `docs/generated/local-tooling/make-target-index-summary.md`
- Validation evidence: `ruby -c scripts/audits/audit-validation-evidence-quality.rb` passed; `ruby scripts/generate-agent-operating-model.rb` passed; `make audit-validation-evidence-quality` passed; `./mvnw test -Dtest=AgentOperatingModelValidationTest` passed; `make audit-agent-safety` passed; `make audit-documentation` passed; `cd apps/themuffinman/frontend && npm run type-check` passed; `cd apps/themuffinman/frontend && npm run build` passed; `ruby scripts/audits/audit-generated-artifact-freshness.rb` passed with `stale: 0`; `ruby scripts/audits/audit-make-target-index.rb` passed; `ruby scripts/todo-audit.rb` passed; `cd apps/themuffinman && ./mvnw test` passed with 273 tests.
- Backlog update: removed `AGENT-TEST-EVIDENCE-QUALITY-GATES` from `docs/agent-improvement-backlog.md`.
- Residual risk: the audit currently checks concrete validation evidence records under `.agents/validation-evidence/`; later feature-closeout hard-enforcement work still owns manifest-level fail-hard integration.

# AGENT-DOCS-OWNERSHIP-MATRIX Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `medium`
Master order: 41 of 82

## Backlog Item

Add a machine-readable mapping from domains and change categories to required living docs, generated artifacts, and validation tests.

Source notes:
  Purpose: preserve documentation logic without making Codex repeatedly infer which docs are in scope.

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
- [x] `cd apps/themuffinman/frontend && npm run type-check`
- [x] `cd apps/themuffinman/frontend && npm run build`

## Completion Evidence

- Status: complete
- Changed files: `docs/agent-operating-model/sections/documentation_ownership.yaml`, `docs/agent-operating-model.yaml`, `docs/agent-operating-model.schema.json`, `scripts/generate-agent-operating-model.rb`, `apps/themuffinman/src/test/java/com/themuffinman/app/docs/AgentOperatingModelValidationTest.java`, `docs/agent-operating-model.md`, `docs/domain-technical.md`, `docs/change-completion-checklist.md`, `docs/documentation-sync-policy.md`, `docs/agent-improvement-backlog.md`, `docs/generated/source-of-truth-audit.json`
- Validation evidence: `ruby scripts/generate-agent-operating-model.rb` passed; `./mvnw test -Dtest=AgentOperatingModelValidationTest` passed; `ruby scripts/todo-audit.rb` passed; `make audit-documentation` passed; `make audit-agent-safety` passed; `ruby scripts/generate-source-of-truth-audit.rb` passed; `ruby scripts/audits/audit-generated-artifact-freshness.rb` passed with stale=0; `make audit-local-tooling-incremental` passed; `cd apps/themuffinman/frontend && npm run type-check` passed; `cd apps/themuffinman/frontend && npm run build` passed.
- Backlog update: removed `AGENT-DOCS-OWNERSHIP-MATRIX` from `docs/agent-improvement-backlog.md`
- Residual risk: ownership matrix is validated for path existence and backend-domain coverage; report-only local tooling still reports broader docs-to-code and docs coverage gaps for future backlog items.

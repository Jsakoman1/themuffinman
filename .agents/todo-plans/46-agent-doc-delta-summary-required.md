# AGENT-DOC-DELTA-SUMMARY-REQUIRED Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `medium`
Master order: 46 of 82

## Backlog Item

Add a closeout rule that every logic change records a short doc delta summary: what behavior changed, which docs were updated, and what intentionally did not change.

Source notes:
  Purpose: reduce drift between code, docs, and final Codex explanations.

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
- Changed files: `docs/agent-operating-model/sections/policies.yaml`, `docs/agent-operating-model.yaml`, `docs/agent-operating-model.schema.json`, `docs/feature-completion-manifest.schema.json`, `.agents/templates/feature-completion-manifest.template.yaml`, `.agents/templates/feature-implementation-plan.template.md`, `apps/themuffinman/src/test/java/com/themuffinman/app/docs/AgentOperatingModelValidationTest.java`, `docs/agent-operating-model.md`, `docs/change-completion-checklist.md`, `docs/documentation-sync-policy.md`, `docs/domain-technical.md`, `docs/agent-improvement-backlog.md`, `docs/generated/source-of-truth-audit.json`
- Validation evidence: `ruby scripts/generate-agent-operating-model.rb` passed; `ruby scripts/todo-audit.rb` passed; `make audit-documentation` passed; `make audit-agent-safety` passed; `make audit-local-tooling-incremental` passed; `ruby scripts/generate-source-of-truth-audit.rb` passed; `ruby scripts/audits/audit-generated-artifact-freshness.rb` passed with `stale: 0`; `cd apps/themuffinman/frontend && npm run type-check` passed; `cd apps/themuffinman/frontend && npm run build` passed.
- Backlog update: removed `AGENT-DOC-DELTA-SUMMARY-REQUIRED` from `docs/agent-improvement-backlog.md`.
- Residual risk: this adds the rule, schema surface, and templates; hard-fail closeout enforcement remains assigned to later enforcement backlog items.

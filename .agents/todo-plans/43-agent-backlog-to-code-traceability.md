# AGENT-BACKLOG-TO-CODE-TRACEABILITY Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `high`
Master order: 43 of 82

## Backlog Item

Add audits that link persistent backlog IDs to inline TODO/FIXME references, plans, feature manifests, docs, and code surfaces.

Source notes:
  Purpose: prevent forgotten backlog work and make future cleanup sessions cheaper.

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
- Changed files: `scripts/todo-audit.rb`, `docs/agent-operating-model.md`, `docs/domain-technical.md`, `docs/change-completion-checklist.md`, `docs/documentation-sync-policy.md`, `docs/agent-improvement-backlog.md`
- Validation evidence: `ruby scripts/todo-audit.rb` passed with backlog traceability output; `make audit-documentation` passed; `make audit-agent-safety` passed; `make audit-local-tooling-incremental` passed; `cd apps/themuffinman/frontend && npm run type-check` passed; `cd apps/themuffinman/frontend && npm run build` passed.
- Backlog update: removed `AGENT-BACKLOG-TO-CODE-TRACEABILITY` from `docs/agent-improvement-backlog.md`
- Residual risk: traceability now requires at least one non-backlog link per open backlog ID; richer semantic code-surface linkage beyond literal ID mentions remains a future possible enhancement, not required for this backlog item.

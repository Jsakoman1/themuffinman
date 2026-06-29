# AGENT-WORKFLOW-AWARE-FRONTEND-HELPERS Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `medium`
Master order: 33 of 82

## Backlog Item

Tighten workflow-aware frontend helper coverage as a separate control-system slice.

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
- [x] `cd apps/themuffinman/frontend && npm run type-check`
- [x] `cd apps/themuffinman/frontend && npm run build`

## Completion Evidence

- Status: complete
- Changed files: added frontend workflow guard helpers for generated intent, endpoint, and safety-flag ids; wired simulation reference issue detection into the admin-agent page; tightened the admin-agent UI scenario validator to reject fixture intent, endpoint, or safety-flag drift; updated docs/backlog.
- Validation evidence: `npm run validate:admin-agent-ui` passed; `npm run type-check` passed; `npm run build` passed; `make audit-agent-safety` passed; `cd apps/themuffinman && ./mvnw test` passed with 269 tests; `ruby scripts/todo-audit.rb` passed with 27 open backlog items remaining.
- Backlog update: removed `AGENT-WORKFLOW-AWARE-FRONTEND-HELPERS`.
- Residual risk: helper enforcement currently covers admin-agent simulation references and fixture safety flags; broader unresolved-input id semantics remain limited by the backend payload using display strings rather than stable unresolved-input ids.

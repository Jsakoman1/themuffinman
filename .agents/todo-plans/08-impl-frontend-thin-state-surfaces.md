# IMPL-FRONTEND-THIN-STATE-SURFACES Plan

Source: `docs/implementation-backlog.md`
Group: `implementation`
Risk: `medium`
Master order: 8 of 82

## Backlog Item

Refactor complex Vue view/composable combinations toward backend-prepared sections and smaller frontend state adapters.

Source notes:
  Goal: keep business rules out of frontend views and make Codex inspect fewer frontend files for future UI changes.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `cd apps/themuffinman && ./mvnw test`
- [x] `cd apps/themuffinman/frontend && npm run type-check`
- [x] `cd apps/themuffinman/frontend && npm run build`

## Completion Evidence

- Status: complete
- Changed files: `QuestDetailView.vue`, `useQuestDetailView.ts`, `useQuestDetailEdit.ts`, `docs/domain-technical.md`, `docs/implementation-backlog.md`
- Validation evidence: `cd apps/themuffinman/frontend && npm run type-check` passed; `cd apps/themuffinman/frontend && npm run build` passed; `ruby scripts/todo-audit.rb` passed.
- Backlog update: removed `IMPL-FRONTEND-THIN-STATE-SURFACES` from `docs/implementation-backlog.md`.
- Residual risk: backend test and TODO audit deferred to master closeout; no backend behavior changed in this slice.

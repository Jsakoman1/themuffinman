# IMPL-FRONTEND-APP-SHELL-CONSOLIDATION Plan

Source: `docs/implementation-backlog.md`
Group: `implementation`
Risk: `medium`
Master order: 18 of 82

## Backlog Item

Finish standardizing authenticated pages around one app shell, shared detail surfaces, shared dialogs, and shared action panels.

Source notes:
  Goal: reduce one-off UI patterns and make future frontend implementation mostly data wiring.

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
- Changed files: added `frontend/src/components/ui/UiAppShellPage.vue`; kept `UiDashboardPage.vue` as a compatibility wrapper; migrated work, social, business, things, rides, chat, and admin route pages to the shared shell; updated `docs/business-logic.md`, `docs/domain-technical.md`, and `docs/implementation-backlog.md`.
- Validation evidence: `cd apps/themuffinman/frontend && npm run type-check` passed; `cd apps/themuffinman/frontend && npm run build` passed; `cd apps/themuffinman && ./mvnw test` passed with 265 tests; `make audit-documentation` passed; `ruby scripts/todo-audit.rb` passed.
- Backlog update: removed `IMPL-FRONTEND-APP-SHELL-CONSOLIDATION` from `docs/implementation-backlog.md`.
- Residual risk: detail/dialog/action panel internals are standardized through existing shared components, but future visual cleanup can still refine admin-specific header composition without re-opening the shell backlog item.

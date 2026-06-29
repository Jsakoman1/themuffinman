# IMPL-BACKEND-PREPARED-UI-SECTIONS Plan

Source: `docs/implementation-backlog.md`
Group: `implementation`
Risk: `medium`
Master order: 16 of 82

## Backlog Item

Expand backend-prepared view DTO sections for complex screens so frontend components render state instead of deriving product logic.

Source notes:
  Goal: keep frontend thin and lower future token cost for UI changes.

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
- Changed files: `DashboardNavigationItemDTO.java`, `DashboardNavigationSectionDTO.java`, `DashboardSectionsDTO.java`, `DashboardSectionsFactory.java`, `DashboardServiceTest.java`, frontend dashboard selectors/state, generated frontend contract, `docs/business-logic.md`, `docs/domain-technical.md`, `docs/implementation-backlog.md`
- Validation evidence: `cd apps/themuffinman && ./mvnw test -Dtest=DashboardServiceTest` passed with 5 tests; full `cd apps/themuffinman && ./mvnw test` passed with 261 tests; `cd apps/themuffinman/frontend && npm run type-check` passed; `cd apps/themuffinman/frontend && npm run build` passed; `make audit-documentation` passed; `ruby scripts/todo-audit.rb` passed.
- Backlog update: removed `IMPL-BACKEND-PREPARED-UI-SECTIONS` from `docs/implementation-backlog.md`.
- Residual risk: this closes the dashboard navigation/heading slice of backend-prepared sections; broader future dashboard panel copy can continue through the same DTO section pattern.

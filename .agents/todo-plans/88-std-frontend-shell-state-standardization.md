# STD-FRONTEND-SHELL-STATE-STANDARDIZATION Plan

Source: `docs/implementation-backlog.md`
Group: `implementation`
Risk: `medium`
Master order: 88 of 89

## Backlog Item

Standardize frontend route shells, shared state helpers, and stale UI primitives so pages become thin adapters over backend-prepared DTOs.

## Source Findings

- [`.agents/system-standardization-audit-findings.md`](/Users/jsakoman/Desktop/themuffinman/.agents/system-standardization-audit-findings.md)
- Route surfaces: `router.ts`, `QuestsPage.vue`, `QuestDetailView.vue`, `ApplicationDetailView.vue`, `AdminOverviewPage.vue`, `AdminApplicationsPage.vue`, `AdminQuestsPage.vue`
- State surfaces: `useQuestDashboard*`, `useQuestDetailView`, `useAdminApplicationsPage`, `useAppUsersPage`, `composables/dashboard/*`, `composables/quest-detail/*`
- Stale surfaces: legacy frontend UI helpers and detached shared assets flagged by the audit

## Implementation Plan

- [x] Inventory route-level and page-level shell patterns.
- [x] Standardize the shell pattern for workmarket and shared module pages.
- [x] Collapse duplicated state/mutation/feedback helpers into shared utilities where the audit shows repeated patterns.
- [x] Remove or reconnect stale frontend assets instead of leaving them as ambiguous leftovers.
- [x] Keep the UI thin and aligned with backend-prepared DTOs.

## Expected Validation

- `npm --prefix apps/themuffinman/frontend run type-check`
- `npm --prefix apps/themuffinman/frontend run build`
- `ruby scripts/todo-audit.rb`

## Completion Evidence

- Status: complete
- Changed files: `apps/themuffinman/frontend/src/modules/workmarket/views/QuestDetailView.vue`, `apps/themuffinman/frontend/src/modules/workmarket/views/ApplicationDetailView.vue`, `docs/domain-technical.md`
- Validation evidence: `npm run type-check` passed; `npm run build` passed; `ruby scripts/todo-audit.rb` passed
- Doc delta summary: detail views now use the shared authenticated shell and domain technical notes no longer reference deleted shell wrappers
- Backlog update: removed from open backlog.
- Residual risk: none known

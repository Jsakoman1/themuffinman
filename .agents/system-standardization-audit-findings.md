# System Standardization Audit Findings

Purpose: turn the current repo inventory into a stable standardisation backlog. This file groups concrete drift by surface so the follow-up TODOs can be created without re-scanning the whole repository.

## Backend Surface Standardisation

- `TODO-BACKEND-SERVICE-BOUNDARIES-001`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestStateTransitionService.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestValidationService.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardService.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestNewsService.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationService.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestExecutionPrimitiveService.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleService.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleReadService.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/social/controller/CircleController.java`
  - Why: architecture drift audit flags these as oversized or mixed-responsibility surfaces, which makes backend standardisation the highest-leverage cleanup.

- `TODO-BACKEND-USECASE-NARROWING-002`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/*UseCase.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CreateQuestUseCase.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/UpdateQuestUseCase.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApplyForQuestUseCase.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApproveApplicationUseCase.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DeclineApplicationUseCase.java`
  - Why: use cases are already a distinct pattern, but they need tighter consistency around single-entry orchestration, naming, and handoff to policy/read services.

- `TODO-BACKEND-READMODEL-STANDARDISATION-003`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestReadService.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestViewAssembler.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationReadService.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationViewAssembler.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestPresentationAssembler.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationPresentationAssembler.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/UserProfileViewService.java`
  - Why: read surfaces are spread across multiple assembler/service combinations; standardising them reduces duplication and makes DTO evolution predictable.

## API Contract Standardisation

- `TODO-API-CONTRACT-COVERAGE-004`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/ApplicationAllowedActionDTO.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/BulkCircleMembershipActionDTO.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/CircleRelationStatusDTO.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/DashboardNotificationDestinationTypeDTO.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/QuestAllowedActionDTO.java`
  - Why: API contract drift audit reports DTOs that are missing generated contracts, which is a direct signal that the frontend/backend contract layer is not fully standardised.

- `TODO-API-DTO-CLEANUP-005`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/AdminAgentPlaygroundResponseDTO.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/AdminQuestApplicationUpdateRequestDTO.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/CircleRequestResponseDTO.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/DashboardNotificationItemDTO.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/QuestResponseDTO.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/QuestApplicationResponseDTO.java`
  - Why: audit output shows many zero-usage or unused fields. Standardising DTO shape and field usefulness will reduce contract noise and keep frontend state thinner.

## Frontend Surface Standardisation

- `TODO-FRONTEND-ROUTE-SHELL-006`
  - `apps/themuffinman/frontend/src/router.ts`
  - `apps/themuffinman/frontend/src/modules/workmarket/pages/QuestsPage.vue`
  - `apps/themuffinman/frontend/src/modules/workmarket/views/QuestDetailView.vue`
  - `apps/themuffinman/frontend/src/modules/workmarket/views/ApplicationDetailView.vue`
  - `apps/themuffinman/frontend/src/modules/workmarket/pages/AdminOverviewPage.vue`
  - `apps/themuffinman/frontend/src/modules/workmarket/pages/AdminApplicationsPage.vue`
  - `apps/themuffinman/frontend/src/modules/workmarket/pages/AdminQuestsPage.vue`
  - Why: routes already exist, but the page shell pattern is inconsistent and some views carry a lot of orchestration directly inside the surface component.

- `TODO-FRONTEND-DASHBOARD-STATE-007`
  - `apps/themuffinman/frontend/src/modules/workmarket/composables/useQuestDashboard.ts`
  - `apps/themuffinman/frontend/src/modules/workmarket/composables/useQuestDashboardState.ts`
  - `apps/themuffinman/frontend/src/modules/workmarket/composables/useQuestDashboardActions.ts`
  - `apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/*`
  - Why: the dashboard is split across many small composables and mutation helpers; that is workable, but it needs a clearer canonical shape before further features land.

- `TODO-FRONTEND-DUPLICATE-STATE-008`
  - `apps/themuffinman/frontend/src/modules/workmarket/composables/useQuestDetailView.ts`
  - `apps/themuffinman/frontend/src/modules/workmarket/composables/quest-detail/*`
  - `apps/themuffinman/frontend/src/modules/workmarket/composables/useAdminApplicationsPage.ts`
  - `apps/themuffinman/frontend/src/modules/workmarket/composables/useAppUsersPage.ts`
  - Why: frontend state duplication audit shows repeated patterns for feedback, dialogs, and mutation runners. These should be standard helpers, not locally reimplemented variations.

- `TODO-FRONTEND-STALE-ASSET-009`
  - `apps/themuffinman/frontend/src/components/ui/UiAmountField.vue`
  - `apps/themuffinman/frontend/src/components/ui/UiDashboardPage.vue`
  - `apps/themuffinman/frontend/src/components/ui/UiLaunchCard.vue`
  - `apps/themuffinman/frontend/src/components/ui/UiMetricPills.vue`
  - `apps/themuffinman/frontend/src/components/ui/UiReadonlyField.vue`
  - `apps/themuffinman/frontend/src/components/ui/UiSplitLayout.vue`
  - `apps/themuffinman/frontend/src/modules/workmarket/components/shared/QuestEditFields.vue`
  - `apps/themuffinman/frontend/src/shared/questNews.ts`
  - `apps/themuffinman/frontend/src/modules/workmarket/api/adminAgentContractGate.ts`
  - Why: stale surface audit identifies unused or detached assets that should be either removed, reconnected, or explicitly retained as shared primitives.

## Docs And Vocabulary Standardisation

- `TODO-DOCS-TERMINOLOGY-010`
  - `docs/business-logic.md`
  - `docs/domain-technical.md`
  - `docs/workflow-state-machines.yaml`
  - `docs/agent-operating-model.yaml`
  - Why: naming consistency audit shows repeated vocabulary drift around `applicant` vs `worker` vs `assignee`, `quest detail` vs `record`, and circle connection wording.

- `TODO-DOCS-SURFACE-BOUNDARIES-011`
  - `docs/agent-operating-model.md`
  - `docs/domain-technical.md`
  - `docs/codex-fast-path.md`
  - `docs/documentation-sync-policy.md`
  - Why: oversized doc sections and repeated workflow rules suggest the docs need a clearer split between behavior, technical source of truth, and agent-operating rules.

## Tests And Coverage Standardisation

- `TODO-TEST-SURFACE-012`
  - `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/*`
  - `apps/themuffinman/src/test/java/com/themuffinman/app/social/service/*`
  - `apps/themuffinman/src/test/java/com/themuffinman/app/location/service/*`
  - Why: the test surface inventory shows broad coverage, but some services still lack scenario-level coverage and some read paths rely on indirect coverage only.

- `TODO-VALIDATION-GATE-013`
  - `docs/generated/local-tooling/read-surface-inventory-summary.md`
  - `docs/generated/local-tooling/test-surface-inventory-summary.md`
  - `docs/generated/local-tooling/frontend-route-surface-inventory.json`
  - Why: the inventory reports are already useful, but they should become part of a tighter standardisation loop so drift is caught earlier and in the same format every time.

## Ordering Recommendation

1. Fix contract coverage and backend service boundaries first.
2. Normalize frontend shells and shared state helpers second.
3. Clean vocabulary and docs after the core surfaces are stable.
4. Tighten tests and validation gates last, once the shapes stop moving.

# Frontend State Logic Duplication Audit

- Generated at: `2026-06-29T12:47:10Z`
- Active frontend files scanned: `34`

## `mutation_runner_overlap`

- `createFeedbackMutationRunner` -> `apps/themuffinman/frontend/src/composables/createFeedbackMutationRunner.ts`, `apps/themuffinman/frontend/src/modules/social/composables/useUserProfileView.ts`, `apps/themuffinman/frontend/src/modules/social/pages/AdminCirclesPage.vue`, `apps/themuffinman/frontend/src/modules/workmarket/composables/useAdminApplicationsPage.ts`
- `runWithFeedback` -> `apps/themuffinman/frontend/src/composables/createFeedbackMutationRunner.ts`, `apps/themuffinman/frontend/src/modules/social/composables/useUserProfileView.ts`, `apps/themuffinman/frontend/src/modules/social/pages/AdminCirclesPage.vue`, `apps/themuffinman/frontend/src/modules/workmarket/composables/useAdminApplicationsPage.ts`

## `workflow_action_overlap`

- `approveApplication` -> `apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/dashboardFacades.ts`, `apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/useQuestDialogUiActions.ts`, `apps/themuffinman/frontend/src/modules/workmarket/composables/useAdminApplicationsPage.ts`, `apps/themuffinman/frontend/src/modules/workmarket/pages/AdminApplicationsPage.vue`
- `declineApplication` -> `apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/dashboardFacades.ts`, `apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/useQuestDialogUiActions.ts`, `apps/themuffinman/frontend/src/modules/workmarket/composables/useAdminApplicationsPage.ts`, `apps/themuffinman/frontend/src/modules/workmarket/pages/AdminApplicationsPage.vue`
- `applyForQuest` -> `apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/dashboardFacades.ts`, `apps/themuffinman/frontend/src/modules/workmarket/views/QuestDetailView.vue`
- `blockUser` -> `apps/themuffinman/frontend/src/modules/social/composables/useCirclesView.ts`, `apps/themuffinman/frontend/src/modules/social/views/CirclesView.vue`
- `confirmQuestTermChange` -> `apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/dashboardFacades.ts`, `apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/useQuestDialogUiActions.ts`
- `createCircle` -> `apps/themuffinman/frontend/src/modules/social/composables/useCirclesView.ts`, `apps/themuffinman/frontend/src/modules/social/views/CirclesView.vue`
- `deleteQuest` -> `apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/dashboardFacades.ts`, `apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/useQuestDialogUiActions.ts`
- `rejectQuestTermChange` -> `apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/dashboardFacades.ts`, `apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/useQuestDialogUiActions.ts`
- `unblockUser` -> `apps/themuffinman/frontend/src/modules/social/composables/useCirclesView.ts`, `apps/themuffinman/frontend/src/modules/social/views/CirclesView.vue`

## `dialog_state_overlap`

- `closeApplicationDialog` -> `apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/dashboardFacades.ts`, `apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/useApplicationDialogUiActions.ts`
- `closeQuestDialog` -> `apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/dashboardFacades.ts`, `apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/useQuestDialogUiActions.ts`
- `closeUserProfileDialog` -> `apps/themuffinman/frontend/src/modules/social/views/CirclesView.vue`, `apps/themuffinman/frontend/src/modules/workmarket/pages/QuestsPage.vue`
- `openCreateJobDialog` -> `apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/dashboardFacades.ts`, `apps/themuffinman/frontend/src/modules/workmarket/pages/QuestsPage.vue`
- `openUserProfileDialog` -> `apps/themuffinman/frontend/src/modules/social/views/CirclesView.vue`, `apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/dashboardFacades.ts`

## `feedback_error_overlap`

- `getApiErrorMessage` -> `apps/themuffinman/frontend/src/composables/createFeedbackMutationRunner.ts`, `apps/themuffinman/frontend/src/modules/business/views/BusinessHubView.vue`, `apps/themuffinman/frontend/src/modules/chat/views/ChatWorkspaceView.vue`, `apps/themuffinman/frontend/src/modules/rides/views/RideSharingView.vue`, `apps/themuffinman/frontend/src/modules/social/composables/useUserProfileView.ts`, `apps/themuffinman/frontend/src/modules/social/pages/AdminCirclesPage.vue`, `apps/themuffinman/frontend/src/modules/things/views/ThingSharingView.vue`, `apps/themuffinman/frontend/src/modules/workmarket/composables/useAdminApplicationsPage.ts`, `apps/themuffinman/frontend/src/modules/workmarket/pages/AdminAgentPage.vue`, `apps/themuffinman/frontend/src/modules/workmarket/pages/AdminApplicationsPage.vue`, `apps/themuffinman/frontend/src/modules/workmarket/pages/AdminOverviewPage.vue`, `apps/themuffinman/frontend/src/modules/workmarket/views/ApplicationDetailView.vue`
- `error_assignment` -> `apps/themuffinman/frontend/src/modules/identity/views/LoginView.vue`, `apps/themuffinman/frontend/src/modules/identity/views/RegisterView.vue`, `apps/themuffinman/frontend/src/modules/social/composables/useCirclesView.ts`, `apps/themuffinman/frontend/src/modules/social/composables/useUserProfileView.ts`, `apps/themuffinman/frontend/src/modules/social/pages/AdminCirclesPage.vue`, `apps/themuffinman/frontend/src/modules/workmarket/composables/useAdminApplicationsPage.ts`, `apps/themuffinman/frontend/src/modules/workmarket/pages/AdminAgentPage.vue`, `apps/themuffinman/frontend/src/modules/workmarket/pages/AdminOverviewPage.vue`, `apps/themuffinman/frontend/src/modules/workmarket/views/ApplicationDetailView.vue`
- `showFeedback` -> `apps/themuffinman/frontend/src/composables/createFeedbackMutationRunner.ts`, `apps/themuffinman/frontend/src/modules/social/composables/useUserProfileView.ts`, `apps/themuffinman/frontend/src/modules/social/pages/AdminCirclesPage.vue`, `apps/themuffinman/frontend/src/modules/workmarket/composables/useAdminApplicationsPage.ts`

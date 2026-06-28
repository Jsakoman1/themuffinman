# Async Mutation Flow Audit

- Generated At: `2026-06-28T20:30:23Z`
## `files`

- `{:file: "apps/themuffinman/frontend/src/components/app/AppChatTray.vue", :uses_feedback_runner: false, :uses_run_with_feedback: false, :has_after_success: false, :refresh_calls: []}`
- `{:file: "apps/themuffinman/frontend/src/components/app/topbar/useTopbarNotifications.ts", :uses_feedback_runner: false, :uses_run_with_feedback: false, :has_after_success: false, :refresh_calls: ["loadNotifications"]}`
- `{:file: "apps/themuffinman/frontend/src/composables/createFeedbackMutationRunner.ts", :uses_feedback_runner: true, :uses_run_with_feedback: true, :has_after_success: true, :refresh_calls: []}`
- `{:file: "apps/themuffinman/frontend/src/composables/usePaginatedResults.ts", :uses_feedback_runner: false, :uses_run_with_feedback: false, :has_after_success: false, :refresh_calls: ["loadPage"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/chat/composables/useAppChat.ts", :uses_feedback_runner: false, :uses_run_with_feedback: false, :has_after_success: false, :refresh_calls: []}`
- `{:file: "apps/themuffinman/frontend/src/modules/social/components/profile/UserProfileDialog.vue", :uses_feedback_runner: false, :uses_run_with_feedback: false, :has_after_success: false, :refresh_calls: ["fetchProfile", "loadErrorMessage"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/social/components/profile/UserSettingsDialog.vue", :uses_feedback_runner: false, :uses_run_with_feedback: false, :has_after_success: false, :refresh_calls: ["fetchProfile", "loadErrorMessage"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/social/composables/circles/useCirclesDataLoader.ts", :uses_feedback_runner: false, :uses_run_with_feedback: false, :has_after_success: false, :refresh_calls: ["loadBlockedPage", "loadCircles", "loadConnectionsPage", "loadInboxPage", "loadNearbyPage", "loadOverview", "loadSuggestions"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/social/composables/circles/useCirclesMutationActions.ts", :uses_feedback_runner: true, :uses_run_with_feedback: true, :has_after_success: true, :refresh_calls: []}`
- `{:file: "apps/themuffinman/frontend/src/modules/social/composables/circles/useCirclesPagination.ts", :uses_feedback_runner: false, :uses_run_with_feedback: false, :has_after_success: false, :refresh_calls: ["loadConnectionsPage", "loadInboxPage"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/social/composables/useCirclesView.ts", :uses_feedback_runner: false, :uses_run_with_feedback: false, :has_after_success: false, :refresh_calls: ["loadBlockedPage", "loadConnectionsPage", "loadDirectoryResults", "loadDiscoverResults", "loadInboxPage", "loadNearbyPage", "loadSuggestions"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/social/composables/useUserProfileView.ts", :uses_feedback_runner: true, :uses_run_with_feedback: true, :has_after_success: true, :refresh_calls: ["fetchProfile", "loadErrorMessage", "loadProfileView"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/social/pages/AdminCirclesPage.vue", :uses_feedback_runner: true, :uses_run_with_feedback: true, :has_after_success: true, :refresh_calls: ["loadOverview"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/workmarket/components/admin/AdminUserDetailDialog.vue", :uses_feedback_runner: false, :uses_run_with_feedback: false, :has_after_success: false, :refresh_calls: ["loadDetail"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/workmarket/components/dashboard/DashboardAdmin.vue", :uses_feedback_runner: false, :uses_run_with_feedback: false, :has_after_success: false, :refresh_calls: ["loadQuests"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/workmarket/composables/app-users/useAppUsersDataActions.ts", :uses_feedback_runner: false, :uses_run_with_feedback: false, :has_after_success: false, :refresh_calls: ["fetchAppUsers"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/workmarket/composables/app-users/useAppUsersMutationActions.ts", :uses_feedback_runner: true, :uses_run_with_feedback: true, :has_after_success: true, :refresh_calls: ["fetchAppUsers"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/createDashboardMutationRunner.ts", :uses_feedback_runner: false, :uses_run_with_feedback: false, :has_after_success: true, :refresh_calls: []}`
- `{:file: "apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/questMutationTypes.ts", :uses_feedback_runner: false, :uses_run_with_feedback: false, :has_after_success: false, :refresh_calls: ["loadApplicationDetail", "loadApplicationsForQuest", "loadQuestDetail"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/useDashboardAdminQuestBrowser.ts", :uses_feedback_runner: false, :uses_run_with_feedback: false, :has_after_success: false, :refresh_calls: ["loadQuests"]}`


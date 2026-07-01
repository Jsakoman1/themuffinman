# Async Mutation Flow Audit

- Generated At: `2026-07-01T14:49:44Z`
## `files`

- `{:file: "apps/themuffinman/frontend/src/composables/createFeedbackMutationRunner.ts", :uses_feedback_runner: true, :uses_run_with_feedback: true, :has_after_success: true, :refresh_calls: []}`
- `{:file: "apps/themuffinman/frontend/src/composables/usePaginatedResults.ts", :uses_feedback_runner: false, :uses_run_with_feedback: false, :has_after_success: false, :refresh_calls: ["loadPage"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/chat/composables/useAppChat.ts", :uses_feedback_runner: false, :uses_run_with_feedback: false, :has_after_success: false, :refresh_calls: []}`
- `{:file: "apps/themuffinman/frontend/src/modules/social/composables/circles/useCirclesDataLoader.ts", :uses_feedback_runner: false, :uses_run_with_feedback: false, :has_after_success: false, :refresh_calls: ["loadBlockedPage", "loadCircles", "loadConnectionsPage", "loadInboxPage", "loadNearbyPage", "loadOverview", "loadSuggestions"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/social/composables/circles/useCirclesMutationActions.ts", :uses_feedback_runner: true, :uses_run_with_feedback: true, :has_after_success: true, :refresh_calls: []}`
- `{:file: "apps/themuffinman/frontend/src/modules/social/composables/circles/useCirclesPagination.ts", :uses_feedback_runner: false, :uses_run_with_feedback: false, :has_after_success: false, :refresh_calls: ["loadConnectionsPage", "loadInboxPage"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/social/composables/useCirclesView.ts", :uses_feedback_runner: false, :uses_run_with_feedback: false, :has_after_success: false, :refresh_calls: ["loadBlockedPage", "loadConnectionsPage", "loadDirectoryResults", "loadDiscoverResults", "loadInboxPage", "loadNearbyPage", "loadSuggestions"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/social/composables/useUserProfileView.ts", :uses_feedback_runner: true, :uses_run_with_feedback: true, :has_after_success: true, :refresh_calls: ["fetchProfile", "loadErrorMessage", "loadProfileView"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/vision/views/VisionApplicationDetailView.vue", :uses_feedback_runner: false, :uses_run_with_feedback: false, :has_after_success: false, :refresh_calls: ["loadApplication"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/vision/views/VisionChatWorkspaceView.vue", :uses_feedback_runner: false, :uses_run_with_feedback: false, :has_after_success: false, :refresh_calls: ["loadWorkspace"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/vision/views/VisionUserProfileView.vue", :uses_feedback_runner: false, :uses_run_with_feedback: false, :has_after_success: false, :refresh_calls: ["fetchProfile", "loadErrorMessage"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/vision/views/VisionUserSettingsView.vue", :uses_feedback_runner: false, :uses_run_with_feedback: false, :has_after_success: false, :refresh_calls: ["fetchProfile", "loadErrorMessage", "loadSettingsOptions"]}`


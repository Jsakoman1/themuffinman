# Endpoint Contract Pack circles

- Generated At: `2026-06-29T19:55:04Z`
- Endpoint Family: `circles`
## `endpoints`

- `{:method: "GET", :path: "/circles", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/social/controller/CircleController.java", :dtos: ["CircleOverviewDTO", "AdminCircleOverviewDTO", "ActionResultDTO", "CircleGroupRequestDTO", "CircleGroupResponseDTO"]}`
- `{:method: "DELETE", :path: "/circles/admin/groups/{id}", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/social/controller/CircleController.java", :dtos: ["ActionResultDTO", "CircleGroupResponseDTO"]}`
- `{:method: "GET", :path: "/circles/admin/overview", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/social/controller/CircleController.java", :dtos: ["CircleOverviewDTO", "AdminCircleOverviewDTO", "ActionResultDTO", "CircleGroupRequestDTO", "CircleGroupResponseDTO"]}`
- `{:method: "GET", :path: "/circles/blocked", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/social/controller/CircleController.java", :dtos: ["CircleOverviewDTO", "AdminCircleOverviewDTO", "ActionResultDTO", "CircleGroupRequestDTO", "CircleGroupResponseDTO"]}`
- `{:method: "POST", :path: "/circles/blocks", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/social/controller/CircleController.java", :dtos: ["ActionResultDTO", "CircleGroupRequestDTO", "CircleGroupResponseDTO"]}`
- `{:method: "DELETE", :path: "/circles/blocks/{userId}", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/social/controller/CircleController.java", :dtos: ["ActionResultDTO", "CircleGroupResponseDTO"]}`
- `{:method: "GET", :path: "/circles/candidates", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/social/controller/CircleController.java", :dtos: ["CircleOverviewDTO", "AdminCircleOverviewDTO", "ActionResultDTO", "CircleGroupRequestDTO", "CircleGroupResponseDTO"]}`
- `{:method: "GET", :path: "/circles/connections", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/social/controller/CircleController.java", :dtos: ["CircleOverviewDTO", "AdminCircleOverviewDTO", "ActionResultDTO", "CircleGroupRequestDTO", "CircleGroupResponseDTO"]}`
- `{:method: "PUT", :path: "/circles/connections/circles/bulk", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/social/controller/CircleController.java", :dtos: ["CircleGroupResponseDTO", "CircleGroupRequestDTO", "ActionResultDTO"]}`
- `{:method: "PUT", :path: "/circles/connections/{userId}/circles", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/social/controller/CircleController.java", :dtos: ["CircleGroupResponseDTO", "CircleGroupRequestDTO", "ActionResultDTO"]}`
- `{:method: "GET", :path: "/circles/groups", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/social/controller/CircleController.java", :dtos: ["CircleOverviewDTO", "AdminCircleOverviewDTO", "ActionResultDTO", "CircleGroupRequestDTO", "CircleGroupResponseDTO"]}`
- `{:method: "POST", :path: "/circles/groups", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/social/controller/CircleController.java", :dtos: ["ActionResultDTO", "CircleGroupRequestDTO", "CircleGroupResponseDTO"]}`
- `{:method: "DELETE", :path: "/circles/groups/{id}", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/social/controller/CircleController.java", :dtos: ["ActionResultDTO", "CircleGroupResponseDTO"]}`
- `{:method: "PUT", :path: "/circles/groups/{id}", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/social/controller/CircleController.java", :dtos: ["CircleGroupResponseDTO", "CircleGroupRequestDTO", "ActionResultDTO"]}`
- `{:method: "GET", :path: "/circles/me/overview", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/social/controller/CircleController.java", :dtos: ["CircleOverviewDTO", "AdminCircleOverviewDTO", "ActionResultDTO", "CircleGroupRequestDTO", "CircleGroupResponseDTO"]}`
- `{:method: "GET", :path: "/circles/nearby", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/social/controller/CircleController.java", :dtos: ["CircleOverviewDTO", "AdminCircleOverviewDTO", "ActionResultDTO", "CircleGroupRequestDTO", "CircleGroupResponseDTO"]}`
- `{:method: "GET", :path: "/circles/relations/{userId}", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/social/controller/CircleController.java", :dtos: ["CircleOverviewDTO", "AdminCircleOverviewDTO", "ActionResultDTO", "CircleGroupRequestDTO", "CircleGroupResponseDTO"]}`
- `{:method: "POST", :path: "/circles/requests", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/social/controller/CircleController.java", :dtos: ["ActionResultDTO", "CircleGroupRequestDTO", "CircleGroupResponseDTO"]}`
- `{:method: "GET", :path: "/circles/requests/incoming", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/social/controller/CircleController.java", :dtos: ["CircleOverviewDTO", "AdminCircleOverviewDTO", "ActionResultDTO", "CircleGroupRequestDTO", "CircleGroupResponseDTO"]}`
- `{:method: "GET", :path: "/circles/requests/outgoing", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/social/controller/CircleController.java", :dtos: ["CircleOverviewDTO", "AdminCircleOverviewDTO", "ActionResultDTO", "CircleGroupRequestDTO", "CircleGroupResponseDTO"]}`
- `{:method: "DELETE", :path: "/circles/requests/{id}", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/social/controller/CircleController.java", :dtos: ["ActionResultDTO", "CircleGroupResponseDTO"]}`
- `{:method: "PATCH", :path: "/circles/requests/{id}/accept", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/social/controller/CircleController.java", :dtos: ["ActionResultDTO", "CircleBlockCreateDTO"]}`
- `{:method: "GET", :path: "/circles/search", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/social/controller/CircleController.java", :dtos: ["CircleOverviewDTO", "AdminCircleOverviewDTO", "ActionResultDTO", "CircleGroupRequestDTO", "CircleGroupResponseDTO"]}`

## `dto_files`

- `apps/themuffinman/src/main/java/com/themuffinman/app/common/dto/ActionResultDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/social/dto/AdminCircleGroupResponseDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/social/dto/AdminCircleOverviewDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/social/dto/CircleBlockCreateDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/social/dto/CircleGroupRequestDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/social/dto/CircleGroupResponseDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/social/dto/CircleOverviewDTO.java`

## `frontend_callsites`

- `{:file: "apps/themuffinman/frontend/src/components/app/AppChatTray.vue", :matched_paths: ["circles", "accept"]}`
- `{:file: "apps/themuffinman/frontend/src/components/app/AppNotificationsPanel.vue", :matched_paths: ["groups", "accept"]}`
- `{:file: "apps/themuffinman/frontend/src/components/app/AppTopbar.vue", :matched_paths: ["accept"]}`
- `{:file: "apps/themuffinman/frontend/src/components/app/topbar/createTopbarNavState.ts", :matched_paths: ["/circles", "circles", "connections"]}`
- `{:file: "apps/themuffinman/frontend/src/components/app/topbar/useTopbarNotifications.ts", :matched_paths: ["accept", "search"]}`
- `{:file: "apps/themuffinman/frontend/src/components/app/useAppTopbarState.ts", :matched_paths: ["accept"]}`
- `{:file: "apps/themuffinman/frontend/src/components/editor/RichTextEditor.vue", :matched_paths: ["incoming", "accept"]}`
- `{:file: "apps/themuffinman/frontend/src/components/ui/UiEventPill.vue", :matched_paths: ["incoming", "outgoing"]}`
- `{:file: "apps/themuffinman/frontend/src/components/ui/UiSearchableMultiSelect.vue", :matched_paths: ["search"]}`
- `{:file: "apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts", :matched_paths: ["circles", "overview", "blocked", "candidates", "connections", "bulk", "nearby", "requests"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/chat/composables/useAppChat.ts", :matched_paths: ["circles", "search"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/chat/views/ChatWorkspaceView.vue", :matched_paths: ["accept", "search"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/moduleRegistry.ts", :matched_paths: ["circles"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/social/components/circles/CirclesConnectionsPanel.vue", :matched_paths: ["circles", "overview", "connections", "bulk"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/social/components/circles/CirclesDirectoryPanel.vue", :matched_paths: ["circles"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/social/components/circles/CirclesInboxPanel.vue", :matched_paths: ["circles", "requests", "incoming", "outgoing", "accept"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/social/components/profile/UserProfileDialog.vue", :matched_paths: ["overview", "accept"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/social/components/profile/UserSettingsDialog.vue", :matched_paths: ["circles", "groups", "search"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/social/composables/circles/createCirclesPageState.ts", :matched_paths: ["circles", "overview", "blocked", "connections", "nearby", "incoming", "outgoing", "search"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/social/composables/circles/createCirclesViewState.ts", :matched_paths: ["circles", "overview", "blocked", "connections", "nearby", "incoming", "outgoing", "search"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/social/composables/circles/useCirclesDataLoader.ts", :matched_paths: ["circles", "overview", "blocked", "connections", "nearby", "requests", "incoming", "outgoing"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/social/composables/circles/useCirclesMutationActions.ts", :matched_paths: ["circles", "blocked", "bulk", "accept", "search"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/social/composables/circles/useCirclesPagination.ts", :matched_paths: ["connections", "incoming", "outgoing"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/social/composables/useCirclesView.ts", :matched_paths: ["/circles", "circles", "overview", "blocked", "connections", "bulk", "nearby", "incoming"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/social/composables/useUserProfileView.ts", :matched_paths: ["blocked"]}`

## `docs`



# Endpoint Contract Pack quests

- Generated At: `2026-06-29T19:55:04Z`
- Endpoint Family: `quests`
## `endpoints`

- `{:method: "GET", :path: "/quests", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/QuestController.java", :dtos: ["QuestResponseDTO", "QuestListResponseDTO", "QuestSearchRequestDTO"]}`
- `{:method: "POST", :path: "/quests", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/QuestController.java", :dtos: ["ActionResultDTO", "QuestRequestDTO", "QuestResponseDTO", "QuestListResponseDTO", "QuestSearchRequestDTO"]}`
- `{:method: "GET", :path: "/quests/applications/me", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/QuestApplicationController.java", :dtos: ["QuestApplicationResponseDTO", "QuestApplicationsViewDTO"]}`
- `{:method: "GET", :path: "/quests/presets/{preset}", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/QuestController.java", :dtos: ["QuestResponseDTO", "QuestListResponseDTO", "QuestSearchRequestDTO"]}`
- `{:method: "GET", :path: "/quests/search", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/QuestController.java", :dtos: ["QuestResponseDTO", "QuestListResponseDTO", "QuestSearchRequestDTO"]}`
- `{:method: "DELETE", :path: "/quests/{id}", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/QuestController.java", :dtos: ["ActionResultDTO", "QuestRequestDTO"]}`
- `{:method: "GET", :path: "/quests/{id}", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/QuestController.java", :dtos: ["QuestResponseDTO", "QuestListResponseDTO", "QuestSearchRequestDTO"]}`
- `{:method: "PUT", :path: "/quests/{id}", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/QuestController.java", :dtos: ["ActionResultDTO", "QuestRequestDTO"]}`
- `{:method: "PATCH", :path: "/quests/{id}/complete", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/QuestController.java", :dtos: ["ActionResultDTO", "QuestResponseDTO"]}`
- `{:method: "GET", :path: "/quests/{id}/detail", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/QuestController.java", :dtos: ["QuestResponseDTO", "QuestListResponseDTO", "QuestSearchRequestDTO"]}`
- `{:method: "PATCH", :path: "/quests/{id}/start", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/QuestController.java", :dtos: ["ActionResultDTO", "QuestResponseDTO"]}`
- `{:method: "PATCH", :path: "/quests/{id}/term/confirm", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/QuestController.java", :dtos: ["ActionResultDTO", "QuestResponseDTO"]}`
- `{:method: "PATCH", :path: "/quests/{id}/term/reject", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/QuestController.java", :dtos: ["ActionResultDTO", "QuestResponseDTO"]}`
- `{:method: "GET", :path: "/quests/{questId}/applications", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/QuestApplicationController.java", :dtos: ["QuestApplicationResponseDTO", "QuestApplicationsViewDTO"]}`
- `{:method: "POST", :path: "/quests/{questId}/applications", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/QuestApplicationController.java", :dtos: ["ActionResultDTO", "QuestApplicationRequestDTO", "QuestApplicationResponseDTO", "QuestApplicationsViewDTO"]}`
- `{:method: "PUT", :path: "/quests/{questId}/applications/me", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/QuestApplicationController.java", :dtos: ["ActionResultDTO", "AdminQuestApplicationUpdateRequestDTO"]}`
- `{:method: "PATCH", :path: "/quests/{questId}/applications/me/withdraw", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/QuestApplicationController.java", :dtos: ["ActionResultDTO"]}`
- `{:method: "GET", :path: "/quests/{questId}/applications/view", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/QuestApplicationController.java", :dtos: ["QuestApplicationResponseDTO", "QuestApplicationsViewDTO"]}`
- `{:method: "PATCH", :path: "/quests/{questId}/applications/{applicationId}/approve", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/QuestApplicationController.java", :dtos: ["ActionResultDTO"]}`
- `{:method: "PATCH", :path: "/quests/{questId}/applications/{applicationId}/decline", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/QuestApplicationController.java", :dtos: ["ActionResultDTO"]}`
- `{:method: "POST", :path: "/quests/{questId}/reviews", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/UserReviewController.java", :dtos: ["UserReviewResponseDTO", "UserReviewRequestDTO"]}`

## `dto_files`

- `apps/themuffinman/src/main/java/com/themuffinman/app/common/dto/ActionResultDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/social/dto/CircleRequestListResponseDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/social/dto/CircleRequestResponseDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/things/dto/ThingBorrowRequestResponseDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/AdminQuestApplicationUpdateRequestDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/QuestApplicationRequestDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/QuestApplicationResponseDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/QuestApplicationsViewDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/QuestListResponseDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/QuestRequestDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/QuestResponseDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/QuestSearchRequestDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/UserReviewRequestDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/UserReviewResponseDTO.java`

## `frontend_callsites`

- `{:file: "apps/themuffinman/frontend/src/App.vue", :matched_paths: ["me", "start", "view"]}`
- `{:file: "apps/themuffinman/frontend/src/api/apiErrors.ts", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/api/httpClient.ts", :matched_paths: ["me", "reject"]}`
- `{:file: "apps/themuffinman/frontend/src/components/app/AppChatTray.vue", :matched_paths: ["me", "view"]}`
- `{:file: "apps/themuffinman/frontend/src/components/app/AppNotificationsPanel.vue", :matched_paths: ["me", "decline"]}`
- `{:file: "apps/themuffinman/frontend/src/components/app/AppPageHeader.vue", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/app/AppTopbar.vue", :matched_paths: ["me", "decline"]}`
- `{:file: "apps/themuffinman/frontend/src/components/app/topbar/createTopbarNavState.ts", :matched_paths: ["/quests", "quests", "me", "start", "applications"]}`
- `{:file: "apps/themuffinman/frontend/src/components/app/topbar/useTopbarMenus.ts", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/app/topbar/useTopbarNotifications.ts", :matched_paths: ["me", "search", "decline"]}`
- `{:file: "apps/themuffinman/frontend/src/components/app/useAppTopbarState.ts", :matched_paths: ["me", "decline"]}`
- `{:file: "apps/themuffinman/frontend/src/components/editor/RichTextEditor.vue", :matched_paths: ["me", "start"]}`
- `{:file: "apps/themuffinman/frontend/src/components/editor/RichTextEditorLoading.vue", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/profile/ProfileAvatar.vue", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/profile/ProfileEntityCard.vue", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/profile/ProfileOpenQuestItem.vue", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/profile/ProfileReviewItem.vue", :matched_paths: ["me", "view"]}`
- `{:file: "apps/themuffinman/frontend/src/components/profile/ProfileSummaryCard.vue", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/ui/UiAdminPageSection.vue", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/ui/UiAmountField.vue", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/ui/UiAppShellPage.vue", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/ui/UiAuthCard.vue", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/ui/UiConfirmDialog.vue", :matched_paths: ["me", "confirm"]}`
- `{:file: "apps/themuffinman/frontend/src/components/ui/UiDialog.vue", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/ui/UiEventPill.vue", :matched_paths: ["me"]}`

## `docs`



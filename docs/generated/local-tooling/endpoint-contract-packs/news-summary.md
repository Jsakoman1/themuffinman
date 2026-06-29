# Endpoint Contract Pack news

- Generated At: `2026-06-29T19:55:04Z`
- Endpoint Family: `news`
## `endpoints`

- `{:method: "GET", :path: "/news/me", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/QuestNewsController.java", :dtos: ["QuestNewsItemResponseDTO", "ActionResultDTO"]}`
- `{:method: "PATCH", :path: "/news/me/read", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/QuestNewsController.java", :dtos: ["ActionResultDTO"]}`
- `{:method: "GET", :path: "/news/me/unread-count", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/QuestNewsController.java", :dtos: ["QuestNewsItemResponseDTO", "ActionResultDTO"]}`
- `{:method: "PATCH", :path: "/news/me/{id}/read", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/QuestNewsController.java", :dtos: ["ActionResultDTO"]}`

## `dto_files`

- `apps/themuffinman/src/main/java/com/themuffinman/app/common/dto/ActionResultDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/QuestNewsItemResponseDTO.java`

## `frontend_callsites`

- `{:file: "apps/themuffinman/frontend/src/App.vue", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/api/apiErrors.ts", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/api/httpClient.ts", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/app/AppChatTray.vue", :matched_paths: ["me", "read"]}`
- `{:file: "apps/themuffinman/frontend/src/components/app/AppNotificationsPanel.vue", :matched_paths: ["me", "read"]}`
- `{:file: "apps/themuffinman/frontend/src/components/app/AppPageHeader.vue", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/app/AppTopbar.vue", :matched_paths: ["me", "read", "unread-count"]}`
- `{:file: "apps/themuffinman/frontend/src/components/app/topbar/createTopbarNavState.ts", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/app/topbar/useTopbarMenus.ts", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/app/topbar/useTopbarNotifications.ts", :matched_paths: ["me", "read"]}`
- `{:file: "apps/themuffinman/frontend/src/components/app/useAppTopbarState.ts", :matched_paths: ["me", "read"]}`
- `{:file: "apps/themuffinman/frontend/src/components/editor/RichTextEditor.vue", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/editor/RichTextEditorLoading.vue", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/profile/ProfileAvatar.vue", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/profile/ProfileEntityCard.vue", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/profile/ProfileOpenQuestItem.vue", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/profile/ProfileReviewItem.vue", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/profile/ProfileSummaryCard.vue", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/ui/UiAdminPageSection.vue", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/ui/UiAmountField.vue", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/ui/UiAppShellPage.vue", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/ui/UiAuthCard.vue", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/ui/UiConfirmDialog.vue", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/ui/UiDialog.vue", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/ui/UiEventPill.vue", :matched_paths: ["me"]}`

## `docs`



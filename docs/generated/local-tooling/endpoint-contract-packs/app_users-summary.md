# Endpoint Contract Pack app_users

- Generated At: `2026-06-29T19:55:04Z`
- Endpoint Family: `app_users`
## `endpoints`

- `{:method: "GET", :path: "/app_users", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/identity/controller/AppUserController.java", :dtos: ["AppUserResponseDTO", "WorkmarketOptionsDTO"]}`
- `{:method: "POST", :path: "/app_users", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/identity/controller/AppUserController.java", :dtos: ["ActionResultDTO", "AppUserRequestDTO", "AppUserResponseDTO", "WorkmarketOptionsDTO"]}`
- `{:method: "GET", :path: "/app_users/me", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/identity/controller/AppUserController.java", :dtos: ["AppUserResponseDTO", "WorkmarketOptionsDTO"]}`
- `{:method: "PUT", :path: "/app_users/me", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/identity/controller/AppUserController.java", :dtos: ["ActionResultDTO", "AppUserRequestDTO", "AppUserResponseDTO"]}`
- `{:method: "GET", :path: "/app_users/options", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/identity/controller/AppUserController.java", :dtos: ["AppUserResponseDTO", "WorkmarketOptionsDTO"]}`
- `{:method: "DELETE", :path: "/app_users/{id}", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/identity/controller/AppUserController.java", :dtos: ["ActionResultDTO", "AppUserRequestDTO", "AppUserResponseDTO"]}`
- `{:method: "GET", :path: "/app_users/{id}", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/identity/controller/AppUserController.java", :dtos: ["AppUserResponseDTO", "WorkmarketOptionsDTO"]}`
- `{:method: "PUT", :path: "/app_users/{id}", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/identity/controller/AppUserController.java", :dtos: ["ActionResultDTO", "AppUserRequestDTO", "AppUserResponseDTO"]}`
- `{:method: "GET", :path: "/app_users/{id}/admin-detail", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/identity/controller/AppUserController.java", :dtos: ["AppUserResponseDTO", "WorkmarketOptionsDTO"]}`
- `{:method: "GET", :path: "/app_users/{id}/profile-view", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/identity/controller/AppUserController.java", :dtos: ["AppUserResponseDTO", "WorkmarketOptionsDTO"]}`

## `dto_files`

- `apps/themuffinman/src/main/java/com/themuffinman/app/common/dto/ActionResultDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/identity/dto/AppUserRequestDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/identity/dto/AppUserResponseDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/WorkmarketOptionsDTO.java`

## `frontend_callsites`

- `{:file: "apps/themuffinman/frontend/src/App.vue", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/api/apiErrors.ts", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/api/httpClient.ts", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/app/AppChatTray.vue", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/app/AppNotificationsPanel.vue", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/app/AppPageHeader.vue", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/app/AppTopbar.vue", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/app/topbar/createTopbarNavState.ts", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/app/topbar/useTopbarMenus.ts", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/app/topbar/useTopbarNotifications.ts", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/app/useAppTopbarState.ts", :matched_paths: ["me"]}`
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
- `{:file: "apps/themuffinman/frontend/src/components/ui/UiChoiceChips.vue", :matched_paths: ["options"]}`
- `{:file: "apps/themuffinman/frontend/src/components/ui/UiConfirmDialog.vue", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/ui/UiDialog.vue", :matched_paths: ["me"]}`

## `docs`



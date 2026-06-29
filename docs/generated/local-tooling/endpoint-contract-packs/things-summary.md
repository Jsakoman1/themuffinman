# Endpoint Contract Pack things

- Generated At: `2026-06-29T19:55:04Z`
- Endpoint Family: `things`
## `endpoints`

- `{:method: "GET", :path: "/things/listings", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/things/controller/ThingSharingController.java", :dtos: ["ThingListingListResponseDTO", "ThingListingResponseDTO", "ThingListingRequestDTO", "ThingBorrowRequestResponseDTO"]}`
- `{:method: "POST", :path: "/things/listings", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/things/controller/ThingSharingController.java", :dtos: ["ThingListingResponseDTO", "ThingListingRequestDTO", "ThingBorrowRequestResponseDTO", "ThingBorrowRequestDTO"]}`
- `{:method: "GET", :path: "/things/listings/me", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/things/controller/ThingSharingController.java", :dtos: ["ThingListingListResponseDTO", "ThingListingResponseDTO", "ThingListingRequestDTO", "ThingBorrowRequestResponseDTO"]}`
- `{:method: "POST", :path: "/things/listings/{listingId}/borrow-requests", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/things/controller/ThingSharingController.java", :dtos: ["ThingListingResponseDTO", "ThingListingRequestDTO", "ThingBorrowRequestResponseDTO", "ThingBorrowRequestDTO"]}`

## `dto_files`

- `apps/themuffinman/src/main/java/com/themuffinman/app/things/dto/ThingBorrowRequestDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/things/dto/ThingBorrowRequestResponseDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/things/dto/ThingListingListResponseDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/things/dto/ThingListingRequestDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/things/dto/ThingListingResponseDTO.java`

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
- `{:file: "apps/themuffinman/frontend/src/components/ui/UiConfirmDialog.vue", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/ui/UiDialog.vue", :matched_paths: ["me"]}`
- `{:file: "apps/themuffinman/frontend/src/components/ui/UiEventPill.vue", :matched_paths: ["me"]}`

## `docs`

- `docs/business-logic.md`
- `docs/domain-technical.md`
- `docs/agent-operating-model.md`


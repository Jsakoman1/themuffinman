# Frontend Usage Graph

- Generated At: `2026-06-28T21:02:37Z`
- Node Count: `230`
- Edge Count: `835`
## `nodes`

- `{:file: "apps/themuffinman/frontend/src/App.vue", :category: "other", :domain: "shared"}`
- `{:file: "apps/themuffinman/frontend/src/api/apiErrors.ts", :category: "other", :domain: "shared"}`
- `{:file: "apps/themuffinman/frontend/src/api/httpClient.ts", :category: "other", :domain: "shared"}`
- `{:file: "apps/themuffinman/frontend/src/auth.ts", :category: "other", :domain: "shared"}`
- `{:file: "apps/themuffinman/frontend/src/components/app/AppBrand.vue", :category: "other", :domain: "shared"}`
- `{:file: "apps/themuffinman/frontend/src/components/app/AppChatTray.vue", :category: "other", :domain: "shared"}`
- `{:file: "apps/themuffinman/frontend/src/components/app/AppModuleNav.vue", :category: "other", :domain: "shared"}`
- `{:file: "apps/themuffinman/frontend/src/components/app/AppNotificationsPanel.vue", :category: "other", :domain: "shared"}`
- `{:file: "apps/themuffinman/frontend/src/components/app/AppPageHeader.vue", :category: "other", :domain: "shared"}`
- `{:file: "apps/themuffinman/frontend/src/components/app/AppTopbar.vue", :category: "other", :domain: "shared"}`
- `{:file: "apps/themuffinman/frontend/src/components/app/topbar/createTopbarNavState.ts", :category: "other", :domain: "shared"}`
- `{:file: "apps/themuffinman/frontend/src/components/app/topbar/useTopbarMenus.ts", :category: "other", :domain: "shared"}`
- `{:file: "apps/themuffinman/frontend/src/components/app/topbar/useTopbarNotifications.ts", :category: "other", :domain: "shared"}`
- `{:file: "apps/themuffinman/frontend/src/components/app/useAppTopbarState.ts", :category: "other", :domain: "shared"}`
- `{:file: "apps/themuffinman/frontend/src/components/editor/AsyncRichTextEditor.vue", :category: "other", :domain: "shared"}`
- `{:file: "apps/themuffinman/frontend/src/components/editor/RichTextEditor.vue", :category: "other", :domain: "shared"}`
- `{:file: "apps/themuffinman/frontend/src/components/editor/RichTextEditorLoading.vue", :category: "other", :domain: "shared"}`
- `{:file: "apps/themuffinman/frontend/src/components/profile/ProfileAvatar.vue", :category: "other", :domain: "shared"}`
- `{:file: "apps/themuffinman/frontend/src/components/profile/ProfileBio.vue", :category: "other", :domain: "shared"}`
- `{:file: "apps/themuffinman/frontend/src/components/profile/ProfileEntityCard.vue", :category: "other", :domain: "shared"}`
- `{:file: "apps/themuffinman/frontend/src/components/profile/ProfileOpenQuestItem.vue", :category: "other", :domain: "shared"}`
- `{:file: "apps/themuffinman/frontend/src/components/profile/ProfileReviewItem.vue", :category: "other", :domain: "shared"}`
- `{:file: "apps/themuffinman/frontend/src/components/profile/ProfileSummaryCard.vue", :category: "other", :domain: "shared"}`
- `{:file: "apps/themuffinman/frontend/src/components/ui/UiAdminPageSection.vue", :category: "other", :domain: "shared"}`
- `{:file: "apps/themuffinman/frontend/src/components/ui/UiAdminTableSection.vue", :category: "other", :domain: "shared"}`

## `edges`

- `{:from: "apps/themuffinman/frontend/src/App.vue", :to: "axios"}`
- `{:from: "apps/themuffinman/frontend/src/App.vue", :to: "vue"}`
- `{:from: "apps/themuffinman/frontend/src/App.vue", :to: "vue-router"}`
- `{:from: "apps/themuffinman/frontend/src/App.vue", :to: "./components/app/AppChatTray.vue"}`
- `{:from: "apps/themuffinman/frontend/src/App.vue", :to: "./components/app/AppTopbar.vue"}`
- `{:from: "apps/themuffinman/frontend/src/App.vue", :to: "./modules/identity/api/authApi.ts"}`
- `{:from: "apps/themuffinman/frontend/src/App.vue", :to: "./services/sessionService.ts"}`
- `{:from: "apps/themuffinman/frontend/src/api/apiErrors.ts", :to: "axios"}`
- `{:from: "apps/themuffinman/frontend/src/api/apiErrors.ts", :to: "../contracts/index.ts"}`
- `{:from: "apps/themuffinman/frontend/src/api/httpClient.ts", :to: "axios"}`
- `{:from: "apps/themuffinman/frontend/src/api/httpClient.ts", :to: "../auth.ts"}`
- `{:from: "apps/themuffinman/frontend/src/api/httpClient.ts", :to: "../services/sessionService.ts"}`
- `{:from: "apps/themuffinman/frontend/src/auth.ts", :to: "./modules/identity/auth.ts"}`
- `{:from: "apps/themuffinman/frontend/src/components/app/AppChatTray.vue", :to: "vue"}`
- `{:from: "apps/themuffinman/frontend/src/components/app/AppChatTray.vue", :to: "../profile/ProfileAvatar.vue"}`
- `{:from: "apps/themuffinman/frontend/src/components/app/AppChatTray.vue", :to: "../../modules/chat/composables/useAppChat.ts"}`
- `{:from: "apps/themuffinman/frontend/src/components/app/AppChatTray.vue", :to: "../../shared/questSchedule.ts"}`
- `{:from: "apps/themuffinman/frontend/src/components/app/AppModuleNav.vue", :to: "vue-router"}`
- `{:from: "apps/themuffinman/frontend/src/components/app/AppNotificationsPanel.vue", :to: "vue"}`
- `{:from: "apps/themuffinman/frontend/src/components/app/AppNotificationsPanel.vue", :to: "../../modules/workmarket/api/workmarketApi.ts"}`
- `{:from: "apps/themuffinman/frontend/src/components/app/AppNotificationsPanel.vue", :to: "../../shared/questSchedule.ts"}`
- `{:from: "apps/themuffinman/frontend/src/components/app/AppTopbar.vue", :to: "../../auth.ts"}`
- `{:from: "apps/themuffinman/frontend/src/components/app/AppTopbar.vue", :to: "../profile/ProfileAvatar.vue"}`
- `{:from: "apps/themuffinman/frontend/src/components/app/AppTopbar.vue", :to: "./AppBrand.vue"}`
- `{:from: "apps/themuffinman/frontend/src/components/app/AppTopbar.vue", :to: "./AppModuleNav.vue"}`


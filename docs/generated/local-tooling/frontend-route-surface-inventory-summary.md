# Frontend Route Surface Inventory

- Generated at: `2026-07-01T14:49:22Z`
- Routes: `10`
- Routes with concrete surfaces: `9`
- Redirect routes: `1`
- Placeholder module routes: `0`

## Route sample

- `/` | surface=_none_ | redirect=`/vision` | apis=0 | endpoints=0
- `/login` | surface=`apps/themuffinman/frontend/src/modules/identity/views/LoginView.vue` | redirect=_none_ | apis=1 | endpoints=1
- `/register` | surface=`apps/themuffinman/frontend/src/modules/identity/views/RegisterView.vue` | redirect=_none_ | apis=1 | endpoints=1
- `/vision` | surface=`apps/themuffinman/frontend/src/modules/vision/views/VisionSurfaceModernView.vue` | redirect=_none_ | apis=8 | endpoints=8
  - composables=`apps/themuffinman/frontend/src/composables/useMountedAsync.ts`, `apps/themuffinman/frontend/src/modules/vision/composables/useVisionConversation.ts`, `apps/themuffinman/frontend/src/modules/vision/composables/useVisionSurfaceState.ts`
- `/vision/users/:id` | surface=`apps/themuffinman/frontend/src/modules/vision/views/VisionUserProfileView.vue` | redirect=_none_ | apis=13 | endpoints=13
  - composables=`apps/themuffinman/frontend/src/modules/social/composables/useUserProfileView.ts`, `apps/themuffinman/frontend/src/modules/vision/composables/useVisionConversation.ts`
- `/vision/settings` | surface=`apps/themuffinman/frontend/src/modules/vision/views/VisionUserSettingsView.vue` | redirect=_none_ | apis=17 | endpoints=17
  - composables=`apps/themuffinman/frontend/src/composables/useDebouncedWatch.ts`, `apps/themuffinman/frontend/src/modules/social/composables/useUserProfileView.ts`, `apps/themuffinman/frontend/src/modules/vision/composables/useVisionConversation.ts`
- `/vision/circles` | surface=`apps/themuffinman/frontend/src/modules/vision/views/VisionCirclesView.vue` | redirect=_none_ | apis=26 | endpoints=26
  - composables=`apps/themuffinman/frontend/src/modules/social/composables/useCirclesView.ts`, `apps/themuffinman/frontend/src/modules/vision/composables/useVisionConversation.ts`
- `/vision/chat` | surface=`apps/themuffinman/frontend/src/modules/vision/views/VisionChatWorkspaceView.vue` | redirect=_none_ | apis=9 | endpoints=9
  - composables=`apps/themuffinman/frontend/src/modules/vision/composables/useVisionConversation.ts`
- `/vision/quests/:id` | surface=`apps/themuffinman/frontend/src/modules/vision/views/VisionQuestDetailView.vue` | redirect=_none_ | apis=20 | endpoints=20
  - composables=`apps/themuffinman/frontend/src/composables/useDebouncedWatch.ts`, `apps/themuffinman/frontend/src/modules/vision/composables/useVisionConversation.ts`, `apps/themuffinman/frontend/src/modules/workmarket/composables/useQuestDetailView.ts`
- `/vision/applications/:id` | surface=`apps/themuffinman/frontend/src/modules/vision/views/VisionApplicationDetailView.vue` | redirect=_none_ | apis=9 | endpoints=9
  - composables=`apps/themuffinman/frontend/src/modules/vision/composables/useVisionConversation.ts`
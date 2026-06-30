# Frontend Route Surface Inventory

- Generated at: `2026-06-30T10:54:36Z`
- Routes: `23`
- Routes with concrete surfaces: `19`
- Redirect routes: `4`
- Placeholder module routes: `0`

## Route sample

- `/` | surface=_none_ | redirect=`/work` | apis=0 | endpoints=0
- `/login` | surface=`apps/themuffinman/frontend/src/modules/identity/views/LoginView.vue` | redirect=_none_ | apis=1 | endpoints=1
- `/register` | surface=`apps/themuffinman/frontend/src/modules/identity/views/RegisterView.vue` | redirect=_none_ | apis=1 | endpoints=1
- `/work` | surface=`apps/themuffinman/frontend/src/modules/workmarket/pages/QuestsPage.vue` | redirect=_none_ | apis=11 | endpoints=11
  - composables=`apps/themuffinman/frontend/src/modules/workmarket/composables/useQuestDashboard.ts`
- `/quests` | surface=_none_ | redirect=`/work` | apis=0 | endpoints=0
- `/circles` | surface=`apps/themuffinman/frontend/src/modules/social/views/CirclesView.vue` | redirect=_none_ | apis=26 | endpoints=26
  - composables=`apps/themuffinman/frontend/src/modules/social/composables/useCirclesView.ts`, `apps/themuffinman/frontend/src/modules/social/composables/useUserProfileView.ts`
- `/work/:id` | surface=`apps/themuffinman/frontend/src/modules/workmarket/views/QuestDetailView.vue` | redirect=_none_ | apis=12 | endpoints=12
  - composables=`apps/themuffinman/frontend/src/composables/useDebouncedWatch.ts`, `apps/themuffinman/frontend/src/modules/workmarket/composables/useQuestDetailView.ts`
- `/quests/:id` | surface=`apps/themuffinman/frontend/src/modules/workmarket/views/QuestDetailView.vue` | redirect=_none_ | apis=12 | endpoints=12
  - composables=`apps/themuffinman/frontend/src/composables/useDebouncedWatch.ts`, `apps/themuffinman/frontend/src/modules/workmarket/composables/useQuestDetailView.ts`
- `/applications/:id` | surface=`apps/themuffinman/frontend/src/modules/workmarket/views/ApplicationDetailView.vue` | redirect=_none_ | apis=1 | endpoints=1
- `/users/:id` | surface=`apps/themuffinman/frontend/src/modules/social/views/UserProfileView.vue` | redirect=_none_ | apis=5 | endpoints=5
  - composables=`apps/themuffinman/frontend/src/modules/social/composables/useUserProfileView.ts`
- `/settings` | surface=`apps/themuffinman/frontend/src/modules/social/views/UserSettingsView.vue` | redirect=_none_ | apis=9 | endpoints=9
  - composables=`apps/themuffinman/frontend/src/composables/useDebouncedWatch.ts`, `apps/themuffinman/frontend/src/modules/social/composables/useUserProfileView.ts`
- `/admin` | surface=_none_ | redirect=`/admin/work` | apis=0 | endpoints=0
- ... 11 more routes
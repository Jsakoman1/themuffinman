# Frontend Route Surface Inventory

- Generated at: `2026-07-03T09:17:21Z`
- Routes: `12`
- Routes with concrete surfaces: `3`
- Redirect routes: `1`
- Placeholder module routes: `0`

## Route sample

- `/` | surface=_none_ | redirect=`/vision` | apis=0 | endpoints=0
- `/login` | surface=`apps/themuffinman/frontend/src/modules/identity/views/LoginView.vue` | redirect=_none_ | apis=1 | endpoints=1
- `/register` | surface=`apps/themuffinman/frontend/src/modules/identity/views/RegisterView.vue` | redirect=_none_ | apis=1 | endpoints=1
- `/vision` | surface=`apps/themuffinman/frontend/src/modules/vision/views/VisionSurfaceModernView.vue` | redirect=_none_ | apis=8 | endpoints=8
  - composables=`apps/themuffinman/frontend/src/modules/vision/composables/useVisionConversation.ts`
- `/vision/users/:id` | surface=_none_ | redirect=_none_ | apis=0 | endpoints=0
- `/vision/profile` | surface=_none_ | redirect=_none_ | apis=0 | endpoints=0
- `/vision/settings` | surface=_none_ | redirect=_none_ | apis=0 | endpoints=0
- `/vision/circles` | surface=_none_ | redirect=_none_ | apis=0 | endpoints=0
- `/vision/chat` | surface=_none_ | redirect=_none_ | apis=0 | endpoints=0
- `/vision/quests/:id` | surface=_none_ | redirect=_none_ | apis=0 | endpoints=0
- `/vision/applications/:id` | surface=_none_ | redirect=_none_ | apis=0 | endpoints=0
- `/vision/applications` | surface=_none_ | redirect=_none_ | apis=0 | endpoints=0
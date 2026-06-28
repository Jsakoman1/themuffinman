# Endpoint Callsite Linker

- Generated at: `2026-06-28T20:30:13Z`
- Backend endpoints: `76`
- Frontend client methods: `84`
- Linked endpoints: `76`
- Unlinked endpoints: `0`

## `POST /admin/agent/playground`

- Backend: `AdminAgentController.runPlaygroundPrompt`
- Frontend client: `adminApi.runAdminAgentPrompt` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/adminApi.ts`

## `POST /admin/agent/simulate`

- Backend: `AdminAgentController.runSimulation`
- Frontend client: `adminApi.runAdminAgentSimulation` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/adminApi.ts`

## `GET /admin/applications`

- Backend: `QuestApplicationController.getAllApplicationsForAdmin`
- Frontend client: `adminApi.getAdminApplications` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/adminApi.ts`

## `DELETE /admin/applications/:param`

- Backend: `QuestApplicationController.deleteApplicationForAdmin`
- Frontend client: `applicationsApi.deleteAdminApplication` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/applicationsApi.ts`

## `PUT /admin/applications/:param`

- Backend: `QuestApplicationController.updateApplicationForAdmin`
- Frontend client: `applicationsApi.updateAdminApplication` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/applicationsApi.ts`

## `GET /app_users`

- Backend: `AppUserController.getAllAppUsers`
- Frontend client: `usersApi.getAppUsers` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/usersApi.ts`

## `POST /app_users`

- Backend: `AppUserController.createAppUser`
- Frontend client: `usersApi.createAppUser` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/usersApi.ts`

## `DELETE /app_users/:param`

- Backend: `AppUserController.deleteAppUser`
- Frontend client: `usersApi.deleteAppUser` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/usersApi.ts`

## `GET /app_users/:param`

- Backend: `AppUserController.getAppUser`
- Frontend client: `usersApi.getAppUser` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/usersApi.ts`

## `PUT /app_users/:param`

- Backend: `AppUserController.updateAppUser`
- Frontend client: `usersApi.updateAppUser` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/usersApi.ts`

## `GET /app_users/:param/admin-detail`

- Backend: `AppUserController.getAdminUserDetail`
- Frontend client: `adminApi.getAdminUserDetail` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/adminApi.ts`

## `GET /app_users/:param/profile-view`

- Backend: `AppUserController.getProfileView`
- Frontend client: `usersApi.getUserProfileView` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/usersApi.ts`

## `GET /app_users/me`

- Backend: `AppUserController.getCurrentAppUser`
- Frontend client: `usersApi.getCurrentAppUser` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/usersApi.ts`

## `PUT /app_users/me`

- Backend: `AppUserController.updateCurrentAppUser`
- Frontend client: `usersApi.updateCurrentAppUser` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/usersApi.ts`

## `GET /app_users/options`

- Backend: `AppUserController.getAppUserOptions`
- Frontend client: `usersApi.getAppUserOptions` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/usersApi.ts`

## `GET /applications/:param/detail`

- Backend: `QuestApplicationController.getApplicationDetail`
- Frontend client: `applicationsApi.getApplicationDetail` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/applicationsApi.ts`

## `POST /auth/login`

- Backend: `AuthController.login`
- Frontend client: `authApi.login` in `apps/themuffinman/frontend/src/modules/identity/api/authApi.ts`
- Caller: `apps/themuffinman/frontend/src/modules/identity/views/LoginView.vue` -> surfaces=`apps/themuffinman/frontend/src/modules/identity/views/LoginView.vue`

## `GET /auth/me`

- Backend: `AuthController.me`
- Frontend client: `authApi.me` in `apps/themuffinman/frontend/src/modules/identity/api/authApi.ts`

## `POST /auth/register`

- Backend: `AuthController.register`
- Frontend client: `authApi.register` in `apps/themuffinman/frontend/src/modules/identity/api/authApi.ts`
- Caller: `apps/themuffinman/frontend/src/modules/identity/views/RegisterView.vue` -> surfaces=`apps/themuffinman/frontend/src/modules/identity/views/RegisterView.vue`

## `GET /chat/conversations/:param/messages`

- Backend: `ChatController.getConversationMessages`
- Frontend client: `chatApi.getConversationMessages` in `apps/themuffinman/frontend/src/modules/chat/api/chatApi.ts`
- Caller: `apps/themuffinman/frontend/src/modules/chat/composables/useAppChat.ts` -> surfaces=none

## `POST /chat/conversations/:param/messages`

- Backend: `ChatController.sendMessage`
- Frontend client: `chatApi.sendMessage` in `apps/themuffinman/frontend/src/modules/chat/api/chatApi.ts`
- Caller: `apps/themuffinman/frontend/src/modules/chat/composables/useAppChat.ts` -> surfaces=none

## `PATCH /chat/conversations/:param/read`

- Backend: `ChatController.markConversationRead`
- Frontend client: `chatApi.markConversationRead` in `apps/themuffinman/frontend/src/modules/chat/api/chatApi.ts`
- Caller: `apps/themuffinman/frontend/src/modules/chat/composables/useAppChat.ts` -> surfaces=none

## `POST /chat/conversations/open`

- Backend: `ChatController.openConversation`
- Frontend client: `chatApi.openConversation` in `apps/themuffinman/frontend/src/modules/chat/api/chatApi.ts`
- Caller: `apps/themuffinman/frontend/src/modules/chat/composables/useAppChat.ts` -> surfaces=none

## `POST /chat/presence/heartbeat`

- Backend: `ChatController.heartbeat`
- Frontend client: `chatApi.heartbeat` in `apps/themuffinman/frontend/src/modules/chat/api/chatApi.ts`

## `GET /chat/workspace`

- Backend: `ChatController.getWorkspace`
- Frontend client: `chatApi.getWorkspace` in `apps/themuffinman/frontend/src/modules/chat/api/chatApi.ts`
- Caller: `apps/themuffinman/frontend/src/modules/chat/composables/useAppChat.ts` -> surfaces=none

## `DELETE /circles/admin/groups/:param`

- Backend: `CircleController.deleteCircleAsAdmin`
- Frontend client: `adminApi.deleteAdminCircle` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/adminApi.ts`

## `GET /circles/admin/overview`

- Backend: `CircleController.getAdminOverview`
- Frontend client: `adminApi.getAdminCircleOverview` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/adminApi.ts`

## `GET /circles/blocked`

- Backend: `CircleController.getBlockedUsers`
- Frontend client: `circlesApi.getBlockedCircleUsersPage` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/circlesApi.ts`

## `POST /circles/blocks`

- Backend: `CircleController.blockCircleUser`
- Frontend client: `circlesApi.blockCircleUser` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/circlesApi.ts`

## `DELETE /circles/blocks/:param`

- Backend: `CircleController.unblockCircleUser`
- Frontend client: `circlesApi.unblockCircleUser` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/circlesApi.ts`

## `GET /circles/candidates`

- Backend: `CircleController.getInviteCandidates`
- Frontend client: `circlesApi.getInviteCandidates` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/circlesApi.ts`
- Frontend client: `circlesApi.getInviteCandidatesPage` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/circlesApi.ts`

## `GET /circles/connections`

- Backend: `CircleController.getConnections`
- Frontend client: `circlesApi.getCircleConnectionsPage` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/circlesApi.ts`

## `PUT /circles/connections/:param/circles`

- Backend: `CircleController.updateConnectionCircles`
- Frontend client: `circlesApi.updateConnectionCircles` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/circlesApi.ts`

## `PUT /circles/connections/circles/bulk`

- Backend: `CircleController.updateConnectionCirclesBulk`
- Frontend client: `circlesApi.updateConnectionCirclesBulk` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/circlesApi.ts`

## `POST /circles/groups`

- Backend: `CircleController.createCircle`
- Frontend client: `circlesApi.createCircle` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/circlesApi.ts`

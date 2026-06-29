# Frontend Route Surface Inventory

- Generated at: `2026-06-29T19:54:44Z`
- Routes: `23`
- Routes with concrete surfaces: `19`
- Redirect routes: `4`
- Placeholder module routes: `0`

## `/`

- Surface: _none_
- Redirect: `/work`
- Primary composables: none
- API clients: none
- Backend endpoints: none

## `/login`

- Surface: `apps/themuffinman/frontend/src/modules/identity/views/LoginView.vue`
- Redirect: _none_
- Primary composables: none
- API clients: `authApi.login`
- Backend endpoints: `POST /auth/login`

## `/register`

- Surface: `apps/themuffinman/frontend/src/modules/identity/views/RegisterView.vue`
- Redirect: _none_
- Primary composables: none
- API clients: `authApi.register`
- Backend endpoints: `POST /auth/register`

## `/work`

- Surface: `apps/themuffinman/frontend/src/modules/workmarket/pages/QuestsPage.vue`
- Redirect: _none_
- Primary composables: `apps/themuffinman/frontend/src/modules/workmarket/composables/useQuestDashboard.ts`
- API clients: `applicationsApi.applyForQuest`, `applicationsApi.approveApplication`, `applicationsApi.declineApplication`, `applicationsApi.getApplicationDetail`, `applicationsApi.getQuestApplicationsView`, `applicationsApi.updateMyApplication`, `applicationsApi.withdrawMyApplication`, `dashboardApi.getDashboard`, `newsApi.markMyNewsAsRead`, `newsApi.markMyNewsItemAsRead`, `questsApi.getQuestDetail`
- Backend endpoints: `GET /applications/:param/detail`, `GET /dashboard/me`, `GET /quests/:param/applications/view`, `GET /quests/:param/detail`, `PATCH /news/me/:param/read`, `PATCH /news/me/read`, `PATCH /quests/:param/applications/:param/approve`, `PATCH /quests/:param/applications/:param/decline`, `PATCH /quests/:param/applications/me/withdraw`, `POST /quests/:param/applications`, `PUT /quests/:param/applications/me`

## `/quests`

- Surface: _none_
- Redirect: `/work`
- Primary composables: none
- API clients: none
- Backend endpoints: none

## `/circles`

- Surface: `apps/themuffinman/frontend/src/modules/social/views/CirclesView.vue`
- Redirect: _none_
- Primary composables: `apps/themuffinman/frontend/src/modules/social/composables/useCirclesView.ts`, `apps/themuffinman/frontend/src/modules/social/composables/useUserProfileView.ts`
- API clients: `applicationsApi.getApplicationDetail`, `applicationsApi.getQuestApplicationsView`, `circlesApi.acceptCircleRequest`, `circlesApi.blockCircleUser`, `circlesApi.createCircle`, `circlesApi.createCircleRequest`, `circlesApi.deleteCircle`, `circlesApi.deleteCircleRequest`, `circlesApi.getBlockedCircleUsersPage`, `circlesApi.getCircleConnectionsPage`, `circlesApi.getCircleOverview`, `circlesApi.getIncomingCircleRequestsPage`, `circlesApi.getInviteCandidatesPage`, `circlesApi.getNearbyCircleUsersPage`, `circlesApi.getOutgoingCircleRequestsPage`, `circlesApi.searchCircleUsersPage`, `circlesApi.unblockCircleUser`, `circlesApi.updateCircle`, `circlesApi.updateConnectionCircles`, `circlesApi.updateConnectionCirclesBulk`, `dashboardApi.getDashboard`, `newsApi.markMyNewsAsRead`, `newsApi.markMyNewsItemAsRead`, `questsApi.getQuestDetail`, `usersApi.getUserProfileView`, `usersApi.updateCurrentAppUser`
- Backend endpoints: `DELETE /circles/blocks/:param`, `DELETE /circles/groups/:param`, `DELETE /circles/requests/:param`, `GET /app_users/:param/profile-view`, `GET /applications/:param/detail`, `GET /circles/blocked`, `GET /circles/candidates`, `GET /circles/connections`, `GET /circles/me/overview`, `GET /circles/nearby`, `GET /circles/requests/incoming`, `GET /circles/requests/outgoing`, `GET /circles/search`, `GET /dashboard/me`, `GET /quests/:param/applications/view`, `GET /quests/:param/detail`, `PATCH /circles/requests/:param/accept`, `PATCH /news/me/:param/read`, `PATCH /news/me/read`, `POST /circles/blocks`, `POST /circles/groups`, `POST /circles/requests`, `PUT /app_users/me`, `PUT /circles/connections/:param/circles`, `PUT /circles/connections/circles/bulk`, `PUT /circles/groups/:param`

## `/work/:id`

- Surface: `apps/themuffinman/frontend/src/modules/workmarket/views/QuestDetailView.vue`
- Redirect: _none_
- Primary composables: `apps/themuffinman/frontend/src/composables/useDebouncedWatch.ts`, `apps/themuffinman/frontend/src/modules/workmarket/composables/useQuestDetailView.ts`
- API clients: `applicationsApi.applyForQuest`, `applicationsApi.createQuestReview`, `applicationsApi.withdrawMyApplication`, `locationApi.lookupLocation`, `questsApi.completeQuest`, `questsApi.confirmQuestTermChange`, `questsApi.deleteQuest`, `questsApi.getQuestDetail`, `questsApi.rejectQuestTermChange`, `questsApi.startQuest`, `questsApi.updateQuest`, `usersApi.getAppUserOptions`
- Backend endpoints: `DELETE /quests/:param`, `GET /app_users/options`, `GET /quests/:param/detail`, `PATCH /quests/:param/applications/me/withdraw`, `PATCH /quests/:param/complete`, `PATCH /quests/:param/start`, `PATCH /quests/:param/term/confirm`, `PATCH /quests/:param/term/reject`, `POST /location/lookup`, `POST /quests/:param/applications`, `POST /quests/:param/reviews`, `PUT /quests/:param`

## `/quests/:id`

- Surface: `apps/themuffinman/frontend/src/modules/workmarket/views/QuestDetailView.vue`
- Redirect: _none_
- Primary composables: `apps/themuffinman/frontend/src/composables/useDebouncedWatch.ts`, `apps/themuffinman/frontend/src/modules/workmarket/composables/useQuestDetailView.ts`
- API clients: `applicationsApi.applyForQuest`, `applicationsApi.createQuestReview`, `applicationsApi.withdrawMyApplication`, `locationApi.lookupLocation`, `questsApi.completeQuest`, `questsApi.confirmQuestTermChange`, `questsApi.deleteQuest`, `questsApi.getQuestDetail`, `questsApi.rejectQuestTermChange`, `questsApi.startQuest`, `questsApi.updateQuest`, `usersApi.getAppUserOptions`
- Backend endpoints: `DELETE /quests/:param`, `GET /app_users/options`, `GET /quests/:param/detail`, `PATCH /quests/:param/applications/me/withdraw`, `PATCH /quests/:param/complete`, `PATCH /quests/:param/start`, `PATCH /quests/:param/term/confirm`, `PATCH /quests/:param/term/reject`, `POST /location/lookup`, `POST /quests/:param/applications`, `POST /quests/:param/reviews`, `PUT /quests/:param`

## `/applications/:id`

- Surface: `apps/themuffinman/frontend/src/modules/workmarket/views/ApplicationDetailView.vue`
- Redirect: _none_
- Primary composables: none
- API clients: `applicationsApi.getApplicationDetail`
- Backend endpoints: `GET /applications/:param/detail`

## `/users/:id`

- Surface: `apps/themuffinman/frontend/src/modules/social/views/UserProfileView.vue`
- Redirect: _none_
- Primary composables: `apps/themuffinman/frontend/src/modules/social/composables/useUserProfileView.ts`
- API clients: `circlesApi.blockCircleUser`, `circlesApi.createCircleRequest`, `circlesApi.unblockCircleUser`, `usersApi.getUserProfileView`, `usersApi.updateCurrentAppUser`
- Backend endpoints: `DELETE /circles/blocks/:param`, `GET /app_users/:param/profile-view`, `POST /circles/blocks`, `POST /circles/requests`, `PUT /app_users/me`

## `/settings`

- Surface: `apps/themuffinman/frontend/src/modules/social/views/UserSettingsView.vue`
- Redirect: _none_
- Primary composables: `apps/themuffinman/frontend/src/composables/useDebouncedWatch.ts`, `apps/themuffinman/frontend/src/modules/social/composables/useUserProfileView.ts`
- API clients: `circlesApi.blockCircleUser`, `circlesApi.createCircleRequest`, `circlesApi.getCircleConnectionsPage`, `circlesApi.unblockCircleUser`, `locationApi.lookupLocation`, `locationApi.reverseLookupLocation`, `usersApi.getAppUserOptions`, `usersApi.getUserProfileView`, `usersApi.updateCurrentAppUser`
- Backend endpoints: `DELETE /circles/blocks/:param`, `GET /app_users/:param/profile-view`, `GET /app_users/options`, `GET /circles/connections`, `POST /circles/blocks`, `POST /circles/requests`, `POST /location/lookup`, `POST /location/reverse-lookup`, `PUT /app_users/me`

## `/admin`

- Surface: _none_
- Redirect: `/admin/work`
- Primary composables: none
- API clients: none
- Backend endpoints: none

## `/admin/work`

- Surface: `apps/themuffinman/frontend/src/modules/workmarket/pages/AdminOverviewPage.vue`
- Redirect: _none_
- Primary composables: none
- API clients: `adminApi.getAdminLocationStatus`, `dashboardApi.getDashboardSummary`
- Backend endpoints: `GET /dashboard/me/summary`, `GET /location/admin/status`

## `/admin/quests`

- Surface: `apps/themuffinman/frontend/src/modules/workmarket/pages/AdminQuestsPage.vue`
- Redirect: _none_
- Primary composables: `apps/themuffinman/frontend/src/composables/useMountedAsync.ts`, `apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/createApplicationDialogViewState.ts`, `apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/createQuestDialogViewState.ts`, `apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/dashboardFacades.ts`, `apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/useApplicationDialogUiActions.ts`, `apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/useDashboardAdminQuestBrowser.ts`, `apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/useQuestDialogUiActions.ts`, `apps/themuffinman/frontend/src/modules/workmarket/composables/useQuestDashboard.ts`
- API clients: `applicationsApi.applyForQuest`, `applicationsApi.approveApplication`, `applicationsApi.declineApplication`, `applicationsApi.getApplicationDetail`, `applicationsApi.getQuestApplicationsView`, `applicationsApi.updateMyApplication`, `applicationsApi.withdrawMyApplication`, `dashboardApi.getDashboard`, `locationApi.lookupLocation`, `newsApi.markMyNewsAsRead`, `newsApi.markMyNewsItemAsRead`, `questsApi.confirmQuestTermChange`, `questsApi.deleteQuest`, `questsApi.getQuestDetail`, `questsApi.rejectQuestTermChange`, `questsApi.searchQuests`
- Backend endpoints: `DELETE /quests/:param`, `GET /applications/:param/detail`, `GET /dashboard/me`, `GET /quests/:param/applications/view`, `GET /quests/:param/detail`, `GET /quests/search`, `PATCH /news/me/:param/read`, `PATCH /news/me/read`, `PATCH /quests/:param/applications/:param/approve`, `PATCH /quests/:param/applications/:param/decline`, `PATCH /quests/:param/applications/me/withdraw`, `PATCH /quests/:param/term/confirm`, `PATCH /quests/:param/term/reject`, `POST /location/lookup`, `POST /quests/:param/applications`, `PUT /quests/:param/applications/me`

## `/admin/users`

- Surface: `apps/themuffinman/frontend/src/modules/workmarket/pages/AdminUsersPage.vue`
- Redirect: _none_
- Primary composables: `apps/themuffinman/frontend/src/composables/useMountedAsync.ts`, `apps/themuffinman/frontend/src/modules/workmarket/composables/useAppUsersPage.ts`
- API clients: `adminApi.getAdminUserDetail`, `locationApi.lookupLocation`, `usersApi.createAppUser`, `usersApi.deleteAppUser`, `usersApi.getAppUserOptions`, `usersApi.getAppUsers`, `usersApi.updateAppUser`
- Backend endpoints: `DELETE /app_users/:param`, `GET /app_users`, `GET /app_users/:param/admin-detail`, `GET /app_users/options`, `POST /app_users`, `POST /location/lookup`, `PUT /app_users/:param`

## `/admin/applications`

- Surface: `apps/themuffinman/frontend/src/modules/workmarket/pages/AdminApplicationsPage.vue`
- Redirect: _none_
- Primary composables: `apps/themuffinman/frontend/src/modules/workmarket/composables/useAdminApplicationsPage.ts`
- API clients: `adminApi.getAdminApplications`, `applicationsApi.approveApplication`, `applicationsApi.declineApplication`, `applicationsApi.deleteAdminApplication`, `applicationsApi.updateAdminApplication`, `usersApi.getAppUserOptions`
- Backend endpoints: `DELETE /admin/applications/:param`, `GET /admin/applications`, `GET /app_users/options`, `PATCH /quests/:param/applications/:param/approve`, `PATCH /quests/:param/applications/:param/decline`, `PUT /admin/applications/:param`

## `/admin/circles`

- Surface: `apps/themuffinman/frontend/src/modules/social/pages/AdminCirclesPage.vue`
- Redirect: _none_
- Primary composables: `apps/themuffinman/frontend/src/composables/createFeedbackMutationRunner.ts`, `apps/themuffinman/frontend/src/composables/useAutoDismissFeedback.ts`, `apps/themuffinman/frontend/src/composables/useDebouncedWatch.ts`
- API clients: `adminApi.deleteAdminCircle`, `adminApi.deleteAdminCircleRequest`, `adminApi.getAdminCircleOverview`
- Backend endpoints: `DELETE /circles/admin/groups/:param`, `DELETE /circles/requests/:param`, `GET /circles/admin/overview`

## `/admin/agent`

- Surface: `apps/themuffinman/frontend/src/modules/workmarket/pages/AdminAgentPage.vue`
- Redirect: _none_
- Primary composables: none
- API clients: `adminApi.runAdminAgentPrompt`, `adminApi.runAdminAgentSimulation`
- Backend endpoints: `POST /admin/agent/playground`, `POST /admin/agent/simulate`

## `/app-users`

- Surface: _none_
- Redirect: `/admin/users`
- Primary composables: none
- API clients: none
- Backend endpoints: none

## `/business`

- Surface: `apps/themuffinman/frontend/src/modules/business/views/BusinessHubView.vue`
- Redirect: _none_
- Primary composables: none
- API clients: `businessApi.getDirectory`, `businessApi.getMyProfile`, `businessApi.saveMyProfile`
- Backend endpoints: `GET /business/profiles`, `GET /business/profiles/me`, `PUT /business/profiles/me`

## `/things`

- Surface: `apps/themuffinman/frontend/src/modules/things/views/ThingSharingView.vue`
- Redirect: _none_
- Primary composables: none
- API clients: `thingsApi.createListing`, `thingsApi.getAvailableListings`, `thingsApi.getMyListings`, `thingsApi.requestBorrow`
- Backend endpoints: `GET /things/listings`, `GET /things/listings/me`, `POST /things/listings`, `POST /things/listings/:param/borrow-requests`

## `/rides`

- Surface: `apps/themuffinman/frontend/src/modules/rides/views/RideSharingView.vue`
- Redirect: _none_
- Primary composables: none
- API clients: `ridesApi.createOffer`, `ridesApi.getMyOffers`, `ridesApi.getVisibleOffers`
- Backend endpoints: `GET /rides/offers`, `GET /rides/offers/me`, `POST /rides/offers`

## `/chat`

- Surface: `apps/themuffinman/frontend/src/modules/chat/views/ChatWorkspaceView.vue`
- Redirect: _none_
- Primary composables: none
- API clients: `chatApi.getWorkspace`
- Backend endpoints: `GET /chat/workspace`

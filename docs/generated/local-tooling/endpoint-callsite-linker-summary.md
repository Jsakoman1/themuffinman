# Endpoint Callsite Linker

- Generated at: `2026-06-30T10:54:35Z`
- Backend endpoints: `87`
- Frontend client methods: `95`
- Linked endpoints: `87`
- Unlinked endpoints: `0`

## Endpoint sample

- `POST /admin/agent/playground` | backend=`AdminAgentController.runPlaygroundPrompt` | frontend_matches=1
  - `adminApi.runAdminAgentPrompt` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/adminApi.ts`
- `POST /admin/agent/simulate` | backend=`AdminAgentController.runSimulation` | frontend_matches=1
  - `adminApi.runAdminAgentSimulation` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/adminApi.ts`
- `GET /admin/applications` | backend=`QuestApplicationController.getAllApplicationsForAdmin` | frontend_matches=1
  - `adminApi.getAdminApplications` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/adminApi.ts`
- `DELETE /admin/applications/:param` | backend=`QuestApplicationController.deleteApplicationForAdmin` | frontend_matches=1
  - `applicationsApi.deleteAdminApplication` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/applicationsApi.ts`
- `PUT /admin/applications/:param` | backend=`QuestApplicationController.updateApplicationForAdmin` | frontend_matches=1
  - `applicationsApi.updateAdminApplication` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/applicationsApi.ts`
- `GET /app_users` | backend=`AppUserController.getAllAppUsers` | frontend_matches=1
  - `usersApi.getAppUsers` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/usersApi.ts`
- `POST /app_users` | backend=`AppUserController.createAppUser` | frontend_matches=1
  - `usersApi.createAppUser` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/usersApi.ts`
- `DELETE /app_users/:param` | backend=`AppUserController.deleteAppUser` | frontend_matches=1
  - `usersApi.deleteAppUser` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/usersApi.ts`
- `GET /app_users/:param` | backend=`AppUserController.getAppUser` | frontend_matches=1
  - `usersApi.getAppUser` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/usersApi.ts`
- `PUT /app_users/:param` | backend=`AppUserController.updateAppUser` | frontend_matches=1
  - `usersApi.updateAppUser` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/usersApi.ts`
- `GET /app_users/:param/admin-detail` | backend=`AppUserController.getAdminUserDetail` | frontend_matches=1
  - `adminApi.getAdminUserDetail` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/adminApi.ts`
- `GET /app_users/:param/profile-view` | backend=`AppUserController.getProfileView` | frontend_matches=1
  - `usersApi.getUserProfileView` in `apps/themuffinman/frontend/src/modules/workmarket/api/clients/usersApi.ts`
- ... 75 more endpoints
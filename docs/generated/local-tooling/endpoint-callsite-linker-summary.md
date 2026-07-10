# Endpoint Callsite Linker

- Generated at: `2026-07-10T08:21:25Z`
- Backend endpoints: `157`
- Frontend client methods: `11`
- Linked endpoints: `11`
- Unlinked endpoints: `146`

## Endpoint sample

- `POST /admin/agent/execute` | backend=`AdminAgentController.runExecution` | frontend_matches=0
  - frontend: _no direct client match detected_
- `GET /admin/applications` | backend=`QuestApplicationController.getAllApplicationsForAdmin` | frontend_matches=0
  - frontend: _no direct client match detected_
- `DELETE /admin/applications/:param` | backend=`QuestApplicationController.deleteApplicationForAdmin` | frontend_matches=0
  - frontend: _no direct client match detected_
- `PUT /admin/applications/:param` | backend=`QuestApplicationController.updateApplicationForAdmin` | frontend_matches=0
  - frontend: _no direct client match detected_
- `GET /app_users` | backend=`AppUserController.getAllAppUsers` | frontend_matches=0
  - frontend: _no direct client match detected_
- `POST /app_users` | backend=`AppUserController.createAppUser` | frontend_matches=0
  - frontend: _no direct client match detected_
- `DELETE /app_users/:param` | backend=`AppUserController.deleteAppUser` | frontend_matches=0
  - frontend: _no direct client match detected_
- `GET /app_users/:param` | backend=`AppUserController.getAppUser` | frontend_matches=0
  - frontend: _no direct client match detected_
- `PUT /app_users/:param` | backend=`AppUserController.updateAppUser` | frontend_matches=0
  - frontend: _no direct client match detected_
- `GET /app_users/:param/admin-detail` | backend=`AppUserController.getAdminUserDetail` | frontend_matches=0
  - frontend: _no direct client match detected_
- `GET /app_users/:param/profile-view` | backend=`AppUserController.getProfileView` | frontend_matches=0
  - frontend: _no direct client match detected_
- `GET /app_users/me` | backend=`AppUserController.getCurrentAppUser` | frontend_matches=0
  - frontend: _no direct client match detected_
- ... 145 more endpoints
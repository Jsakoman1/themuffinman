# Endpoint Callsite Linker

- Generated at: `2026-07-01T14:49:22Z`
- Backend endpoints: `97`
- Frontend client methods: `85`
- Linked endpoints: `79`
- Unlinked endpoints: `18`

## Endpoint sample

- `POST /admin/agent/execute` | backend=`AdminAgentController.runExecution` | frontend_matches=0
  - frontend: _no direct client match detected_
- `POST /admin/agent/playground` | backend=`AdminAgentController.runPlaygroundPrompt` | frontend_matches=0
  - frontend: _no direct client match detected_
- `POST /admin/agent/simulate` | backend=`AdminAgentController.runSimulation` | frontend_matches=0
  - frontend: _no direct client match detected_
- `GET /admin/applications` | backend=`QuestApplicationController.getAllApplicationsForAdmin` | frontend_matches=0
  - frontend: _no direct client match detected_
- `GET /app_users/:param/admin-detail` | backend=`AppUserController.getAdminUserDetail` | frontend_matches=0
  - frontend: _no direct client match detected_
- `GET /business/profiles` | backend=`BusinessProfileController.getDirectory` | frontend_matches=0
  - frontend: _no direct client match detected_
- `GET /business/profiles/:param` | backend=`BusinessProfileController.getProfileBySlug` | frontend_matches=0
  - frontend: _no direct client match detected_
- `GET /business/profiles/me` | backend=`BusinessProfileController.getMyProfile` | frontend_matches=0
  - frontend: _no direct client match detected_
- `PUT /business/profiles/me` | backend=`BusinessProfileController.saveMyProfile` | frontend_matches=0
  - frontend: _no direct client match detected_
- `DELETE /circles/admin/groups/:param` | backend=`CircleController.deleteCircleAsAdmin` | frontend_matches=0
  - frontend: _no direct client match detected_
- `GET /circles/admin/overview` | backend=`CircleController.getAdminOverview` | frontend_matches=0
  - frontend: _no direct client match detected_
- `GET /rides/offers` | backend=`RideOfferController.getVisibleOffers` | frontend_matches=0
  - frontend: _no direct client match detected_
- ... 85 more endpoints
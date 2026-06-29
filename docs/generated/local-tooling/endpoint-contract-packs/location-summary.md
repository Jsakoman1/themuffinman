# Endpoint Contract Pack location

- Generated At: `2026-06-29T19:55:04Z`
- Endpoint Family: `location`
## `endpoints`

- `{:method: "GET", :path: "/location/admin/status", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/location/controller/LocationLookupController.java", :dtos: ["LocationDebugStatusViewDTO"]}`
- `{:method: "POST", :path: "/location/lookup", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/location/controller/LocationLookupController.java", :dtos: ["LocationLookupResponseDTO", "LocationLookupRequestDTO", "LocationLookupCandidateDTO", "LocationReverseLookupRequestDTO", "LocationDebugStatusViewDTO"]}`
- `{:method: "POST", :path: "/location/reverse-lookup", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/location/controller/LocationLookupController.java", :dtos: ["LocationLookupResponseDTO", "LocationLookupRequestDTO", "LocationLookupCandidateDTO", "LocationReverseLookupRequestDTO", "LocationDebugStatusViewDTO"]}`

## `dto_files`

- `apps/themuffinman/src/main/java/com/themuffinman/app/location/dto/LocationDebugStatusViewDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/location/dto/LocationLookupCandidateDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/location/dto/LocationLookupRequestDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/location/dto/LocationLookupResponseDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/location/dto/LocationReverseLookupRequestDTO.java`

## `frontend_callsites`

- `{:file: "apps/themuffinman/frontend/src/App.vue", :matched_paths: ["status"]}`
- `{:file: "apps/themuffinman/frontend/src/api/httpClient.ts", :matched_paths: ["status"]}`
- `{:file: "apps/themuffinman/frontend/src/components/app/AppChatTray.vue", :matched_paths: ["status"]}`
- `{:file: "apps/themuffinman/frontend/src/components/profile/ProfileOpenQuestItem.vue", :matched_paths: ["status"]}`
- `{:file: "apps/themuffinman/frontend/src/components/ui/UiStatusBanner.vue", :matched_paths: ["status"]}`
- `{:file: "apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts", :matched_paths: ["status", "lookup"]}`
- `{:file: "apps/themuffinman/frontend/src/httpDebug.ts", :matched_paths: ["status"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/chat/views/ChatWorkspaceView.vue", :matched_paths: ["status"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/rides/views/RideSharingView.vue", :matched_paths: ["status"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/social/components/profile/UserProfileDialog.vue", :matched_paths: ["status"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/social/components/profile/UserSettingsDialog.vue", :matched_paths: ["status", "lookup"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/social/pages/AdminCirclesPage.vue", :matched_paths: ["status"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/things/views/ThingSharingView.vue", :matched_paths: ["status"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/workmarket/api/adminAgentContractGate.ts", :matched_paths: ["status"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/workmarket/api/adminAgentSafetyViewModel.ts", :matched_paths: ["lookup", "reverse-lookup"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/workmarket/api/clients/adminApi.ts", :matched_paths: ["/location/admin/status", "status"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/workmarket/api/clients/locationApi.ts", :matched_paths: ["/location/admin/status", "status", "/location/lookup", "lookup", "/location/reverse-lookup", "reverse-lookup"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/workmarket/api/clients/questsApi.ts", :matched_paths: ["status"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/workmarket/api/contracts.ts", :matched_paths: ["status"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/workmarket/components/admin/AdminUserDetailDialog.vue", :matched_paths: ["status", "lookup"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/workmarket/components/dashboard/DashboardAdmin.vue", :matched_paths: ["status"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/workmarket/components/dashboard/DashboardMyApplications.vue", :matched_paths: ["status"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/workmarket/components/dashboard/DashboardMyQuests.vue", :matched_paths: ["status"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/workmarket/components/dashboard/DashboardQuestApplications.vue", :matched_paths: ["status"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/workmarket/components/dashboard/DashboardQuestDialog.vue", :matched_paths: ["status"]}`

## `docs`

- `docs/business-logic.md`
- `docs/domain-technical.md`
- `docs/location-services.md`
- `docs/agent-operating-model.md`
- `docs/agent-operating-model.yaml`


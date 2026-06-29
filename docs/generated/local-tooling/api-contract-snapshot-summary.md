# API Contract Snapshot

- Generated At: `2026-06-29T19:55:22Z`
- Endpoint Count: `89`
## `endpoints`

- `{:method: "POST", :path: "/admin/agent/playground", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/controller/AdminAgentController.java", :dtos: ["AdminAgentPlaygroundResponseDTO", "AdminAgentPlaygroundRequestDTO", "AdminAgentSimulationResponseDTO", "AdminAgentSimulationRequestDTO"]}`
- `{:method: "POST", :path: "/admin/agent/simulate", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/controller/AdminAgentController.java", :dtos: ["AdminAgentPlaygroundResponseDTO", "AdminAgentPlaygroundRequestDTO", "AdminAgentSimulationResponseDTO", "AdminAgentSimulationRequestDTO"]}`
- `{:method: "GET", :path: "/admin/applications", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/QuestApplicationController.java", :dtos: ["QuestApplicationResponseDTO", "QuestApplicationsViewDTO"]}`
- `{:method: "DELETE", :path: "/admin/applications/{applicationId}", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/QuestApplicationController.java", :dtos: ["ActionResultDTO", "QuestApplicationRequestDTO"]}`
- `{:method: "PUT", :path: "/admin/applications/{applicationId}", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/QuestApplicationController.java", :dtos: ["ActionResultDTO", "AdminQuestApplicationUpdateRequestDTO"]}`
- `{:method: "GET", :path: "/app_users", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/identity/controller/AppUserController.java", :dtos: ["AppUserResponseDTO", "WorkmarketOptionsDTO"]}`
- `{:method: "POST", :path: "/app_users", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/identity/controller/AppUserController.java", :dtos: ["ActionResultDTO", "AppUserRequestDTO", "AppUserResponseDTO", "WorkmarketOptionsDTO"]}`
- `{:method: "GET", :path: "/app_users/me", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/identity/controller/AppUserController.java", :dtos: ["AppUserResponseDTO", "WorkmarketOptionsDTO"]}`
- `{:method: "PUT", :path: "/app_users/me", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/identity/controller/AppUserController.java", :dtos: ["ActionResultDTO", "AppUserRequestDTO", "AppUserResponseDTO"]}`
- `{:method: "GET", :path: "/app_users/options", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/identity/controller/AppUserController.java", :dtos: ["AppUserResponseDTO", "WorkmarketOptionsDTO"]}`
- `{:method: "DELETE", :path: "/app_users/{id}", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/identity/controller/AppUserController.java", :dtos: ["ActionResultDTO", "AppUserRequestDTO", "AppUserResponseDTO"]}`
- `{:method: "GET", :path: "/app_users/{id}", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/identity/controller/AppUserController.java", :dtos: ["AppUserResponseDTO", "WorkmarketOptionsDTO"]}`
- `{:method: "PUT", :path: "/app_users/{id}", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/identity/controller/AppUserController.java", :dtos: ["ActionResultDTO", "AppUserRequestDTO", "AppUserResponseDTO"]}`
- `{:method: "GET", :path: "/app_users/{id}/admin-detail", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/identity/controller/AppUserController.java", :dtos: ["AppUserResponseDTO", "WorkmarketOptionsDTO"]}`
- `{:method: "GET", :path: "/app_users/{id}/profile-view", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/identity/controller/AppUserController.java", :dtos: ["AppUserResponseDTO", "WorkmarketOptionsDTO"]}`
- `{:method: "GET", :path: "/applications/{applicationId}/detail", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/QuestApplicationController.java", :dtos: ["QuestApplicationResponseDTO", "QuestApplicationsViewDTO"]}`
- `{:method: "POST", :path: "/auth/login", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/identity/controller/AuthController.java", :dtos: ["AuthResponseDTO", "RegisterRequestDTO", "LoginRequestDTO"]}`
- `{:method: "GET", :path: "/auth/me", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/identity/controller/AuthController.java", :dtos: ["AuthResponseDTO"]}`
- `{:method: "POST", :path: "/auth/register", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/identity/controller/AuthController.java", :dtos: ["AuthResponseDTO", "RegisterRequestDTO", "LoginRequestDTO"]}`
- `{:method: "GET", :path: "/business/profiles", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/business/controller/BusinessProfileController.java", :dtos: ["BusinessProfileListResponseDTO", "BusinessProfileResponseDTO", "BusinessProfileRequestDTO"]}`
- `{:method: "GET", :path: "/business/profiles/me", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/business/controller/BusinessProfileController.java", :dtos: ["BusinessProfileListResponseDTO", "BusinessProfileResponseDTO", "BusinessProfileRequestDTO"]}`
- `{:method: "PUT", :path: "/business/profiles/me", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/business/controller/BusinessProfileController.java", :dtos: ["BusinessProfileResponseDTO", "BusinessProfileRequestDTO"]}`
- `{:method: "GET", :path: "/business/profiles/{slug}", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/business/controller/BusinessProfileController.java", :dtos: ["BusinessProfileListResponseDTO", "BusinessProfileResponseDTO", "BusinessProfileRequestDTO"]}`
- `{:method: "POST", :path: "/chat/conversations/open", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/chat/controller/ChatController.java", :dtos: ["ChatConversationSummaryDTO", "ChatOpenConversationRequestDTO", "ChatMessageDTO", "ChatMessageRequestDTO"]}`
- `{:method: "GET", :path: "/chat/conversations/{conversationId}/messages", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/chat/controller/ChatController.java", :dtos: ["ChatWorkspaceDTO", "ChatConversationSummaryDTO", "ChatOpenConversationRequestDTO", "ChatMessageDTO"]}`


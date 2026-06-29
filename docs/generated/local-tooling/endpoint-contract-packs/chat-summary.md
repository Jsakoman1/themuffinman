# Endpoint Contract Pack chat

- Generated At: `2026-06-29T12:47:29Z`
- Endpoint Family: `chat`
## `endpoints`

- `{:method: "POST", :path: "/chat/conversations/open", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/chat/controller/ChatController.java", :dtos: ["ChatConversationSummaryDTO", "ChatOpenConversationRequestDTO", "ChatMessageDTO", "ChatMessageRequestDTO"]}`
- `{:method: "GET", :path: "/chat/conversations/{conversationId}/messages", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/chat/controller/ChatController.java", :dtos: ["ChatWorkspaceDTO", "ChatConversationSummaryDTO", "ChatOpenConversationRequestDTO", "ChatMessageDTO"]}`
- `{:method: "POST", :path: "/chat/conversations/{conversationId}/messages", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/chat/controller/ChatController.java", :dtos: ["ChatConversationSummaryDTO", "ChatOpenConversationRequestDTO", "ChatMessageDTO", "ChatMessageRequestDTO"]}`
- `{:method: "PATCH", :path: "/chat/conversations/{conversationId}/read", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/chat/controller/ChatController.java", :dtos: ["ActionResultDTO"]}`
- `{:method: "POST", :path: "/chat/presence/heartbeat", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/chat/controller/ChatController.java", :dtos: ["ChatConversationSummaryDTO", "ChatOpenConversationRequestDTO", "ChatMessageDTO", "ChatMessageRequestDTO"]}`
- `{:method: "GET", :path: "/chat/workspace", :controller: "apps/themuffinman/src/main/java/com/themuffinman/app/chat/controller/ChatController.java", :dtos: ["ChatWorkspaceDTO", "ChatConversationSummaryDTO", "ChatOpenConversationRequestDTO", "ChatMessageDTO"]}`

## `dto_files`

- `apps/themuffinman/src/main/java/com/themuffinman/app/chat/dto/ChatConversationSummaryDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/chat/dto/ChatMessageDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/chat/dto/ChatMessageRequestDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/chat/dto/ChatOpenConversationRequestDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/chat/dto/ChatWorkspaceDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/common/dto/ActionResultDTO.java`

## `frontend_callsites`

- `{:file: "apps/themuffinman/frontend/src/components/app/AppChatTray.vue", :matched_paths: ["open", "messages", "read", "workspace"]}`
- `{:file: "apps/themuffinman/frontend/src/components/app/AppNotificationsPanel.vue", :matched_paths: ["open", "read"]}`
- `{:file: "apps/themuffinman/frontend/src/components/app/AppTopbar.vue", :matched_paths: ["open", "read"]}`
- `{:file: "apps/themuffinman/frontend/src/components/app/topbar/useTopbarNotifications.ts", :matched_paths: ["open", "read"]}`
- `{:file: "apps/themuffinman/frontend/src/components/app/useAppTopbarState.ts", :matched_paths: ["open", "read"]}`
- `{:file: "apps/themuffinman/frontend/src/components/editor/RichTextEditor.vue", :matched_paths: ["open"]}`
- `{:file: "apps/themuffinman/frontend/src/components/profile/ProfileEntityCard.vue", :matched_paths: ["open"]}`
- `{:file: "apps/themuffinman/frontend/src/components/profile/ProfileOpenQuestItem.vue", :matched_paths: ["open"]}`
- `{:file: "apps/themuffinman/frontend/src/components/ui/UiConfirmDialog.vue", :matched_paths: ["open"]}`
- `{:file: "apps/themuffinman/frontend/src/components/ui/UiDialog.vue", :matched_paths: ["open"]}`
- `{:file: "apps/themuffinman/frontend/src/components/ui/UiReadonlyField.vue", :matched_paths: ["read"]}`
- `{:file: "apps/themuffinman/frontend/src/components/ui/UiWorkspace.vue", :matched_paths: ["workspace"]}`
- `{:file: "apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts", :matched_paths: ["open", "messages", "read", "heartbeat", "workspace"]}`
- `{:file: "apps/themuffinman/frontend/src/env.d.ts", :matched_paths: ["read"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/chat/api/chatApi.ts", :matched_paths: ["/chat/conversations/open", "open", "messages", "read", "/chat/presence/heartbeat", "heartbeat", "/chat/workspace", "workspace"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/chat/composables/useAppChat.ts", :matched_paths: ["open", "messages", "read", "heartbeat", "workspace"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/chat/views/ChatWorkspaceView.vue", :matched_paths: ["read", "workspace"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/identity/views/RegisterView.vue", :matched_paths: ["read"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/social/components/circles/CircleCandidateCard.vue", :matched_paths: ["open"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/social/components/circles/CirclesConnectionsPanel.vue", :matched_paths: ["open"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/social/components/circles/CirclesInboxPanel.vue", :matched_paths: ["open"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/social/components/profile/UserProfileDialog.vue", :matched_paths: ["open"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/social/components/profile/UserSettingsDialog.vue", :matched_paths: ["open"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/social/views/CirclesView.vue", :matched_paths: ["open"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/social/views/UserProfileView.vue", :matched_paths: ["open"]}`

## `docs`

- `docs/business-logic.md`
- `docs/domain-technical.md`
- `docs/agent-operating-model.md`
- `docs/agent-operating-model.yaml`


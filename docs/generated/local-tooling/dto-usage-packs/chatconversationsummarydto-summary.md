# DTO Usage Pack ChatConversationSummaryDTO

- Next action: `cd apps/themuffinman && ./mvnw test -Dtest=VisionChatExecutionServiceTest,VisionConversationServiceTest`, `make audit-api-contract-drift`, `make endpoint-contract-packs`

## Details

- Recommended commands: make audit-api-contract-drift | make endpoint-contract-packs
- Recommended commands more: 3
- Backend refs: path: apps/themuffinman/src/main/java/com/themuffinman/app/chat/controller/ChatController.java | category: backend_controller | domain: chat | lines: 107 | path: apps/themuffinman/src/main/java/com/themuffinman/app/chat/dto/ChatConversationSummaryDTO.java | category: backend_dto | domain: chat | lines: 37 | path: apps/themuffinman/src/main/java/com/themuffinman/app/chat/dto/ChatSocketEventDTO.java | category: backend_dto | domain: chat | lines: 37
- Backend refs more: 8
- Controller methods: method: POST | path: /chat/conversations/open | controller: apps/themuffinman/src/main/java/com/themuffinman/app/chat/controller/ChatController.java | dtos: ChatConversationSummaryDTO, ChatOpenConversationRequestDTO, ChatMessagePageDTO | method: GET | path: /chat/conversations/{conversationId}/messages | controller: apps/themuffinman/src/main/java/com/themuffinman/app/chat/controller/ChatController.java | dtos: ChatWorkspaceDTO, ChatConversationSummaryDTO, ChatOpenConversationRequestDTO | method: POST | path: /chat/conversations/{conversationId}/messages | controller: apps/themuffinman/src/main/java/com/themuffinman/app/chat/controller/ChatController.java | dtos: ChatConversationSummaryDTO, ChatOpenConversationRequestDTO, ChatMessagePageDTO
- Controller methods more: 2
- Docs refs: docs/agent-operating-model.yaml | docs/agent-operating-model/sections/automation_read_models.yaml | docs/agent-operating-model/sections/backend_contract_snapshots.yaml
- Docs refs more: 7
- Frontend refs: path: apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts | category: frontend_contract | domain: shared | lines: 1768
- Generated contract refs: apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts | docs/generated/automation-read-model-inventory.json | docs/generated/backend-audit-inventory.json
- Generated contract refs more: 17

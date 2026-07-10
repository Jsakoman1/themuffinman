# DTO Usage Pack ChatConversationListDTO

- Next action: `cd apps/themuffinman && ./mvnw test -Dtest=ChatControllerTest,ChatServiceTest`, `make audit-api-contract-drift`, `make endpoint-contract-packs`

## Details

- Recommended commands: make audit-api-contract-drift | make endpoint-contract-packs
- Recommended commands more: 3
- Backend refs: path: apps/themuffinman/src/main/java/com/themuffinman/app/chat/controller/ChatController.java | category: backend_controller | domain: chat | lines: 318 | path: apps/themuffinman/src/main/java/com/themuffinman/app/chat/dto/ChatConversationListDTO.java | category: backend_dto | domain: chat | lines: 34 | path: apps/themuffinman/src/main/java/com/themuffinman/app/chat/service/ChatService.java | category: backend_service | domain: chat | lines: 2084
- Controller methods: method: GET | path: /chat/applications/{applicationId}/thread | controller: apps/themuffinman/src/main/java/com/themuffinman/app/chat/controller/ChatController.java | dtos: ChatWorkspaceDTO, ChatConversationListDTO | method: GET | path: /chat/attachments/object | controller: apps/themuffinman/src/main/java/com/themuffinman/app/chat/controller/ChatController.java | dtos: ChatWorkspaceDTO, ChatConversationListDTO | method: GET | path: /chat/attachments/storage-status | controller: apps/themuffinman/src/main/java/com/themuffinman/app/chat/controller/ChatController.java | dtos: ChatWorkspaceDTO, ChatConversationListDTO
- Controller methods more: 6
- Docs refs: docs/agent-operating-model.yaml | docs/agent-operating-model/sections/backend_contract_snapshots.yaml | docs/agent-operating-model/sections/source_of_truth.yaml
- Docs refs more: 6
- Frontend refs: path: apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts | category: frontend_contract | domain: shared | lines: 2338
- Generated contract refs: apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts | docs/generated/automation-read-model-inventory.json | docs/generated/backend-audit-inventory.json
- Generated contract refs more: 17

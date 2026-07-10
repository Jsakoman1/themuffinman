# DTO Usage Pack VisionConversationTurnRequestDTO

- Next action: `cd apps/themuffinman && ./mvnw test -Dtest=VisionConversationServiceTest`, `make audit-api-contract-drift`, `make endpoint-contract-packs`

## Details

- Recommended commands: make audit-api-contract-drift | make endpoint-contract-packs
- Recommended commands more: 3
- Backend refs: path: apps/themuffinman/src/main/java/com/themuffinman/app/vision/controller/VisionConversationController.java | category: backend_controller | domain: shared | lines: 67 | path: apps/themuffinman/src/main/java/com/themuffinman/app/vision/dto/VisionConversationTurnRequestDTO.java | category: backend_dto | domain: shared | lines: 54 | path: apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionConversationService.java | category: backend_service | domain: shared | lines: 3317
- Backend refs more: 1
- Controller methods: method: POST | path: /vision/conversations/turns | controller: apps/themuffinman/src/main/java/com/themuffinman/app/vision/controller/VisionConversationController.java | dtos: VisionConversationTurnResponseDTO, VisionConversationTurnRequestDTO | method: POST | path: /vision/conversations/{conversationId}/cancel | controller: apps/themuffinman/src/main/java/com/themuffinman/app/vision/controller/VisionConversationController.java | dtos: VisionConversationTurnResponseDTO, VisionConversationTurnRequestDTO | method: POST | path: /vision/conversations/{conversationId}/reset | controller: apps/themuffinman/src/main/java/com/themuffinman/app/vision/controller/VisionConversationController.java | dtos: VisionConversationTurnResponseDTO, VisionConversationTurnRequestDTO
- Docs refs: docs/agent-operating-model.yaml | docs/agent-operating-model/sections/source_of_truth.yaml | docs/domain-technical.md
- Docs refs more: 5
- Frontend refs: path: apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts | category: frontend_contract | domain: shared | lines: 2336
- Generated contract refs: apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts | docs/generated/automation-read-model-inventory.json | docs/generated/backend-audit-inventory.json
- Generated contract refs more: 17

# DTO Usage Pack VisionConversationTurnResponseDTO

- Next action: `cd apps/themuffinman && ./mvnw test -Dtest=VisionConversationServiceTest`, `make audit-api-contract-drift`, `make endpoint-contract-packs`

## Details

- Recommended commands: make audit-api-contract-drift | make endpoint-contract-packs
- Recommended commands more: 3
- Backend refs: path: apps/themuffinman/src/main/java/com/themuffinman/app/vision/controller/VisionConversationController.java | category: backend_controller | domain: shared | lines: 63 | path: apps/themuffinman/src/main/java/com/themuffinman/app/vision/dto/VisionConversationTurnResponseDTO.java | category: backend_dto | domain: shared | lines: 41 | path: apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionCanvasAssembler.java | category: backend_service | domain: shared | lines: 752
- Backend refs more: 1
- Controller methods: method: GET | path: /vision/conversations/recent | controller: apps/themuffinman/src/main/java/com/themuffinman/app/vision/controller/VisionConversationController.java | dtos: VisionConversationListResponseDTO, VisionConversationTurnResponseDTO | method: POST | path: /vision/conversations/turns | controller: apps/themuffinman/src/main/java/com/themuffinman/app/vision/controller/VisionConversationController.java | dtos: VisionConversationTurnResponseDTO, VisionConversationTurnRequestDTO | method: GET | path: /vision/conversations/{conversationId} | controller: apps/themuffinman/src/main/java/com/themuffinman/app/vision/controller/VisionConversationController.java | dtos: VisionConversationListResponseDTO, VisionConversationTurnResponseDTO
- Controller methods more: 2
- Docs refs: docs/agent-operating-model.yaml | docs/agent-operating-model/sections/source_of_truth.yaml | docs/domain-technical.md
- Docs refs more: 5
- Frontend refs: path: apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts | category: frontend_contract | domain: shared | lines: 1666
- Generated contract refs: apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts | docs/generated/automation-read-model-inventory.json | docs/generated/backend-audit-inventory.json
- Generated contract refs more: 17

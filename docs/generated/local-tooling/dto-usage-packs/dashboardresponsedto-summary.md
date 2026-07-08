# DTO Usage Pack DashboardResponseDTO

- Next action: `make audit-api-contract-drift`, `make endpoint-contract-packs`, `npm --prefix apps/themuffinman/frontend run type-check`

## Details

- Recommended commands: make audit-api-contract-drift | make endpoint-contract-packs
- Recommended commands more: 2
- Backend refs: path: apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/DashboardController.java | category: backend_controller | domain: workmarket | lines: 73 | path: apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/DashboardResponseDTO.java | category: backend_dto | domain: workmarket | lines: 30 | path: apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/WorkmarketDashboardReadService.java | category: backend_service | domain: workmarket | lines: 234
- Controller methods: method: GET | path: /dashboard/me | controller: apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/DashboardController.java | dtos: DashboardResponseDTO, DashboardSummaryDTO, DashboardVoiceConfigDTO | method: GET | path: /dashboard/me/summary | controller: apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/DashboardController.java | dtos: DashboardResponseDTO, DashboardSummaryDTO, DashboardVoiceConfigDTO | method: GET | path: /dashboard/me/voice-config | controller: apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/DashboardController.java | dtos: DashboardResponseDTO, DashboardSummaryDTO, DashboardVoiceConfigDTO
- Docs refs: docs/agent-operating-model.yaml | docs/agent-operating-model/sections/backend_audit_coverage.yaml | docs/agent-operating-model/sections/source_of_truth.yaml
- Docs refs more: 5
- Frontend refs: path: apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts | category: frontend_contract | domain: shared | lines: 1740
- Generated contract refs: apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts | docs/generated/automation-read-model-inventory.json | docs/generated/backend-audit-inventory.json
- Generated contract refs more: 17

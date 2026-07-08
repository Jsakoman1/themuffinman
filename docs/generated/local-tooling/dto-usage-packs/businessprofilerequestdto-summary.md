# DTO Usage Pack BusinessProfileRequestDTO

- Next action: `cd apps/themuffinman && ./mvnw test -Dtest=BusinessProfileServiceTest`, `make audit-api-contract-drift`, `make endpoint-contract-packs`

## Details

- Recommended commands: make audit-api-contract-drift | make endpoint-contract-packs
- Recommended commands more: 3
- Backend refs: path: apps/themuffinman/src/main/java/com/themuffinman/app/business/controller/BusinessProfileController.java | category: backend_controller | domain: business | lines: 47 | path: apps/themuffinman/src/main/java/com/themuffinman/app/business/dto/BusinessProfileRequestDTO.java | category: backend_dto | domain: business | lines: 63 | path: apps/themuffinman/src/main/java/com/themuffinman/app/business/service/BusinessProfileService.java | category: backend_service | domain: business | lines: 128
- Controller methods: method: GET | path: /business/profiles | controller: apps/themuffinman/src/main/java/com/themuffinman/app/business/controller/BusinessProfileController.java | dtos: BusinessProfileListResponseDTO, BusinessProfileResponseDTO, BusinessProfileRequestDTO | method: GET | path: /business/profiles/me | controller: apps/themuffinman/src/main/java/com/themuffinman/app/business/controller/BusinessProfileController.java | dtos: BusinessProfileListResponseDTO, BusinessProfileResponseDTO, BusinessProfileRequestDTO | method: PUT | path: /business/profiles/me | controller: apps/themuffinman/src/main/java/com/themuffinman/app/business/controller/BusinessProfileController.java | dtos: BusinessProfileResponseDTO, BusinessProfileRequestDTO
- Controller methods more: 1
- Docs refs: docs/agent-operating-model.yaml | docs/agent-operating-model/sections/source_of_truth.yaml | docs/domain-technical.md
- Docs refs more: 3
- Frontend refs: path: apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts | category: frontend_contract | domain: shared | lines: 1935
- Generated contract refs: apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts | docs/generated/automation-read-model-inventory.json | docs/generated/backend-audit-inventory.json
- Generated contract refs more: 7

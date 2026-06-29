# Backend Dependency Graph

- Generated At: `2026-06-29T19:55:19Z`
- Node Count: `319`
- Edge Count: `978`
## `nodes`

- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/TheMuffinManApplication.java", :class_name: "TheMuffinManApplication", :category: "other", :domain: "shared"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/controller/AdminAgentController.java", :class_name: "AdminAgentController", :category: "backend_controller", :domain: "agent"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/dto/AdminAgentPlaygroundRequestDTO.java", :class_name: "AdminAgentPlaygroundRequestDTO", :category: "backend_dto", :domain: "agent"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/dto/AdminAgentPlaygroundResponseDTO.java", :class_name: "AdminAgentPlaygroundResponseDTO", :category: "backend_dto", :domain: "agent"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/dto/AdminAgentSimulationRequestDTO.java", :class_name: "AdminAgentSimulationRequestDTO", :category: "backend_dto", :domain: "agent"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/dto/AdminAgentSimulationResponseDTO.java", :class_name: "AdminAgentSimulationResponseDTO", :category: "backend_dto", :domain: "agent"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/dto/AgentCapabilityAssessmentDTO.java", :class_name: "AgentCapabilityAssessmentDTO", :category: "backend_dto", :domain: "agent"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/dto/AgentClarificationContractDTO.java", :class_name: "AgentClarificationContractDTO", :category: "backend_dto", :domain: "agent"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/dto/AgentEndpointPlanDTO.java", :class_name: "AgentEndpointPlanDTO", :category: "backend_dto", :domain: "agent"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/dto/AgentExecutionReadinessDTO.java", :class_name: "AgentExecutionReadinessDTO", :category: "backend_dto", :domain: "agent"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/dto/AgentIntentLineageDTO.java", :class_name: "AgentIntentLineageDTO", :category: "backend_dto", :domain: "agent"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/dto/AgentResolutionConfidenceDTO.java", :class_name: "AgentResolutionConfidenceDTO", :category: "backend_dto", :domain: "agent"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/dto/AgentResolutionRequirementDTO.java", :class_name: "AgentResolutionRequirementDTO", :category: "backend_dto", :domain: "agent"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/sandbox/SandboxGenerationPlan.java", :class_name: "SandboxGenerationPlan", :category: "other", :domain: "agent"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/sandbox/SandboxGenerationPlanner.java", :class_name: "SandboxGenerationPlanner", :category: "other", :domain: "agent"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/service/AdminAgentPlaygroundService.java", :class_name: "AdminAgentPlaygroundService", :category: "backend_service", :domain: "agent"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/service/AdminAgentPromptTranslation.java", :class_name: "AdminAgentPromptTranslation", :category: "backend_service", :domain: "agent"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/service/AdminAgentPromptTranslator.java", :class_name: "AdminAgentPromptTranslator", :category: "backend_service", :domain: "agent"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/service/AdminAgentTextProvider.java", :class_name: "AdminAgentTextProvider", :category: "backend_service", :domain: "agent"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/service/LocalAdminAgentPromptTranslator.java", :class_name: "LocalAdminAgentPromptTranslator", :category: "backend_service", :domain: "agent"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/service/OpenAiAdminAgentClient.java", :class_name: "OpenAiAdminAgentClient", :category: "backend_service", :domain: "agent"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/business/controller/BusinessProfileController.java", :class_name: "BusinessProfileController", :category: "backend_controller", :domain: "business"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/business/dto/BusinessProfileListResponseDTO.java", :class_name: "BusinessProfileListResponseDTO", :category: "backend_dto", :domain: "business"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/business/dto/BusinessProfileRequestDTO.java", :class_name: "BusinessProfileRequestDTO", :category: "backend_dto", :domain: "business"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/business/dto/BusinessProfileResponseDTO.java", :class_name: "BusinessProfileResponseDTO", :category: "backend_dto", :domain: "business"}`

## `edges`

- `{:from: "AdminAgentController", :to: "AdminAgentPlaygroundService", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/controller/AdminAgentController.java"}`
- `{:from: "AdminAgentController", :to: "AdminAgentPlaygroundRequestDTO", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/controller/AdminAgentController.java"}`
- `{:from: "AdminAgentController", :to: "AdminAgentPlaygroundResponseDTO", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/controller/AdminAgentController.java"}`
- `{:from: "AdminAgentController", :to: "AdminAgentSimulationRequestDTO", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/controller/AdminAgentController.java"}`
- `{:from: "AdminAgentController", :to: "AdminAgentSimulationResponseDTO", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/controller/AdminAgentController.java"}`
- `{:from: "AdminAgentPlaygroundRequestDTO", :to: "AdminAgentPlaygroundRequestDTO", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/dto/AdminAgentPlaygroundRequestDTO.java"}`
- `{:from: "AdminAgentPlaygroundResponseDTO", :to: "AdminAgentPlaygroundResponseDTO", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/dto/AdminAgentPlaygroundResponseDTO.java"}`
- `{:from: "AdminAgentPlaygroundResponseDTO", :to: "AgentResolutionRequirementDTO", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/dto/AdminAgentPlaygroundResponseDTO.java"}`
- `{:from: "AdminAgentPlaygroundResponseDTO", :to: "AgentClarificationContractDTO", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/dto/AdminAgentPlaygroundResponseDTO.java"}`
- `{:from: "AdminAgentPlaygroundResponseDTO", :to: "AgentExecutionReadinessDTO", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/dto/AdminAgentPlaygroundResponseDTO.java"}`
- `{:from: "AdminAgentSimulationRequestDTO", :to: "AdminAgentSimulationRequestDTO", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/dto/AdminAgentSimulationRequestDTO.java"}`
- `{:from: "AdminAgentSimulationResponseDTO", :to: "AdminAgentSimulationResponseDTO", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/dto/AdminAgentSimulationResponseDTO.java"}`
- `{:from: "AdminAgentSimulationResponseDTO", :to: "AgentResolutionConfidenceDTO", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/dto/AdminAgentSimulationResponseDTO.java"}`
- `{:from: "AdminAgentSimulationResponseDTO", :to: "AgentCapabilityAssessmentDTO", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/dto/AdminAgentSimulationResponseDTO.java"}`
- `{:from: "AdminAgentSimulationResponseDTO", :to: "AgentIntentLineageDTO", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/dto/AdminAgentSimulationResponseDTO.java"}`
- `{:from: "AdminAgentSimulationResponseDTO", :to: "AgentEndpointPlanDTO", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/dto/AdminAgentSimulationResponseDTO.java"}`
- `{:from: "AdminAgentSimulationResponseDTO", :to: "AgentClarificationContractDTO", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/dto/AdminAgentSimulationResponseDTO.java"}`
- `{:from: "AdminAgentSimulationResponseDTO", :to: "AgentExecutionReadinessDTO", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/dto/AdminAgentSimulationResponseDTO.java"}`
- `{:from: "AgentCapabilityAssessmentDTO", :to: "AgentCapabilityAssessmentDTO", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/dto/AgentCapabilityAssessmentDTO.java"}`
- `{:from: "AgentClarificationContractDTO", :to: "AgentClarificationContractDTO", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/dto/AgentClarificationContractDTO.java"}`
- `{:from: "AgentEndpointPlanDTO", :to: "AgentEndpointPlanDTO", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/dto/AgentEndpointPlanDTO.java"}`
- `{:from: "AgentExecutionReadinessDTO", :to: "AgentExecutionReadinessDTO", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/dto/AgentExecutionReadinessDTO.java"}`
- `{:from: "AgentIntentLineageDTO", :to: "AgentIntentLineageDTO", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/dto/AgentIntentLineageDTO.java"}`
- `{:from: "AgentResolutionConfidenceDTO", :to: "AgentResolutionConfidenceDTO", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/dto/AgentResolutionConfidenceDTO.java"}`
- `{:from: "AgentResolutionRequirementDTO", :to: "AgentResolutionRequirementDTO", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/dto/AgentResolutionRequirementDTO.java"}`


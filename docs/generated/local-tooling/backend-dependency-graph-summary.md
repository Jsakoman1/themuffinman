# Backend Dependency Graph

- Generated At: `2026-06-28T21:02:37Z`
- Node Count: `255`
- Edge Count: `780`
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
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/service/AdminAgentPlaygroundService.java", :class_name: "AdminAgentPlaygroundService", :category: "backend_service", :domain: "agent"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/service/AdminAgentPromptTranslation.java", :class_name: "AdminAgentPromptTranslation", :category: "backend_service", :domain: "agent"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/service/AdminAgentPromptTranslator.java", :class_name: "AdminAgentPromptTranslator", :category: "backend_service", :domain: "agent"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/service/AdminAgentTextProvider.java", :class_name: "AdminAgentTextProvider", :category: "backend_service", :domain: "agent"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/service/LocalAdminAgentPromptTranslator.java", :class_name: "LocalAdminAgentPromptTranslator", :category: "backend_service", :domain: "agent"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/service/OpenAiAdminAgentClient.java", :class_name: "OpenAiAdminAgentClient", :category: "backend_service", :domain: "agent"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/chat/controller/ChatController.java", :class_name: "ChatController", :category: "backend_controller", :domain: "chat"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/chat/dto/ChatCircleOptionDTO.java", :class_name: "ChatCircleOptionDTO", :category: "backend_dto", :domain: "chat"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/chat/dto/ChatContactDTO.java", :class_name: "ChatContactDTO", :category: "backend_dto", :domain: "chat"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/chat/dto/ChatConversationSummaryDTO.java", :class_name: "ChatConversationSummaryDTO", :category: "backend_dto", :domain: "chat"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/chat/dto/ChatMessageDTO.java", :class_name: "ChatMessageDTO", :category: "backend_dto", :domain: "chat"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/chat/dto/ChatMessageRequestDTO.java", :class_name: "ChatMessageRequestDTO", :category: "backend_dto", :domain: "chat"}`

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


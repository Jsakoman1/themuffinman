# API Contract Drift Cleanup Shortlist

Generated from the current `api-contract-drift` audit as a review-first cleanup list. These items are not auto-delete candidates; they are the smallest manual-review set worth checking first.

## High-Signal UI Cleanup Review

- `QuestApplicationPresentationDTO.questStatusBadgeClass`
  Reason:
  - UI-facing presentation field with zero observed frontend references.

- `QuestPresentationDTO.executionHelperText`
- `QuestPresentationDTO.primaryExecutionAction`
- `QuestPresentationDTO.primaryExecutionActionLabel`
  Reason:
  - Presentation-only quest execution helpers with zero observed frontend references.

- `DashboardNotificationItemDTO.destinationId`
- `DashboardNotificationItemDTO.destinationType`
- `DashboardNotificationItemDTO.unread`
  Reason:
  - Dashboard notification navigation fields currently appear unused by the frontend and may indicate dead response shape or missing UI wiring.

- `ChatMessageDTO.senderAvatarDataUrl`
- `ChatMessageDTO.senderUserId`
- `ChatMessageDTO.senderUsername`
  Reason:
  - Repeated chat sender fields with zero observed frontend references; verify whether the chat UI now relies only on grouped conversation context.

- `AppUserResponseDTO.profileNavigation`
  Reason:
  - Navigation helper field with zero observed frontend references; confirm whether profile navigation is now derived elsewhere.

## Review Before Deleting

- `QuestApplicationResponseDTO.allowedActions`
- `QuestApplicationResponseDTO.questStatus`
- `QuestApplicationResponseDTO.questCreatorUsername`
  Reason:
  - These may still matter for future self-service application actions, admin surfaces, or automation-facing read models even if the current frontend does not reference them directly.

- `CircleRelationDTO.blockedByCurrentUser`
- `CircleRelationDTO.relationStatus`
- `CircleSearchResultDTO.blockedByCurrentUser`
- `CircleSearchResultDTO.relationStatus`
  Reason:
  - Relation-state fields are domain-significant and may be consumed indirectly through helper transforms or pending UI paths.

- `CircleRequestResponseDTO.*ProfileDescription`
- `CircleRequestResponseDTO.*ProfileAvatarDataUrl`
  Reason:
  - These are plausible future preview fields and should only be removed after checking pending dialog/detail work.

## Likely Intentional Non-Frontend Payload

- `AdminAgentPlaygroundResponseDTO.*`
- `AdminAgentSimulationResponseDTO.translatedPrompt`
- `AgentClarificationContractDTO.*`
- `AgentExecutionReadinessDTO.requiresExternalTranslationProvider`
- `AgentIntentLineageDTO.*`
- `AgentResolutionConfidenceDTO.reasons`
- `AgentResolutionRequirementDTO.*`
- `LocationDebugStatusDTO.lookupCacheEntries`
- `LocationDebugStatusDTO.rateLimitedRequests`
- `LocationDebugStatusDTO.reverseLookupCacheEntries`
  Reason:
  - These fields are likely retained for admin diagnostics, planner workflows, or future automation visibility rather than current end-user rendering.

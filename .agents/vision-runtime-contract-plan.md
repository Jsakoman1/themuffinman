---
machine_kind: plan
machine_status: complete
machine_title: Vision Runtime Contract Plan
machine_goal: Define the backend-owned runtime contract for mobile/audio-first Vision
  interaction.
---

# Vision Runtime Contract Plan

## Status

Complete.

## Goal

Define the backend-owned runtime contract for mobile/audio-first Vision interaction.

## Workflow Frame

- Feature tier: tier3-high-risk-multi-layer
- Scope: turn response contract, mode state, audio/haptic cues, mobile role hints
- Out of scope: visual polish and task-specific flow redesign
- Manifest decision: required when implementation starts
- Manifest path: TBD
- Master plan: `.agents/vision-next-experience-master-plan.md`

## Implementation Slices

- [x] Slice 1: define the runtime state model for iPhone and Watch.
- [x] Slice 2: add backend response fields for audio summaries, haptic hints, and attention states.
- [x] Slice 3: keep the contract explicit for mode switching, session anchors, and action hints.

## Validation Plan

- Targeted checks: contract and DTO tests
- Broader checks: conversation service tests and preview serialization tests
- Skipped checks or reasons: none

## Docs and Artifacts

- Expected docs: `docs/product-vision.md`, `docs/vision-architecture-patterns.md`, `docs/vision-status-ledger.md`, `docs/domain-technical.md`
- Expected generated artifacts: generated frontend contract, agent-operating slices if machine-operational contract wording changes
- Temporary work products: runtime state sketch and field map under `.agents/tmp/`

## Closeout Gates

- Required closeout checks: the contract can describe attention state, audio state, haptics, and mode without frontend inference
- Final response evidence: iPhone and Watch can render the same backend-owned state shape in different UI densities
- Backlog follow-up rule: any unresolved state-field gaps become persistent backlog items before closeout

## Open Questions

- Resolver outputs still needed: exact field names for haptic and audio cues
- Risks or approvals: none yet

## Completion Evidence

- Status: complete
- Changed files:
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/dto/VisionAttentionStateDTO.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/dto/VisionDeviceRoleDTO.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/dto/VisionRuntimeContextDTO.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/dto/VisionRuntimeCueDTO.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/dto/VisionConversationTurnRequestDTO.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/dto/VisionConversationTurnResponseDTO.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionCanvasAssembler.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionConversationService.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionSemanticOrchestrationContextService.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionSemanticOrchestrationRequest.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionSemanticPromptPayloadBuilder.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionSemanticRuntimeHints.java`
  - `apps/themuffinman/frontend/src/modules/vision/api/visionConversationApi.ts`
  - `apps/themuffinman/frontend/src/modules/vision/composables/useVisionConversation.ts`
  - `apps/themuffinman/frontend/src/modules/vision/components/VisionCanvasRenderer.vue`
  - `apps/themuffinman/frontend/src/modules/vision/views/VisionSurfaceModernView.vue`
- Validation evidence:
  - `./mvnw test -Dtest=VisionCanvasAssemblerTest,VisionConversationServiceTest`
  - `npm run type-check`
  - `npm run build`
  - `make audit-generated-artifact-freshness`
- Doc delta summary:
  - `docs/product-vision.md`
  - `docs/domain-technical.md`
  - `docs/vision-architecture-patterns.md`
  - `docs/vision-status-ledger.md`
- Backlog update: none required for the runtime contract slice
- Residual risk: deeper audio playback semantics and broader watch-native rendering can still be refined in the later audio/haptic phase

# Vision Context Gateway

This file is the shortest stable map of where `/vision` work lives.

Read it before broad repository search when the task changes the vision backend, API contract, frontend canvas, or supporting docs.

## Read Order

1. `AGENTS.md`
2. `docs/codex-fast-path.md`
3. `docs/vision-architecture-patterns.md`
4. this file
5. `docs/vision-decision-record.md`
6. `docs/implementation-control.md` and the owning `docs/work/*.yaml` plan for implementation and validation
9. `docs/vision-status-ledger.md` for current delivery state and intentional deferrals

## Vision Backend Map

Primary package:

- `apps/themuffinman/src/main/java/com/themuffinman/app/vision/`

Key surfaces:

- `controller/VisionConversationController.java`: HTTP entrypoints for conversation turns and lifecycle actions
- `service/VisionConversationService.java`: top-level orchestration facade
- `service/VisionConversationLifecycleService.java`: reset, cancel, load, and recent conversation lifecycle boundary
- `service/VisionClarificationService.java`: deterministic next-question selection
- `service/VisionSlotService.java`: slot extraction, normalization, and merge behavior
- `service/VisionSemanticOrchestrationContextService.java`: user, session, runtime, and memory pack assembly for semantic understanding
- `service/VisionScheduleParserService.java`: deterministic spoken and typed schedule parsing
- `service/VisionLocationParserService.java`: custom-place parsing
- `service/VisionLocationResolutionService.java`: lookup-backed location candidate handling
- `service/VisionCanvasAssembler.java`: backend-prepared canvas response assembly
- `service/VisionCreateQuestExecutionAdapter.java`: first mutation adapter
- `model/`: persisted conversation, turn, intent, status, and action primitives
- `dto/`: conversation turn contract, review DTOs, blocks, and summary DTOs
- `repository/`: persisted conversation and turn access

Supporting dependency:

- `apps/themuffinman/src/main/java/com/themuffinman/app/location/service/LocationLookupService.java`

## Vision API And Contract Map

- `apps/themuffinman/frontend/src/modules/vision/api/visionApi.ts`: frontend client boundary
- `apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts`: generated contract output
- shared enum names in the generated contract are prefixed by backend domain when vision and workmarket models reuse the same simple name
- `docs/agent-operating-model/sections/api.yaml`: machine-operational API source slice when endpoints or contracts change

Continuity contract:

- `VisionConversationSummaryDTO` is the compact recent/resume summary source for stage, progress, resumable/completed state, recent-task grouping, stale marking, and pending-slot context

Continuity rules:

- Treat `VisionConversationSummaryDTO` as the long-session memory surface for the shell.
- Use raw turn history for detailed audit only; do not rebuild resume state from transcript fragments when the summary already knows the stage, progress, and pending slot.
- Prefer compact in-shell reveal for active continuation state, and keep completed or stale work out of the main canvas unless the user explicitly reopens it.
- When a conversation changes intent or becomes stale, refresh the summary instead of leaving the frontend to infer continuity from older turn text.

## Vision Frontend Map

- `apps/themuffinman/frontend/src/modules/vision/views/VisionSurfaceModernView.vue`: route-level blank-canvas shell
- `apps/themuffinman/frontend/src/modules/vision/composables/useVisionConversation.ts`: conversation state and API wiring
- `apps/themuffinman/frontend/src/modules/vision/components/VisionAgentOrb.vue`: animated agent surface
- `apps/themuffinman/frontend/src/modules/vision/components/VisionPromptDock.vue`: prompt and voice dock
- `apps/themuffinman/frontend/src/modules/vision/components/VisionCanvasRenderer.vue`: backend-driven canvas block router
- `apps/themuffinman/frontend/src/modules/vision/components/VisionCanvasSection.vue`: shared status/info framing primitive
- `apps/themuffinman/frontend/src/modules/vision/components/VisionFieldRequestBlock.vue`: focused field-request renderer
- `apps/themuffinman/frontend/src/modules/vision/components/VisionResultSummaryBlock.vue`: focused result-summary renderer
- `apps/themuffinman/frontend/src/modules/vision/components/VisionReviewSummaryBlock.vue`: focused review-summary renderer

Preferred future shape:

- route shell
- animated agent surface
- prompt dock
- backend-driven canvas renderer
- focused block components

## Vision Tests Map

- `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionConversationServiceTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionLocationParserServiceTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionLocationResolutionServiceTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionScheduleParserServiceTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/vision/testing/`: reusable builders and presets for future slices

## Vision Documentation Map

- `docs/product-vision.md`: product direction
- `docs/vision-architecture-patterns.md`: implementation architecture baseline
- `docs/vision-decision-record.md`: locked operating decisions
- `docs/implementation-control.md`: universal implementation and validation rules
- `docs/vision-status-ledger.md`: continuity ledger
- `docs/business-logic.md`: user-visible behavior rules
- `docs/domain-technical.md`: entities, workflows, validations, permissions

## Compact Search Hints

Use these paths before broad repo search:

- orchestration bugs: `app/vision/service`
- persisted conversation state: `app/vision/model`, `app/vision/repository`
- response shape and canvas mode: `app/vision/dto`, `VisionCanvasAssembler`
- API/client drift: `frontend/src/modules/vision/api`, generated contract
- stepwise UX drift: `useVisionConversation.ts`, `VisionSurfaceModernView.vue`
- agent-operating sync: `docs/agent-operating-model*`

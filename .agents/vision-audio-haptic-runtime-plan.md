---
machine_kind: plan
machine_status: complete
machine_title: Vision Audio Haptic Runtime Plan
machine_goal: Make spoken output, concise voice summaries, and haptic hints first-class
  parts of the Vision runtime.
---

# Vision Audio Haptic Runtime Plan

## Status

Complete.

## Goal

Make spoken output, concise voice summaries, and haptic hints first-class parts of the Vision runtime.

## Workflow Frame

- Feature tier: tier3-high-risk-multi-layer
- Scope: voice output pipeline, speech summaries, haptic hint contract, watch-friendly turn flow
- Out of scope: full standalone audio engine rewrite
- Manifest decision: required when implementation starts
- Manifest path: TBD
- Master plan: `.agents/vision-next-experience-master-plan.md`

## Implementation Slices

- [x] Slice 1: define the voice and haptic cues that accompany a turn.
- [x] Slice 2: wire audio output so a response can be heard without losing the textual surface.
- [x] Slice 3: keep the watch interaction loop short, confirmable, and safe for low-attention use.

## Validation Plan

- Targeted checks: audio-state and runtime contract tests
- Broader checks: iPhone and Watch interaction smoke checks
- Skipped checks or reasons: none

## Docs and Artifacts

- Expected docs: `docs/product-vision.md`, `docs/vision-architecture-patterns.md`, `docs/vision-status-ledger.md`, `docs/business-logic.md`
- Expected generated artifacts: generated contracts and any voice-config or preview updates
- Temporary work products: audio flow sketch and haptic cue map under `.agents/tmp/`

## Closeout Gates

- Required closeout checks: voice output is understandable without hiding the visual state
- Final response evidence: the same backend turn can drive text, audio, and haptic feedback coherently
- Backlog follow-up rule: any missing audio or haptic semantics become persistent follow-up items

## Open Questions

- Resolver outputs still needed: whether the first audio slice should stream or stay turn-based
- Risks or approvals: none yet

## Completion Evidence

- Status: complete
- Changed files:
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/dto/VisionRuntimeContextDTO.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/dto/VisionRuntimeCueDTO.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionCanvasAssembler.java`
  - `apps/themuffinman/frontend/src/modules/vision/composables/useVisionConversation.ts`
  - `apps/themuffinman/frontend/src/modules/vision/components/VisionCanvasRenderer.vue`
- Validation evidence:
  - `./mvnw test -Dtest=VisionCanvasAssemblerTest,VisionConversationServiceTest,VisionConversationSnapshotSupportTest`
  - `npm run type-check`
  - `npm run build`
- Doc delta summary:
  - `docs/product-vision.md`
  - `docs/vision-architecture-patterns.md`
  - `docs/vision-status-ledger.md`
- Backlog update: none required for the audio/haptic slice
- Residual risk: richer watch-native rendering and device-specific haptic mapping remain future polish rather than blockers

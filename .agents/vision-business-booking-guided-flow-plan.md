---
machine_kind: plan
machine_status: complete
machine_title: Vision Business Booking Guided Flow Plan
machine_goal: Make business booking feel guided, capacity-aware, and backend-owned.
---

# Vision Business Booking Guided Flow Plan

## Status

Complete.

## Goal

Make business booking feel guided, capacity-aware, and backend-owned.

## Workflow Frame

- Feature tier: tier3-high-risk-multi-layer
- Scope: availability guidance, capacity prompts, business-specific next steps, booking review prompts
- Out of scope: public business page rendering details and unrelated search flows
- Manifest decision: required when implementation starts
- Manifest path: TBD
- Master plan: `.agents/vision-next-experience-master-plan.md`

## Implementation Slices

- [x] Slice 1: make booking guidance consume the business read model for availability and capacity.
- [x] Slice 2: keep booking review compact, explicit, and safe for confirmation.
- [x] Slice 3: make booking cancellation or change guidance clearly distinct from initial booking creation.

## Validation Plan

- Targeted checks: booking guidance and availability contract tests
- Broader checks: business scheduling scenario tests and review-flow tests
- Skipped checks or reasons: none

## Docs and Artifacts

- Expected docs: `docs/product-vision.md`, `docs/vision-architecture-patterns.md`, `docs/vision-status-ledger.md`, `docs/domain-technical.md`, `docs/business-logic.md`
- Expected generated artifacts: route-catalog updates and any business read-model contract refreshes
- Temporary work products: booking guidance sketch and capacity matrix under `.agents/tmp/`

## Closeout Gates

- Required closeout checks: booking prompts do not invent client-side availability or capacity logic
- Final response evidence: the backend can guide booking without the frontend guessing
- Backlog follow-up rule: any unresolved booking ambiguity becomes a persistent backlog item before closeout

## Open Questions

- Resolver outputs still needed: whether reschedule and cancel should share the same guidance path or split later
- Risks or approvals: none yet

## Completion Evidence

- Status: complete
- Changed files:
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/model/VisionIntent.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionBusinessPreviewRenderer.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionConversationSnapshotSupport.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionIntentRouter.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionIntentSignalSupport.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionSemanticRouteCatalogService.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionReadOnlyConversationTurnHandler.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionConversationService.java`
  - `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionCapabilityPreviewServiceTest.java`
  - `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionConversationSnapshotSupportTest.java`
  - `apps/themuffinman/frontend/src/modules/app-shell/visionHandoff.ts`
- Validation evidence:
  - `./mvnw test -Dtest=VisionCapabilityPreviewServiceTest,VisionConversationSnapshotSupportTest,VisionConversationServiceTest`
  - `npm run type-check`
  - `npm run build`
- Doc delta summary:
  - business booking review now combines owner-dashboard capacity context with the booking list and has a dedicated read-only Vision surface
- Backlog update:
  - removed `VISION-BUSINESS-BOOKING-INTENT-01`
- Residual risk:
  - future booking mutation guidance could still be split further, but the read surface and review guidance are now backend-prepared

---
machine_kind: plan
machine_status: complete
machine_title: Vision Business Read Model Plan
machine_goal: Define backend read surfaces for owner schedules, public business pages,
  and booking availability inside the Vision conversation system.
---

# Vision Business Read Model Plan

## Status

Complete.

## Goal

Define backend read surfaces for owner schedules, public business pages, and booking availability inside the Vision conversation system.

## Workflow Frame

- Feature tier: tier3-high-risk-multi-layer
- Scope: owner schedule summary, public business page projection, booking availability read model, calendar projection
- Out of scope: frontend page composition and standalone booking mutations
- Manifest decision: required when implementation starts
- Manifest path: TBD
- Master plan: `.agents/vision-next-experience-master-plan.md`

## Implementation Slices

- [x] Slice 1: define the owner schedule summary and calendar-projection read model.
- [x] Slice 2: define the public business page read model and its reusable business metadata.
- [x] Slice 3: define the booking availability projection so guided business flows can consume one backend surface.

## Validation Plan

- Targeted checks: read-model contract tests and projection tests
- Broader checks: business service tests and Vision response-shape tests
- Skipped checks or reasons: none

## Docs and Artifacts

- Expected docs: `docs/product-vision.md`, `docs/vision-architecture-patterns.md`, `docs/vision-status-ledger.md`, `docs/business-logic.md`, `docs/domain-technical.md`
- Expected generated artifacts: contract refreshes and any route-catalog changes that expose the new read surfaces
- Temporary work products: read-model sketch and projection matrix under `.agents/tmp/`

## Closeout Gates

- Required closeout checks: owner and public business surfaces can be rendered without frontend-derived schedule or availability logic
- Final response evidence: the same backend read model can support iPhone, Watch, and future clients at different densities
- Backlog follow-up rule: any unresolved business read-model gap becomes a persistent backlog item before closeout

## Open Questions

- Resolver outputs still needed: whether calendar projection should live beside the business read model or in a dedicated projection service
- Risks or approvals: none yet

## Completion Evidence

- Status: complete
- Changed files:
  - `apps/themuffinman/src/main/java/com/themuffinman/app/business/service/BusinessOwnerDashboardReadService.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/business/service/BusinessOwnerScheduleReadService.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/business/service/BusinessPublicReadService.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionBusinessPreviewRenderer.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionConversationSnapshotSupport.java`
  - `apps/themuffinman/src/test/java/com/themuffinman/app/business/service/BusinessPublicReadServiceTest.java`
  - `apps/themuffinman/src/test/java/com/themuffinman/app/business/service/BusinessOwnerScheduleReadServiceTest.java`
  - `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionConversationSnapshotSupportTest.java`
- Validation evidence:
  - `./mvnw test -Dtest=VisionConversationSnapshotSupportTest,VisionCanvasAssemblerTest,VisionConversationServiceTest`
- Doc delta summary:
  - `docs/vision-status-ledger.md`
  - `docs/product-vision.md`
  - `docs/domain-technical.md`
  - `docs/vision-architecture-patterns.md`
- Backlog update: none required for the business read-model slice
- Residual risk: dedicated booking mutation guidance is still expressed through shared business routes rather than its own Vision booking-intent family

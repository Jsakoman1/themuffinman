---
machine_kind: plan
machine_status: complete
machine_title: Vision Conversation Decomposition Plan
machine_goal: Split repeated conversation orchestration and snapshot handling out of VisionConversationService so the coordinator stays thin.
---

# Vision Conversation Decomposition Plan

Purpose: reduce the size of `VisionConversationService` by moving repeated turn-routing and snapshot mapping into focused helpers.

## Workflow Frame

- Feature tier: tier2-normal-feature
- Scope: conversation turn routing, read-only snapshot mapping, and repeated state/message helpers
- Out of scope: new intent families, UI changes, and business-rule changes
- Manifest decision: required
- Manifest path: TBD

## Implementation Slices

- [x] Slice 1: extract read-only snapshot preview and reset-message mapping from `VisionConversationService`.
- [x] Slice 2: move repeated turn handling into smaller intent-family handlers.

## Validation Plan

- Targeted checks: `VisionConversationServiceTest`, `VisionConversationReadModelAssemblerTest`
- Broader checks: `VisionCapabilityPreviewServiceTest`, full backend Maven test suite
- Skipped checks or reasons: none

## Docs and Artifacts

- Expected docs: `docs/domain-technical.md`, `docs/business-logic.md`, `docs/vision-architecture-patterns.md`
- Expected generated artifacts: generated contract and generated audit freshness outputs

## Closeout Gates

- Required closeout checks: snapshot message mapping is behavior-preserving and the coordinator owns less branching
- Final response evidence: read-only snapshot routing can be changed in one helper instead of inside the main service

## Open Questions

- Whether the next turn-handler split should be by capability family or by turn type
- Risks or approvals: none

## Completion Evidence

- Status: complete
- Changed files:
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionConversationService.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionConversationSnapshotSupport.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionDetailConversationTurnSupport.java`
  - `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionConversationSnapshotSupportTest.java`
  - `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionDetailConversationTurnSupportTest.java`
- Validation evidence:
  - `./mvnw -q -Dtest=VisionDetailConversationTurnSupportTest,VisionConversationServiceTest test`
  - `./mvnw -q -Dtest=VisionConversationSnapshotSupportTest,VisionConversationServiceTest test`
  - `./mvnw -q test`
  - `make audit-generated-artifact-freshness`
- Doc delta summary:
  - Read-only snapshot messages and capability-preview routing moved out of `VisionConversationService`.
  - Detail-view query resolution and target hydration moved into a dedicated support class.
- Backlog update: none
- Residual risk: none for this plan

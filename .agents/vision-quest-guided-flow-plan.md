---
machine_kind: plan
machine_status: complete
machine_title: Vision Quest Guided Flow Plan
machine_goal: Make create-work intake feel guided, backend-owned, and low-friction.
---

# Vision Quest Guided Flow Plan

## Status

Complete.

## Goal

Make create-work intake feel guided, backend-owned, and low-friction.

## Workflow Frame

- Feature tier: tier3-high-risk-multi-layer
- Scope: quest intake guidance, slot prioritization, review prompts, next-step selection
- Out of scope: unrelated business booking or search flows
- Manifest decision: required when implementation starts
- Manifest path: TBD
- Master plan: `.agents/vision-next-experience-master-plan.md`

## Implementation Slices

- [x] Slice 1: make quest intake ask for the next best field one step at a time.
- [x] Slice 2: keep quest review compact and deterministic so the user can confirm with confidence.
- [x] Slice 3: make quest follow-up suggestions feel like transforms of the current turn instead of generic commands.

## Validation Plan

- Targeted checks: quest guidance and slot-priority tests
- Broader checks: create-work scenario tests and review-flow tests
- Skipped checks or reasons: none

## Docs and Artifacts

- Expected docs: `docs/product-vision.md`, `docs/vision-architecture-patterns.md`, `docs/vision-status-ledger.md`, `docs/domain-technical.md`, `docs/business-logic.md`
- Expected generated artifacts: route-catalog updates if the quest guidance needs new backend surfaces
- Temporary work products: quest guidance sketch and slot-priority matrix under `.agents/tmp/`

## Closeout Gates

- Required closeout checks: create-work flows always produce one clear next step
- Final response evidence: the quest flow remains the reference pattern for other guided flows
- Backlog follow-up rule: any unresolved quest guidance ambiguity becomes a persistent backlog item before closeout

## Open Questions

- Resolver outputs still needed: which quest fields should be surfaced as default suggestion chips first
- Risks or approvals: none yet

## Completion Evidence

- Status: complete
- Changed files:
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionClarificationService.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionConversationService.java`
  - `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionClarificationServiceTest.java`
  - `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionConversationServiceTest.java`
- Validation evidence:
  - `./mvnw test -Dtest=VisionClarificationServiceTest,VisionConversationServiceTest,VisionExecutionPlannerTest`
- Doc delta summary:
  - quest-guidance copy now asks for the next best field with slot-aware wording instead of only generic fallback text
- Backlog update: none required
- Residual risk: quest review can still be tuned further, but the core guided intake is now deterministic and backend-owned

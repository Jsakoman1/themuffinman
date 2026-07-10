---
machine_kind: plan
machine_status: complete
machine_title: Vision Side Job Search Guided Flow Plan
machine_goal: Make side-job discovery feel guided, compare-friendly, and backend-prepared.
---

# Vision Side Job Search Guided Flow Plan

## Status

Complete.

## Goal

Make side-job discovery feel guided, compare-friendly, and backend-prepared.

## Workflow Frame

- Feature tier: tier3-high-risk-multi-layer
- Scope: search guidance, ranking comparisons, result narrowing, next-step selection
- Out of scope: work creation, booking, and person outreach flows
- Manifest decision: required when implementation starts
- Manifest path: TBD
- Master plan: `.agents/vision-next-experience-master-plan.md`

## Implementation Slices

- [x] Slice 1: make side-job search guidance narrow the result set before expanding detail.
- [x] Slice 2: keep comparison and trust cues visible so the next step is obvious.
- [x] Slice 3: keep the search flow connected to contact or apply actions without forcing a separate navigation model.

## Validation Plan

- Targeted checks: search guidance and ranking contract tests
- Broader checks: discovery scenario tests and compare-flow tests
- Skipped checks or reasons: none

## Docs and Artifacts

- Expected docs: `docs/product-vision.md`, `docs/vision-architecture-patterns.md`, `docs/vision-status-ledger.md`, `docs/domain-technical.md`, `docs/business-logic.md`
- Expected generated artifacts: route-catalog updates and any discovery contract refreshes
- Temporary work products: search guidance sketch and compare matrix under `.agents/tmp/`

## Closeout Gates

- Required closeout checks: the search flow produces ranked results and one obvious next step
- Final response evidence: side-job discovery stays backend-guided instead of becoming a generic results list
- Backlog follow-up rule: any unresolved search ambiguity becomes a persistent backlog item before closeout

## Open Questions

- Resolver outputs still needed: whether side-job search should expose a dedicated comparison mode or remain a single guided flow
- Risks or approvals: none yet

## Completion Evidence

- Status: complete
- Changed files:
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionSearchDiscoveryService.java`
  - `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionSearchDiscoveryServiceTest.java`
- Validation evidence:
  - `./mvnw test -Dtest=VisionSearchDiscoveryServiceTest,VisionConversationServiceTest`
- Doc delta summary:
  - search discovery summary now explains when matches span multiple families and tells the user how to narrow the result set
- Backlog update: none required
- Residual risk: further family-specific search modes can still be tuned later, but compare-friendly narrowing now exists

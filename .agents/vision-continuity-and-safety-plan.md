---
machine_kind: plan
machine_status: complete
machine_title: Vision Continuity And Safety Plan
machine_goal: Define interruption recovery, idempotency, consent, and retry safety
  for mobile and audio-driven Vision turns.
---

# Vision Continuity And Safety Plan

## Status

Complete.

## Goal

Define interruption recovery, idempotency, consent, and retry safety for mobile and audio-driven Vision turns.

## Workflow Frame

- Feature tier: tier3-high-risk-multi-layer
- Scope: retry safety, interruption state, cancel/undo semantics, consent boundaries, device handoff continuity
- Out of scope: layout polish and voice-synthesis implementation details
- Manifest decision: required when implementation starts
- Manifest path: TBD
- Master plan: `.agents/vision-next-experience-master-plan.md`

## Implementation Slices

- [x] Slice 1: define idempotency and duplicate-turn protection for turn creation and action confirmation.
- [x] Slice 2: define interruption and recovery semantics for switching devices, pausing turns, and resuming conversations.
- [x] Slice 3: define consent and safety cues for actions that contact people or affect shared business state.

## Validation Plan

- Targeted checks: turn retry and duplicate-action tests
- Broader checks: interruption, resume, and cancel flow tests
- Skipped checks or reasons: none

## Docs and Artifacts

- Expected docs: `docs/product-vision.md`, `docs/vision-architecture-patterns.md`, `docs/vision-status-ledger.md`, `docs/business-logic.md`, `docs/domain-technical.md`
- Expected generated artifacts: contract refreshes if state fields change
- Temporary work products: interruption-state sketch and retry/consent matrix under `.agents/tmp/`

## Closeout Gates

- Required closeout checks: the backend can describe when a turn is safe to retry, resume, or cancel without frontend inference
- Final response evidence: voice, iPhone, and Watch flows all resolve to one safe action state instead of creating duplicates
- Backlog follow-up rule: any unresolved retry or consent ambiguity becomes a persistent backlog item before closeout

## Open Questions

- Resolver outputs still needed: none
- Risks or approvals: none

## Completion Evidence

- Status: complete
- Changed files: `.agents/vision-continuity-and-safety-plan.md`, `apps/themuffinman/src/main/java/com/themuffinman/app/vision/model/VisionConversation.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/vision/repository/VisionConversationRepository.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionConversationService.java`, `apps/themuffinman/src/main/resources/db/migration/V52__add_vision_conversation_last_client_request_id.sql`, `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionConversationServiceTest.java`, `docs/business-logic.md`, `docs/domain-technical.md`, `docs/vision-status-ledger.md`
- Validation evidence: `./mvnw test -Dtest=VisionConversationServiceTest,VisionConversationLifecycleSupportTest,VisionCanvasAssemblerTest`, `ruby scripts/audits/audit-generated-artifact-freshness.rb`, `ruby scripts/generate-source-of-truth-audit.rb`, `ruby scripts/generate-backend-audit-inventory.rb`, `npm --prefix apps/themuffinman/frontend run generate:contracts`
- Doc delta summary: retry safety now replays a retried first submit by `clientRequestId` even before the browser has a conversation id; backend docs now describe the persisted request-id replay path and the updated continuity rules.
- Backlog update: none
- Residual risk: low

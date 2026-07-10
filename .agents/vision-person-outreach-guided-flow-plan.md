---
machine_kind: plan
machine_status: complete
machine_title: Vision Person Outreach Guided Flow Plan
machine_goal: Make person outreach feel guided, socially aware, and safe.
---

# Vision Person Outreach Guided Flow Plan

## Status

Complete.

## Goal

Make person outreach feel guided, socially aware, and safe.

## Workflow Frame

- Feature tier: tier3-high-risk-multi-layer
- Scope: contact-target resolution, social-boundary cues, outreach next steps, conversation handoff
- Out of scope: quest creation and business booking guidance
- Manifest decision: required when implementation starts
- Manifest path: TBD
- Master plan: `.agents/vision-next-experience-master-plan.md`

## Implementation Slices

- [x] Slice 1: make outreach resolve the target person or circle member clearly before action.
- [x] Slice 2: keep consent and social-boundary cues visible before contact actions proceed.
- [x] Slice 3: keep the outreach flow connected to chat or follow-up actions without frontend inference.

## Validation Plan

- Targeted checks: contact-resolution and social-boundary tests
- Broader checks: outreach scenario tests and conversation handoff tests
- Skipped checks or reasons: none

## Docs and Artifacts

- Expected docs: `docs/product-vision.md`, `docs/vision-architecture-patterns.md`, `docs/vision-status-ledger.md`, `docs/domain-technical.md`, `docs/business-logic.md`
- Expected generated artifacts: route-catalog updates and any contact-resolution contract refreshes
- Temporary work products: outreach sketch and consent matrix under `.agents/tmp/`

## Closeout Gates

- Required closeout checks: outreach flows do not bypass the backend social-boundary rules
- Final response evidence: person outreach stays one guided flow instead of becoming a raw contact picker
- Backlog follow-up rule: any unresolved outreach ambiguity becomes a persistent backlog item before closeout

## Open Questions

- Resolver outputs still needed: whether follow-up actions should open chat automatically or stay on the guided surface
- Risks or approvals: none yet

## Completion Evidence

- Status: complete
- Changed files:
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionChatExecutionService.java`
  - `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionChatExecutionServiceTest.java`
  - `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionConversationServiceTest.java`
- Validation evidence:
  - `./mvnw test -Dtest=VisionChatExecutionServiceTest,VisionConversationServiceTest`
- Doc delta summary:
  - outreach copy now refuses to guess a contact and makes the social-boundary requirement explicit before opening chat
- Backlog update: none required
- Residual risk: deeper outreach-specific follow-up actions can still be refined later, but the core contact safety path is now explicit

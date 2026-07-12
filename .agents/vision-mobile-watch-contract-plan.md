---
machine_kind: plan
machine_status: draft
machine_closeout_contract: 2
machine_baseline_ref: 0cf598d
machine_delivery_mode: implementation
machine_title: Vision Mobile And Watch Contract Plan
machine_goal: Define a backend-owned presentation policy and test fixtures that let iPhone and Watch render Vision safely without recreating product logic.
---

# Vision Mobile And Watch Contract Plan

## Status

Draft. This plan specifies client readiness; it does not build native applications.

## Workflow Frame

- Feature tier: tier3-high-risk-multi-layer
- Scope: device-role policy, safe-area/responsive behavior, phone/watch fixtures, native handoff boundaries, and contract documentation.
- Out of scope: native framework selection, offline sync engine, or multi-field Watch editing.
- Manifest decision: required
- Manifest path: .agents/feature-manifests/vision-mobile-watch-contract-manifest.yaml
- Master plan: .agents/vision-adaptive-mobile-first-master-plan.md
- Depends on: Plans 1-3

## Current Findings

- `VisionRuntimeContextDTO` already provides `deviceRole`, `attentionState`, cues, and `watchFriendly`.
- `VisionCanvasAssembler` creates runtime context in backend; frontend currently projects it too literally.
- Browser voice support exists in `useVisionConversation.ts`; native lifecycle and handoff policy are not yet formalized.

## Implementation Checkboxes

- [ ] Define a device-role presentation matrix for phone, watch, and web across all public Vision states.
- [ ] Specify which actions are allowed on Watch: status, acknowledge, confirm/decline, compact voice capture, and phone handoff only.
- [ ] Define phone safe-area, one-thumb, text-size, interruption, consent, notification, and deep-link behavior.
- [ ] Add backend/API fixtures for device-role and attention-state combinations and generated contract coverage.
- [ ] Update responsive web rendering to use the same policy without pretending to be a native client.

## Local Tools And Validation

- Read: `VisionRuntimeContextDTO`, `VisionCanvasAssembler`, `visionConversationApi.ts`, `useVisionConversation.ts`, and generated frontend contracts.
- Run: `make audit-api-contract-drift`, `npm run validate:contracts`, `npm run type-check`, `npm run build`, focused Vision backend tests, and narrow viewport smoke checks.
- Acceptance: a client can render every allowed state from DTOs alone; Watch has no browse-heavy or unsafe mutation path.

## Risks And Boundaries

- Device role is a renderer policy, never a permission bypass.
- Voice capture must retain text fallback and explicit consent/error behavior.
- New fields require a defined renderer and must be generated into client contracts.

## Completion Evidence

- Status: draft
- Baseline ref: `0cf598d`
- Implemented code paths: TBD
- Changed files: TBD
- Validation evidence: TBD
- Doc delta summary: TBD
- Backlog update: TBD
- Residual risk: TBD

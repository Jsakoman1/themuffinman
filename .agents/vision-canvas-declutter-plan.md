---
machine_kind: plan
machine_status: draft
machine_closeout_contract: 2
machine_baseline_ref: 0cf598d
machine_delivery_mode: implementation
machine_title: Vision Canvas Declutter Plan
machine_goal: Replace the default support/debug rail with one inline, conditional, user-facing contextual disclosure while preserving typed Vision behavior.
---

# Vision Canvas Declutter Plan

## Status

Draft. Starts after Plan 1 state vocabulary and Plan 2 primitives are available.

## Workflow Frame

- Feature tier: tier3-high-risk-multi-layer
- Scope: `VisionSurfaceModernView`, `VisionCanvasRenderer`, support/debug panels, inline progressive turn feedback, and developer-only diagnostics path.
- Out of scope: changing semantic routing, permissions, execution adapters, or typed review behavior.
- Manifest decision: required
- Manifest path: .agents/feature-manifests/vision-canvas-declutter-manifest.yaml
- Master plan: .agents/vision-adaptive-mobile-first-master-plan.md
- Depends on: Plans 1-2

## Current Findings

- `VisionSurfaceModernView.vue` imports `VisionFlowDebugPanel` and `VisionIntentPreviewPanel` and can create a second grid column.
- `VisionCanvasRenderer.vue` renders raw device, attention, cue, consent, resume, and action-hint metadata as normal UI.
- Existing backend turn responses already preserve typed review, confirmation, runtime context, and replay-safe continuity.

## Implementation Checkboxes

- [ ] Inventory every visible support-rail datum and classify it against Plan 1 public-state policy.
- [ ] Remove default two-column preview/debug layout and move diagnostics behind an explicit developer-only path.
- [ ] Build one compact contextual disclosure that appears only for review, blocked, completion, active voice, or explicit user reveal.
- [ ] Render heard text, active slot, applied value, review, confirmation, and completion as one progressive inline feed with no duplicate task model.
- [ ] Preserve typed actions, explicit confirmation, voice/haptic behavior, replay behavior, keyboard access, and error feedback.

## Local Tools And Validation

- Read: `VisionSurfaceModernView.vue`, `VisionCanvasRenderer.vue`, `VisionFlowDebugPanel.vue`, `VisionIntentPreviewPanel.vue`, `useVisionConversation.ts`, and Vision architecture docs.
- Run: `npm run validate:contracts`, `npm run type-check`, `npm run build`, Vision component/unit scenarios, `make audit-frontend-state-logic-duplication`, and route smoke at phone and desktop widths.
- Acceptance: normal Vision states use one column and one task model; diagnostics are absent by default; all existing typed turn actions still work.

## Risks And Boundaries

- Do not hide consent, blocked reason, or required confirmation when it is necessary for safe progress.
- Do not convert runtime fields into frontend-derived state; request a Plan 6 projection only when the current contract cannot render the approved public state.

## Completion Evidence

- Status: draft
- Baseline ref: `0cf598d`
- Implemented code paths: TBD
- Changed files: TBD
- Validation evidence: TBD
- Doc delta summary: TBD
- Backlog update: TBD
- Residual risk: TBD

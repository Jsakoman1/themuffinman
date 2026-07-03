---
machine_kind: plan
machine_status: unknown
machine_title: Vision Canvas Execution Surface Plan
---

# Vision Canvas Execution Surface Plan

Purpose: consume the new read-only execution candidate in the frontend vision surface and remove the remaining stacked-panel feel from the review state.

## Workflow Frame

- Feature tier: tier3-high-risk-multi-layer
- Scope: `/vision` frontend canvas, composable state, generated contract sync, and targeted docs
- Out of scope: backend mutation logic, new executors, or broad layout redesign outside `/vision`
- Manifest decision: skipped with reason; this slice changes frontend presentation only
- Manifest path: not used

## Routing Snapshot

- Context commands: inspect current vision surface, conversation composable, and contract definitions
- Routing commands: manual scope routing based on the active execution-planning backend slice
- Validation commands: frontend type-check and build, plus backend tests only if contract sync requires it
- Closeout commands: `make audit-todo`, `make audit-plan-completion plan=.agents/vision-canvas-execution-surface-plan.md`

## Implementation Slices

- [x] Slice 1: extend the frontend contract and vision composable to expose execution candidate state.
- [x] Slice 2: render the execution candidate in the adaptive canvas as a compact read-only surface.
- [x] Slice 3: reduce the visible overlap between review, message, and utility panels in the vision shell.
- [x] Slice 4: update docs/status ledger and reconcile plan evidence.

## Validation Plan

- Targeted checks: `npm run type-check`, `npm run build`
- Broader checks: `./mvnw test` only if contract generation or shared DTO changes require it
- Skipped checks or reasons: backend unit tests are not expected unless the generated contract or DTO synchronization changes

## Docs and Artifacts

- Expected docs: `.agents/vision-adaptive-architecture-master-plan.md`, `docs/vision-status-ledger.md`, `docs/domain-technical.md`, `docs/product-memory.md`, `docs/vision-architecture-patterns.md`
- Expected generated artifacts: `frontend/src/contracts/generated/themuffinmanContract.ts` if regenerated through the repo's contract pipeline

## Closeout Gates

- Required closeout checks: targeted frontend validation, `make audit-todo`, `make audit-plan-completion plan=.agents/vision-canvas-execution-surface-plan.md`
- Final response evidence: state that execution candidate state now surfaces in the vision canvas and that execution remains backend-controlled

## Open Questions

- Resolver outputs still needed: none for this slice
- Risks or approvals: no external approval expected

## Completion Evidence

- Status: complete
- Changed files: `apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts`, `apps/themuffinman/frontend/src/modules/vision/api/visionApi.ts`, `apps/themuffinman/frontend/src/modules/vision/components/VisionCanvasRenderer.vue`, `apps/themuffinman/frontend/src/modules/vision/components/VisionExecutionCandidateBlock.vue`, `apps/themuffinman/frontend/src/modules/vision/components/VisionReviewSummaryBlock.vue`, `apps/themuffinman/frontend/src/modules/vision/composables/useVisionConversation.ts`, `apps/themuffinman/frontend/src/modules/vision/views/VisionSurfaceModernView.vue`, `.agents/vision-adaptive-architecture-master-plan.md`, `docs/domain-technical.md`, `docs/product-memory.md`, `docs/vision-status-ledger.md`
- Validation evidence: `npm run type-check` passed; `npm run build` passed
- Doc delta summary: documented the frontend execution-candidate surface and the remaining read-only planning boundary
- Backlog update: no persistent backlog changes expected
- Residual risk: the canvas still contains multiple sections, but the review state is now driven by a single execution candidate summary rather than a separate popup

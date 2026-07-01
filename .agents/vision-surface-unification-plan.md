# Vision Surface Unification Plan

Purpose: collapse the remaining stacked-panel feel in `/vision` by merging the main state summary, execution candidate, canvas blocks, and prompt dock into one coherent adaptive surface.

## Workflow Frame

- Feature tier: tier3-high-risk-multi-layer
- Scope: `/vision` frontend shell, canvas composition, prompt dock framing, and living docs
- Out of scope: backend orchestration changes, new executors, or unrelated module UI redesign
- Manifest decision: skipped with reason; this slice changes frontend presentation only
- Manifest path: not used

## Routing Snapshot

- Context commands: inspect the current vision surface shell, canvas renderer, prompt dock, and surface tone logic
- Routing commands: manual scope routing based on the active vision master plans and the execution-candidate surface already in place
- Validation commands: frontend type-check and build, plus targeted docs/audit closeout
- Closeout commands: `make audit-todo`, `make audit-plan-completion plan=.agents/vision-surface-unification-plan.md`

## Implementation Slices

- [x] Slice 1: merge the visible context summary and canvas content into one adaptive surface frame.
- [x] Slice 2: fold the prompt dock into the same visual surface so it reads as an embedded composer, not a separate panel.
- [x] Slice 3: tune surface tone and spacing so review, blocked, and complete states feel like one morphing canvas.
- [x] Slice 4: update docs/status notes and reconcile the master plan.

## Validation Plan

- Targeted checks: `npm run type-check`, `npm run build`
- Broader checks: `./mvnw test` only if frontend contract generation or shared response types change again
- Skipped checks or reasons: backend unit tests are not expected unless the generated contract changes

## Docs and Artifacts

- Expected docs: `.agents/vision-modern-prompt-master-plan.md`, `.agents/vision-adaptive-architecture-master-plan.md`, `docs/vision-status-ledger.md`, `docs/product-memory.md`
- Expected generated artifacts: `frontend/src/contracts/generated/themuffinmanContract.ts` only if contract sync changes

## Closeout Gates

- Required closeout checks: targeted frontend validation, `make audit-todo`, `make audit-plan-completion plan=.agents/vision-surface-unification-plan.md`
- Final response evidence: state that the visible surface is more unified and that execution remains backend-governed

## Open Questions

- Resolver outputs still needed: none for this slice
- Risks or approvals: no external approval expected

## Completion Evidence

- Status: complete
- Changed files: `.agents/god-plans/vision-god-plan.md`, `.agents/god-plans/vision-god-plan.yaml`, `.agents/vision-adaptive-architecture-master-plan.md`, `docs/product-memory.md`, `docs/vision-status-ledger.md`, `apps/themuffinman/frontend/src/modules/vision/views/VisionSurfaceModernView.vue`
- Validation evidence: `npm run type-check` passed; `npm run build` passed
- Doc delta summary: documented the unified adaptive surface and the remaining prompt/context cohesion work
- Backlog update: no persistent backlog changes expected
- Residual risk: the prompt dock is still a dedicated component inside the shell, so further visual tightening may still be useful later

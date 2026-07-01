# Vision Context Toggle Pass Plan

Purpose: remove the remaining top-level `/vision` context chrome and keep context reveal inside the same adaptive shell.

## Workflow Frame

- Feature tier: tier2-frontend-focused
- Scope: `/vision` shell-level context reveal control, small style tightening, and God Plan/doc reconciliation
- Out of scope: backend orchestration changes, new voice behavior, or new capability routing
- Manifest decision: skipped with reason; this slice changes frontend presentation and living docs only
- Manifest path: not used

## Implementation Slices

- [x] Slice 1: replace the top-level context controls row with a quieter shell-level reveal affordance.
- [x] Slice 2: reconcile vision docs and God Plan notes with the reduced chrome.

## Validation Plan

- Targeted checks: `npm run type-check`, `npm run build`
- Closeout checks: `make audit-todo`, `make audit-plan-completion plan=.agents/vision-context-toggle-pass-plan.md`

## Completion Evidence

- Status: complete
- Changed files: `VisionSurfaceModernView.vue`, `docs/vision-status-ledger.md`, `.agents/god-plans/vision-god-plan.md`, `.agents/god-plans/vision-god-plan.yaml`
- Validation evidence: `npm run type-check` passed; `npm run build` passed
- Doc delta summary: moved context reveal from top-level chrome into the shell and updated the God Plan current-position notes to reflect the calmer frontend state
- Residual risk: the surface is materially calmer now, but the continuation model still needs another pass if recent-task browsing starts competing with the primary active turn

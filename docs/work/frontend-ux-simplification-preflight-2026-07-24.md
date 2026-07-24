# UX simplification master preflight — 2026-07-24

Status: prepared, not goal-pursued.

## Baseline

- Git baseline: `dcb983f4ad2b0354101caa8c1cdc47c624fb8b71`
- Primary audit: `docs/work/ui-ux-full-product-audit-2026-07-24.md`
- Reused frontend baseline: `docs/work/frontend-app-remaster-master.yaml`
- Reused Business baseline: `docs/work/business-booking-end-to-end-master.yaml`
- Reused VisionForWeb baseline: `docs/work/ui-linking-vision-inline-master.yaml`

Existing verified plans are baseline evidence only. They are not counted as new UX completion in this master.

## Confirmed residual scope

1. Shared task grammar: one mode, one primary action, progressive disclosure, explicit completion.
2. Visible multi-business context and viewer-scoped schedule ownership.
3. Booking-first Business calendar with availability/resource layers.
4. Progressive owner service setup and public service booking.
5. Migration of every authenticated module family to the shared grammar.
6. VisionForWeb context preservation, responsive transformation, keyboard/focus behavior, and current visual/runtime evidence.

## Architecture guardrails

- Frontend may own presentation state, task mode, context selection presentation, drawers, responsive layout, loading, error, and recovery rendering.
- Backend remains authoritative for business ownership, active scope eligibility, permissions, visibility, consent, booking rules, availability, capacity, pricing, resources, and workflow actions.
- A business switcher is a viewer-scoped read contract, not a frontend-created permission boundary.
- The personal calendar and owner business calendar may share a visual workspace, but their event authority and allowed actions remain role-specific.
- Flexible service schema fields must drive progressive UI sections; the frontend must not infer unsupported fields or pricing rules.
- VisionForWeb stays an inline contextual assistant and canonical handoff layer; it does not become a second navigation system.

## Pre-goal gates

- [x] Master, child plans, and inventory parse as YAML.
- [x] Each inventory item maps to exactly one child task.
- [x] Plan recursion audit passes.
- [x] Plan coverage audit passes for the prepared draft boundary.
- [ ] Atomic-task hardening is verifier-verified.
- [ ] System Map impact review is captured for shared shell, Business context, and calendar contract changes.
- [ ] Backend read-model decision is approved or explicitly deferred with a stable backlog ID.
- [ ] Goal pursuing is started only after the hardening item is verified and inventory sequence 0 is complete.

## Stop conditions

- Any requested UX behavior requires a new capability, endpoint, permission, visibility, consent, or workflow state not covered by the Business baseline.
- The multi-business schedule contract cannot be derived from existing ownership/read-model authority without a backend design decision.
- A shared primitive migration causes an unchanged verified surface to regress.
- Current browser or visual evidence cannot be captured in the owned local runtime.
- Dirty-worktree overlap prevents exact changed-file attribution for a task.

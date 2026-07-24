# UX next evolution preflight — 2026-07-24

This plan extends the verified full-product UX simplification program. It does not reopen completed task-surface or flexible Business booking work.

## Product decisions

- The active context is explicit and can be Personal, one Business, All businesses, a Circle, a Work item, or a Conversation.
- Every screen has one primary next action; secondary actions remain contextual or overflow-hidden.
- “All businesses” is a real aggregate calendar/read context, not a duplicate first-business selection.
- Backend remains authoritative for permissions, pricing, availability, capacity, resource conflicts, visibility, consent, and workflow transitions.
- Templates are starting points only; owners can override every generated rule before publishing.
- Mobile layouts transform into agenda, sheet, drawer, or step flows instead of merely stacking desktop columns.
- Every changed flow must expose loading, empty, stale, permission, validation, conflict, retry, and success states.

## Preflight gates

- [x] Verify baseline and dirty-worktree boundary.
- [x] Generate System Map impact report and review material recommendations.
- [x] Verify atomic task hardening before implementation.
- [x] Verify exact master/child/inventory mapping and non-recursive leaf validations.
- [x] Confirm All businesses read-model/API dependency before UI work.
- [x] Confirm no child introduces frontend-owned business authority.
- [x] Keep previous simplification evidence baseline-only.
- [x] Validate all 39 coverage rows map to an owned task and acceptance outcome.
- [x] Confirm every P1/P2 recommendation has a runtime or explicit deferred evidence path.
- [x] Confirm module-level tasks cannot close while mapped rows remain open.
- [x] Confirm the 39 coverage rows equal the 39 atomic implementation task IDs and inventory items.

## Stop conditions

Stop the batch if the aggregate calendar contract is ambiguous, a template would silently create production rules, a shared state component changes backend semantics, a task lacks exact evidence paths, or runtime ownership cannot be safely established.

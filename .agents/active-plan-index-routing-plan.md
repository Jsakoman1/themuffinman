---
machine_kind: plan
machine_status: complete
machine_title: Active Plan Index Routing Plan
machine_goal: Make the planning docs point at the active-plan index first so broad
  work can find open plan state quickly without
---

# Active Plan Index Routing Plan

## Status

Complete.

## Goal

Make the planning docs point at the active-plan index first so broad work can find open plan state quickly without
digging through many plan files.

## Scope

- Included:
  - `docs/program-planning-model.md`
  - `docs/codex-fast-path.md`
  - `docs/documentation-sync-policy.md`
  - `docs/tooling/codex-local-audits.md`
  - `docs/change-completion-checklist.md`
- Excluded:
  - generator internals
  - unrelated workflow rewrites

## Implementation Slices

1. Add active-plan index references to the planning entrypoints.
2. Keep the routing language compact and explicit.
3. Align any closeout or session-start instructions that should use the new index first.

## Validation

- `make audit-doc-canonical-phrases`
- `make audit-plan-completion plan=<plan-file>`

## Completion Evidence

- Status: complete
- Validation evidence: `make audit-doc-canonical-phrases` passed; `make audit-todo` passed
- Doc delta summary: routed `docs/program-planning-model.md`, `docs/codex-fast-path.md`, and `docs/tooling/codex-local-audits.md` toward the new plan index

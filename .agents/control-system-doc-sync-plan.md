---
machine_kind: plan
machine_status: complete
machine_title: Control System Doc Sync Plan
machine_goal: Update the planning and workflow docs so the new control refresh path,
  control-start entrypoint, and machine status
---

# Control System Doc Sync Plan

## Status

Complete.

## Goal

Update the planning and workflow docs so the new control refresh path, control-start entrypoint, and machine status
marker become the default guidance instead of implicit knowledge.

## Scope

- Included:
  - planning model docs
  - fast-path docs
  - workflow docs touched by the new refresh/closeout behavior
  - generated README guidance for the new control surfaces
- Excluded:
  - runtime product docs unrelated to control tooling

## Implementation Slices

1. Update the compact planning and fast-path docs with the new refresh and startup guidance.
2. Update the closeout workflow docs so the new refresh path is part of normal plan completion.
3. Update generated README guidance for the new control-system artifacts and summaries.

## Validation

- `make audit-doc-canonical-phrases`
- `make audit-todo`

## Completion Evidence

- Status: complete
- Validation evidence: `make audit-doc-canonical-phrases` passed; `make audit-todo` passed; `make control-start` passed
- Doc delta summary: updated the planning, fast-path, closeout, and generated-readme docs to reflect the new control refresh path and machine status marker

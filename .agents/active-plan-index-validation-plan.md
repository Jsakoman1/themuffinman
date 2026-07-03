---
machine_kind: plan
machine_status: complete
machine_title: Active Plan Index Validation Plan
machine_goal: Make the new active-plan index deterministic, auditable, and easy to
  keep fresh when plans are added, closed, or
---

# Active Plan Index Validation Plan

## Status

Complete.

## Goal

Make the new active-plan index deterministic, auditable, and easy to keep fresh when plans are added, closed, or
renamed.

## Scope

- Included:
  - freshness and summary registration for the new plan index
  - plan-completion and backlog hygiene checks for the planning workflow
  - generated summary outputs that should stay compact enough for review
- Excluded:
  - unrelated product or runtime logic

## Implementation Slices

1. Add any missing generated-artifact or registry coverage for the new index.
2. Confirm closeout and plan-completion checks still pass with the new control surface.
3. Record the validation command set and any intentionally unchanged surfaces.

## Validation

- `make audit-generated-artifact-freshness`
- `make audit-todo`
- `make audit-plan-completion plan=<plan-file>`

## Completion Evidence

- Status: complete
- Validation evidence: `make audit-generated-artifact-freshness` passed after refreshing the codex-context execution manifest; `make audit-todo` passed
- Doc delta summary: confirmed the new plan index stays in the tracked local-tooling review context and remains fresh with the current tooling sources

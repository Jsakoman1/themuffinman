---
machine_kind: plan
machine_status: unknown
machine_title: Feature Implementation Plan
---

# Feature Implementation Plan

Purpose: implement a frontend stale-surface audit that flags unreferenced or weakly connected frontend components, composables, helpers, and stylesheets using route and callsite context.

## Scope

- [x] Create this plan before substantial edits when the change spans multiple files, layers, or control surfaces.
- [x] Break the feature into small enforceable implementation steps.
- [x] Run relevant gates after each meaningful implementation milestone.
- [x] Close the plan only after code, docs, and validation are all green.

## Feature Tasks

- [x] Add `audit-frontend-stale-surfaces` script that inventories import counts, route reachability, endpoint-linker reachability, and likely stale frontend files.
- [x] Wire the audit into `Makefile` and local-tooling docs so it becomes a stable local helper instead of an open TODO.
- [x] Generate the report and rerun local-tooling and todo validation.

## Working Notes

- The audit should be conservative and review-first, not a delete list.
- Prefer separate buckets such as `likely_unused`, `route_detached`, `callsite_detached`, and `review_needed`.
- Shared framework entry files, generated contracts, and placeholder module surfaces should not be mislabeled as stale.

## Completion Check

- [x] Backend validation passes if backend code changes require it.
- [x] Frontend validation passes if frontend contracts changed.
- [x] Docs and machine-operating artifacts are updated.
- [x] This plan is updated to reflect actual completion state before closing the task.

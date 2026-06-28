# Feature Implementation Plan

Purpose: implement a local frontend route-surface inventory that maps routes and primary views/pages to composables, frontend API clients, and backend endpoint dependencies.

## Scope

- [x] Create this plan before substantial edits when the change spans multiple files, layers, or control surfaces.
- [x] Break the feature into small enforceable implementation steps.
- [x] Run relevant gates after each meaningful implementation milestone.
- [x] Close the plan only after code, docs, and validation are all green.

## Feature Tasks

- [x] Add `audit-frontend-route-surfaces` script that inventories router entries, resolved surface files, imported composables, frontend API client usage, and linked backend endpoints.
- [x] Wire the audit into `Makefile` and local-tooling docs so it becomes a stable local helper instead of an open TODO.
- [x] Generate the report and rerun local-tooling and todo validation.

## Working Notes

- Route entries with redirects or placeholder modules should still appear, but clearly marked as such.
- Prioritize route-level navigation value over exhaustive component trees.
- Use the endpoint linker output as a dependency source instead of rebuilding endpoint matching from scratch.

## Completion Check

- [x] Backend validation passes if backend code changes require it.
- [x] Frontend validation passes if frontend contracts changed.
- [x] Docs and machine-operating artifacts are updated.
- [x] This plan is updated to reflect actual completion state before closing the task.

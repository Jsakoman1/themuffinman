# Feature Implementation Plan

Purpose: close the final read-transaction audit gaps by distinguishing true transactional read surfaces from helper or mixed read/write flows, then rerun validation.

## Scope

- [x] Create this plan before substantial edits when the change spans multiple files, layers, or control surfaces.
- [x] Break the feature into small enforceable implementation steps.
- [x] Run relevant gates after each meaningful implementation milestone.
- [x] Close the plan only after code, docs, and validation are all green.

## Feature Tasks

- [x] Tighten the read-surface audit heuristic so mixed read/write lookup flows and pure DTO assembly helpers are not counted as missing read-only transactions.
- [x] Extend transaction coverage tests only if any real read-only annotations are introduced during this pass.
- [x] Regenerate local-tooling outputs and rerun backend, frontend, and todo validation.

## Working Notes

- Remaining reported gaps are `LocationLookupService.lookup`, `LocationLookupService.lookupFirst`, and `LocationSettingsService.toDto`.
- `lookup*` records provider-call audit rows, so it is intentionally not read-only.
- `toDto` assembles a DTO from an already loaded entity and should not drive transaction policy on its own.

## Completion Check

- [x] Backend validation passes.
- [x] Frontend validation passes if frontend contracts changed.
- [x] Docs and machine-operating artifacts are updated.
- [x] This plan is updated to reflect actual completion state before closing the task.

# Feature Implementation Plan

Purpose: implement `CODEX-LOCAL-CHANGE-IMPACT-PREFLIGHT` so changed files can be mapped to likely dependent docs, tests, generated artifacts, and sibling read surfaces before Codex starts broad manual discovery.

## Scope

- [x] Create this plan before substantial edits when the change spans multiple files, layers, or control surfaces.
- [x] Break the feature into small enforceable implementation steps.
- [x] Run relevant gates after each meaningful implementation milestone.
- [x] Close the plan only after code, docs, and validation are all green.

## Feature Tasks

- [x] Design the script input contract for explicit file arguments plus a sensible local default for changed files.
- [x] Implement the script and compact generated outputs under `docs/generated/local-tooling/`.
- [x] Add a `Makefile` entrypoint.
- [x] Document how the preflight output should be used.
- [x] Update the local-tooling backlog entry when the implementation is complete.

## Working Notes

- Prioritize compact actionable output over exhaustive dependency graphs.
- Include docs, generated artifacts, tests, and nearby read-surface suggestions.

## Completion Check

- [x] Backend validation passes.
- [x] Frontend validation passes if frontend contracts changed.
- [x] Docs and machine-operating artifacts are updated.
- [x] This plan is updated to reflect actual completion state before closing the task.

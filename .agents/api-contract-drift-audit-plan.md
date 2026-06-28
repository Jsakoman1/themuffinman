# Feature Implementation Plan

Purpose: implement `CODEX-LOCAL-API-CONTRACT-DRIFT-AUDIT` so backend DTO fields, generated frontend contracts, and actual frontend field references can be compared from one local report.

## Scope

- [x] Create this plan before substantial edits when the change spans multiple files, layers, or control surfaces.
- [x] Break the feature into small enforceable implementation steps.
- [x] Run relevant gates after each meaningful implementation milestone.
- [x] Close the plan only after code, docs, and validation are all green.

## Feature Tasks

- [x] Define DTO-field extraction rules for backend Java DTOs.
- [x] Define generated-contract and frontend usage extraction rules.
- [x] Implement the drift audit script and compact generated outputs.
- [x] Add a `Makefile` entrypoint and shared docs updates.
- [x] Update the local-tooling backlog entry when the implementation is complete.

## Working Notes

- Focus on signal such as missing contract fields, zero frontend usage, and suspicious frontend-only references.
- Prefer repo-native heuristics over heavyweight AST dependencies.

## Completion Check

- [x] Backend validation passes.
- [x] Frontend validation passes if frontend contracts changed.
- [x] Docs and machine-operating artifacts are updated.
- [x] This plan is updated to reflect actual completion state before closing the task.

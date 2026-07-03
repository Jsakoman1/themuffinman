---
machine_kind: plan
machine_status: unknown
machine_title: Feature Implementation Plan
---

# Feature Implementation Plan

Purpose: implement `CODEX-LOCAL-READ-SURFACE-INVENTORY` so backend read-oriented service methods, DTO assembly paths, repository usage, and transaction coverage can be inspected from a compact local report.

## Scope

- [x] Create this plan before substantial edits when the change spans multiple files, layers, or control surfaces.
- [x] Break the feature into small enforceable implementation steps.
- [x] Run relevant gates after each meaningful implementation milestone.
- [x] Close the plan only after code, docs, and validation are all green.

## Feature Tasks

- [x] Define the method classification heuristics for read-oriented service methods.
- [x] Implement the inventory script and machine-readable output.
- [x] Add a compact markdown summary for quick Codex consumption.
- [x] Add a `Makefile` entrypoint and shared docs updates.
- [x] Update the local-tooling backlog entry when the implementation is complete.

## Working Notes

- Favor high-signal heuristics over a brittle parser.
- Include repository fields, mapper hints, DTO return types, and transaction annotation visibility.

## Completion Check

- [x] Backend validation passes.
- [x] Frontend validation passes if frontend contracts changed.
- [x] Docs and machine-operating artifacts are updated.
- [x] This plan is updated to reflect actual completion state before closing the task.

# Feature Implementation Plan

Purpose: implement a local endpoint-callsite linker that maps backend endpoints to frontend client methods and primary UI entry surfaces so Codex can load feature context without manual cross-layer tracing.

## Scope

- [x] Create this plan before substantial edits when the change spans multiple files, layers, or control surfaces.
- [x] Break the feature into small enforceable implementation steps.
- [x] Run relevant gates after each meaningful implementation milestone.
- [x] Close the plan only after code, docs, and validation are all green.

## Feature Tasks

- [x] Add `audit-endpoint-callsite-linker` script that inventories backend controller endpoints, frontend API client methods, and main composable/view/page callsites.
- [x] Wire the audit into `Makefile` and local-tooling docs so it becomes a stable local helper instead of an open TODO.
- [x] Generate the report and rerun local-tooling and todo validation.

## Working Notes

- Prefer stable heuristic linking over full AST parsing.
- The most useful output is path-level navigation: endpoint, frontend client, and top UI surfaces that depend on it.
- Surface prioritization should prefer views and pages over deep helper-only files.

## Completion Check

- [x] Backend validation passes if backend code changes require it.
- [x] Frontend validation passes if frontend contracts changed.
- [x] Docs and machine-operating artifacts are updated.
- [x] This plan is updated to reflect actual completion state before closing the task.

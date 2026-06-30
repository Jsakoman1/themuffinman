# System Standardization Master Plan

Purpose: execute the repository-wide standardisation backlog in a controlled sequence so backend, frontend, docs, and validation surfaces converge on one repeatable product shape.

## Goal

Standardize the current codebase into a small number of predictable module patterns:

- backend service boundaries
- backend read-model and API contract families
- frontend route, view, and composable shells
- docs, generated audits, and validation coverage

This master plan coordinates the child plans below. It is intentionally broad because the current drift spans multiple domains and multiple layers.

## Execution Model

- Run child plans sequentially in the order listed below.
- Do not pause for user confirmation between child plans unless a real blocker, unsafe ambiguity, or conflicting user change appears.
- If a child plan proves too broad, split the remainder into a new stable child plan and keep going.
- Do not close the master plan until the code, docs, generated artifacts, and validation evidence line up.

## Source Findings

- [`.agents/system-standardization-audit-findings.md`](/Users/jsakoman/Desktop/themuffinman/.agents/system-standardization-audit-findings.md)
- [`docs/implementation-backlog.md`](/Users/jsakoman/Desktop/themuffinman/docs/implementation-backlog.md)

## Child Plans

- [x] `STD-WORKMARKET-SURFACE-STANDARDIZATION` - `.agents/todo-plans/83-std-workmarket-surface-standardization.md`
- [x] `STD-SOCIAL-SURFACE-STANDARDIZATION` - `.agents/todo-plans/84-std-social-surface-standardization.md`
- [x] `STD-IDENTITY-SURFACE-STANDARDIZATION` - `.agents/todo-plans/85-std-identity-surface-standardization.md`
- [x] `STD-LOCATION-SURFACE-STANDARDIZATION` - `.agents/todo-plans/86-std-location-surface-standardization.md`
- [x] `STD-CORE-MODULE-STANDARDIZATION` - `.agents/todo-plans/87-std-core-module-standardization.md`
- [x] `STD-FRONTEND-SHELL-STATE-STANDARDIZATION` - `.agents/todo-plans/88-std-frontend-shell-state-standardization.md`
- [x] `STD-DOCS-VALIDATION-STANDARDIZATION` - `.agents/todo-plans/89-std-docs-validation-standardization.md`

## Execution Phases

1. Standardize workmarket first.
2. Standardize social next, then identity, then location.
3. Standardize the smaller shared backend domains after the core domains are stable.
4. Standardize the frontend shell and state helpers.
5. Standardize docs, generated audit artifacts, and validation coverage.
6. Run the final closeout pass across every touched surface.

## Closeout Rules

- Remove or narrow the persistent backlog items only when the corresponding child plan is complete and validated.
- Update living docs whenever behavior, workflow, permissions, or contracts change.
- Keep the change set split by domain when that avoids mixed, hard-to-review edits.
- Record any new deferred remainder as a new stable backlog item before closing the current child plan.

## Completion Evidence

- Status: complete
- Execution status: complete
- Persistent backlog source: `docs/implementation-backlog.md`
- Residual risk: none known

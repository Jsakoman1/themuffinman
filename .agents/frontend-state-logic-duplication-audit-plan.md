# Feature Implementation Plan

Purpose: implement a frontend state-logic duplication audit that groups repeated mutation runners, feedback flows, dialog state patterns, and workflow-specific action logic across active frontend surfaces.

## Scope

- [x] Create this plan before substantial edits when the change spans multiple files, layers, or control surfaces.
- [x] Break the feature into small enforceable implementation steps.
- [x] Run relevant gates after each meaningful implementation milestone.
- [x] Close the plan only after code, docs, and validation are all green.

## Feature Tasks

- [x] Add `audit-frontend-state-logic-duplication` script that inventories repeated workflow action names, mutation-runner helpers, feedback/error patterns, and dialog open/close state helpers across active frontend modules.
- [x] Wire the audit into `Makefile` and local-tooling docs so it becomes a stable local helper instead of an open TODO.
- [x] Generate the report and rerun local-tooling and todo validation.

## Working Notes

- Focus on live workmarket and social surfaces first; placeholders and obviously isolated stubs should not dominate the report.
- Report-first is the goal: identify duplication clusters, not auto-refactor.
- Prefer buckets that humans can act on, such as `mutation_runner_overlap`, `workflow_action_overlap`, `dialog_state_overlap`, and `feedback_error_overlap`.

## Completion Check

- [x] Backend validation passes if backend code changes require it.
- [x] Frontend validation passes if frontend contracts changed.
- [x] Docs and machine-operating artifacts are updated.
- [x] This plan is updated to reflect actual completion state before closing the task.

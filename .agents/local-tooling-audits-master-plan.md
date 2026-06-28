# Feature Implementation Plan

Purpose: orchestrate five sequential local-tooling audit implementations that reduce Codex token usage by shifting repetitive repo inspection onto deterministic workstation-side scripts.

## Scope

- [x] Create this plan before substantial edits when the change spans multiple files, layers, or control surfaces.
- [x] Break the feature into small enforceable implementation steps.
- [x] Run relevant gates after each meaningful implementation milestone.
- [x] Close the plan only after code, docs, and validation are all green.

## Feature Tasks

- [x] Execute `.agents/change-impact-preflight-plan.md`.
- [x] Execute `.agents/read-surface-inventory-plan.md`.
- [x] Execute `.agents/mapper-usage-audit-plan.md`.
- [x] Execute `.agents/generated-artifact-freshness-plan.md`.
- [x] Execute `.agents/api-contract-drift-audit-plan.md`.
- [x] Keep outputs compact under `docs/generated/local-tooling/` so Codex can consume summaries instead of rerunning broad discovery.
- [x] Update shared docs, `Makefile`, and `docs/codex-local-tooling-todo.md` as each child plan completes.
- [x] Run repo validation relevant to the touched surfaces and leave this master plan reflecting actual completion state.

## Working Notes

- Child plans are the implementation source for each audit.
- The master plan owns sequencing and shared completion checks.
- Prefer deterministic scripts with zero network requirements.
- Favor Ruby for repo inspection because the repo already uses Ruby generators.

## Completion Check

- [x] Backend validation passes.
- [x] Frontend validation passes if frontend contracts changed.
- [x] Docs and machine-operating artifacts are updated.
- [x] This plan is updated to reflect actual completion state before closing the task.

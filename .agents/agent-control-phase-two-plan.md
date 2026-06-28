# Agent Control Phase Two Plan

Purpose: extend the operating-agent control system with stronger manifest rules, source-of-truth audits, richer intent contracts, canonical scenarios, and generated frontend workflow helpers.

## Scope

- [x] Create this plan before substantial edits when the change spans multiple files, layers, or control surfaces.
- [x] Break the feature into small enforceable implementation steps.
- [x] Run relevant gates after each meaningful implementation milestone.
- [x] Close the plan only after code, docs, generators, and validation are all green.

## Feature Tasks

- [x] Add feature manifest profiles, change modes, and enforcement rules.
- [x] Add source-of-truth audit generation for missing registrations and missing coverage.
- [x] Extend the agent operating model with explicit mutating intent contracts.
- [x] Add shared use-case workflow contract test harness and concrete use-case tests.
- [x] Add canonical backend scenario tests for lifecycle and fail-closed behavior.
- [x] Generate workflow-aware frontend helpers from the operating model.
- [x] Update docs, manifests, generated artifacts, and close-out validation.

## Working Notes

- Keep this file inside `.agents/`.
- Treat it as working memory, not as a permanent source of truth.
- Prefer generator-backed maintenance surfaces over manual duplication where possible.

## Completion Check

- [x] Backend validation passes.
- [x] Frontend validation passes.
- [x] Docs and machine-operating artifacts are updated.
- [x] This plan is updated to reflect actual completion state before closing the task.

# Agent Operating Refactor Plan

Purpose: track the controller, workflow, contract, and operating-model refactor from initial scope through final validation.

## Scope

- [x] Create this plan before substantial edits when the change spans multiple files, layers, or control surfaces.
- [x] Break the feature into small enforceable implementation steps.
- [x] Run relevant gates after each meaningful implementation milestone.
- [x] Close the plan only after code, docs, and validation are all green.

## Feature Tasks

- [x] Refactor auth into service and mapper layers so the controller stays transport-only.
- [x] Introduce explicit quest workflow/use-case services and align quest mutations around shared execution primitives.
- [x] Strengthen runtime contract, policy, and scenario tests for the agent operating model.
- [x] Split the operating model into maintainable source sections and generate the combined validated artifact.
- [x] Add source-of-truth generation for endpoint and automation read-model inventory, then align frontend contract generation where practical.
- [x] Update living docs, manifests, and validation coverage to match the new architecture.

## Working Notes

- Keep this file inside `.agents/`.
- Treat it as working memory, not as a permanent source of truth.
- Prefer phased delivery that keeps backend tests green after each architectural slice.

## Completion Check

- [x] Backend validation passes.
- [x] Frontend validation passes if frontend contracts changed.
- [x] Docs and machine-operating artifacts are updated.
- [x] This plan is updated to reflect actual completion state before closing the task.

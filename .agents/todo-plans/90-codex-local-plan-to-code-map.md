# CODEX-LOCAL-PLAN-TO-CODE-MAP Plan

Source: `docs/codex-local-tooling-todo.md`
Group: `local-tooling`
Risk: `medium`
Master order: 90 of 92

## Backlog Item

Generate a compact map from one plan file to likely code, docs, generated artifacts, validations, manifests, and related audits.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make plan-code-map plan=.agents/todo-plans/86-codex-local-manifest-path-resolver.md`
- [x] `make generate-audit-registry-artifacts`
- [x] `make audit-plan-completion plan=.agents/todo-plans/90-codex-local-plan-to-code-map.md`

## Completion Evidence

- Status: completed

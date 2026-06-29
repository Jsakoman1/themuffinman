# CODEX-LOCAL-COMPACT-DOMAIN-PACKS Plan

Source: `docs/codex-local-tooling-todo.md`
Group: `local-tooling`
Risk: `medium`
Master order: 92 of 92

## Backlog Item

Generate very small, stable domain packs with key services, controllers, DTO groups, workflows, docs, tests, and first commands for a named domain.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make domain-pack domain=workmarket`
- [x] `make generate-audit-registry-artifacts`
- [x] `make audit-plan-completion plan=.agents/todo-plans/92-codex-local-compact-domain-packs.md`

## Completion Evidence

- Status: completed

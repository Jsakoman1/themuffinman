# CODEX-LOCAL-WORKFLOW-SLICE-PACK Plan

Source: `docs/codex-local-tooling-todo.md`
Group: `local-tooling`
Risk: `medium`
Master order: 89 of 92

## Backlog Item

Generate one compact workflow slice with services, transitions, permissions, docs, frontend actions, and scenario tests for a named workflow.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make workflow-slice-pack workflow=quest-application`
- [x] `make generate-audit-registry-artifacts`
- [x] `make audit-plan-completion plan=.agents/todo-plans/89-codex-local-workflow-slice-pack.md`

## Completion Evidence

- Status: complete
- Changed files:
  - `scripts/audits/generate-workflow-slice-pack.rb`
  - `scripts/audits/local_tooling_extended_tools.rb`
  - `Makefile`
  - `docs/codex-local-tooling-todo.md`
  - `docs/domain-technical.md`
  - `docs/generated/local-tooling/workflow-slices/quest-application.json`
  - `docs/generated/local-tooling/workflow-slices/quest-application-summary.md`
- Validation evidence:
  - `ruby -c scripts/audits/local_tooling_extended_tools.rb` passed.
  - `ruby -c scripts/audits/generate-workflow-slice-pack.rb` passed.
  - `make workflow-slice-pack workflow=quest-application` passed and wrote the workflow slice JSON and summary.
  - `make generate-audit-registry-artifacts` passed with 56 registered audits.
  - `make audit-local-tooling-incremental` passed and included `make workflow-slice-pack workflow=quest-application`.
  - `ruby scripts/todo-audit.rb` passed.
  - `make audit-plan-completion plan=.agents/todo-plans/89-codex-local-workflow-slice-pack.md` passed.
- Backlog update: `CODEX-LOCAL-WORKFLOW-SLICE-PACK` is now marked complete in `docs/codex-local-tooling-todo.md` and listed in the available audits section.
- Residual risk: workflow matching is anchored to the documented state-machine catalog and scenario files; undocumented workflow fragments may still need manual review.

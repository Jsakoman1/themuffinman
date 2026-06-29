# CODEX-LOCAL-SELECTIVE-GENERATED-ARTIFACT-COMMIT-GUIDE Plan

Source: `docs/codex-local-tooling-todo.md`
Group: `local-tooling`
Risk: `medium`
Master order: 63 of 82

## Backlog Item

Generate a report that separates source changes from generated audit outputs and recommends which generated files should be committed for the current task.

Source notes:
  Proposed entrypoints:
  - `ruby scripts/audits/audit-generated-commit-scope.rb`
  - `make audit-generated-commit-scope`
  Proposed outputs:
  - `docs/generated/local-tooling/generated-commit-scope.json`
  - `docs/generated/local-tooling/generated-commit-scope-summary.md`
  Notes:
  - This would reduce accidental large commits of stale or unrelated generated reports.
  - It should classify outputs as `task_required`, `supporting_context`, `stale_or_unrelated`, or `do_not_commit_by_default`.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make audit-local-tooling-incremental`
- [x] `cd apps/themuffinman/frontend && npm run type-check`
- [x] `cd apps/themuffinman/frontend && npm run build`

## Completion Evidence

- Status: complete
- Changed files:
  - `scripts/audits/audit-generated-commit-scope.rb`
  - `Makefile`
  - `scripts/audits/local_tooling_extended_tools.rb`
  - `docs/generated/artifact-policy.yaml`
  - `docs/generated/README.md`
  - `docs/codex-local-tooling-todo.md`
  - `docs/domain-technical.md`
  - `docs/generated/local-tooling/generated-commit-scope.json`
  - `docs/generated/local-tooling/generated-commit-scope-summary.md`
- Validation evidence:
  - `ruby -c scripts/audits/audit-generated-commit-scope.rb` passed.
  - `make audit-generated-commit-scope` passed.
  - `make audit-plan-completion plan=.agents/todo-plans/63-codex-local-selective-generated-artifact-commit-guide.md` passed after closeout.
  - `ruby scripts/todo-audit.rb` passed.
  - `make audit-local-tooling-incremental` passed.
- Backlog update: removed `CODEX-LOCAL-SELECTIVE-GENERATED-ARTIFACT-COMMIT-GUIDE` from open local-tooling TODO items and added it to available local audits.
- Residual risk: recommendations depend on the maintained artifact policy; unknown generated paths are intentionally flagged as `stale_or_unrelated`.

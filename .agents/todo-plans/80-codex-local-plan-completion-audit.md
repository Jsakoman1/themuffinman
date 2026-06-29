# CODEX-LOCAL-PLAN-COMPLETION-AUDIT Plan

Source: `docs/codex-local-tooling-todo.md`
Group: `local-tooling`
Risk: `high`
Master order: 80 of 82

## Backlog Item

Validate temporary plans and master plans against referenced manifests and child plans.

Source notes:
  Proposed entrypoints:
  - `ruby scripts/audits/audit-plan-completion.rb plan=<plan-file>`
  - `make audit-plan-completion plan=<plan-file>`
  Proposed outputs:
  - `docs/generated/local-tooling/plan-completion/<plan-id>.json`
  - `docs/generated/local-tooling/plan-completion/<plan-id>-summary.md`
  Checks:
  - Plan exists and has a completion evidence section.
  - Required task checkboxes are complete or explicitly deferred to a backlog ID.
  - Master plans list child plans and each child status is complete or explicitly deferred.
  - Feature manifest status does not claim complete while plan evidence is incomplete.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make audit-agent-safety`
- [x] `make audit-local-tooling-incremental`

## Completion Evidence

- Status: complete
- Changed files:
  - `scripts/audits/audit-plan-completion.rb`
  - `Makefile`
  - `scripts/audits/local_tooling_extended_tools.rb`
  - `docs/codex-local-tooling-todo.md`
  - `docs/tooling/codex-local-audits.yml`
  - `docs/generated/local-tooling/plan-completion/backend-audit-tiering-plan.json`
  - `docs/generated/local-tooling/plan-completion/backend-audit-tiering-plan-summary.md`
  - `docs/generated/local-tooling/plan-completion/59-agent-plan-completion-enforcement.json`
  - `docs/generated/local-tooling/plan-completion/59-agent-plan-completion-enforcement-summary.md`
  - `docs/generated/local-tooling/plan-completion/80-codex-local-plan-completion-audit.json`
  - `docs/generated/local-tooling/plan-completion/80-codex-local-plan-completion-audit-summary.md`
- Validation evidence:
  - `ruby -c scripts/audits/audit-plan-completion.rb` passed.
  - `make audit-plan-completion plan=.agents/backend-audit-tiering-plan.md manifest=.agents/feature-manifests/backend-audit-tiering-manifest.yaml` passed.
  - `make audit-plan-completion plan=.agents/todo-plans/59-agent-plan-completion-enforcement.md` passed after closeout.
  - `make audit-plan-completion plan=.agents/todo-plans/80-codex-local-plan-completion-audit.md` passed after closeout.
  - `ruby scripts/todo-audit.rb` passed.
  - `make audit-agent-safety` passed.
  - `make audit-local-tooling-incremental` passed.
- Backlog update: removed `CODEX-LOCAL-PLAN-COMPLETION-AUDIT` from the open local-tooling TODO list and added it to available local audits.
- Residual risk: none for the local audit; master-plan closeout remains pending on the remaining TODO batch.

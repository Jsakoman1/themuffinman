# CODEX-LOCAL-CHANGESET-RISK-SCORER Plan

Source: `docs/codex-local-tooling-todo.md`
Group: `local-tooling`
Risk: `medium`
Master order: 66 of 82

## Backlog Item

Score a current changeset by risk factors such as controller contracts, entity/migration drift, workflow state changes, permission logic, docs-sync requirements, and generated-artifact churn.

Source notes:
  Proposed entrypoints:
  - `ruby scripts/audits/score-changeset-risk.rb [files...]`
  - `make changeset-risk`
  Proposed outputs:
  - `docs/generated/local-tooling/changeset-risk.json`
  - `docs/generated/local-tooling/changeset-risk-summary.md`
  Notes:
  - Output should be concise enough to read before choosing validation scope.
  - Prefer transparent rule-based scoring over vague natural-language risk labels.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make audit-documentation`
- [x] `make audit-agent-safety`
- [x] `make audit-local-tooling-incremental`
- [x] `cd apps/themuffinman && ./mvnw test`
- [x] `cd apps/themuffinman/frontend && npm run type-check`
- [x] `cd apps/themuffinman/frontend && npm run build`

## Completion Evidence

- Status: complete
- Changed files:
  - `scripts/audits/score-changeset-risk.rb`
  - `Makefile`
  - `scripts/audits/local_tooling_extended_tools.rb`
  - `docs/codex-local-tooling-todo.md`
  - `docs/domain-technical.md`
  - `docs/generated/local-tooling/changeset-risk.json`
  - `docs/generated/local-tooling/changeset-risk-summary.md`
- Validation evidence:
  - `ruby -c scripts/audits/score-changeset-risk.rb` passed.
  - `make changeset-risk` passed.
  - `make audit-plan-completion plan=.agents/todo-plans/66-codex-local-changeset-risk-scorer.md` passed after closeout.
  - `ruby scripts/todo-audit.rb` passed.
  - `make audit-documentation` passed.
  - `make audit-agent-safety` passed.
  - `make audit-local-tooling-incremental` passed.
- Backlog update: removed `CODEX-LOCAL-CHANGESET-RISK-SCORER` from open local-tooling TODO items and added it to available local audits.
- Residual risk: scoring is heuristic and should inform, not replace, required validation and review.

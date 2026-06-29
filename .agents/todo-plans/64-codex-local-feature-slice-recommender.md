# CODEX-LOCAL-FEATURE-SLICE-RECOMMENDER Plan

Source: `docs/codex-local-tooling-todo.md`
Group: `local-tooling`
Risk: `medium`
Master order: 64 of 82

## Backlog Item

Generate a recommended implementation slice plan from a feature topic, including the smallest backend, frontend, docs, tests, and generated-artifact scope that should be changed together.

Source notes:
  Proposed entrypoints:
  - `ruby scripts/audits/recommend-feature-slices.rb topic=<topic>`
  - `make recommend-feature-slices topic=<topic>`
  Proposed outputs:
  - `docs/generated/local-tooling/feature-slices/<topic>.json`
  - `docs/generated/local-tooling/feature-slices/<topic>-summary.md`
  Notes:
  - This should reduce oversized Codex turns by proposing sequential phases before implementation starts.
  - Use repo map, endpoint packs, validation matrix, and docs preflight as inputs.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make audit-documentation`
- [x] `make audit-local-tooling-incremental`
- [x] `cd apps/themuffinman && ./mvnw test`
- [x] `cd apps/themuffinman/frontend && npm run type-check`
- [x] `cd apps/themuffinman/frontend && npm run build`

## Completion Evidence

- Status: complete
- Changed files:
  - `scripts/audits/recommend-feature-slices.rb`
  - `scripts/audits/local_tooling_extended_tools.rb`
  - `Makefile`
  - `docs/codex-local-tooling-todo.md`
  - `docs/domain-technical.md`
  - `docs/generated/local-tooling/feature-slices/todo-master-plan.json`
  - `docs/generated/local-tooling/feature-slices/todo-master-plan-summary.md`
- Validation evidence:
  - `ruby -c scripts/audits/recommend-feature-slices.rb && ruby -c scripts/audits/local_tooling_extended_tools.rb` passed.
  - `make recommend-feature-slices topic=todo-master-plan` passed.
  - `make audit-plan-completion plan=.agents/todo-plans/64-codex-local-feature-slice-recommender.md` passed after closeout.
  - `ruby scripts/todo-audit.rb` passed.
  - `make audit-documentation` passed.
  - `make audit-local-tooling-incremental` passed.
  - `make audit-agent-safety` passed.
- Backlog update: removed `CODEX-LOCAL-FEATURE-SLICE-RECOMMENDER` from open local-tooling TODO items and added it to available local audits.
- Residual risk: recommendation quality is heuristic and should seed, not replace, human/agent implementation planning.

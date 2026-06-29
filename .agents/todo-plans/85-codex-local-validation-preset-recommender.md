# CODEX-LOCAL-VALIDATION-PRESET-RECOMMENDER Plan

Source: `docs/codex-local-tooling-todo.md`
Group: `local-tooling`
Risk: `medium`
Master order: 85 of 85

## Backlog Item

Recommend a validation preset for the current changeset so routine work does not require manual command assembly.

Source notes:
  Proposed entrypoints:
  - `ruby scripts/audits/recommend-validation-preset.rb [files...]`
  - `make recommend-validation-preset files=<csv>`
  Proposed outputs:
  - `docs/generated/local-tooling/validation-preset.json`
  - `docs/generated/local-tooling/validation-preset-summary.md`
  Notes:
  - Classify a changeset into presets like `fast`, `standard-backend`, `standard-frontend`, `full-closeout`, or `manifest-required`.
  - Sit one layer above targeted tests and the validation matrix so Codex does not need to assemble a command set manually on routine work.
  - Primary value is faster, more consistent validation selection with lower prompt overhead.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make recommend-validation-preset`
- [x] `make generate-audit-registry-artifacts`
- [x] `make audit-plan-completion plan=.agents/todo-plans/85-codex-local-validation-preset-recommender.md`

## Completion Evidence

- Status: complete
- Changed files:
  - `scripts/audits/recommend-validation-preset.rb`
  - `scripts/audits/local_tooling_extended_tools.rb`
  - `Makefile`
  - `docs/codex-local-tooling-todo.md`
  - `docs/domain-technical.md`
  - `docs/generated/local-tooling/validation-preset.json`
  - `docs/generated/local-tooling/validation-preset-summary.md`
- Validation evidence:
  - `ruby -c scripts/audits/local_tooling_extended_tools.rb` passed.
  - `ruby -c scripts/audits/recommend-validation-preset.rb` passed.
  - `make recommend-validation-preset` passed and wrote the preset JSON and summary.
  - `make generate-audit-registry-artifacts` passed with 52 registered audits.
  - `make audit-local-tooling-incremental` passed and included `make recommend-validation-preset`.
  - `ruby scripts/todo-audit.rb` passed.
  - `make audit-plan-completion plan=.agents/todo-plans/85-codex-local-validation-preset-recommender.md` passed.
- Backlog update: `CODEX-LOCAL-VALIDATION-PRESET-RECOMMENDER` is now marked complete in `docs/codex-local-tooling-todo.md` and listed in the available audits section.
- Residual risk: preset classification is heuristic and intentionally conservative; `manifest-required` and `full-closeout` presets still expect human review for unusually broad cross-domain changes.

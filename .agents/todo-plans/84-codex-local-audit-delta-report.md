# CODEX-LOCAL-AUDIT-DELTA-REPORT Plan

Source: `docs/codex-local-tooling-todo.md`
Group: `local-tooling`
Risk: `medium`
Master order: 84 of 85

## Backlog Item

Compare the latest and previous audit outputs so Codex can read small deltas instead of full reports after each rerun.

Source notes:
  Proposed entrypoints:
  - `ruby scripts/audits/audit-delta-report.rb audit=<audit-id>`
  - `make audit-delta-report audit=<audit-id>`
  Proposed outputs:
  - `docs/generated/local-tooling/audit-deltas/<audit-id>.json`
  - `docs/generated/local-tooling/audit-deltas/<audit-id>-summary.md`
  Notes:
  - Compare the latest audit output with the previous saved output and highlight only newly introduced risks, fixed findings, and count deltas.
  - Reduce token usage by letting Codex read small deltas instead of re-reading full audit summaries after each change.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make audit-delta-report audit=audit-router`
- [x] `make generate-audit-registry-artifacts`
- [x] `make audit-plan-completion plan=.agents/todo-plans/84-codex-local-audit-delta-report.md`

## Completion Evidence

- Status: complete
- Changed files:
  - `scripts/audits/audit-delta-report.rb`
  - `scripts/audits/local_tooling_extended_tools.rb`
  - `Makefile`
  - `docs/codex-local-tooling-todo.md`
  - `docs/domain-technical.md`
  - `docs/generated/local-tooling/.history/diff-summary/2026-06-29T12-04-50Z.json`
  - `docs/generated/local-tooling/.history/audit-router/2026-06-29T12-04-50Z.json`
  - `docs/generated/local-tooling/audit-deltas/diff-summary.json`
  - `docs/generated/local-tooling/audit-deltas/diff-summary-summary.md`
  - `docs/generated/local-tooling/audit-deltas/audit-router.json`
  - `docs/generated/local-tooling/audit-deltas/audit-router-summary.md`
- Validation evidence:
  - `ruby -c scripts/audits/local_tooling_extended_tools.rb` passed.
  - `ruby -c scripts/audits/audit-delta-report.rb` passed.
  - `make audit-delta-report audit=diff-summary` passed.
  - `make audit-delta-report audit=audit-router` passed.
  - `make generate-audit-registry-artifacts` passed with 52 registered audits.
  - `make audit-local-tooling-incremental` passed and included `make audit-delta-report audit=diff-summary`.
  - `ruby scripts/todo-audit.rb` passed.
  - `make audit-plan-completion plan=.agents/todo-plans/84-codex-local-audit-delta-report.md` passed.
- Backlog update: `CODEX-LOCAL-AUDIT-DELTA-REPORT` is now marked complete in `docs/codex-local-tooling-todo.md` and listed in the available audits section.
- Residual risk: delta extraction is intentionally generic and compact; it reports count changes and risk-like signal changes rather than a full structural diff.

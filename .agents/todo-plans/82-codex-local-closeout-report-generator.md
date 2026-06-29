# CODEX-LOCAL-CLOSEOUT-REPORT-GENERATOR Plan

Source: `docs/codex-local-tooling-todo.md`
Group: `local-tooling`
Risk: `medium`
Master order: 82 of 82

## Backlog Item

Generate a final closeout report suitable for Codex final responses and commit review.

Source notes:
  Proposed entrypoints:
  - `ruby scripts/audits/generate-closeout-report.rb manifest=<manifest-file>`
  - `make closeout-report manifest=<manifest-file>`
  Proposed outputs:
  - `docs/generated/local-tooling/closeout-reports/<feature-id>.json`
  - `docs/generated/local-tooling/closeout-reports/<feature-id>-summary.md`
  Notes:
  - Include changed files, validation evidence, docs delta, generated artifact delta, backlog delta, and residual risks.
  - Keep the summary short enough to paste into a final response without rereading the whole repo.

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
- [x] `cd apps/themuffinman/frontend && npm run type-check`
- [x] `cd apps/themuffinman/frontend && npm run build`

## Completion Evidence

- Status: complete
- Changed files:
  - `scripts/audits/generate-closeout-report.rb`
  - `Makefile`
  - `scripts/audits/local_tooling_extended_tools.rb`
  - `docs/codex-local-tooling-todo.md`
  - `docs/generated/local-tooling/closeout-reports/backend_audit_tiering.json`
  - `docs/generated/local-tooling/closeout-reports/backend_audit_tiering-summary.md`
- Validation evidence:
  - `ruby -c scripts/audits/generate-closeout-report.rb` passed.
  - `make closeout-report manifest=.agents/feature-manifests/backend-audit-tiering-manifest.yaml` passed.
  - `make audit-plan-completion plan=.agents/todo-plans/82-codex-local-closeout-report-generator.md` passed after closeout.
  - `ruby scripts/todo-audit.rb` passed.
  - `make audit-agent-safety` passed, including frontend type-check and build.
  - `make audit-local-tooling-incremental` passed.
- Backlog update: removed `CODEX-LOCAL-CLOSEOUT-REPORT-GENERATOR` from open local-tooling TODO items and added it to available local audits.
- Residual risk: none known; summaries intentionally stay compact and do not replace the full JSON report.

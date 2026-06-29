# CODEX-LOCAL-FAILURE-KNOWLEDGE-BASE Plan

Source: `docs/codex-local-tooling-todo.md`
Group: `local-tooling`
Risk: `low`
Master order: 67 of 82

## Backlog Item

Capture recurring validation failures and their fixes into a compact local troubleshooting index.

Source notes:
  Proposed entrypoints:
  - `ruby scripts/audits/update-failure-knowledge-base.rb source=<diagnostic-report>`
  - `make failure-knowledge-base`
  Proposed outputs:
  - `docs/generated/local-tooling/failure-knowledge-base.json`
  - `docs/generated/local-tooling/failure-knowledge-base-summary.md`
  Notes:
  - This should help Codex resolve repeated Maven, frontend type-check, Flyway, and documentation validation failures without rereading long logs.
  - Keep entries factual: failure pattern, owning surface, likely cause, verified fix, and source report.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make audit-local-tooling-incremental`

## Completion Evidence

- Status: complete
- Changed files: `scripts/audits/local_tooling_extended_tools.rb`, `scripts/audits/update-failure-knowledge-base.rb`, `Makefile`, `docs/codex-local-tooling-todo.md`, `docs/domain-technical.md`, generated local-tooling artifacts
- Validation evidence: `ruby -c scripts/audits/update-failure-knowledge-base.rb`; `ruby -c scripts/audits/local_tooling_extended_tools.rb`; `make failure-knowledge-base`; `make generate-audit-registry-artifacts`; `make audit-local-tooling-incremental`; `make audit-generated-commit-scope`; `ruby scripts/todo-audit.rb`; `make audit-documentation`
- Backlog update: `CODEX-LOCAL-FAILURE-KNOWLEDGE-BASE` marked complete in `docs/codex-local-tooling-todo.md`
- Residual risk: Pattern matching is intentionally advisory and may leave uncommon failures in `review_needed`.

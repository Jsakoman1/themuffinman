# CODEX-LOCAL-PROMPTABLE-CODEBASE-CAPSULE Plan

Source: `docs/codex-local-tooling-todo.md`
Group: `local-tooling`
Risk: `low`
Master order: 68 of 82

## Backlog Item

Generate a very small "read this first" capsule for new Codex sessions with current repo layout, active conventions, current open backlogs, and the preferred first commands.

Source notes:
  Proposed entrypoints:
  - `ruby scripts/audits/generate-codebase-capsule.rb`
  - `make codebase-capsule`
  Proposed outputs:
  - `docs/generated/local-tooling/codebase-capsule.md`
  - `docs/generated/local-tooling/codebase-capsule.json`
  Notes:
  - This should be intentionally shorter than repo map and audit summary index.
  - Use it as the default first read before broad work.

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

## Completion Evidence

- Status: complete
- Changed files: `scripts/audits/local_tooling_extended_tools.rb`, `scripts/audits/generate-codebase-capsule.rb`, `Makefile`, `docs/codex-local-tooling-todo.md`, `docs/domain-technical.md`, generated local-tooling artifacts
- Validation evidence: `ruby -c scripts/audits/generate-codebase-capsule.rb`; `ruby -c scripts/audits/local_tooling_extended_tools.rb`; `make codebase-capsule`; `make generate-audit-registry-artifacts`; `ruby scripts/todo-audit.rb`; `make audit-documentation`; `make audit-local-tooling-incremental`
- Backlog update: `CODEX-LOCAL-PROMPTABLE-CODEBASE-CAPSULE` marked complete in `docs/codex-local-tooling-todo.md`
- Residual risk: Capsule is intentionally compact and points to generated reports for deeper inspection.

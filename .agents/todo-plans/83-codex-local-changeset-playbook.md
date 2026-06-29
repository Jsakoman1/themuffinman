# CODEX-LOCAL-CHANGESET-PLAYBOOK Plan

Source: `docs/codex-local-tooling-todo.md`
Group: `local-tooling`
Risk: `medium`
Master order: 83 of 85

## Backlog Item

Combine the main pre-implementation reports into one deterministic ordered workflow for the current changeset.

Source notes:
  Proposed entrypoints:
  - `ruby scripts/audits/generate-changeset-playbook.rb [files...]`
  - `make changeset-playbook files=<csv>`
  Proposed outputs:
  - `docs/generated/local-tooling/changeset-playbook.json`
  - `docs/generated/local-tooling/changeset-playbook-summary.md`
  Notes:
  - Combine diff summary, audit router, validation matrix, doc-sync preflight, and manifest decision into one ordered action list.
  - Emit a deterministic "read this, then run this, then update these docs" workflow for the current changeset.
  - Primary value is simpler operator flow and fewer separate reports to read before implementation.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make changeset-playbook`
- [x] `make generate-audit-registry-artifacts`
- [x] `make audit-plan-completion plan=.agents/todo-plans/83-codex-local-changeset-playbook.md`

## Completion Evidence

- Status: complete
- Changed files:
  - `scripts/audits/generate-changeset-playbook.rb`
  - `scripts/audits/local_tooling_extended_tools.rb`
  - `Makefile`
  - `docs/codex-local-tooling-todo.md`
  - `docs/domain-technical.md`
  - `docs/generated/local-tooling/changeset-playbook.json`
  - `docs/generated/local-tooling/changeset-playbook-summary.md`
- Validation evidence:
  - `ruby -c scripts/audits/local_tooling_extended_tools.rb` passed.
  - `ruby -c scripts/audits/generate-changeset-playbook.rb` passed.
  - `make changeset-playbook` passed and wrote the playbook JSON and summary.
  - `make generate-audit-registry-artifacts` passed with 52 registered audits.
  - `make audit-local-tooling-incremental` passed and included `make changeset-playbook`.
  - `ruby scripts/todo-audit.rb` passed.
  - `make audit-plan-completion plan=.agents/todo-plans/83-codex-local-changeset-playbook.md` passed.
- Backlog update: `CODEX-LOCAL-CHANGESET-PLAYBOOK` is now marked complete in `docs/codex-local-tooling-todo.md` and listed in the available audits section.
- Residual risk: playbook guidance is deterministic and report-first; it does not replace human judgment on final implementation order for unusually broad mixed-domain changes.

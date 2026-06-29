# CODEX-LOCAL-VALIDATION-EVIDENCE-RECORDER Plan

Source: `docs/codex-local-tooling-todo.md`
Group: `local-tooling`
Risk: `high`
Master order: 78 of 82

## Backlog Item

Add a small wrapper that records validation command results into a feature manifest or companion evidence file.

Source notes:
  Proposed entrypoints:
  - `ruby scripts/audits/record-validation-evidence.rb manifest=<manifest-file> -- <command...>`
  - `make record-validation manifest=<manifest-file> command="<command>"`
  Proposed outputs:
  - Updated `.agents/feature-manifests/<feature-id>-manifest.yaml`
  - Optional `docs/generated/local-tooling/validation-evidence/<feature-id>.json`
  Notes:
  - Capture command, scope, result, timestamp, duration, summary, output path, and skipped reason.
  - Keep raw command logs out of git by default.
  - This should feed `CODEX-LOCAL-FEATURE-CLOSEOUT-ENFORCER`.

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
- Changed files: `scripts/audits/record-validation-evidence.rb`, `Makefile`, `scripts/audits/local_tooling_extended_tools.rb`, `docs/domain-technical.md`, `docs/codex-local-tooling-todo.md`, generated validation-evidence self-test and registry artifacts
- Validation evidence: `ruby -c scripts/audits/record-validation-evidence.rb` passed; direct `ruby scripts/audits/record-validation-evidence.rb manifest=/private/tmp/validation-recorder-selftest.yaml -- ruby -e exit` passed; `make record-validation manifest=/private/tmp/validation-recorder-selftest.yaml command="ruby -e exit"` passed; `make generate-audit-registry-artifacts` passed with 47 registered audits; `ruby scripts/todo-audit.rb` passed with 0 open backlog items and 0 inline TODO/FIXME references; `make audit-agent-safety` passed including 96 backend target tests, frontend type-check, admin-agent UI validation, frontend build, validation evidence quality, and todo audit; `make audit-local-tooling-incremental` passed with 91 Make targets after regeneration.
- Backlog update: `CODEX-LOCAL-VALIDATION-EVIDENCE-RECORDER` marked complete in `docs/codex-local-tooling-todo.md`
- Residual risk: recorder executes the provided command and records compact metadata only; it intentionally does not store raw command logs.

# CODEX-LOCAL-DOC-SYNC-DUPLICATE-CLEANUP-AUDIT Plan

Source: `docs/codex-local-tooling-todo.md`
Group: `local-tooling`
Risk: `medium`
Master order: 81 of 82

## Backlog Item

Find duplicated protected phrases, fragment-only policy bullets, and conflicting doc-sync wording across `AGENTS.md`, documentation policy, checklist, and operating model docs.

Source notes:
  Proposed entrypoints:
  - `ruby scripts/audits/audit-doc-sync-duplicates.rb`
  - `make audit-doc-sync-duplicates`
  Proposed outputs:
  - `docs/generated/local-tooling/doc-sync-duplicates.json`
  - `docs/generated/local-tooling/doc-sync-duplicates-summary.md`
  Notes:
  - Must preserve exact protected canonical phrases.
  - Should recommend consolidation targets without auto-rewriting docs.

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

## Completion Evidence

- Status: complete
- Changed files: `scripts/audits/audit-doc-sync-duplicates.rb`, `Makefile`, `scripts/audits/local_tooling_extended_tools.rb`, `docs/domain-technical.md`, `docs/codex-local-tooling-todo.md`, generated doc-sync-duplicates and registry artifacts
- Validation evidence: `ruby -c scripts/audits/audit-doc-sync-duplicates.rb` passed; `make audit-doc-sync-duplicates` passed with 18 protected duplicate groups, 2 fragment-only bullets, and 4 conflict groups reported; `make generate-audit-registry-artifacts` passed with 49 registered audits; `ruby scripts/todo-audit.rb` passed with 0 open backlog items and 0 inline TODO/FIXME references; `make audit-documentation` passed with 92 targets before batch regeneration; `make audit-agent-safety` passed including 96 backend target tests, frontend type-check, admin-agent UI validation, frontend build, validation evidence quality, and todo audit; `make audit-local-tooling-incremental` passed with 93 Make targets and included `make audit-doc-sync-duplicates`.
- Backlog update: `CODEX-LOCAL-DOC-SYNC-DUPLICATE-CLEANUP-AUDIT` marked complete in `docs/codex-local-tooling-todo.md`
- Residual risk: audit is report-only and recommends review targets; it intentionally does not rewrite protected canonical wording.

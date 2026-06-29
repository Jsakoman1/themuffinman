# CODEX-LOCAL-FEATURE-CLOSEOUT-ENFORCER Plan

Source: `docs/codex-local-tooling-todo.md`
Group: `local-tooling`
Risk: `high`
Master order: 77 of 82

## Backlog Item

Replace advisory closeout output with a local hard-fail auditor that validates manifest schema, checklist completion, validation evidence, artifact paths, duplicate artifact buckets, backlog links, and plan completion.

Source notes:
  Proposed entrypoints:
  - `ruby scripts/audits/enforce-feature-closeout.rb manifest=<manifest-file>`
  - `make enforce-feature-closeout manifest=<manifest-file>`
  Proposed outputs:
  - `docs/generated/local-tooling/closeout-enforcement/<feature-id>.json`
  - `docs/generated/local-tooling/closeout-enforcement/<feature-id>-summary.md`
  Required checks:
  - Fail if `status: complete` is missing for final closeout.
  - Fail if any required checklist field is false without an allowed `not_applicable` evidence record.
  - Fail if required profile commands have no validation evidence.
  - Fail if generated artifact commands are declared but output files are missing or stale.
  - Fail if the same path appears in more than one artifact group.
  - Fail if backlog created/resolved IDs do not match persistent backlog state.
  - Fail if the referenced plan is missing or lacks completion evidence.

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

## Completion Evidence

- Status: complete
- Changed files: `scripts/audits/enforce-feature-closeout.rb`, `Makefile`, `scripts/audits/local_tooling_extended_tools.rb`, `docs/domain-technical.md`, `docs/codex-local-tooling-todo.md`, generated closeout-enforcement and registry artifacts
- Validation evidence: `ruby -c scripts/audits/enforce-feature-closeout.rb` passed; `make enforce-feature-closeout manifest=.agents/feature-manifests/agent-control-phase-two-manifest.yaml` passed and wrote closeout-enforcement JSON/summary; `make generate-audit-registry-artifacts` passed with 46 registered audits; `ruby scripts/todo-audit.rb` passed with 0 open backlog items and 0 inline TODO/FIXME references; `make audit-documentation` passed with 89 targets before batch regeneration; `make audit-agent-safety` passed including 96 backend target tests, frontend type-check, admin-agent UI validation, frontend build, validation evidence quality, and todo audit; `make audit-local-tooling-incremental` passed with 90 Make targets after regeneration; `cd apps/themuffinman && ./mvnw test` passed with 273 tests, 0 failures, 0 errors.
- Backlog update: `CODEX-LOCAL-FEATURE-CLOSEOUT-ENFORCER` marked complete in `docs/codex-local-tooling-todo.md`
- Residual risk: enforcement is manifest-structure based and checks recorded evidence; it does not rerun the recorded validation commands itself.

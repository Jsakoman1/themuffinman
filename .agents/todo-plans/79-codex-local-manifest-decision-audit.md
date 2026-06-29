# CODEX-LOCAL-MANIFEST-DECISION-AUDIT Plan

Source: `docs/codex-local-tooling-todo.md`
Group: `local-tooling`
Risk: `high`
Master order: 79 of 82

## Backlog Item

Determine whether the current changeset requires a feature manifest and report the reason.

Source notes:
  Proposed entrypoints:
  - `ruby scripts/audits/audit-manifest-decision.rb [files...]`
  - `make audit-manifest-decision files=<csv>`
  Proposed outputs:
  - `docs/generated/local-tooling/manifest-decision.json`
  - `docs/generated/local-tooling/manifest-decision-summary.md`
  Rules:
  - Require manifest for multi-file multi-layer changes.
  - Require manifest for high-risk, executor-critical, agent-contract, workflow-expansion, frontend-contract, schema, generated-artifact, or automation-safety changes.
  - Allow skip for cosmetic or single-file contract-neutral refactors with a documented reason.

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
- Changed files: `scripts/audits/audit-manifest-decision.rb`, `Makefile`, `scripts/audits/local_tooling_extended_tools.rb`, `docs/domain-technical.md`, `docs/codex-local-tooling-todo.md`, generated manifest-decision and registry artifacts
- Validation evidence: `ruby -c scripts/audits/audit-manifest-decision.rb` passed; `make audit-manifest-decision` passed with decision `required`; `make generate-audit-registry-artifacts` passed with 48 registered audits; `ruby scripts/todo-audit.rb` passed with 0 open backlog items and 0 inline TODO/FIXME references; `cd apps/themuffinman/frontend && npm run type-check` passed; `cd apps/themuffinman/frontend && npm run build` passed and validated generated contracts; `make audit-agent-safety` passed including 96 backend target tests, frontend type-check, admin-agent UI validation, frontend build, validation evidence quality, and todo audit; `make audit-local-tooling-incremental` passed with 92 Make targets and included `make audit-manifest-decision`.
- Backlog update: `CODEX-LOCAL-MANIFEST-DECISION-AUDIT` marked complete in `docs/codex-local-tooling-todo.md`
- Residual risk: audit uses deterministic file/category heuristics and reports review decisions; it does not replace human judgment for ambiguous cosmetic changes.

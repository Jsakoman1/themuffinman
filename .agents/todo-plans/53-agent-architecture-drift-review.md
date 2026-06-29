# AGENT-ARCHITECTURE-DRIFT-REVIEW Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `medium`
Master order: 53 of 82

## Backlog Item

Add a recurring audit that flags services, controllers, Vue views, and docs sections that exceed size or responsibility thresholds.

Source notes:
  Purpose: catch architectural decay before it makes future Codex changes expensive.

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
- [x] `cd apps/themuffinman/frontend && npm run type-check`
- [x] `cd apps/themuffinman/frontend && npm run build`

## Completion Evidence

- Status: complete
- Changed files: added `scripts/audits/audit-architecture-drift.rb`; extended `scripts/audits/local_tooling_batch_audits.rb`, `scripts/audits/local_tooling_extended_tools.rb`, `Makefile`, `docs/agent-improvement-backlog.md`, `docs/codex-local-tooling-todo.md`, `docs/change-completion-checklist.md`, `docs/domain-technical.md`, and generated local-tooling architecture drift and registry/index artifacts.
- Validation evidence: `ruby -c scripts/audits/audit-architecture-drift.rb`, `ruby -c scripts/audits/local_tooling_batch_audits.rb`, `make audit-architecture-drift`, `ruby scripts/audits/generate-audit-registry-artifacts.rb`, `ruby scripts/audits/audit-make-target-index.rb`, `ruby scripts/todo-audit.rb`, `make audit-documentation`, `make audit-agent-safety`, `make audit-local-tooling-incremental`, `ruby scripts/audits/audit-generated-artifact-freshness.rb`, `cd apps/themuffinman/frontend && npm run type-check`, `cd apps/themuffinman/frontend && npm run build`, and `cd apps/themuffinman && ./mvnw test` all passed.
- Backlog update: removed `AGENT-ARCHITECTURE-DRIFT-REVIEW` from `docs/agent-improvement-backlog.md`; the overlapping local tooling item `CODEX-LOCAL-ARCHITECTURE-DRIFT-AUDIT` was implemented by the same audit and removed from `docs/codex-local-tooling-todo.md`.
- Residual risk: the audit is intentionally report-first and heuristic; it identifies candidates for review without failing builds or automatically refactoring flagged files.

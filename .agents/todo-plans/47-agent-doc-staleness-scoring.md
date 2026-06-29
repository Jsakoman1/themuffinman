# AGENT-DOC-STALENESS-SCORING Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `medium`
Master order: 47 of 82

## Backlog Item

Add report-first scoring for stale docs based on code changes, endpoint inventory, DTO inventory, and workflow state changes since each doc section was last touched.

Source notes:
  Purpose: direct Codex to the highest-risk documentation gaps instead of rereading all docs.

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
- Changed files: `scripts/audits/audit-doc-staleness-scoring.rb`, `scripts/audits/local_tooling_batch_audits.rb`, `scripts/audits/local_tooling_extended_tools.rb`, `Makefile`, `docs/tooling/codex-local-audits.yml`, `docs/tooling/codex-local-audits.md`, `docs/generated/local-tooling/doc-staleness-scoring.json`, `docs/generated/local-tooling/doc-staleness-scoring-summary.md`, `docs/generated/local-tooling/audit-registry-artifacts.json`, `docs/generated/local-tooling/audit-registry-artifacts-summary.md`, `docs/generated/local-tooling/audit-summary-index.json`, `docs/generated/local-tooling/audit-summary-index.md`, `docs/agent-operating-model.md`, `docs/domain-technical.md`, `docs/change-completion-checklist.md`, `docs/agent-improvement-backlog.md`
- Validation evidence: `make audit-doc-staleness-scoring` passed; `ruby scripts/audits/generate-audit-registry-artifacts.rb` passed; `ruby scripts/todo-audit.rb` passed; `make audit-documentation` passed; `make audit-agent-safety` passed; `make audit-local-tooling-incremental` passed; `cd apps/themuffinman && ./mvnw test` passed with 273 tests.
- Backlog update: removed `AGENT-DOC-STALENESS-SCORING` from `docs/agent-improvement-backlog.md`.
- Residual risk: scoring is intentionally report-first and heuristic; it ranks likely stale sections but does not prove semantic documentation drift.

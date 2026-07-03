---
machine_kind: master-plan
machine_status: unknown
machine_title: Codex Summary Compactification Master Plan
machine_goal: Reduce repeated token spend on broad report review by making the most
  frequently read generated summaries shorter, more decision-first, and more consistent.
---

# Codex Summary Compactification Master Plan

Purpose: shorten the highest-noise generated local-tooling summaries so Codex gets decision-first context faster without losing the underlying JSON evidence or validation signal.

## Goal

Reduce repeated token spend on broad report review by making the most frequently read generated summaries shorter, more decision-first, and more consistent.

## Target Summaries

- `docs/generated/local-tooling/changeset-risk-summary.md`
- `docs/generated/local-tooling/generated-artifact-freshness-summary.md`
- `docs/generated/local-tooling/generated-commit-scope-summary.md`
- `docs/generated/local-tooling/duplicate-logic-audit-summary.md`
- `docs/generated/local-tooling/permission-rule-duplication-audit-summary.md`
- `docs/generated/local-tooling/frontend-state-logic-duplication-audit-summary.md`
- `docs/generated/local-tooling/doc-sync-duplicates-summary.md`
- `docs/generated/local-tooling/test-history-summary.md`
- `docs/generated/local-tooling/codebase-capsule.md`
- `docs/generated/local-tooling/change-impact-preflight-summary.md`

## Child Plans

- [x] `CODEX-SUMMARY-READ-FIRST-COMPACTION` - `.agents/todo-plans/109-codex-summary-read-first-compaction.md`
- [x] `CODEX-SUMMARY-VALIDATION-COMPACTION` - `.agents/todo-plans/110-codex-summary-validation-compaction.md`
- [x] `CODEX-SUMMARY-HISTORY-COMPACTION` - `.agents/todo-plans/111-codex-summary-history-compaction.md`

## Execution Order

1. Compact the read-first routing and inventory summaries.
2. Compact validation, closeout, and doc-sync summaries.
3. Compact history, freshness, and reference summaries.
4. Regenerate the affected local-tooling artifacts and compare the before/after output size.
5. Run the relevant validation and plan-completion audits before closing the master plan.

## Closeout Rules

- Keep the shared formatter change narrow to local-tooling summaries only.
- Preserve JSON evidence unchanged.
- If a child plan reveals a separate high-noise family, split it into a new stable child plan rather than widening the current one.
- Record any deferred remainder in `docs/agent-improvement-backlog.md` before closing the master plan.

## Completion Evidence

- Status: complete
- Execution status: complete
- Persistent backlog item: none
- Primary source files: `scripts/audits/local_tooling_extended_tools.rb`, `scripts/audits/audit-read-surface-inventory.rb`, `scripts/audits/audit-repository-fetch.rb`, `scripts/audits/audit-frontend-route-surfaces.rb`, `scripts/audits/audit-endpoint-callsite-linker.rb`, `scripts/audits/audit-generated-artifact-freshness.rb`, `scripts/audits/audit-doc-sync-required-surfaces.rb`, `scripts/audits/audit-generated-commit-scope.rb`, `scripts/audits/recommend-validation-preset.rb`, `scripts/audits/generate-fast-check-report.rb`, `scripts/audits/generate-test-history-summary.rb`, `scripts/audits/generate-architecture-decision-index.rb`, `scripts/audits/audit-doc-canonical-phrases.rb`, `scripts/audits/generate-audit-summary-index.rb`, `scripts/audits/generate-changeset-playbook.rb`, `scripts/audits/generate-endpoint-contract-packs.rb`

## Execution Notes

- Keep JSON outputs unchanged.
- Prefer shorter counts, shortlist entries, and top factors over long file inventories.
- Keep failure anchors and regeneration commands when they are the main decision signal.
- If a summary is primarily used as a context pack input, keep the first read-first bullets only.

## Completion Criteria

- The targeted summaries are materially shorter and still usable as first-pass Codex context.
- The generated JSON evidence remains intact and in sync.
- Validation and closeout audits still pass.

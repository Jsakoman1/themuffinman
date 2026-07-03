---
machine_kind: master-plan
machine_status: unknown
machine_title: Codex Summary Compactification Phase 2 Master Plan
machine_goal: Reduce the remaining broad-report token cost by tightening the last
  three noisy summary surfaces and regenerating the stale context manifest.
---

# Codex Summary Compactification Phase 2 Master Plan

Purpose: finish compacting the remaining high-noise summary families and refresh the codex-context execution manifest so freshness returns to green after the formatter changes.

## Goal

Reduce the remaining broad-report token cost by tightening the last three noisy summary surfaces and regenerating the stale context manifest.

## Child Plans

- [x] `CODEX-SUMMARY-CHANGESET-RISK-COMPACTION` - `.agents/todo-plans/112-codex-summary-changeset-risk-compaction.md`
- [x] `CODEX-SUMMARY-DOC-SYNC-DUPLICATES-COMPACTION` - `.agents/todo-plans/113-codex-summary-doc-sync-duplicates-compaction.md`
- [x] `CODEX-SUMMARY-CODEBASE-CAPSULE-COMPACTION` - `.agents/todo-plans/114-codex-summary-codebase-capsule-compaction.md`
- [x] `CODEX-SUMMARY-FRESHNESS-REFRESH` - `.agents/todo-plans/115-codex-summary-freshness-refresh.md`

## Execution Order

1. Tighten `changeset-risk` to keep only the top decision signal and top factor samples.
2. Tighten `doc-sync-duplicates` to keep only the strongest fragment/conflict samples.
3. Tighten `codebase-capsule` to make the read-first capsule shorter and sharper.
4. Refresh `make codex-context` so the execution manifest and freshness audit reflect the new source timestamps.
5. Re-run the relevant validation and plan-completion audits, then close the batch.

## Closeout Rules

- Keep JSON evidence unchanged unless the source report itself must change.
- Prefer top-sample outputs over full inventories.
- If the stale freshness signal persists after regeneration, treat it as a real source drift issue and document the exact blocker instead of masking it.

## Completion Evidence

- Status: complete
- Execution status: complete
- Persistent backlog item: none
- Primary source files: `scripts/audits/score-changeset-risk.rb`, `scripts/audits/audit-doc-sync-duplicates.rb`, `scripts/audits/local_tooling_extended_tools.rb`, `scripts/audits/codex_local_context_gateway.rb`, `scripts/audits/audit-generated-artifact-freshness.rb`

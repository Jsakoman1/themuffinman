---
machine_kind: master-plan
machine_status: unknown
machine_title: Codex Summary Compactification Phase 4 Master Plan
machine_goal: Reduce token spend on the remaining high-noise report families by compacting
  them in a fixed sequence of narrow batch plans.
---

# Codex Summary Compactification Phase 4 Master Plan

Purpose: compact the remaining broad generated summaries and keep the local tooling outputs short enough for fast first-pass review.

## Goal

Reduce token spend on the remaining high-noise report families by compacting them in a fixed sequence of narrow batch plans.

## Child Plans

- [x] `CODEX-SUMMARY-INVENTORY-MAPS-COMPACTION` - `.agents/todo-plans/119-codex-summary-inventory-maps-compaction.md`
- [x] `CODEX-SUMMARY-SESSION-CONTEXT-COMPACTION` - `.agents/todo-plans/120-codex-summary-session-context-compaction.md`
- [x] `CODEX-SUMMARY-VALIDATION-CLOSEOUT-COMPACTION` - `.agents/todo-plans/121-codex-summary-validation-closeout-compaction.md`
- [x] `CODEX-SUMMARY-DIAGNOSTICS-REFERENCES-COMPACTION` - `.agents/todo-plans/122-codex-summary-diagnostics-references-compaction.md`

## Execution Order

1. Compact the inventory and map-style reports.
2. Compact session handoff, context, and pack-style reports.
3. Compact validation and closeout reports.
4. Compact diagnostics and reference reports.
5. Regenerate the affected summaries and run plan-completion audits before closing the batch.

## Closeout Rules

- Keep the change surface local to the shared local-tooling summary formatter and the affected generators.
- Preserve JSON outputs and validation evidence.
- If a new noisy family appears, split it into a follow-up batch instead of widening the current one.

## Completion Evidence

- Status: complete
- Execution status: complete
- Persistent backlog item: none
- Primary source files: `scripts/audits/local_tooling_extended_tools.rb`, `scripts/audits/audit-api-contract-drift.rb`, `scripts/audits/audit-mapper-usage.rb`, `scripts/audits/generate-architecture-decision-index.rb`, `scripts/audits/codex_local_context_gateway.rb`

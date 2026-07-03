---
machine_kind: master-plan
machine_status: unknown
machine_title: Codex Summary Compactification Phase 3 Master Plan
machine_goal: Make the remaining broad generated summaries shorter without losing
  the decision signal or the top evidence samples.
---

# Codex Summary Compactification Phase 3 Master Plan

Purpose: compact the remaining broad inventory summaries and finish the current summary-noise cleanup wave.

## Goal

Make the remaining broad generated summaries shorter without losing the decision signal or the top evidence samples.

## Child Plans

- [x] `CODEX-SUMMARY-CHANGESET-PLAYBOOK-COMPACTION` - `.agents/todo-plans/116-codex-summary-changeset-playbook-compaction.md`
- [x] `CODEX-SUMMARY-AUDIT-SUMMARY-INDEX-COMPACTION` - `.agents/todo-plans/117-codex-summary-audit-summary-index-compaction.md`
- [x] `CODEX-SUMMARY-ENDPOINT-CONTRACT-PACKS-COMPACTION` - `.agents/todo-plans/118-codex-summary-endpoint-contract-packs-compaction.md`

## Execution Order

1. Compact `changeset-playbook`.
2. Compact `audit-summary-index`.
3. Compact `endpoint-contract-packs/index`.
4. Regenerate the affected outputs and verify the reduced line counts.
5. Run plan-completion audits and close the batch.

## Closeout Rules

- Keep the summary changes local to the shared local-tooling formatter.
- Preserve JSON payloads and referenced evidence.
- If a new noisy family appears, split it into a follow-up batch rather than widening this one.

## Completion Evidence

- Status: complete
- Execution status: complete
- Persistent backlog item: none
- Primary source files: `scripts/audits/local_tooling_extended_tools.rb`, `scripts/audits/generate-changeset-playbook.rb`, `scripts/audits/generate-audit-summary-index.rb`, `scripts/audits/generate-endpoint-contract-packs.rb`

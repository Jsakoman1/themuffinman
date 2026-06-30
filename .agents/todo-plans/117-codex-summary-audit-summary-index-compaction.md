# Codex Summary Audit Summary Index Compaction

Purpose: shorten the audit summary index so it remains a quick lookup instead of a long registry dump.

## Goal

Turn the audit summary index into a compact registry overview with a small sample of representative summaries.

## Scope

- Compact the audit summary index markdown output.
- Keep the JSON index intact.
- Preserve the first relevant targets and output hints.

## Checklist

- [x] Tighten the summary rendering for registry and summary index payloads.
- [x] Regenerate the summary index and confirm it stays useful as a lookup.

## Validation

- `ruby -c scripts/audits/local_tooling_extended_tools.rb`
- `make audit-summary-index`

## Completion Evidence

- Status: complete
- Execution status: complete
- Persistent backlog item: none
- Primary source files: `scripts/audits/local_tooling_extended_tools.rb`, `scripts/audits/generate-audit-summary-index.rb`

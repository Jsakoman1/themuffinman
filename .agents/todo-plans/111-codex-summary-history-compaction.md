# Codex Summary History Compaction

Purpose: shorten history, index, and reference summaries that are mainly used to answer "what changed" without rereading the full generated reports.

## Goal

Reduce the size of the history and reference summaries while keeping the current decision, counts, and representative evidence lines available.

## Scope

- Compact test history, architecture decision index, codebase capsule, doc-canonical-phrases, and similar reference-style summaries.
- Preserve the ability to trace the source report if the compact summary is insufficient.
- Keep the JSON payload unchanged.

## Checklist

- [x] Tighten the shared summary formatter for history and reference-style reports.
- [x] Regenerate the affected reference summaries.
- [x] Confirm the compact summaries still surface the right anchor lines and counts.

## Validation

- `ruby -c scripts/audits/local_tooling_extended_tools.rb`
- `make test-history-summary`
- `make architecture-decision-index`
- `make audit-doc-canonical-phrases`
- `make audit-summary-index`

## Completion Evidence

- Status: complete
- Execution status: complete
- Persistent backlog item: none
- Primary source files: `scripts/audits/local_tooling_extended_tools.rb`, `scripts/audits/generate-test-history-summary.rb`, `scripts/audits/generate-architecture-decision-index.rb`, `scripts/audits/audit-doc-canonical-phrases.rb`, `scripts/audits/generate-audit-summary-index.rb`

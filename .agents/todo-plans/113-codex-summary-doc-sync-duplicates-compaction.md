# Codex Summary Doc Sync Duplicates Compaction

Purpose: make the doc-sync duplicates summary shorter by keeping only the strongest fragment and conflict samples.

## Goal

Reduce the doc-sync duplicates summary to a compact review list that still points at the exact maintenance hotspots.

## Scope

- Shorten the generated summary output for `doc-sync-duplicates`.
- Keep the JSON report untouched.
- Preserve fragment and conflict evidence.

## Checklist

- [x] Tighten the summary output to the top fragment and conflict samples.
- [x] Regenerate the report and confirm the review signal remains obvious.

## Validation

- `ruby -c scripts/audits/audit-doc-sync-duplicates.rb`
- `make audit-doc-sync-duplicates`

## Completion Evidence

- Status: complete
- Execution status: complete
- Persistent backlog item: none
- Primary source files: `scripts/audits/audit-doc-sync-duplicates.rb`

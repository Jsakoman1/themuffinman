# Codex Summary Validation Compaction

Purpose: shorten validation, doc-sync, and closeout-oriented summaries so the evidence path stays compact without losing failure anchors.

## Goal

Make validation-facing summaries shorter and easier to scan while keeping the commands, reasons, and counts that matter for closeout.

## Scope

- Compact generated summaries for validation presets, artifact freshness, doc-sync required surfaces, generated commit scope, and fast-check style reports.
- Keep the important commands and risk anchors visible.
- Avoid touching the underlying JSON evidence format.

## Checklist

- [x] Tighten the shared summary formatter for validation-style reports.
- [x] Regenerate the validation and closeout summaries.
- [x] Confirm the reports still expose the concrete next action and the main evidence signals.

## Validation

- `ruby -c scripts/audits/local_tooling_extended_tools.rb`
- `make audit-generated-artifact-freshness`
- `make audit-doc-sync-required-surfaces`
- `make audit-generated-commit-scope`
- `make recommend-validation-preset`
- `make fast-check`

## Completion Evidence

- Status: complete
- Execution status: complete
- Persistent backlog item: none
- Primary source files: `scripts/audits/local_tooling_extended_tools.rb`, `scripts/audits/audit-generated-artifact-freshness.rb`, `scripts/audits/audit-doc-sync-required-surfaces.rb`, `scripts/audits/audit-generated-commit-scope.rb`, `scripts/audits/recommend-validation-preset.rb`, `scripts/audits/generate-fast-check-report.rb`

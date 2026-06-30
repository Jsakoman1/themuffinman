# Codex Summary Changeset Risk Compaction

Purpose: make the changeset risk summary shorter while preserving the decision, the main risk drivers, and the next validation step.

## Goal

Reduce the size of the changeset-risk summary to a compact decision-first note with only the top signal factors.

## Scope

- Shorten the generated summary output for `changeset-risk`.
- Keep the JSON report untouched.
- Preserve the recommended commands and top factor evidence.

## Checklist

- [x] Tighten the summary output to the top decision signal.
- [x] Regenerate the report and confirm it still exposes the right next action.

## Validation

- `ruby -c scripts/audits/score-changeset-risk.rb`
- `make changeset-risk`

## Completion Evidence

- Status: complete
- Execution status: complete
- Persistent backlog item: none
- Primary source files: `scripts/audits/score-changeset-risk.rb`

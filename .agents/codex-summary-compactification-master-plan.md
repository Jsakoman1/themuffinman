# Codex Summary Compactification Master Plan

Purpose: shorten the highest-noise generated local-tooling summaries so Codex gets decision-first context faster without losing the underlying JSON evidence or validation signal.

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

## Execution Notes

- Keep JSON outputs unchanged.
- Prefer shorter counts, shortlist entries, and top factors over long file inventories.
- Keep failure anchors and regeneration commands when they are the main decision signal.
- If a summary is primarily used as a context pack input, keep the first read-first bullets only.

## Completion Criteria

- The targeted summaries are materially shorter and still usable as first-pass Codex context.
- The generated JSON evidence remains intact and in sync.
- Validation and closeout audits still pass.

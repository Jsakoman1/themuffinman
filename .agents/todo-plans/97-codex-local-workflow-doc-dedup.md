# CODEX-LOCAL-WORKFLOW-DOC-DEDUP Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `medium`
Master order: 97 of 99

## Backlog Item

Reduce duplicate workflow guidance across the main Codex docs so the canonical rules stay in one place and the supporting docs stay shorter and easier to scan.

## Source Findings

- [`docs/codex-fast-path.md`](/Users/jsakoman/Desktop/themuffinman/docs/codex-fast-path.md)
- [`docs/feature-delivery-workflow.md`](/Users/jsakoman/Desktop/themuffinman/docs/feature-delivery-workflow.md)
- [`docs/documentation-sync-policy.md`](/Users/jsakoman/Desktop/themuffinman/docs/documentation-sync-policy.md)
- [`docs/agent-operating-model.md`](/Users/jsakoman/Desktop/themuffinman/docs/agent-operating-model.md)
- [`docs/change-completion-checklist.md`](/Users/jsakoman/Desktop/themuffinman/docs/change-completion-checklist.md)

## Implementation Plan

- [x] Identify the repeated workflow statements that can safely become references instead of duplicate prose.
- [x] Preserve protected canonical wording where it is required by the sync rules.
- [x] Shorten the supporting docs so the compact entrypoint and the full workflow each have a clearer role.
- [x] Regenerate the affected doc summaries after the text changes.

## Expected Validation

- `make audit-documentation`
- `make audit-doc-sync-duplicates`
- `make audit-doc-canonical-phrases`
- `make audit-plan-completion plan=.agents/todo-plans/97-codex-local-workflow-doc-dedup.md`

## Completion Evidence

- Status: complete
- Backlog update: complete.
- Residual risk: protected canonical phrases must remain exact even when nearby guidance becomes shorter.

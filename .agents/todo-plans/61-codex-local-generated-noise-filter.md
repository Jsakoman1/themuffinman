# CODEX-LOCAL-GENERATED-NOISE-FILTER Plan

Source: `docs/codex-local-tooling-todo.md`
Group: `local-tooling`
Risk: `medium`
Master order: 61 of 82

## Backlog Item

Add a shared filter that excludes generated reports, large inventories, and transient `.agents/` plans from default `diff-summary`, `context-pack`, `audit-router`, and `fast-check` output unless explicitly requested.

Source notes:
  Proposed entrypoints:
  - Shared support in `scripts/local_tooling_common.rb`
  - Optional flags: `include_generated=true`, `include_agents=true`
  Proposed outputs:
  - Existing report JSON and Markdown files with `filtered_file_count` and `excluded_file_count`
  Notes:
  - This should make session-start summaries much cheaper to read when generated artifacts dominate the working tree.
  - Keep full output available for audit/debug sessions, but default Codex-facing reports should prioritize hand-authored source files.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make audit-agent-safety`
- [x] `make audit-local-tooling-incremental`
- [x] `cd apps/themuffinman/frontend && npm run type-check`
- [x] `cd apps/themuffinman/frontend && npm run build`

## Completion Evidence

- Status: complete
- Changed files:
  - `scripts/local_tooling_common.rb`
  - `scripts/audits/local_tooling_extended_tools.rb`
  - `docs/codex-local-tooling-todo.md`
  - `docs/domain-technical.md`
  - `docs/generated/local-tooling/diff-summary.json`
  - `docs/generated/local-tooling/diff-summary.md`
  - `docs/generated/local-tooling/context-packs/todo-master-plan.json`
  - `docs/generated/local-tooling/context-packs/todo-master-plan-summary.md`
  - `docs/generated/local-tooling/audit-router.json`
  - `docs/generated/local-tooling/audit-router-summary.md`
  - `docs/generated/local-tooling/fast-check-report.json`
  - `docs/generated/local-tooling/fast-check-report-summary.md`
- Validation evidence:
  - `ruby -c scripts/local_tooling_common.rb && ruby -c scripts/audits/local_tooling_extended_tools.rb` passed.
  - `make diff-summary` passed with default filtering.
  - `ruby scripts/audits/generate-diff-summary.rb include_generated=true include_agents=true` passed with full output.
  - `make context-pack topic=todo-master-plan` passed.
  - `make audit-router` passed.
  - `make fast-check` passed.
  - `make audit-plan-completion plan=.agents/todo-plans/61-codex-local-generated-noise-filter.md` passed after closeout.
  - `ruby scripts/todo-audit.rb` passed.
  - `make audit-agent-safety` passed.
  - `make audit-local-tooling-incremental` passed.
- Backlog update: removed `CODEX-LOCAL-GENERATED-NOISE-FILTER` from open local-tooling TODO items and added it to available local audits.
- Residual risk: default filtering is intentionally conservative; pass `include_generated=true` or `include_agents=true` when generated or `.agents` files are the primary review target.

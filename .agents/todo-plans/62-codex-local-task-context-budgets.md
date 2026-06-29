# CODEX-LOCAL-TASK-CONTEXT-BUDGETS Plan

Source: `docs/codex-local-tooling-todo.md`
Group: `local-tooling`
Risk: `medium`
Master order: 62 of 82

## Backlog Item

Add size budgets to context packs and summaries so generated Markdown never expands beyond a small, predictable token footprint.

Source notes:
  Proposed entrypoints:
  - `make context-pack topic=<topic> budget=small|medium|large`
  - `make session-handoff topic=<topic> budget=small|medium|large`
  Proposed outputs:
  - Existing context pack and handoff reports with `budget`, `omitted_sections`, and `read_next` fields
  Notes:
  - The default should be small enough for Codex to read first in nearly every implementation session.
  - Larger output should be opt-in when broad review is explicitly needed.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make audit-local-tooling-incremental`

## Completion Evidence

- Status: complete
- Changed files:
  - `Makefile`
  - `scripts/audits/local_tooling_extended_tools.rb`
  - `docs/codex-local-tooling-todo.md`
  - `docs/domain-technical.md`
  - `docs/generated/local-tooling/context-packs/todo-master-plan.json`
  - `docs/generated/local-tooling/context-packs/todo-master-plan-summary.md`
  - `docs/generated/local-tooling/session-handoffs/todo-master-plan.json`
  - `docs/generated/local-tooling/session-handoffs/todo-master-plan-summary.md`
- Validation evidence:
  - `ruby -c scripts/audits/local_tooling_extended_tools.rb` passed.
  - `make context-pack topic=todo-master-plan` passed with default `small` budget.
  - `make session-handoff topic=todo-master-plan` passed with default `small` budget.
  - `ruby scripts/audits/generate-context-pack.rb topic=todo-master-plan budget=large include_agents=true include_generated=true` passed with full opt-in context.
  - `make audit-plan-completion plan=.agents/todo-plans/62-codex-local-task-context-budgets.md` passed after closeout.
  - `ruby scripts/todo-audit.rb` passed.
  - `make audit-local-tooling-incremental` passed.
- Backlog update: removed `CODEX-LOCAL-TASK-CONTEXT-BUDGETS` from open local-tooling TODO items and added it to available local audits.
- Residual risk: summary size is controlled by file-count budgets, not byte-perfect token accounting.

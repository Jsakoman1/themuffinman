# Memory Active Backlog Cleanup

Purpose: move completed work out of active backlog registries and preserve only historical records and completion evidence.

## Goal

Ensure completed plans and master plans are not kept in open backlog files while their history remains discoverable through plan-completion and retrospective artifacts.

## Scope

- `docs/implementation-backlog.md`
- `docs/agent-improvement-backlog.md`
- `docs/codex-local-tooling-todo.md`
- `docs/generated/local-tooling/plan-completion/`
- `docs/generated/local-tooling/post-merge-retrospectives/`

## Checklist

- [x] Audit the active backlog files for resolved or historical-only items.
- [x] Remove completed items from the active backlog registries.
- [x] Keep historical evidence in plan-completion and retrospective artifacts.
- [x] Confirm no resolved item is left behind as an open task or checkbox.

## Validation

- `ruby scripts/todo-audit.rb`
- `make audit-plan-completion plan=.agents/product-memory-and-vision-master-plan.md`

## Completion Evidence

- Status: complete
- Execution status: complete
- Persistent backlog item: none
- Validation evidence:
  - `ruby scripts/todo-audit.rb`
  - `make audit-documentation`

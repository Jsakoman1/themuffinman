# Implementation Backlog

This file is the persistent source of truth for open implementation and product-delivery work that should survive across Codex sessions.

It complements `docs/agent-improvement-backlog.md`, which stays focused on agent-safety, audit tightening, and control-system work.

Keep only currently open items here. When an item is implemented, remove it from the open backlog in the same change and clear matching inline `TODO(<ID>):` or `FIXME(<ID>):` references.
Completed plans and master plans belong in plan-completion or retrospective artifacts, not as open backlog items in this file.

## Open Items

- `BACKEND-DRIFT-WORKMARKET-002`: Reduce the remaining workmarket backend hotspots by continuing the service-boundary cleanup around dashboard, quest state, validation, news, execution primitives, and application/admin/controller orchestration. Planned in `.agents/todo-plans/94-backend-hotspot-reduction-workmarket-social.md`.
- `BACKEND-DRIFT-SOCIAL-003`: Reduce the remaining social backend hotspots by continuing the service-boundary cleanup around circle read, discovery, relation, membership, assembler, and controller orchestration. Planned in `.agents/todo-plans/94-backend-hotspot-reduction-workmarket-social.md`.
- `BACKEND-DRIFT-IDENTITY-LOCATION-004`: Reduce the remaining identity and location backend drift after the workmarket and social slices settle, using the same service-boundary and read-model standardization pattern. Planned in `.agents/backend-drift-remediation-master-plan.md`.

## Intake Rules

- Record new deferred implementation or control-system work in the appropriate persistent backlog with a stable ID before closing the change that discovered it.
- Record new persistent implementation follow-ups here with a stable uppercase ID before closing the change that discovered them.
- If a deferred follow-up also needs an inline code note, use `TODO(<ID>):` or `FIXME(<ID>):` and make sure the same ID exists in an open backlog entry.
- When a backlog item is implemented, remove it from the open backlog and clear matching inline `TODO(<ID>):` or `FIXME(<ID>):` references in the same change.
- Do not keep resolved items here as historical checkboxes. Remove them once the implementing change is complete.

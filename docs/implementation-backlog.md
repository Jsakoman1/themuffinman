# Implementation Backlog

This file is the persistent source of truth for open implementation and product-delivery work that should survive across Codex sessions.

It complements `docs/agent-improvement-backlog.md`, which stays focused on agent-safety, audit tightening, and control-system work.

Keep only currently open items here. When an item is implemented, remove it from the open backlog in the same change and clear matching inline `TODO(<ID>):` or `FIXME(<ID>):` references.

## Open Items

- `BACKEND-MODEL-STANDARDIZATION-001`: Standardize backend transport models, read-model families, and service boundaries so similar domains can share naming, assemblers, DTO shapes, and validation patterns without ad-hoc per-feature drift. Planned in `.agents/backend-model-standardization-master-plan.md`.

## Intake Rules

- Record new deferred implementation or control-system work in the appropriate persistent backlog with a stable ID before closing the change that discovered it.
- Record new persistent implementation follow-ups here with a stable uppercase ID before closing the change that discovered them.
- If a deferred follow-up also needs an inline code note, use `TODO(<ID>):` or `FIXME(<ID>):` and make sure the same ID exists in an open backlog entry.
- When a backlog item is implemented, remove it from the open backlog and clear matching inline `TODO(<ID>):` or `FIXME(<ID>):` references in the same change.
- Do not keep resolved items here as historical checkboxes. Remove them once the implementing change is complete.

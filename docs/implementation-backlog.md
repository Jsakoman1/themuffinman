# Implementation Backlog

This file is the persistent source of truth for open implementation and product-delivery work that should survive across Codex sessions.

It complements `docs/agent-improvement-backlog.md`, which stays focused on agent-safety, audit tightening, and control-system work.

Keep only currently open items here. When an item is implemented, remove it from the open backlog in the same change and clear matching inline `TODO(<ID>):` or `FIXME(<ID>):` references.

## Open Items

- [ ] IMPL-QUEST-APPLICATION-DTO-AUDIT: Audit every backend read surface that returns `QuestApplicationResponseDTO` or derivative application collections, remove duplicated applicant-action DTO assembly, and harden remaining lazy-loading paths before more self-service actions are added.
  Scope to cover in the next pass: `QuestApplicationService.getApplicationsForQuest`, `getApplicationsViewForQuest`, `getPublicApprovedApplicationsViewForQuest`, `getAllApplicationsForAdmin`, `searchApplicationsForAdmin`, and the default transaction strategy for non-mutating application read methods.
  Working note: `.agents/quest-application-dto-audit-todo.md`
- [ ] IMPL-BUSINESS-HUB-MODULE: Replace the Business Hub placeholder with the first implemented module slice for business profiles, mini websites, calendars, or appointment booking.
- [ ] IMPL-THING-SHARING-MODULE: Replace the Thing Sharing placeholder with the first implemented lending and borrowing workflow slice.
- [ ] IMPL-CAR-SHARING-MODULE: Replace the Car Sharing placeholder with the first implemented voluntary circle-based ride coordination slice.
- [ ] IMPL-SHARED-CHAT-MODULE-SURFACE: Promote shared chat from cross-module capability into a fully implemented standalone module surface instead of a planned placeholder entry.

## Intake Rules

- Record new deferred implementation or control-system work in the appropriate persistent backlog with a stable ID before closing the change that discovered it.
- Record new persistent implementation follow-ups here with a stable uppercase ID before closing the change that discovered them.
- If a deferred follow-up also needs an inline code note, use `TODO(<ID>):` or `FIXME(<ID>):` and make sure the same ID exists in an open backlog entry.
- When a backlog item is implemented, remove it from the open backlog and clear matching inline `TODO(<ID>):` or `FIXME(<ID>):` references in the same change.
- Do not keep resolved items here as historical checkboxes. Remove them once the implementing change is complete.

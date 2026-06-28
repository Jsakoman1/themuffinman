# Agent Improvement Backlog

This file is the persistent forward-looking backlog for agent-safety, backend audit tightening, control-flow hardening, and documentation automation improvements.

It is not the execution source of truth for a single change. Active implementation details should still live in temporary plans under `.agents/`.

Use this file to preserve what remains worth tightening across sessions.

## Current State

- `executor_critical` backend audit tier is fail-hard.
- Strict `automation_relevant` DTO subsets now include:
- admin-agent DTO contracts
- chat DTO contracts
- identity DTO contracts
- location DTO contracts
- Backend audit inventory now assigns every backend file a `domainId` and `ownerId`.
- Current report-only remainder inside `automation_relevant` is still intentionally broad and should be tightened in small slices.

## Open Items

- [ ] AGENT-SOCIAL-DTO-REQUEST-RELATION: Tighten `social/dto` request and relation DTOs as the next small strict automation-relevant slice.
- [ ] AGENT-SOCIAL-DTO-OVERVIEW-MEMBER: Tighten `social/dto` overview and member DTOs after the request and relation slice is stable.
- [ ] AGENT-SOCIAL-DTO-SEARCH-CONTACT: Tighten `social/dto` search and contact-list DTOs after the overview and member slice is stable.
- [ ] AGENT-SOCIAL-DTO-ADMIN-CIRCLE: Tighten admin circle DTOs as the last social DTO slice instead of promoting the whole package at once.
- [ ] AGENT-WORKMARKET-DASHBOARD-DTO: Tighten workmarket dashboard DTOs as the first selected workmarket read-contract slice.
- [ ] AGENT-WORKMARKET-QUEST-DETAIL-DTO: Tighten workmarket quest-detail DTOs after the dashboard slice is stable.
- [ ] AGENT-WORKMARKET-APPLICATION-DETAIL-DTO: Tighten workmarket application-detail DTOs after the quest-detail slice is stable.
- [ ] AGENT-WORKMARKET-LIST-SEARCH-OPTIONS-DTO: Tighten workmarket list, search, and option DTOs after the detail slices are stable.
- [ ] AGENT-AUTOMATION-RELEVANT-SERVICE-COVERAGE: Keep broader `automation_relevant` service coverage report-first until DTO contract drift is much lower.
- [ ] AGENT-RULE-SCOPED-READ-MODEL-SLICES: Add more rule-scoped strict slices for high-value automation read models before considering wider tier promotion.
- [ ] AGENT-OWNERSHIP-AWARE-SOURCE-REPORTING: Add ownership-aware reporting to source-of-truth audit output, not only backend inventory output.
- [ ] AGENT-WORKFLOW-AWARE-FRONTEND-HELPERS: Tighten workflow-aware frontend helper coverage as a separate control-system slice.
- [ ] AGENT-USE-CASE-CONTRACT-HARNESS: Standardize use-case workflow contract harness coverage across more mutation surfaces.
- [ ] AGENT-DOC-TO-RUNTIME-SEMANTIC-CHECKS: Add stronger semantic checks between documented rules and runtime behavior.
- [ ] AGENT-BROADER-PLANNER-DTO-REGISTRATION: Extend source-of-truth registration audits to broader planner-visible DTO surfaces.

## Operating Rule

- Prefer small, low-noise strict slices over whole-tier tightening.
- When one slice is promoted, update this backlog to remove or narrow the remaining work.
- Do not treat this file as proof that a change was implemented; it is a continuity aid for future sessions.
- Record new deferred implementation or control-system work in the appropriate persistent backlog with a stable ID before closing the change that discovered it.
- Keep only open items here. When an item is implemented, remove it from the open backlog in the same change and clear matching inline `TODO(<ID>):` or `FIXME(<ID>):` references.
- When a backlog item is implemented, remove it from the open backlog and clear matching inline `TODO(<ID>):` or `FIXME(<ID>):` references in the same change.

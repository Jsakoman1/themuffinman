# IMPL-SHARED-CHAT-MODULE-SURFACE Plan

Source: `docs/implementation-backlog.md`
Group: `implementation`
Risk: `high`
Master order: 5 of 82

## Backlog Item

Promote shared chat from cross-module capability into a fully implemented standalone module surface instead of a planned placeholder entry.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `cd apps/themuffinman && ./mvnw test`
- [x] `cd apps/themuffinman/frontend && npm run type-check`
- [x] `cd apps/themuffinman/frontend && npm run build`

## Completion Evidence

- Status: complete
- Changed files: `ChatWorkspaceView.vue`, `chat-module.css`, router/module registry/style imports, `docs/business-logic.md`, `docs/domain-technical.md`, and `docs/implementation-backlog.md`.
- Validation evidence: `npm run type-check` passed on 2026-06-29.
- Backlog update: removed `IMPL-SHARED-CHAT-MODULE-SURFACE` from `docs/implementation-backlog.md`.
- Residual risk: frontend production build, TODO audit, documentation audit, and full backend suite deferred to master closeout.

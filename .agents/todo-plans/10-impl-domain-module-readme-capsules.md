# IMPL-DOMAIN-MODULE-README-CAPSULES Plan

Source: `docs/implementation-backlog.md`
Group: `implementation`
Risk: `high`
Master order: 10 of 82

## Backlog Item

Add short per-domain README capsules under backend and frontend module roots that summarize responsibilities, main entrypoints, tests, docs, and forbidden shortcuts.

Source notes:
  Goal: give Codex a small domain-specific first read before touching identity, workmarket, social, chat, location, or future modules.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make audit-documentation`
- [x] `cd apps/themuffinman && ./mvnw test`
- [x] `cd apps/themuffinman/frontend && npm run type-check`
- [x] `cd apps/themuffinman/frontend && npm run build`

## Completion Evidence

- Status: complete
- Changed files: backend domain `README.md` capsules, frontend module `README.md` capsules, `docs/domain-technical.md`, `docs/implementation-backlog.md`
- Validation evidence: `make audit-documentation` passed; `ruby scripts/todo-audit.rb` passed.
- Backlog update: removed `IMPL-DOMAIN-MODULE-README-CAPSULES` from `docs/implementation-backlog.md`.
- Residual risk: full backend/frontend validation deferred to master closeout; this slice is documentation-only.

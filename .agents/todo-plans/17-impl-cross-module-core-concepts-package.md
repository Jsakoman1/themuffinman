# IMPL-CROSS-MODULE-CORE-CONCEPTS-PACKAGE Plan

Source: `docs/implementation-backlog.md`
Group: `implementation`
Risk: `high`
Master order: 17 of 82

## Backlog Item

Create explicit shared backend abstractions for reusable concepts such as actor identity, circle visibility, scheduling windows, consent, and module ownership before adding more planned modules.

Source notes:
  Goal: avoid copy-paste domain concepts when business hub, thing sharing, car sharing, and booking features are introduced.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `cd apps/themuffinman && ./mvnw test`

## Completion Evidence

- Status: complete
- Changed files: added `common.concepts` value abstractions and tests, then wired actor identity, ownership, circle visibility selection, and scheduling windows into quest, location, ride, and dashboard services; updated business/domain docs and the implementation backlog.
- Validation evidence: targeted backend suite passed with 79 tests; full `cd apps/themuffinman && ./mvnw test` passed with 265 tests; `make audit-documentation` passed; `ruby scripts/todo-audit.rb` passed.
- Backlog update: removed `IMPL-CROSS-MODULE-CORE-CONCEPTS-PACKAGE` from `docs/implementation-backlog.md`.
- Residual risk: consent remains domain-specific until a concrete consent workflow is introduced; shared concepts now cover actor identity, ownership, circle visibility selection, and scheduling windows.

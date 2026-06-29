# IMPL-DOMAIN-EVENT-BOUNDARIES Plan

Source: `docs/implementation-backlog.md`
Group: `implementation`
Risk: `high`
Master order: 20 of 82

## Backlog Item

Add lightweight domain events for cross-module side effects such as quest news, chat notifications, reviews, booking updates, and future sharing workflows.

Source notes:
  Goal: keep service methods smaller and reduce hidden coupling as modules grow.

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

## Completion Evidence

- Status: complete
- Changed files: added `common/event` domain event primitives, `workmarket/event` application news event handling, event publisher wiring in application mutation use cases, docs, source-of-truth coverage, and tests.
- Validation evidence: `./mvnw test -Dtest=QuestApplicationServiceTest,QuestApplicationNewsEventHandlerTest` passed; `./mvnw test -Dtest=AgentOperatingModelValidationTest` passed; `./mvnw test` passed with 266 tests; `make audit-documentation` passed; `ruby scripts/todo-audit.rb` passed.
- Backlog update: removed `IMPL-DOMAIN-EVENT-BOUNDARIES` from `docs/implementation-backlog.md`.
- Residual risk: synchronous Spring event delivery is intentionally preserved for now; no async event bus or broad notification expansion was added.

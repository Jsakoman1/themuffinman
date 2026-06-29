# IMPL-DOMAIN-APPLICATION-SERVICE-LAYERING Plan

Source: `docs/implementation-backlog.md`
Group: `implementation`
Risk: `medium`
Master order: 12 of 82

## Backlog Item

Define and enforce a clearer split between application/use-case services, domain policy services, query/read services, and low-level primitives.

Source notes:
  Goal: make it obvious where new behavior belongs and reduce the need for Codex to inspect many neighboring services before editing.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make audit-documentation`
- [x] `cd apps/themuffinman && ./mvnw test -Dtest=ServiceLayeringConventionTest,ServiceTransactionConfigurationTest,QuestApplicationServiceTest,QuestServiceTest`

## Completion Evidence

- Status: complete
- Changed files: `ServiceLayeringConventionTest.java`, `docs/domain-technical.md`, `workmarket/README.md`, `docs/implementation-backlog.md`
- Validation evidence: `cd apps/themuffinman && ./mvnw test -Dtest=ServiceLayeringConventionTest,ServiceTransactionConfigurationTest,QuestApplicationServiceTest,QuestServiceTest` passed; `make audit-documentation` passed; `ruby scripts/todo-audit.rb` passed.
- Backlog update: removed `IMPL-DOMAIN-APPLICATION-SERVICE-LAYERING` from `docs/implementation-backlog.md`.
- Residual risk: full backend suite deferred to master closeout; enforcement currently covers the workmarket `*UseCase` public entrypoint rule.

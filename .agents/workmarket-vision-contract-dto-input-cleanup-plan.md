---
machine_kind: plan
machine_status: complete
machine_title: Workmarket Vision Contract DTO Input Cleanup Plan
machine_goal: Move workmarket-owned mutation request DTOs onto the workmarket package boundary so controllers and services no longer depend on vision input DTO classes.
---

# Workmarket Vision Contract DTO Input Cleanup Plan

## Status

Complete.

## Goal

Move workmarket-owned mutation request DTOs onto the workmarket package boundary so controllers and services no longer depend on vision input DTO classes.

## Scope

- Included: workmarket request/admin DTO ownership for quests, quest applications, and reviews; controller and service wiring; documentation and source-of-truth sync; focused validation.
- Excluded: response/read DTO extraction out of `vision`, frontend route or payload shape changes, and unrelated vision package cleanup.

## Slices

- [x] Add workmarket-owned request/admin DTO classes for quest, quest application, admin application update/query, and user review writes.
- [x] Rewire workmarket controllers, services, and mappers to consume the workmarket DTOs without `vision` request DTO imports.
- [x] Update source-of-truth and boundary documentation so the new DTO ownership is machine-auditable.
- [x] Run focused backend validation plus agent-operating-model validation and only then close the plan.

## Validation

- `./mvnw -q -Dtest=QuestServiceTest,QuestApplicationServiceTest,UserReviewServiceTest test`
- `./mvnw -q -Dtest=AgentOperatingModelValidationTest test`
- `make generate-agent-operating-model generate-agent-artifacts`
- `make control-refresh-full`

## Completion Evidence

- Status: complete
- Validation evidence:
  - `make generate-agent-operating-model generate-agent-artifacts`
  - `./mvnw -q -Dtest=WorkmarketRequestDtoValidationTest,QuestServiceTest,QuestApplicationServiceTest,UserReviewServiceTest,AgentOperatingModelValidationTest test`
  - `npm --prefix apps/themuffinman/frontend run generate:contracts`
  - `make control-refresh-full`
  - `ruby scripts/audits/audit-plan-completion.rb plan=.agents/workmarket-vision-contract-dto-input-cleanup-plan.md`
- Doc delta summary: workmarket now owns the mutation request DTO boundary for quests, quest applications, admin application updates/query, and reviews, while the legacy vision facade layer performs explicit DTO translation into workmarket-owned write contracts and the control-system inventory tracks both the new ownership and the remaining legacy adapter surface.
- Remaining boundary: shared read/response DTO contracts still live under `vision/dto` by design and continue to bridge through workmarket assemblers and the explicit vision-contract bridge.

---
machine_kind: plan
machine_status: complete
machine_title: Workmarket Read DTO Ownership Plan
machine_goal: Move workmarket-owned read and response DTO classes into the workmarket package while keeping legacy vision consumers compatible through explicit adapter translation.
---

# Workmarket Read DTO Ownership Plan

## Status

Complete.

## Goal

Move workmarket-owned read and response DTO classes into the workmarket package while keeping legacy vision consumers compatible through explicit adapter translation.

## Scope

- Included: quest, application, dashboard, news, review, and options read/response DTO classes currently imported by the workmarket package; mapper/service/controller import rewiring; compatibility bridge updates.
- Excluded: vision-only conversation DTOs and unrelated frontend-only request shapes.

## Slices

- [x] Create workmarket-owned copies of the read/response DTO classes and enums that the workmarket package currently imports from `vision.dto`.
- [x] Rewire workmarket mappers, assemblers, services, and controllers to the workmarket DTO package.
- [x] Update legacy vision facades and any remaining compatibility code to translate between workmarket DTOs and legacy vision DTOs explicitly.
- [x] Sync source-of-truth and documentation coverage for the new DTO ownership before validation.

## Validation

- `./mvnw -q -Dtest=VisionQuestFacadeServiceTest,VisionQuestApplicationFacadeServiceTest,QuestServiceTest,QuestApplicationServiceTest,DashboardServiceTest,QuestNewsServiceTest,UserReviewServiceTest,AgentOperatingModelValidationTest test`
- `npm --prefix apps/themuffinman/frontend run generate:contracts`
- `make control-refresh-full`

## Completion Evidence

- Status: complete
- Added workmarket-owned read/response DTO copies under `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/` and rewired workmarket controllers, services, mappers, and assemblers to import those contracts instead of legacy `vision.dto` read types.
- Kept legacy `vision` read surfaces compatible by remapping workmarket DTOs back to legacy compatibility DTOs inside the remaining `Vision*FacadeService` adapters through `WorkmarketVisionContractBridge`.
- Synced source-of-truth ownership and coverage in `docs/agent-operating-model/sections/source_of_truth.yaml`, `docs/agent-operating-model/sections/documentation_coverage.yaml`, `docs/source-of-truth-inventory.md`, and `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/README.md`.
- Validation passed on `2026-07-07`:
  - `./mvnw -q -Dtest=VisionQuestFacadeServiceTest,VisionQuestApplicationFacadeServiceTest,QuestServiceTest,QuestApplicationServiceTest,DashboardServiceTest,QuestNewsServiceTest,UserReviewServiceTest,AgentOperatingModelValidationTest test`
  - `npm --prefix apps/themuffinman/frontend run generate:contracts`
  - `make control-refresh-full`

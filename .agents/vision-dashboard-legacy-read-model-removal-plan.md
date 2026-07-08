---
machine_kind: plan
machine_status: completed
machine_title: Vision Dashboard Legacy Read Model Removal Plan
machine_goal: Migrate the last dashboard read-model island off legacy vision quest/application compatibility services and models.
---

# Vision Dashboard Legacy Read Model Removal Plan

## Status

Completed.

## Goal

Migrate the last dashboard read-model island off legacy `vision` quest/application compatibility services and models.

## Slices

- [x] Inventory every dashboard query, assembler, and DTO/model dependency that still assumes `vision.model.Quest` or `QuestApplication`.
- [x] Rewire dashboard read/query loading to workmarket-owned read contracts.
- [x] Rewire summary and section assembly to the new contract boundary.
- [x] Rewrite dashboard tests and validate the migrated path.

## Outcome

- The dashboard legacy read-model island was confirmed to be dead runtime code rather than an active migration target.
- Removed the unused legacy dashboard classes: `DashboardReadQueryService`, `DashboardReadModel`, `DashboardSummaryAssembler`, `DashboardSectionsFactory`, `DashboardSectionGrouper`, and `DashboardPlannerAssembler`.
- Removed the matching dead tests: `DashboardReadQueryServiceTest` and `DashboardSummaryAssemblerTest`.
- Retained `DashboardService` as the narrow legacy response-shape adapter over `WorkmarketDashboardService`; active dashboard orchestration remains workmarket-owned.

## Validation

- `cd apps/themuffinman && ./mvnw -q -DskipTests compile`
- `cd apps/themuffinman && ./mvnw -q -Dtest=DashboardServiceTest test`

## Completion Evidence

- Status: completed
- Removed the dead dashboard legacy read-model island and its obsolete tests.
- Validation:
  - `cd apps/themuffinman && ./mvnw -q -DskipTests compile`
  - `cd apps/themuffinman && ./mvnw -q -Dtest=DashboardServiceTest test`

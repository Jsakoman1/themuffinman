---
machine_kind: plan
machine_status: complete
machine_title: Workmarket Legacy Adapter Reduction Plan
machine_goal: Remove remaining workmarket HTTP dependence on legacy vision facade classes and keep only compatibility adapters that still serve the vision surface directly.
---

# Workmarket Legacy Adapter Reduction Plan

## Status

Complete.

## Goal

Remove remaining workmarket HTTP dependence on legacy vision facade classes and keep only compatibility adapters that still serve the vision surface directly.

## Scope

- Included: dashboard and news controller wiring, remaining facade indirections, and README/control-surface sync for the reduced adapter role.
- Excluded: deleting all legacy vision facades when they still have direct vision callers.

## Slices

- [x] Rewire workmarket dashboard and news controllers to workmarket services and mappers directly where no compatibility layer is needed.
- [x] Keep or tighten only the compatibility adapters that still serve direct `vision` consumers.
- [x] Update docs and control artifacts to reflect the narrower adapter footprint.
- [x] Validate and audit the plan after the read DTO ownership slice lands.

## Validation

- `./mvnw -q -Dtest=VisionQuestFacadeServiceTest,VisionQuestApplicationFacadeServiceTest,DashboardServiceTest,QuestNewsServiceTest,AgentOperatingModelValidationTest test`
- `make control-refresh-full`

## Completion Evidence

- Status: complete
- Rewired `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/DashboardController.java` to call `WorkmarketDashboardService` directly and `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/QuestNewsController.java` to call `WorkmarketQuestNewsService` and `WorkmarketQuestNewsMgr` directly.
- Narrowed the legacy adapter layer so the remaining `Vision*FacadeService` classes serve only legacy `vision` callers while translating through `WorkmarketVisionContractBridge`.
- Verified direct workmarket facade dependence was removed on `2026-07-07`:
  - `rg -n "Vision[A-Za-z]+FacadeService" apps/themuffinman/src/main/java/com/themuffinman/app/workmarket`
  - Result: only README text matched before wording cleanup; no workmarket Java code still depended on a vision facade.
  - `rg -n "com\.themuffinman\.app\.vision\.dto\." apps/themuffinman/src/main/java/com/themuffinman/app/workmarket`
  - Result: only intentional dashboard vision prompt/voice DTO inputs in `DashboardController` and legacy compatibility input mappers in `WorkmarketVisionContractBridge` remained.
- Validation passed on `2026-07-07`:
  - `./mvnw -q -Dtest=VisionQuestFacadeServiceTest,VisionQuestApplicationFacadeServiceTest,DashboardServiceTest,QuestNewsServiceTest,AgentOperatingModelValidationTest test`
  - `make control-refresh-full`

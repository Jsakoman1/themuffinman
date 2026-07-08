---
machine_kind: plan
machine_status: completed
machine_title: Vision Workmarket Facade Contract Cutover Plan
machine_goal: Shrink or remove the remaining vision marketplace facade contract layer so /vision orchestration no longer carries duplicate marketplace DTO ownership without a justified compatibility need.
---

# Vision Workmarket Facade Contract Cutover Plan

## Status

Completed.

## Goal

Shrink or remove the remaining `vision` marketplace facade contract layer so `/vision` orchestration no longer carries duplicate marketplace DTO ownership without a justified compatibility need.

## Slices

- [x] Re-audit `VisionQuestFacadeService`, `VisionQuestApplicationFacadeService`, `VisionDashboardFacadeService`, `VisionQuestNewsFacadeService`, `VisionOptionsService`, and `VisionUserReviewFacadeService` to classify which ones are still required by `/vision`.
- [x] Reduce `WorkmarketVisionContractBridge` to the minimum justified adapter surface and eliminate unused DTO/model translation paths.
- [x] Delete obsolete legacy `vision.dto` marketplace contract copies that are no longer consumed by `/vision` orchestration or stable public routes.
- [x] Refresh docs, generated contracts, and validation coverage for the final contract boundary.

## Completion Evidence

- Status: complete
- Legacy marketplace facade services and `WorkmarketVisionContractBridge` have been removed.
- `/dashboard`, `/quests`, `/applications`, `/news`, and `/reviews` now terminate in `workmarket.controller` with workmarket-owned services and contracts.
- `VisionCreateQuestExecutionAdapter` is the remaining explicit `/vision` execution bridge into workmarket.
- Validation passed on `2026-07-08`:
  - `./mvnw test`
  - `npm run generate:contracts`
  - `npm run type-check`
  - `npm run build`

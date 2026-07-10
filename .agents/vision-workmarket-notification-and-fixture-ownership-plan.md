---
machine_kind: plan
machine_status: complete
machine_title: Vision Workmarket Notification And Fixture Ownership Plan
machine_goal: Move the last cross-domain notification and fixture ownership from legacy
  `vision` marketplace types into `workmarket`-owned surfaces.
---

# Vision Workmarket Notification And Fixture Ownership Plan

## Status

Completed.

## Goal

Move the last cross-domain notification and fixture ownership from legacy `vision` marketplace types into `workmarket`-owned surfaces.

## Slices

- [x] Replace `CircleRelationService -> vision.service.QuestNewsService` with a workmarket-owned notification path.
- [x] Remove the remaining `vision.model.Quest` helper overloads from location and shared utility surfaces that are only needed for marketplace compatibility.
- [x] Move shared backend fixture builders away from `vision.model.Quest*` entities to `workmarket.model.Quest*`, then update affected tests.
- [x] Refresh docs and validate the ownership move.

## Completion Evidence

- Status: complete
- `CircleRelationService` now publishes quest-news side effects through `WorkmarketQuestNewsService`.
- Shared location/helper and test-fixture surfaces no longer depend on `vision.model.Quest*`.
- Validation passed on `2026-07-08`:
  - `./mvnw test`
  - `npm run type-check`
  - `npm run build`

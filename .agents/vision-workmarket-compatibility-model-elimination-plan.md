---
machine_kind: plan
machine_status: completed
machine_title: Vision Workmarket Compatibility Model Elimination Plan
machine_goal: Remove entity, repository, and mapper coupling that still keeps workmarket and legacy vision marketplace types alive in parallel.
---

# Vision Workmarket Compatibility Model Elimination Plan

## Status

Completed.

## Goal

Remove entity, repository, and mapper coupling that still keeps `workmarket` and legacy `vision` marketplace types alive in parallel.

## Slices

- [x] Audit the live callers of `vision.model`, `vision.repository`, and `vision.mapper` marketplace classes and keep only the callers required by explicit `/vision` compatibility boundaries.
- [x] Remove `WorkmarketQuestMgr` entity conversion methods that depend on `vision.model.Quest` once their callers are migrated.
- [x] Delete obsolete legacy marketplace repositories, models, and mappers that no longer serve a live compatibility path.
- [x] Refresh docs and validate the reduced runtime coupling.

## Completion Evidence

- Status: complete
- Obsolete legacy `vision` quest, application, news, and review entities, repositories, mappers, and enum copies have been removed from the runtime path.
- Remaining `/vision` marketplace references are limited to conversation, preview, and semantic-orchestration DTO surfaces.
- Validation passed on `2026-07-08`:
  - `./mvnw test`
  - `make generate-agent-operating-model`
  - `make generate-agent-artifacts`

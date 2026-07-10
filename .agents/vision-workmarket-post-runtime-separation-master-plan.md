---
machine_kind: master-plan
machine_status: complete
machine_title: Vision Workmarket Post Runtime Separation Master Plan
machine_goal: Remove the remaining legacy `vision` marketplace compatibility layers
  after runtime quest, application, dashboard, news, and review ownership has already
  moved into `workmarket`.
---

# Vision Workmarket Post Runtime Separation Master Plan

## Status

Completed.

## Goal

Remove the remaining legacy `vision` marketplace compatibility layers after runtime quest, application, dashboard, news, and review ownership has already moved into `workmarket`.

## Findings

- `workmarket` now owns the real marketplace runtime boundary, but `vision` still contains duplicate marketplace DTOs, models, repositories, and mappers.
- `vision` facade services still translate between legacy `vision` contracts and `workmarket` contracts through `WorkmarketVisionContractBridge`.
- `CircleRelationService` still publishes notification side effects through legacy `vision.service.QuestNewsService` instead of the workmarket-owned news service.
- `LocationGeoService` still keeps a `vision.model.Quest` overload, and shared backend fixtures still create `vision.model.Quest*` entities.
- `WorkmarketQuestMgr` still converts to and from `vision.model.Quest`, which keeps entity-level coupling alive even after service ownership moved.

## Child Plans

1. `.agents/vision-workmarket-notification-and-fixture-ownership-plan.md`
2. `.agents/vision-workmarket-compatibility-model-elimination-plan.md`
3. `.agents/vision-workmarket-facade-contract-cutover-plan.md`

## Execution Order

1. Move the remaining cross-domain notification and fixture ownership to workmarket.
2. Remove the remaining entity/repository/model coupling between workmarket and legacy vision marketplace types.
3. Cut the remaining facade contract bridge down to the minimum `/vision` orchestration surface and remove fully obsolete compatibility types.
4. Run a final closeout pass across docs, generated artifacts, tests, and plan audits.

## Closeout Requirements

- No `social`, `location`, `testing`, or `workmarket` runtime path should depend on `vision.model.Quest*`, `vision.repository.Quest*`, or `vision.service.QuestNewsService`.
- Remaining `vision` marketplace references, if any, must be limited to explicit `/vision` orchestration compatibility boundaries and documented as such.
- `docs/domain-technical.md`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/README.md`, and affected agent-operating artifacts must reflect the final ownership boundary.
- Validation must include backend compile/tests, frontend contract/type/build checks when contracts move, generated artifact refresh, freshness audit, and plan completion audit for the child plan plus this master plan.

## Completion Evidence

- Status: complete
- Workmarket now owns the marketplace runtime boundary across controller, service, mapper, repository, model, and notification surfaces.
- `/vision` retains only conversation, preview, voice, prompt, and semantic orchestration surfaces plus the DTOs those flows still render.
- Child plan status: complete
- Validation passed on `2026-07-08`:
  - `./mvnw test`
  - `npm run generate:contracts`
  - `npm run type-check`
  - `npm run build`
  - `make generate-agent-operating-model`
  - `make generate-agent-artifacts`

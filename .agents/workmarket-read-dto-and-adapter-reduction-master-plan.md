---
machine_kind: master-plan
machine_status: complete
machine_title: Workmarket Read DTO And Adapter Reduction Master Plan
machine_goal: Move workmarket-owned read/response contracts out of the legacy vision DTO package and shrink the remaining vision facade layer to compatibility-only adapters.
---

# Workmarket Read DTO And Adapter Reduction Master Plan

## Status

Complete.

## Goal

Move workmarket-owned read/response contracts out of the legacy vision DTO package and shrink the remaining vision facade layer to compatibility-only adapters.

## Scope

- Included: workmarket quest, application, dashboard, news, review, and options read/response DTO ownership; compatibility translation for legacy vision consumers; direct workmarket controller wiring where legacy facades are no longer needed; docs and control-model sync.
- Excluded: unrelated vision conversation DTOs, frontend UX redesign, and a full removal of legacy vision endpoints in one pass.

## Child Plans

1. Read DTO ownership
- Plan: `.agents/workmarket-read-dto-ownership-plan.md`
- Role: copy workmarket-owned read/response contracts into `workmarket/dto`, rewire workmarket services/controllers/assemblers to those types, and preserve compatibility bridges for legacy vision consumers.
- Status: complete

2. Legacy adapter reduction
- Plan: `.agents/workmarket-legacy-adapter-reduction-plan.md`
- Role: remove remaining dashboard and news dependence on vision facade classes inside the workmarket HTTP boundary and keep only compatibility adapters that are still externally consumed from `vision`.
- Status: complete

3. Final closeout
- Role: docs sync, generated artifacts, targeted/backend validation, and completion audits for both child plans plus the master plan.
- Status: complete

## Validation

- `./mvnw -q -Dtest=VisionQuestFacadeServiceTest,VisionQuestApplicationFacadeServiceTest,QuestServiceTest,QuestApplicationServiceTest,DashboardServiceTest,QuestNewsServiceTest,UserReviewServiceTest,AgentOperatingModelValidationTest test`
- `npm --prefix apps/themuffinman/frontend run generate:contracts`
- `make control-refresh-full`
- `ruby scripts/audits/audit-plan-completion.rb plan=.agents/workmarket-read-dto-ownership-plan.md`
- `ruby scripts/audits/audit-plan-completion.rb plan=.agents/workmarket-legacy-adapter-reduction-plan.md`
- `ruby scripts/audits/audit-plan-completion.rb plan=.agents/workmarket-read-dto-and-adapter-reduction-master-plan.md`

## Completion Evidence

- Status: complete
- Workmarket now owns its main quest, application, dashboard, news, review, and options read/response DTOs under `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/`, and the workmarket package reads those contracts natively.
- Legacy `vision` facade services were reduced to compatibility-only adapters that translate between legacy consumers and workmarket-owned contracts through `WorkmarketVisionContractBridge`.
- Docs and control surfaces were synced in the workmarket capsule README, source-of-truth inventory, documentation coverage, and generated agent-operating artifacts.
- Validation passed on `2026-07-07`:
  - `./mvnw -q -Dtest=VisionQuestFacadeServiceTest,VisionQuestApplicationFacadeServiceTest,QuestServiceTest,QuestApplicationServiceTest,DashboardServiceTest,QuestNewsServiceTest,UserReviewServiceTest,AgentOperatingModelValidationTest test`
  - `npm --prefix apps/themuffinman/frontend run generate:contracts`
  - `make control-refresh-full`
- Direct dependency audit passed on `2026-07-07`:
  - `rg -n "Vision[A-Za-z]+FacadeService" apps/themuffinman/src/main/java/com/themuffinman/app/workmarket`
  - `rg -n "com\.themuffinman\.app\.vision\.dto\." apps/themuffinman/src/main/java/com/themuffinman/app/workmarket`

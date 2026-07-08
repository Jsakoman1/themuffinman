---
machine_kind: master-plan
machine_status: complete
machine_title: Workmarket Extraction Master Plan
machine_goal: Extract the quest, application, dashboard, and review domain from the overloaded vision package into a dedicated workmarket module while keeping the current product behavior stable.
---

# Workmarket Extraction Master Plan

## Status

Complete.

## Goal

Extract the quest, application, dashboard, and review domain from the overloaded vision package into a dedicated workmarket module while keeping the current product behavior stable.

## Parent God Plan

- God Plan: `Plan System God Plan`
- Machine-readable path: `.agents/god-plans/plan-system-god-plan.yaml`

## Scope

- Included: workmarket package bootstrap, use-case/service migration, repository and model boundary stabilization, and documentation alignment.
- Excluded: frontend redesign, unrelated domain rewrites, and a risky all-at-once package rename.

## Child Plans

1. `.agents/workmarket-bootstrap-plan.md`
- Role: establish the first real `com.themuffinman.app.workmarket` entrypoints and fix the layering contract to recognize them.
- Status: complete

2. `.agents/workmarket-model-and-repository-plan.md`
- Role: move the core quest/application entities, repositories, and fetch profiles behind the dedicated workmarket package.
- Status: complete

3. `.agents/workmarket-vision-alignment-plan.md`
- Role: switch `vision` to consume the new workmarket boundary, then remove duplicated ownership from the vision package.
- Status: complete

4. `.agents/workmarket-closeout-plan.md`
- Role: validate the migration, reconcile docs, and close the extraction cleanly.
- Status: complete

## Pros

- Gives quests and applications a real ownership boundary instead of only a conceptual one.
- Reduces the amount of domain logic hidden inside the adaptive vision surface.
- Lets the migration happen in smaller, auditable slices.

## Cons

- Introduces a temporary duplication boundary while the migration is in flight.
- Requires careful coordination so the product surface does not drift during the extraction.

## Dependencies

- `docs/domain-technical.md`
- `docs/business-logic.md`
- `docs/source-of-truth-inventory.md`
- `apps/themuffinman/src/test/java/com/themuffinman/app/config/ServiceLayeringConventionTest.java`

## Validation

- Targeted checks: workmarket service-layering contract tests and focused unit tests for the first adapter slice.
- Broader checks: backend test suite coverage for the migrated domain surfaces.
- Closeout checks: docs and source-of-truth inventory reflect the new package ownership.

## Completion Evidence

- Status: complete
- Child plan status: complete
- Validation evidence: targeted layering, facade, fetch-profile, generated-artifact, operating-model, and broader quest/application/dashboard/news/review regression validation all pass for the extracted ownership state
- Doc delta summary: workmarket ownership now includes quest, application, dashboard, news, review, the HTTP entrypoints for those surfaces, and the canonical quest/application repository fetch-profile contract
- Deferred work: DTO-facing vision adapter types remain an intentional boundary and are not required for this extraction closeout

## Follow-up Todo

- Split the remaining shared `vision` contract DTOs and enum-facing boundary out of `workmarket` only if the controller/service API layer is intentionally being rewritten with it.
- Keep `workmarket` README, source-of-truth inventory, and operating-model coverage aligned with any later DTO boundary split.

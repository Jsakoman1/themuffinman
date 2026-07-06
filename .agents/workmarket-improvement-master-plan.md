---
machine_kind: master-plan
machine_status: draft
machine_title: Workmarket Improvement Master Plan
machine_goal: Improve the workmarket module so quests, applications, dashboards, reviews, and notifications have a cleaner backend boundary and fewer vision-era leftovers.
---

# Workmarket Improvement Master Plan

## Status

Draft.

## Goal

Improve the workmarket module so quests, applications, dashboards, reviews, and notifications have a cleaner backend boundary and fewer vision-era leftovers.

## Parent God Plan

- God Plan: `Plan System God Plan`
- Machine-readable path: `.agents/god-plans/plan-system-god-plan.yaml`

## Scope

- Included: quest and application boundary cleanup, read-model simplification, policy consolidation, notification flow cleanup, and workmarket-specific testing and docs.
- Excluded: unrelated module rewrites, frontend redesign outside workmarket surfaces, and a full package rename in one pass.

## Child Plans

1. Boundary cleanup
- Role: remove remaining vision ownership from the workmarket domain.
- Status: pending

2. Read-model cleanup
- Role: make list, detail, dashboard, and review reads more consistent and fetch-safe.
- Status: pending

3. Workflow cleanup
- Role: tighten transitions, approval flows, and notification side effects.
- Status: pending

4. Docs and tests cleanup
- Role: keep workmarket tests, README, and domain docs aligned with the actual boundary.
- Status: pending

## Improvement Checklist

- [ ] Finish moving any remaining quest and application ownership logic out of `vision` and into `workmarket`.
- [ ] Reduce the number of `vision` DTO types that still leak across the workmarket service boundary.
- [ ] Introduce or strengthen workmarket-native DTOs where the boundary is now stable enough to own them directly.
- [ ] Keep quest, application, dashboard, and review policy decisions in dedicated service-layer policy classes.
- [ ] Standardize fetch-safe repository methods for quest list, quest detail, application detail, and dashboard reads.
- [ ] Make read-model assemblers consistent so the controller layer does less orchestration work.
- [ ] Keep notification side effects behind domain events instead of calling them directly from every mutation path.
- [ ] Separate owner, applicant, admin, and public read paths more explicitly so permission logic is easier to audit.
- [ ] Align quest state transitions and term-change flows with a smaller, clearer service surface.
- [ ] Add more scenario-style tests around the full quest and application lifecycle instead of only narrow unit coverage.
- [ ] Tighten the workmarket README so it reflects the actual responsibility boundary and no longer lags behind the code.
- [ ] Make the workmarket module more self-contained so future `vision` work can consume it without re-implementing the same rules.
- [ ] Review whether dashboard section assembly can be simplified further without losing backend-prepared meaning.
- [ ] Keep review and news flows explicit so they do not become hidden side effects of unrelated quest mutations.

## Pros

- Gives the workmarket boundary a clearer identity.
- Reduces the amount of domain logic trapped inside the adaptive vision surface.
- Makes future quest/application changes easier to test and reason about.

## Cons

- Requires careful boundary migration so behavior does not drift during extraction.
- Some shared DTO and mapper cleanup will still be transitional.

## Dependencies

- `docs/business-logic.md`
- `docs/domain-technical.md`
- `apps/themuffinman/src/test/java/com/themuffinman/app/config/ServiceLayeringConventionTest.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/README.md`

## Validation

- Targeted checks: service-layering tests and focused workmarket service tests.
- Broader checks: backend tests for quest, application, dashboard, review, and news flows.
- Closeout checks: confirm docs and boundary notes match the implemented package ownership.

## Completion Evidence

- Status: draft
- Child plan status: pending
- Validation evidence: not run
- Doc delta summary: new workmarket improvement plan created to continue boundary cleanup and read-model simplification.
- Deferred work: none

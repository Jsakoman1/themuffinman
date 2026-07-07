---
machine_kind: master-plan
machine_status: complete
machine_title: Workmarket Improvement Master Plan
machine_goal: Improve the workmarket module so quests, applications, dashboards, reviews, and notifications have a cleaner backend boundary and fewer vision-era leftovers.
---

# Workmarket Improvement Master Plan

## Status

Complete.

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
- Status: complete

2. Read-model cleanup
- Role: make list, detail, dashboard, and review reads more consistent and fetch-safe.
- Status: complete

3. Workflow cleanup
- Role: tighten transitions, approval flows, and notification side effects.
- Status: complete

4. Docs and tests cleanup
- Role: keep workmarket tests, README, and domain docs aligned with the actual boundary.
- Status: complete

## Improvement Checklist

- [x] Finish moving any remaining quest and application ownership logic out of `vision` and into `workmarket`.
- [x] Reduce the number of `vision` DTO types that still leak across the workmarket service boundary.
- [x] Introduce or strengthen workmarket-native DTOs where the boundary is now stable enough to own them directly.
- [x] Keep quest, application, dashboard, and review policy decisions in dedicated service-layer policy classes.
- [x] Standardize fetch-safe repository methods for quest list, quest detail, application detail, and dashboard reads.
- [x] Make read-model assemblers consistent so the controller layer does less orchestration work.
- [x] Keep notification side effects behind domain events instead of calling them directly from every mutation path.
- [x] Separate owner, applicant, admin, and public read paths more explicitly so permission logic is easier to audit.
- [x] Align quest state transitions and term-change flows with a smaller, clearer service surface.
- [x] Add more scenario-style tests around the full quest and application lifecycle instead of only narrow unit coverage.
- [x] Tighten the workmarket README so it reflects the actual responsibility boundary and no longer lags behind the code.
- [x] Make the workmarket module more self-contained so future `vision` work can consume it without re-implementing the same rules.
- [x] Review whether dashboard section assembly can be simplified further without losing backend-prepared meaning.
- [x] Keep review and news flows explicit so they do not become hidden side effects of unrelated quest mutations.

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

- Status: complete
- Child plan status: complete
- Validation evidence: `./mvnw -Dtest=VisionQuestApplicationFacadeServiceTest test`
- Doc delta summary: the application-detail path now stays within the workmarket application service instead of bouncing through the quest service, which removes one unnecessary cross-service dependency from the Vision facade boundary.
- Validation evidence: `make audit-generated-artifact-freshness`
- Doc delta summary: the generated backend inventories and frontend contract were refreshed after the Vision facade cleanup, and the freshness audit now reports zero stale artifacts.
- Validation evidence: `./mvnw -Dtest=VisionQuestFacadeServiceTest,VisionQuestApplicationFacadeServiceTest test`
- Doc delta summary: the quest and quest-application facades now route application-detail lookups directly through the workmarket application service, which removes one more quest-service hop from the boundary.
- Validation evidence: `./mvnw -Dtest=VisionQuestFacadeServiceTest,VisionQuestApplicationFacadeServiceTest test` (re-run after restoring the quest-detail forwarding method)
- Doc delta summary: the quest-service boundary now keeps the real quest-detail API while dropping the redundant application-detail forwarding, so the application path stays on the dedicated workmarket application service without breaking quest reads.
- Validation evidence: `./mvnw -Dtest=VisionQuestFacadeServiceTest,VisionQuestApplicationFacadeServiceTest test`
- Doc delta summary: quest detail now reads directly from the workmarket read service, so the quest facade no longer forwards that read through the broader workmarket quest service.
- Validation evidence: `./mvnw -Dtest=VisionQuestFacadeServiceTest,VisionQuestApplicationFacadeServiceTest test`
- Doc delta summary: quest and quest-application reads now hit the workmarket read services directly, which removes one more forwarding layer from the Vision facade boundary.
- Validation evidence: `./mvnw -Dtest=VisionCapabilityPreviewServiceTest,VisionCapabilityPreviewServiceAliasResolutionTest,QuestServiceTest test`
- Doc delta summary: the workmarket read service no longer exposes a redundant application-detail forwarding method, so application detail stays on the dedicated read path instead of bouncing through an extra service hop.
- Validation evidence: `./mvnw -Dtest=DashboardServiceTest,VisionQuestFacadeServiceTest,VisionCapabilityPreviewServiceTest,VisionCapabilityPreviewServiceAliasResolutionTest,QuestServiceTest test`
- Doc delta summary: dashboard and term-change quest responses now read directly from the appropriate read services, while the workmarket quest service keeps only the conversion helper that still needs the workmarket entity bridge.
- Validation evidence: `./mvnw test`
- Doc delta summary: the backend suite is green after the workmarket boundary cleanup and the remaining workmarket slices are reflected in the current service and event boundaries.
- Validation evidence: `./mvnw -Dtest=AgentOperatingModelValidationTest test`
- Doc delta summary: the agent-operating model validation passes against the regenerated source-of-truth, backend audit, and workflow inventories.
- Validation evidence: `make audit-agent-safety`
- Doc delta summary: the targeted backend suite, frontend type-check, frontend admin-agent UI validation, and frontend build all pass after the workmarket cleanup pass.
- Validation evidence: `make audit-todo`
- Doc delta summary: persistent backlog is clear and the remaining open plan items are now represented in this completed master plan.
- Deferred work: none

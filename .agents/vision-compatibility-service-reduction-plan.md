---
machine_kind: plan
machine_status: complete
machine_title: Vision Compatibility Service Reduction Plan
machine_goal: Reduce duplicated legacy vision service logic by turning isolated read and review services into compatibility adapters over workmarket-owned services.
---

# Vision Compatibility Service Reduction Plan

## Status

Complete.

## Goal

Reduce duplicated legacy vision service logic by turning isolated read and review services into compatibility adapters over workmarket-owned services.

## Scope

- Included: `VisionOptionsService`, `UserReviewService`, their tests, and the supporting compatibility bridge updates and docs sync.
- Excluded: broader rewrite of `QuestService`, `QuestApplicationService`, `QuestReadService`, or `DashboardService` in this slice.

## Slices

- [x] Convert `VisionOptionsService` into a compatibility adapter over `WorkmarketOptionsService`.
- [x] Convert `UserReviewService` into a compatibility adapter over `WorkmarketUserReviewService`.
- [x] Sync affected docs and control artifacts for the narrower legacy vision service footprint.
- [x] Validate and audit the plan before closeout.

## Validation

- `./mvnw -q -Dtest=VisionOptionsServiceTest,UserReviewServiceTest,QuestWorkflowScenarioTest,ServiceTransactionConfigurationTest,AgentOperatingModelValidationTest test`
- `make generate-agent-operating-model generate-agent-artifacts`
- `make control-refresh-full`
- `ruby scripts/audits/audit-plan-completion.rb plan=.agents/vision-compatibility-service-reduction-plan.md`

## Completion Evidence

- Status: complete
- `VisionOptionsService` now delegates to `WorkmarketOptionsService` and remaps the workmarket-owned options contract back into legacy `vision` DTOs instead of rebuilding a parallel options surface.
- `UserReviewService` now delegates to `WorkmarketUserReviewService`; rating and recent-review reads use workmarket-owned contracts, while legacy `vision` review responses are rebuilt explicitly for compatibility.
- The compatibility bridge now includes `toWorkmarketReviewRole(...)`, and the workmarket capsule README records that `VisionOptionsService` and `UserReviewService` have been reduced to compatibility adapters.
- Validation passed on `2026-07-07`:
  - `./mvnw -q -DskipTests compile`
  - `./mvnw -q -Dtest=VisionOptionsServiceTest,UserReviewServiceTest,QuestWorkflowScenarioTest,ServiceTransactionConfigurationTest,AgentOperatingModelValidationTest test`
  - `npm --prefix apps/themuffinman/frontend run generate:contracts`
  - `make generate-agent-operating-model generate-agent-artifacts`
  - `make control-refresh-full`

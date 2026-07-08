---
machine_kind: plan
machine_status: complete
machine_title: Legacy Vision Service Inventory And Dashboard Reduction Plan
machine_goal: Record the remaining legacy vision service dependency graph and reduce DashboardService to a compatibility adapter over workmarket-owned dashboard services.
---

# Legacy Vision Service Inventory And Dashboard Reduction Plan

## Status

Complete.

## Goal

Record the remaining legacy vision service dependency graph and reduce `DashboardService` to a compatibility adapter over workmarket-owned dashboard services.

## Scope

- Included: usage inventory for `QuestService`, `QuestReadService`, `QuestApplicationService`, and `DashboardService`; `DashboardService` adapter reduction; test and doc sync for that slice.
- Excluded: rewriting `QuestService`, `QuestReadService`, or `QuestApplicationService` in this same pass.

## Slices

- [x] Capture the remaining runtime and test dependency inventory for the main legacy vision services.
- [x] Convert `DashboardService` into a compatibility adapter over `WorkmarketDashboardService`.
- [x] Update tests and docs for the reduced legacy dashboard role.
- [x] Validate and audit the plan before closeout.

## Validation

- `./mvnw -q -Dtest=DashboardServiceTest,ServiceTransactionConfigurationTest,AgentOperatingModelValidationTest test`
- `make generate-agent-operating-model generate-agent-artifacts`
- `make control-refresh-full`
- `ruby scripts/audits/audit-plan-completion.rb plan=.agents/legacy-vision-service-inventory-and-dashboard-reduction-plan.md`

## Completion Evidence

- Status: complete
- Remaining legacy inventory captured on `2026-07-07`:
  - `DashboardService` had no remaining runtime consumers outside the legacy `vision` package and tests, making it the safest first large compatibility-service reduction.
  - `QuestService`, `QuestReadService`, and `QuestApplicationService` still anchor the heavier legacy runtime/test graph through preview, discovery, agent execution, and workflow tests, so they remain the next larger reduction surfaces.
- `DashboardService` now delegates to `WorkmarketDashboardService` and remaps workmarket dashboard DTOs back into legacy `vision` DTOs through `WorkmarketVisionContractBridge`.
- `DashboardServiceTest` was rewritten to assert delegation and compatibility mapping instead of the removed legacy dashboard assembly path, and the workmarket capsule README records the narrower dashboard role.
- Validation passed on `2026-07-07`:
  - `./mvnw -q -DskipTests compile`
  - `./mvnw -q -Dtest=DashboardServiceTest,ServiceTransactionConfigurationTest,AgentOperatingModelValidationTest test`

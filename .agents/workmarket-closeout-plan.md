---
machine_kind: plan
machine_status: complete
machine_title: Workmarket Closeout Plan
machine_goal: Validate the extracted workmarket ownership end-to-end, reconcile closeout evidence, and close the master plan only if every child slice and validation requirement is actually complete.
---

# Workmarket Closeout Plan

## Status

Complete.

## Goal

Validate the extracted workmarket ownership end-to-end, reconcile closeout evidence, and close the master plan only if every child slice and validation requirement is actually complete.

## Scope

- Included: final validation for quest/application/dashboard/news/review surfaces, generated-artifact freshness, plan evidence reconciliation, and master-plan closeout.
- Excluded: deeper DTO package rewrites and unrelated vision cleanup.

## Slices

- [x] Run broader quest/application/dashboard/news/review validation now that workmarket owns the HTTP entrypoints and repositories.
- [x] Reconcile docs and generated artifacts with the final ownership state.
- [x] Close the child plan and then close the master plan only if completion audit passes with no open child statuses.

## Validation

- `./mvnw -q -Dtest=QuestServiceTest,QuestApplicationServiceTest,DashboardServiceTest,QuestNewsServiceTest,UserReviewServiceTest,QuestWorkflowScenarioTest test`
- `./mvnw -q -Dtest=AgentOperatingModelValidationTest test`
- `make control-refresh-full`
- `ruby scripts/audits/audit-plan-completion.rb plan=.agents/workmarket-closeout-plan.md`
- `ruby scripts/audits/audit-plan-completion.rb plan=.agents/workmarket-extraction-master-plan.md`

## Completion Evidence

- Status: complete
- Validation evidence:
  - `./mvnw -q -Dtest=QuestServiceTest,QuestApplicationServiceTest,DashboardServiceTest,QuestNewsServiceTest,UserReviewServiceTest,QuestWorkflowScenarioTest test`
  - `./mvnw -q -Dtest=AgentOperatingModelValidationTest test`
  - `make control-refresh-full`
- Doc delta summary: the final closeout confirms workmarket now owns quest, application, dashboard, news, and review HTTP entrypoints plus the canonical quest/application repository boundary, with generated artifacts refreshed to that ownership state.
- Master-plan gate: close the master plan only after both the child closeout plan audit and the master-plan audit pass with no open child statuses.

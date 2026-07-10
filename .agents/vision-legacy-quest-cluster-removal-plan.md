---
machine_kind: plan
machine_status: complete
machine_title: Vision Legacy Quest Cluster Removal Plan
machine_goal: Remove the remaining dead legacy `vision` quest/application cluster
  and then delete the now-unused vision location compatibility overloads.
---

# Vision Legacy Quest Cluster Removal Plan

## Status

Completed.

## Goal

Remove the remaining dead legacy `vision` quest/application cluster and then delete the now-unused vision location compatibility overloads.

## Slices

- [x] Confirm the remaining legacy quest/application services and use cases have no live runtime entrypoint outside their own cluster.
- [x] Delete the dead quest/application cluster and its obsolete tests.
- [x] Remove the now-unused `vision` quest overloads from location services, sync docs/artifacts, and validate the final state.

## Completion Evidence

- Status: completed
- Confirmed the remaining legacy `vision` quest/application services had no live runtime entrypoint outside the already-removed compatibility layer, then deleted the dead quest/application cluster, mapper, event handler, and obsolete tests.
- Removed the last `vision` quest overloads from `LocationSettingsService` and `LocationQuestPresentationService`, leaving workmarket-owned quest location handling as the only runtime path.
- Repointed the agent-operating source-of-truth, workflow, audit, regression, and docs-as-contract sections to workmarket-owned services and surviving regression coverage.
- Updated the frontend contract generator to namespace colliding DTO schemas across `vision` and `workmarket`, then regenerated the generated contract successfully.
- Validation:
  - `cd apps/themuffinman && ./mvnw -q -DskipTests compile`
  - `cd apps/themuffinman && ./mvnw -q -Dtest=QuestWorkflowScenarioTest,VisionQuestFacadeServiceTest,VisionQuestApplicationFacadeServiceTest,QuestNewsServiceTest,DashboardServiceTest,ServiceTransactionConfigurationTest,AgentOperatingModelValidationTest test`
  - `cd apps/themuffinman/frontend && npm run type-check`
  - `cd apps/themuffinman/frontend && npm run build`
  - `make generate-agent-operating-model`
  - `make generate-agent-artifacts`
  - `make plan-index`
  - `make codex-context topic=vision-legacy-quest-cluster-removal intent='remove remaining vision quest legacy cluster'`
  - `make control-start`
  - `make audit-generated-artifact-freshness`

---
machine_kind: master-plan
machine_status: complete
machine_title: Platform Ten Out Of Ten Master Plan
machine_goal: Lift the control implementation system, workmarket system, and vision system from their current strong state to a cleaner and more durable ten-out-of-ten baseline without reintroducing ownership drift or validation noise.
---

# Platform Ten Out Of Ten Master Plan

## Status

Complete.

## Goal

Lift the control implementation system, workmarket system, and vision system from their current strong state to a cleaner and more durable ten-out-of-ten baseline without reintroducing ownership drift or validation noise.

## Parent God Plan

- God Plan: none
- Machine-readable path: none

## Scope

- Included: control-surface hardening, workmarket complexity reduction, vision orchestration tightening, doc sync, generated-artifact hygiene, validation, and final closeout evidence.
- Excluded: new product features, large UX redesign, speculative shared abstraction layers, and any rollback of the workmarket-versus-vision separation.

## Child Plans

1. `.agents/control-system-ten-out-of-ten-plan.md`
- Role: remove the last control-system friction, noise, and false-positive space so the implementation system stays strict without feeling heavy.
- Status: complete

2. `.agents/workmarket-ten-out-of-ten-plan.md`
- Role: reduce internal complexity inside the marketplace owner boundary until the module is not just separated, but also easier to read, change, and validate.
- Status: complete

3. `.agents/vision-ten-out-of-ten-plan.md`
- Role: keep vision orchestration-only, shrink shadow-contract risk, and make remaining cross-domain dependencies explicit and stable.
- Status: complete

## Improvement Checklist

- [x] Define one ordered child plan per concrete improvement slice.
- [x] Confirm that a Master Plan is really needed and that one durable Plan would not be clearer.
- [x] Keep the child-plan order stable unless the implementation sequence changes.
- [x] Include a short validation and closeout path for the whole batch.
- [x] Record any deferred follow-up items in a persistent backlog.
- [x] Do not mark the master plan complete until every child plan row is complete or explicitly deferred.

## Dependencies

- The current `main` baseline already has closed open plans, green validation, and a finished workmarket-versus-vision runtime separation.
- The child plans must preserve `Open count: 0` in the compact control snapshot by the end of the batch.
- Workmarket must remain the marketplace runtime owner and vision must remain orchestration-only throughout all slices.

## Validation

- Targeted checks:
  - child-plan targeted backend suites
  - `npm --prefix apps/themuffinman/frontend run type-check`
  - `npm --prefix apps/themuffinman/frontend run build`
- Broader checks:
  - `./mvnw test`
  - `make control-start`
  - `make audit-generated-artifact-freshness`
- Closeout checks:
  - `make generate-agent-operating-model`
  - `make generate-agent-artifacts`
  - `make generate-frontend-contracts`
  - `ruby scripts/audits/audit-plan-completion.rb plan=.agents/control-system-ten-out-of-ten-plan.md`
  - `ruby scripts/audits/audit-plan-completion.rb plan=.agents/workmarket-ten-out-of-ten-plan.md`
  - `ruby scripts/audits/audit-plan-completion.rb plan=.agents/vision-ten-out-of-ten-plan.md`
  - `ruby scripts/audits/audit-plan-completion.rb plan=.agents/platform-ten-out-of-ten-master-plan.md`

## Completion Evidence

- Status: complete
- Child plan status: complete
- Validation evidence: control child validated with `make control-start`, `make audit-generated-artifact-freshness`, and `./mvnw -Dtest=AgentOperatingModelValidationTest,WorkflowStateMachineCatalogTest test`; workmarket child validated with `./mvnw -Dtest=QuestWorkflowScenarioTest,WorkmarketRequestDtoValidationTest,AdminUserDetailServiceTest,WorkflowStateMachineCatalogTest test`, `npm --prefix apps/themuffinman/frontend run type-check`, `npm --prefix apps/themuffinman/frontend run build`, `make generate-agent-operating-model`, `make generate-agent-artifacts`, `make generate-frontend-contracts`, `make control-start`, and `make audit-generated-artifact-freshness`; vision child validated with `./mvnw -Dtest=VisionConversationServiceTest,VisionCapabilityPreviewServiceTest,VisionCapabilityPreviewServiceAliasResolutionTest,VisionSearchDiscoveryServiceTest,VisionSemanticAuditMatrixTest,AgentOperatingModelValidationTest test`, `npm --prefix apps/themuffinman/frontend run type-check`, `npm --prefix apps/themuffinman/frontend run build`, `./mvnw test`, `make generate-agent-operating-model`, `make generate-agent-artifacts`, and `make generate-frontend-contracts`
- Doc delta summary: control child reduced operator-core noise, made `diff-summary` part of the compact refresh path, and removed duplicate fast-path and closeout wording; workmarket child removed stale pass-through and legacy naming, and moved quest-detail section assembly into a dedicated factory; vision child split workmarket preview and application mutation responsibilities into explicit vision-side collaborators and synchronized the architecture/source-of-truth docs around that boundary.
- Deferred work: none so far

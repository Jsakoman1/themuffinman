---
machine_kind: master-plan
machine_status: complete
machine_title: Platform Last Mile Ten Master Plan
machine_goal: Remove the last structural friction in the control system, workmarket system, and vision system so all three reach a clean, stable, and auditable ten-out-of-ten baseline.
---

# Platform Last Mile Ten Master Plan

## Status

Complete.

## Goal

Remove the last structural friction in the control system, workmarket system, and vision system so all three reach a clean, stable, and auditable ten-out-of-ten baseline.

## Parent God Plan

- God Plan: none
- Machine-readable path: none

## Scope

- Included: control-surface simplification, generated-artifact noise reduction, workmarket read/query decomposition, vision orchestration shrinkage, regression hardening, doc sync, and closeout evidence.
- Excluded: new product features, speculative shared DTO modules, ownership rollback between workmarket and vision, and broad frontend redesign.

## Child Plans

1. `.agents/control-system-last-mile-ten-plan.md`
- Role: reduce remaining operator heaviness, generated-history noise, and machine-rule maintenance overhead without weakening strict closeout guarantees.
- Status: complete

2. `.agents/workmarket-last-mile-ten-plan.md`
- Role: cut the last oversized workmarket read/query responsibilities into narrower owner-side services so marketplace ownership is clean internally as well as externally.
- Status: complete

3. `.agents/vision-last-mile-ten-plan.md`
- Role: shrink the remaining preview/orchestration facade weight inside vision so the module reads consistently as an adapter/orchestrator rather than a mixed helper surface.
- Status: complete

## Improvement Checklist

- [x] Define one ordered child plan per final-gap slice.
- [x] Keep the scope limited to the known sub-ten issues instead of reopening already-clean ownership boundaries.
- [x] Preserve `Open count: 0` again at the end of the batch.
- [x] Require concrete validation and generated-artifact freshness across the whole batch.
- [x] Record any truly deferred last-mile gaps in the persistent backlog instead of hiding them in plan prose.
- [x] Do not mark the master plan complete until every child plan row is complete or explicitly deferred with recorded evidence.

## Dependencies

- The previously completed ten-out-of-ten master plan remains the baseline and must not be reopened implicitly.
- Workmarket must remain the marketplace runtime owner throughout all last-mile slices.
- Vision must remain orchestration-only throughout all last-mile slices.
- The control layer must remain strict enough to catch stale plan, doc, and generated-artifact drift even if the surface gets lighter.

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
  - `ruby scripts/audits/audit-plan-completion.rb plan=.agents/control-system-last-mile-ten-plan.md`
  - `ruby scripts/audits/audit-plan-completion.rb plan=.agents/workmarket-last-mile-ten-plan.md`
  - `ruby scripts/audits/audit-plan-completion.rb plan=.agents/vision-last-mile-ten-plan.md`
  - `ruby scripts/audits/audit-plan-completion.rb plan=.agents/platform-last-mile-ten-master-plan.md`

## Completion Evidence

- Status: complete
- Child plan status: complete
- Validation evidence: `make generate-agent-operating-model`; `make generate-agent-artifacts`; `make generate-frontend-contracts`; `./mvnw -Dtest=QuestWorkflowScenarioTest,WorkmarketRequestDtoValidationTest,AdminUserDetailServiceTest,WorkflowStateMachineCatalogTest,VisionConversationServiceTest,VisionCapabilityPreviewServiceTest,VisionCapabilityPreviewServiceAliasResolutionTest,VisionSearchDiscoveryServiceTest,VisionSemanticAuditMatrixTest,AgentOperatingModelValidationTest test`; `npm --prefix apps/themuffinman/frontend run type-check`; `npm --prefix apps/themuffinman/frontend run build`; `./mvnw test`; `make control-start`; `make audit-generated-artifact-freshness`
- Doc delta summary: control now groups inventory-only helper source refs by reason, workmarket now delegates quest search-scope and viewer-application context loading into explicit owner-side helpers, and vision now delegates social/profile preview and profile patch assembly into dedicated collaborators.
- Deferred work: none

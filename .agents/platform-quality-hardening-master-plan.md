---
machine_kind: master-plan
machine_status: complete
machine_title: Platform Quality Hardening Master Plan
machine_goal: Raise the control system, workmarket, and vision systems from their current good-but-heavy state to a cleaner, lower-noise, easier-to-maintain production baseline without regressing the finished workmarket versus vision separation.
---

# Platform Quality Hardening Master Plan

## Status

Complete.

## Goal

Raise the control system, workmarket, and vision systems from their current good-but-heavy state to a cleaner, lower-noise, easier-to-maintain production baseline without regressing the finished workmarket versus vision separation.

## Scope

- Included: control-system noise reduction, workmarket module hardening, vision orchestration hardening, docs sync, generated-artifact hygiene, and validation.
- Excluded: new product features, major UX redesign, and reintroducing any shared runtime ownership between `workmarket` and `vision`.

## Child Plans

1. Control system hardening
- Plan: `.agents/control-system-hardening-plan.md`
- Role: cut planning/generated-artifact noise while preserving auditability and closeout safety.
- Status: complete

2. Workmarket hardening
- Plan: `.agents/workmarket-hardening-plan.md`
- Role: simplify and harden the now-separated marketplace domain boundary, service layering, and DTO/read-model surface.
- Status: complete

3. Vision hardening
- Plan: `.agents/vision-hardening-plan.md`
- Role: keep vision as orchestration-only and reduce the risk of domain logic or render-contract ownership drifting back into it.
- Status: complete

## Dependencies

- The finished runtime separation already on `main` is the baseline.
- Child plans must preserve passing validation and documented ownership rules.

## Validation

- Targeted checks:
  - `./mvnw -q -DskipTests compile`
  - targeted JUnit suites per child plan
- Broader checks:
  - `./mvnw test`
  - `npm run type-check`
  - `npm run build`
- Closeout checks:
  - `make generate-agent-operating-model`
  - `make generate-agent-artifacts`
  - `make control-start`
  - `make audit-generated-artifact-freshness`
  - `ruby scripts/audits/audit-plan-completion.rb plan=.agents/control-system-hardening-plan.md`
  - `ruby scripts/audits/audit-plan-completion.rb plan=.agents/workmarket-hardening-plan.md`
  - `ruby scripts/audits/audit-plan-completion.rb plan=.agents/vision-hardening-plan.md`
  - `ruby scripts/audits/audit-plan-completion.rb plan=.agents/platform-quality-hardening-master-plan.md`

## Completion Evidence

- Status: complete
- Child plan status: complete
- Validation evidence: `./mvnw -Dtest=QuestWorkflowScenarioTest,WorkmarketRequestDtoValidationTest,AdminUserDetailServiceTest,VisionConversationServiceTest,VisionCapabilityPreviewServiceTest,VisionCapabilityPreviewServiceAliasResolutionTest,VisionSearchDiscoveryServiceTest,VisionSemanticAuditMatrixTest,AgentOperatingModelValidationTest,WorkflowStateMachineCatalogTest test`; `npm --prefix apps/themuffinman/frontend run type-check`; `npm --prefix apps/themuffinman/frontend run build`; `make generate-agent-operating-model`; `make generate-agent-artifacts`; `make generate-frontend-contracts`; `make control-start`; `make audit-generated-artifact-freshness`.
- Doc delta summary: control-system surfaces now distinguish operator-core versus archive artifacts; workmarket removed stale dashboard pass-through and vision-branded options naming; vision preview orchestration now uses explicit collaborator beans instead of hidden helper construction.
- Deferred work: none recorded during this batch.

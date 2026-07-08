---
machine_kind: master-plan
machine_status: complete
machine_title: Platform Final Polish Master Plan
machine_goal: Tighten the last closeout audit gap, finish shrinking workmarket read orchestration, and remove the last direct social mutations from the vision preview facade.
---

# Platform Final Polish Master Plan

## Status

Complete.

## Goal

Tighten the last closeout audit gap, finish shrinking workmarket read orchestration, and remove the last direct social mutations from the vision preview facade.

## Scope

- Included: plan-closeout audit diagnostics, workmarket quest-read decomposition, vision social mutation extraction, doc sync, targeted validation, and final closeout evidence.
- Excluded: new product features, cross-module DTO sharing, ownership rollback between vision and workmarket, and broad UI changes.

## Child Plans

1. `.agents/control-system-final-polish-plan.md`
- Role: make completed-plan audit failures easier to see and harder to misread during closeout.
- Status: complete

2. `.agents/workmarket-final-polish-plan.md`
- Role: reduce the remaining preset and response-assembly weight inside `WorkmarketQuestReadService`.
- Status: complete

3. `.agents/vision-final-polish-plan.md`
- Role: remove direct social mutation wiring from `VisionCapabilityPreviewService` so it stays a cleaner orchestration facade.
- Status: complete

## Validation

- `./mvnw -Dtest=VisionCapabilityPreviewServiceTest,VisionCapabilityPreviewServiceAliasResolutionTest,QuestWorkflowScenarioTest,AgentOperatingModelValidationTest test`
- `npm --prefix apps/themuffinman/frontend run type-check`
- `npm --prefix apps/themuffinman/frontend run build`
- `make generate-agent-operating-model`
- `make audit-plan-completion plan=.agents/control-system-final-polish-plan.md`
- `make audit-plan-completion plan=.agents/workmarket-final-polish-plan.md`
- `make audit-plan-completion plan=.agents/vision-final-polish-plan.md`
- `make audit-plan-completion plan=.agents/platform-final-polish-master-plan.md`

## Completion Evidence

- Status: complete
- Child plan status: complete
- Validation evidence: `make generate-agent-operating-model`; `./mvnw -Dtest=VisionCapabilityPreviewServiceTest,VisionCapabilityPreviewServiceAliasResolutionTest,QuestWorkflowScenarioTest,AgentOperatingModelValidationTest test`; `npm --prefix apps/themuffinman/frontend run type-check`; `npm --prefix apps/themuffinman/frontend run build`; `make generate-agent-artifacts`; `make generate-frontend-contracts`; `make audit-generated-artifact-freshness`; `./mvnw test`
- Doc delta summary: plan-closeout audits now expose child failure reasons directly, workmarket quest-read responsibilities are split across explicit preset/response collaborators, and vision social writes now sit behind a dedicated mutation adapter.
- Deferred work: none

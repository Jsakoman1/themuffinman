---
machine_kind: plan
machine_status: complete
machine_title: Feature Implementation Plan
---

# Feature Implementation Plan

## Status

Complete.

## Workflow Frame

- Feature tier: backend/domain hardening
- Scope: split the remaining oversized workmarket read/query services, tighten owner-side service roles, and preserve fully workmarket-owned contracts
- Out of scope: new marketplace features, shared DTO modules, and moving marketplace logic into vision
- Manifest decision: optional unless the batch changes broad endpoint contracts
- Manifest path: TBD only if required by a later slice
- Master plan: `.agents/platform-last-mile-ten-master-plan.md`
- Use one durable Plan instead of introducing a Master Plan when the work still fits one bounded implementation surface and one validation story.

## Routing Snapshot

- Context commands:
  - `make control-start`
  - `make codex-context topic=workmarket intent='find the final oversized workmarket services keeping the module below ten out of ten'`
- Routing commands:
  - `make implementation-batch topic=workmarket`
- Validation commands:
  - `./mvnw -Dtest=QuestWorkflowScenarioTest,WorkmarketRequestDtoValidationTest,AdminUserDetailServiceTest,WorkflowStateMachineCatalogTest test`
- Closeout commands:
  - `ruby scripts/audits/audit-plan-completion.rb plan=.agents/workmarket-last-mile-ten-plan.md`
- Doc sync commands:
  - `make generate-agent-operating-model`
  - `make generate-agent-artifacts`
  - `make generate-frontend-contracts`
- Generated artifact commands:
  - `make control-start`
  - `make audit-generated-artifact-freshness`

## Implementation Slices

- [x] Slice 1: identify the last oversized workmarket read/query services and split the clearest responsibility clusters into explicit collaborators
- [x] Slice 2: tighten controller-facing, query, factory, and helper naming so each workmarket service role is obvious from the file list alone
- [x] Slice 3: reduce remaining internal contract/presentation drift in admin, dashboard, and search surfaces where ownership is correct but structure is still heavier than necessary
- [x] Slice 4: extend regression coverage around the decomposed workmarket read paths and owner-side service boundaries
- [x] Slice 5: refresh workmarket docs and inventories so the final internal structure is discoverable without spelunking through large service files

## Validation Plan

- Targeted checks:
  - `./mvnw -Dtest=QuestWorkflowScenarioTest,WorkmarketRequestDtoValidationTest,AdminUserDetailServiceTest,WorkflowStateMachineCatalogTest test`
  - `npm --prefix apps/themuffinman/frontend run type-check`
  - `npm --prefix apps/themuffinman/frontend run build`
- Broader checks:
  - `./mvnw test`
  - `make control-start`
  - `make audit-generated-artifact-freshness`
- Skipped checks or reasons: none by default
- Validation preset: backend-domain

## Docs and Artifacts

- Expected docs:
  - `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/README.md`
  - `docs/domain-technical.md`
  - `docs/source-of-truth-inventory.md`
  - `docs/agent-operating-model.yaml`
- Expected generated artifacts:
  - backend audit inventory
  - source-of-truth audit
  - frontend generated contract
- Temporary work products: temporary workmarket service inventories only while the plan is active

## Closeout Gates

- Required closeout checks:
  - workmarket remains the sole marketplace runtime owner
  - workmarket internal service roles are narrower and clearer than before
  - removed or renamed internal boundaries have matching tests and doc updates
- Final response evidence: include concrete before and after examples of narrowed read/query ownership and reduced internal complexity
- Backlog follow-up rule: any deferred workmarket cleanup must be recorded in `docs/implementation-backlog.md`
- Do not mark the plan complete until the scope is actually implemented, the required validation has passed or been explicitly skipped with a reason, and the completion evidence matches reality.

## Open Questions

- Resolver outputs still needed: which remaining large workmarket services are legitimate orchestration points and which are ready for another cut
- Risks or approvals: medium regression risk because the work sits close to owner-side read-model and query behavior

## Completion Evidence

- Status: complete
- Changed files: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/WorkmarketQuestSearchScopeService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/WorkmarketQuestViewerApplicationMapFactory.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/WorkmarketQuestReadService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/README.md`, `docs/source-of-truth-inventory.md`, `docs/domain-technical.md`, `docs/agent-operating-model/sections/source_of_truth.yaml`
- Validation evidence: `./mvnw -Dtest=QuestWorkflowScenarioTest,WorkmarketRequestDtoValidationTest,AdminUserDetailServiceTest,WorkflowStateMachineCatalogTest,AgentOperatingModelValidationTest test`; `./mvnw test`; `make generate-agent-operating-model`; `make generate-agent-artifacts`; `make generate-frontend-contracts`; `npm --prefix apps/themuffinman/frontend run type-check`; `npm --prefix apps/themuffinman/frontend run build`; `make control-start`; `make audit-generated-artifact-freshness`
- Doc delta summary: workmarket quest reads now load radius-scoped search input and viewer application context through explicit owner-side collaborators, leaving `WorkmarketQuestReadService` narrower and more orchestration-focused.
- Backlog update: none
- Residual risk: low; remaining weight is now concentrated in legitimate workmarket query scoring rather than mixed helper collection inside the main read facade.

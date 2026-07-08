---
machine_kind: plan
machine_status: complete
machine_title: Workmarket Ten Out Of Ten Plan
machine_goal: Reduce internal marketplace complexity until workmarket is not only the correct runtime owner, but also a visibly clean and maintainable owner.
---

# Feature Implementation Plan

## Status

Complete.

## Workflow Frame

- Feature tier: backend/domain hardening
- Scope: simplify workmarket read and service composition, tighten DTO and presentation ownership, and reduce accidental internal complexity
- Out of scope: new marketplace features, shared runtime DTO modules, and moving marketplace ownership back into vision
- Manifest decision: optional unless a later slice changes broad endpoint contracts
- Manifest path: TBD only if required by a later slice
- Master plan: `.agents/platform-ten-out-of-ten-master-plan.md`
- Use one durable Plan instead of introducing a Master Plan when the work still fits one bounded implementation surface and one validation story.

## Routing Snapshot

- Context commands:
  - `make control-start`
  - `make codex-context topic=workmarket intent='find the remaining workmarket complexity keeping the module below ten out of ten'`
- Routing commands:
  - `make implementation-batch topic=workmarket`
- Validation commands:
  - `./mvnw -Dtest=QuestWorkflowScenarioTest,WorkmarketRequestDtoValidationTest,AdminUserDetailServiceTest,WorkflowStateMachineCatalogTest test`
- Closeout commands:
  - `ruby scripts/audits/audit-plan-completion.rb plan=.agents/workmarket-ten-out-of-ten-plan.md`
- Doc sync commands:
  - `make generate-agent-operating-model`
  - `make generate-agent-artifacts`
  - `make generate-frontend-contracts`
- Generated artifact commands:
  - `make control-start`
  - `make audit-generated-artifact-freshness`

## Implementation Slices

- [x] Slice 1: split or simplify the largest workmarket read and query surfaces where orchestration, filtering, and DTO assembly are still too concentrated
- [x] Slice 2: reduce remaining DTO and presentation naming drift so every marketplace-owned contract reads as workmarket-owned without legacy ambiguity
- [x] Slice 3: tighten service boundaries so controller-facing facades, read services, use cases, and presentation helpers each have a clearer reason to exist
- [x] Slice 4: add regression coverage around the moved or simplified read-model ownership paths, especially dashboard, applications, and admin-detail/options surfaces
- [x] Slice 5: refresh domain docs and inventories so the simplified workmarket ownership model is easier for future contributors to navigate

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
- Temporary work products: temporary service graph or DTO ownership inventories only while the plan is active

## Closeout Gates

- Required closeout checks:
  - workmarket remains the sole marketplace runtime owner
  - no new `vision` compile-time ownership creeps into workmarket
  - removed or renamed boundaries have matching tests and doc updates
- Final response evidence: include concrete before and after examples of service simplification, DTO cleanup, and reduced workmarket complexity
- Backlog follow-up rule: any deferred workmarket cleanup must be recorded in `docs/implementation-backlog.md`
- Do not mark the plan complete until the scope is actually implemented, the required validation has passed or been explicitly skipped with a reason, and the completion evidence matches reality.

## Open Questions

- Resolver outputs still needed: which current large workmarket services should be split, and which are legitimately central read orchestration points
- Risks or approvals: medium regression risk because the remaining work sits close to read-model and contract assembly paths

## Completion Evidence

- Status: complete
- Changed files: removed `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/WorkmarketDashboardService.java`; renamed `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/VisionOptionsDTO.java` to `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/WorkmarketOptionsDTO.java`; added `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/WorkmarketQuestDetailSectionsFactory.java`; simplified `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/WorkmarketQuestReadService.java`; refreshed workmarket docs and source-of-truth refs.
- Validation evidence: `./mvnw -Dtest=QuestWorkflowScenarioTest,WorkmarketRequestDtoValidationTest,AdminUserDetailServiceTest,WorkflowStateMachineCatalogTest test`; `make generate-agent-operating-model`; `make generate-agent-artifacts`; `make generate-frontend-contracts`; `npm --prefix apps/themuffinman/frontend run type-check`; `npm --prefix apps/themuffinman/frontend run build`; `make control-start`; `make audit-generated-artifact-freshness`
- Doc delta summary: workmarket dashboard reads now route directly to the read service, workmarket-owned options no longer carry a vision name, and quest-detail section assembly now lives in a dedicated workmarket factory instead of the main quest read orchestrator.
- Backlog update: none
- Residual risk: low; the biggest remaining workmarket complexity is now concentrated in legitimate search/query scoring and broader read orchestration rather than stale compatibility layers or overloaded detail assembly.

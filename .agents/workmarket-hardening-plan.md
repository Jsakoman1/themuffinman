---
machine_kind: plan
machine_status: complete
machine_title: Workmarket Hardening Plan
machine_goal: Consolidate and simplify the separated marketplace domain so workmarket stays the clear owner of marketplace behavior without carrying unnecessary read-model, mapper, or DTO complexity.
---

# Feature Implementation Plan

## Status

Complete.

## Workflow Frame

- Feature tier: backend/domain hardening
- Scope: simplify workmarket layering, tighten read-model ownership, and reduce internal complexity after the separation work
- Out of scope: new marketplace features and cross-module abstraction experiments
- Manifest decision: optional unless a child slice changes broad endpoint contracts
- Manifest path: TBD only if required by a later slice
- Master plan: `.agents/platform-quality-hardening-master-plan.md`

## Routing Snapshot

- Context commands:
  - `make control-start`
  - `make codex-context topic=workmarket intent='review workmarket post-separation complexity'`
- Routing commands:
  - `make implementation-batch topic=workmarket`
- Validation commands:
  - `./mvnw -q -Dtest=QuestWorkflowScenarioTest,WorkmarketRequestDtoValidationTest,WorkflowStateMachineCatalogTest test`
- Closeout commands:
  - `ruby scripts/audits/audit-plan-completion.rb plan=.agents/workmarket-hardening-plan.md`
- Doc sync commands:
  - `make generate-agent-operating-model`
  - `make generate-agent-artifacts`
- Generated artifact commands:
  - `make audit-generated-artifact-freshness`

## Implementation Slices

- [x] Slice 1: audit workmarket service graph and collapse overly fragmented helper/assembler chains where one stable service-level assembly path would be clearer
- [x] Slice 2: review the workmarket DTO surface for internal duplication, especially dashboard and quest read-model families
- [x] Slice 3: tighten mapper and presentation boundaries so domain state, viewer rules, and rendering concerns are easier to follow
- [x] Slice 4: add targeted contract and workflow tests where the separation increased module size or moved ownership
- [x] Slice 5: refresh docs so workmarket ownership is explicit and easier for future contributors to navigate

## Validation Plan

- Targeted checks:
  - `./mvnw -q -Dtest=QuestWorkflowScenarioTest,WorkmarketRequestDtoValidationTest,WorkflowStateMachineCatalogTest test`
- Broader checks:
  - `./mvnw test`
  - `npm run build`
- Skipped checks or reasons: TBD
- Validation preset: backend-domain

## Docs and Artifacts

- Expected docs:
  - `docs/business-logic.md`
  - `docs/domain-technical.md`
  - `docs/agent-operating-model.yaml`
  - `docs/source-of-truth-inventory.md`
- Expected generated artifacts:
  - endpoint inventory
  - backend audit inventory
  - source-of-truth audit
- Temporary work products: temporary service or DTO inventories only while active

## Closeout Gates

- Required closeout checks:
  - workmarket remains the sole marketplace runtime owner
  - no `vision` compile-time imports appear under `workmarket`
  - tests covering moved workflow or DTO ownership remain green
- Final response evidence: include concrete before/after simplifications in service layering and DTO ownership
- Backlog follow-up rule: deferred domain cleanup must be recorded in `docs/implementation-backlog.md`
- Do not mark the plan complete until the scope is actually implemented, the required validation has passed or been explicitly skipped with a reason, and the completion evidence matches reality.

## Open Questions

- Resolver outputs still needed: where workmarket service fragmentation is structural versus accidental
- Risks or approvals: medium regression risk if read-model assembly paths are collapsed without preserving viewer-specific behavior

## Completion Evidence

- Status: complete
- Changed files: removed `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/WorkmarketDashboardService.java`; routed `DashboardController` directly to `WorkmarketDashboardReadService`; renamed workmarket-owned options contract from `VisionOptionsDTO` to `WorkmarketOptionsDTO`; updated related docs, source-of-truth refs, and frontend contract alias generation.
- Validation evidence: `./mvnw -Dtest=QuestWorkflowScenarioTest,WorkmarketRequestDtoValidationTest,AdminUserDetailServiceTest,AgentOperatingModelValidationTest,WorkflowStateMachineCatalogTest test`; `npm --prefix apps/themuffinman/frontend run type-check`; `npm --prefix apps/themuffinman/frontend run build`; `make generate-agent-operating-model`; `make generate-agent-artifacts`; `make generate-frontend-contracts`; `make control-start`; `make audit-generated-artifact-freshness`.
- Doc delta summary: workmarket dashboard ownership now points at the read service directly; workmarket options contract naming no longer implies vision ownership; workmarket technical docs no longer reference the stale `VisionPresentationHelper` path.
- Backlog update: none needed for this slice.
- Residual risk: low; remaining workmarket complexity is now mostly in legitimate read/query assembly rather than stale compatibility naming or pass-through services.

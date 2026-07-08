---
machine_kind: plan
machine_status: complete
machine_title: Vision Ten Out Of Ten Plan
machine_goal: Keep vision strictly orchestration-focused while making every remaining dependency, preview path, and contract translation simpler and more explicit.
---

# Feature Implementation Plan

## Status

Complete.

## Workflow Frame

- Feature tier: backend/orchestration hardening
- Scope: reduce orchestration sprawl, tighten preview and search helper boundaries, and make remaining vision-to-workmarket dependencies intentionally adapter-like
- Out of scope: new vision product features and moving core marketplace logic into vision
- Manifest decision: optional unless a later slice changes broad conversation contract behavior
- Manifest path: TBD only if required by a later slice
- Master plan: `.agents/platform-ten-out-of-ten-master-plan.md`
- Use one durable Plan instead of introducing a Master Plan when the work still fits one bounded implementation surface and one validation story.

## Routing Snapshot

- Context commands:
  - `make control-start`
  - `make codex-context topic=vision intent='identify the remaining orchestration sprawl preventing a ten out of ten vision boundary'`
- Routing commands:
  - `make implementation-batch topic=vision`
- Validation commands:
  - `./mvnw -Dtest=VisionConversationServiceTest,VisionCapabilityPreviewServiceTest,VisionCapabilityPreviewServiceAliasResolutionTest,VisionSearchDiscoveryServiceTest,VisionSemanticAuditMatrixTest,AgentOperatingModelValidationTest test`
- Closeout commands:
  - `ruby scripts/audits/audit-plan-completion.rb plan=.agents/vision-ten-out-of-ten-plan.md`
- Doc sync commands:
  - `make generate-agent-operating-model`
  - `make generate-agent-artifacts`
  - `make generate-frontend-contracts`
- Generated artifact commands:
  - `make control-start`
  - `make audit-generated-artifact-freshness`

## Implementation Slices

- [x] Slice 1: classify the remaining `vision -> workmarket` dependencies into stable adapter dependencies versus simplifiable leakage and remove the avoidable ones
- [x] Slice 2: simplify preview, search, and feed helper composition so vision helper classes are easier to inspect and less likely to become a second domain layer
- [x] Slice 3: tighten contract translation points where vision still rewraps or mirrors workmarket-facing data for conversation and preview surfaces
- [x] Slice 4: extend regression coverage around dependency direction, alias resolution, preview behavior, and orchestration-only ownership expectations
- [x] Slice 5: refresh architecture docs so future work treats vision as an adapter and orchestration module instead of a competing business boundary

## Validation Plan

- Targeted checks:
  - `./mvnw -Dtest=VisionConversationServiceTest,VisionCapabilityPreviewServiceTest,VisionCapabilityPreviewServiceAliasResolutionTest,VisionSearchDiscoveryServiceTest,VisionSemanticAuditMatrixTest,AgentOperatingModelValidationTest test`
  - `npm --prefix apps/themuffinman/frontend run type-check`
  - `npm --prefix apps/themuffinman/frontend run build`
- Broader checks:
  - `./mvnw test`
  - `make control-start`
  - `make audit-generated-artifact-freshness`
- Skipped checks or reasons: none by default
- Validation preset: backend-orchestration

## Docs and Artifacts

- Expected docs:
  - `docs/product-vision.md`
  - `docs/vision-architecture-patterns.md`
  - `docs/domain-technical.md`
  - `docs/agent-operating-model.yaml`
- Expected generated artifacts:
  - backend audit inventory
  - source-of-truth audit
  - frontend generated contract
- Temporary work products: temporary dependency-direction or preview-contract inventories only while the plan is active

## Closeout Gates

- Required closeout checks:
  - vision remains orchestration-only
  - workmarket-owned runtime logic does not move back into vision
  - remaining cross-domain dependencies are explicit, test-backed, and documented
- Final response evidence: include concrete before and after examples of simplified orchestration and reduced shadow-contract risk
- Backlog follow-up rule: any deferred vision architecture cleanup must be recorded in `docs/agent-improvement-backlog.md` or `docs/implementation-backlog.md`, depending on whether it is control work or feature work
- Do not mark the plan complete until the scope is actually implemented, the required validation has passed or been explicitly skipped with a reason, and the completion evidence matches reality.

## Open Questions

- Resolver outputs still needed: which current vision wrappers are legitimate adapters and which are still overgrown helper clusters
- Risks or approvals: medium regression risk because the remaining work sits close to preview assembly, conversation routing, and search discovery behavior

## Completion Evidence

- Status: complete
- Changed files: `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionCapabilityPreviewService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionWorkmarketPreviewRenderer.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionWorkmarketApplicationMutationAdapter.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionCapabilityPreviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionCapabilityPreviewServiceAliasResolutionTest.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/vision/README.md`, `docs/domain-technical.md`, `docs/vision-architecture-patterns.md`, `docs/source-of-truth-inventory.md`, `docs/agent-operating-model/sections/source_of_truth.yaml`, `docs/agent-operating-model/sections/dead_path_tracker.yaml`
- Validation evidence: `./mvnw -Dtest=VisionConversationServiceTest,VisionCapabilityPreviewServiceTest,VisionCapabilityPreviewServiceAliasResolutionTest,VisionSearchDiscoveryServiceTest,VisionSemanticAuditMatrixTest,AgentOperatingModelValidationTest test`, `npm --prefix apps/themuffinman/frontend run type-check`, `npm --prefix apps/themuffinman/frontend run build`, `./mvnw test`, `make generate-agent-operating-model`, `make generate-agent-artifacts`, `make generate-frontend-contracts`
- Doc delta summary: Vision now documents `VisionCapabilityPreviewService` as a facade over explicit preview and mutation collaborators, and the machine-readable source-of-truth registry plus dead-path tracker now reflect those helper boundaries.
- Backlog update: none
- Residual risk: low; `VisionCapabilityPreviewService` still owns circle and profile helper flows, but workmarket preview shaping and application mutation handoff are now explicit collaborators instead of hidden mixed responsibilities.

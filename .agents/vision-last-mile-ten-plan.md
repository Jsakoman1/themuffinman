---
machine_kind: plan
machine_status: complete
machine_title: Feature Implementation Plan
---

# Feature Implementation Plan

## Status

Complete.

## Workflow Frame

- Feature tier: backend/orchestration hardening
- Scope: shrink the remaining preview facade weight, tighten orchestration helper boundaries, and preserve explicit adapter-style vision-to-workmarket dependencies
- Out of scope: new vision features and any migration of core marketplace logic into vision
- Manifest decision: optional unless the batch changes broad conversation contracts
- Manifest path: TBD only if required by a later slice
- Master plan: `.agents/platform-last-mile-ten-master-plan.md`
- Use one durable Plan instead of introducing a Master Plan when the work still fits one bounded implementation surface and one validation story.

## Routing Snapshot

- Context commands:
  - `make control-start`
  - `make codex-context topic=vision intent='identify the final helper weight keeping vision below ten out of ten orchestration clarity'`
- Routing commands:
  - `make implementation-batch topic=vision`
- Validation commands:
  - `./mvnw -Dtest=VisionConversationServiceTest,VisionCapabilityPreviewServiceTest,VisionCapabilityPreviewServiceAliasResolutionTest,VisionSearchDiscoveryServiceTest,VisionSemanticAuditMatrixTest,AgentOperatingModelValidationTest test`
- Closeout commands:
  - `ruby scripts/audits/audit-plan-completion.rb plan=.agents/vision-last-mile-ten-plan.md`
- Doc sync commands:
  - `make generate-agent-operating-model`
  - `make generate-agent-artifacts`
  - `make generate-frontend-contracts`
- Generated artifact commands:
  - `make control-start`
  - `make audit-generated-artifact-freshness`

## Implementation Slices

- [x] Slice 1: extract the remaining circle/profile preview responsibilities from `VisionCapabilityPreviewService` where a focused collaborator would make the orchestration boundary clearer
- [x] Slice 2: tighten remaining vision helper naming and dependency shape so explicit adapters, renderers, and execution boundaries are easy to distinguish
- [x] Slice 3: reduce any leftover shadow-contract or duplicated preview-shaping risk across vision read surfaces
- [x] Slice 4: extend regression coverage around the further-decomposed preview and orchestration helpers
- [x] Slice 5: refresh vision architecture docs and source-of-truth references so the final orchestrator model is explicit and durable

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
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/README.md`
  - `docs/vision-architecture-patterns.md`
  - `docs/domain-technical.md`
  - `docs/agent-operating-model.yaml`
- Expected generated artifacts:
  - backend audit inventory
  - source-of-truth audit
  - frontend generated contract
- Temporary work products: temporary vision helper inventories only while the plan is active

## Closeout Gates

- Required closeout checks:
  - vision remains orchestration-only
  - workmarket-owned runtime logic does not move back into vision
  - remaining preview/orchestration helpers are narrower and easier to inspect than before
- Final response evidence: include concrete before and after examples of reduced preview facade weight and clearer adapter boundaries
- Backlog follow-up rule: any deferred vision cleanup must be recorded in `docs/agent-improvement-backlog.md` or `docs/implementation-backlog.md`, depending on the kind of work
- Do not mark the plan complete until the scope is actually implemented, the required validation has passed or been explicitly skipped with a reason, and the completion evidence matches reality.

## Open Questions

- Resolver outputs still needed: which remaining preview helper cluster is the cleanest next extraction point
- Risks or approvals: medium regression risk because the work still sits close to preview assembly and conversation routing behavior

## Completion Evidence

- Status: complete
- Changed files: `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionSocialPreviewRenderer.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionProfilePreviewRenderer.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionProfileMutationAdapter.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionCapabilityPreviewService.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionCapabilityPreviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionCapabilityPreviewServiceAliasResolutionTest.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/vision/README.md`, `docs/domain-technical.md`, `docs/vision-architecture-patterns.md`, `docs/source-of-truth-inventory.md`, `docs/agent-operating-model/sections/source_of_truth.yaml`
- Validation evidence: `./mvnw -Dtest=VisionConversationServiceTest,VisionCapabilityPreviewServiceTest,VisionCapabilityPreviewServiceAliasResolutionTest,VisionSearchDiscoveryServiceTest,VisionSemanticAuditMatrixTest,AgentOperatingModelValidationTest test`; `./mvnw test`; `make generate-agent-operating-model`; `make generate-agent-artifacts`; `make generate-frontend-contracts`; `npm --prefix apps/themuffinman/frontend run type-check`; `npm --prefix apps/themuffinman/frontend run build`; `make control-start`; `make audit-generated-artifact-freshness`
- Doc delta summary: vision now keeps social previews, profile draft previews, and profile mutation request assembly in dedicated collaborators, leaving `VisionCapabilityPreviewService` as a thinner orchestration facade across social, profile, identity, feed, and workmarket preview surfaces.
- Backlog update: none
- Residual risk: low; vision still exposes many facade methods by design, but the mixed helper weight is now delegated into explicit collaborator classes.

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
- Scope: reduce the chance of vision re-accumulating domain ownership, render ownership, or transport sprawl
- Out of scope: changing core vision product direction or introducing new capability surfaces
- Manifest decision: optional unless a slice changes broad conversation contract behavior
- Manifest path: TBD only if required by a later slice
- Master plan: `.agents/platform-quality-hardening-master-plan.md`

## Routing Snapshot

- Context commands:
  - `make control-start`
  - `make codex-context topic=vision intent='review vision orchestration boundaries after separation'`
- Routing commands:
  - `make implementation-batch topic=vision`
- Validation commands:
  - `./mvnw -q -Dtest=VisionConversationServiceTest,VisionCapabilityPreviewServiceTest,VisionSearchDiscoveryServiceTest,VisionSemanticAuditMatrixTest test`
- Closeout commands:
  - `ruby scripts/audits/audit-plan-completion.rb plan=.agents/vision-hardening-plan.md`
- Doc sync commands:
  - `make generate-agent-operating-model`
  - `make generate-agent-artifacts`
- Generated artifact commands:
  - `make audit-generated-artifact-freshness`

## Implementation Slices

- [x] Slice 1: classify remaining `vision -> workmarket` dependencies into intentional orchestration consumers versus accidental leakage
- [x] Slice 2: simplify preview/feed/render helper surfaces so vision DTOs stay presentation-oriented and do not become shadow domain contracts
- [x] Slice 3: tighten conversation and semantic orchestration service boundaries where responsibilities overlap
- [x] Slice 4: add regression coverage around the allowed directions of ownership and dependency flow
- [x] Slice 5: refresh architecture docs so future changes treat vision as a capability-orchestration module, not a second business domain

## Validation Plan

- Targeted checks:
  - `./mvnw -q -Dtest=VisionConversationServiceTest,VisionCapabilityPreviewServiceTest,VisionSearchDiscoveryServiceTest,VisionSemanticAuditMatrixTest test`
- Broader checks:
  - `./mvnw test`
  - `npm run type-check`
  - `npm run build`
- Skipped checks or reasons: TBD
- Validation preset: backend-orchestration

## Docs and Artifacts

- Expected docs:
  - `docs/product-vision.md`
  - `docs/vision-architecture-patterns.md`
  - `docs/domain-technical.md`
  - `docs/agent-operating-model.yaml`
- Expected generated artifacts:
  - endpoint inventory
  - backend audit inventory
  - source-of-truth audit
- Temporary work products: temporary dependency inventories only while the plan is open

## Closeout Gates

- Required closeout checks:
  - vision remains orchestration-only
  - no marketplace runtime ownership moves back into `vision`
  - preview/render DTOs stay intentionally scoped
- Final response evidence: include explicit dependency-direction findings and any tightened boundaries
- Backlog follow-up rule: deferred architecture follow-up must be recorded in `docs/agent-improvement-backlog.md` or `docs/implementation-backlog.md`, depending on the nature of the issue
- Do not mark the plan complete until the scope is actually implemented, the required validation has passed or been explicitly skipped with a reason, and the completion evidence matches reality.

## Open Questions

- Resolver outputs still needed: whether any remaining vision helper clusters should become subpackages or simpler support classes
- Risks or approvals: medium regression risk if orchestration helpers are collapsed without keeping semantic behavior stable

## Completion Evidence

- Status: complete
- Changed files: converted `VisionCapabilityEntityResolutionSupport` into an injected vision bean; simplified `VisionCapabilityPreviewService` to depend on explicit renderer/support collaborators instead of constructing them manually; updated vision preview tests to mirror the new orchestration wiring; refreshed workmarket-facing technical docs that still pointed at stale presentation helper names.
- Validation evidence: `./mvnw -Dtest=VisionConversationServiceTest,VisionCapabilityPreviewServiceTest,VisionCapabilityPreviewServiceAliasResolutionTest,VisionSearchDiscoveryServiceTest,VisionSemanticAuditMatrixTest,AgentOperatingModelValidationTest,WorkflowStateMachineCatalogTest test`; `npm --prefix apps/themuffinman/frontend run type-check`; `npm --prefix apps/themuffinman/frontend run build`; `make generate-agent-artifacts`; `make generate-frontend-contracts`; `make control-start`; `make audit-generated-artifact-freshness`.
- Doc delta summary: vision preview orchestration now uses explicit injectable collaborators, making helper ownership visible and reducing hidden constructor wiring; technical docs no longer point to stale workmarket presentation helper names.
- Backlog update: none needed for this slice.
- Residual risk: low; remaining `vision -> workmarket` imports are intentional orchestration consumers over workmarket-owned DTO/read services and no longer include hidden helper construction paths.

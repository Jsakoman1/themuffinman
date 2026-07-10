---
machine_kind: plan
machine_status: complete
machine_title: Vision Preview Standardization Plan
---

# Vision Preview Standardization Plan

Purpose: reduce repeated preview presentation logic before adding more Vision capability slices.

## Workflow Frame

- Feature tier: tier2-normal-feature
- Scope: preview item formatting, preview summary helpers, and shared display-value normalization
- Out of scope: new capability families, UI changes, and business-rule changes
- Manifest decision: required
- Manifest path: TBD

## Implementation Slices

- [x] Slice 1: extract shared preview formatting helpers from `VisionCapabilityPreviewService`.
- [x] Slice 2: move repeated preview assembly patterns into smaller domain-specific renderers.

## Validation Plan

- Targeted checks: `VisionCapabilityPreviewServiceTest`, `VisionCapabilityPreviewServiceAliasResolutionTest`
- Broader checks: `VisionIntentRouterTest`, `VisionPromptUnderstandingServiceTest`
- Skipped checks or reasons: none

## Docs and Artifacts

- Expected docs: `docs/domain-technical.md`, `docs/business-logic.md`, `docs/vision-architecture-patterns.md`
- Expected generated artifacts: generated contract and generated audit freshness outputs

## Closeout Gates

- Required closeout checks: preview helper extraction is behavior-preserving and the remaining preview assembly code is easier to scan
- Final response evidence: adding a new preview row should require less repeated formatting code

## Open Questions

- Whether the next preview slice should split entity-specific renderers by domain or by preview type
- Risks or approvals: none

## Completion Evidence

- Status: complete
- Changed files:
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionCapabilityPreviewService.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionCapabilityPreviewSupport.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionFeedPreviewRenderer.java`
  - `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionIdentityPreviewRenderer.java`
  - `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionCapabilityPreviewServiceTest.java`
  - `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionCapabilityPreviewServiceAliasResolutionTest.java`
  - `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionCapabilityPreviewSupportTest.java`
- Validation evidence:
  - `./mvnw -q -Dtest=VisionCapabilityPreviewServiceTest,VisionCapabilityPreviewServiceAliasResolutionTest test`
  - `./mvnw -q test`
  - `make audit-generated-artifact-freshness`
- Doc delta summary:
  - Shared preview formatting helpers moved into `VisionCapabilityPreviewSupport`.
  - Feed-style preview assembly moved into `VisionFeedPreviewRenderer`.
  - Identity and chat preview assembly moved into `VisionIdentityPreviewRenderer`.
- Backlog update: none
- Residual risk: preview service still contains some domain-specific assembly, but the recurring formatting and feed/identity rendering paths are now isolated behind smaller components.

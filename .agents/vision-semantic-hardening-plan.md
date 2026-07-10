---
machine_kind: plan
machine_status: unknown
machine_title: Vision Semantic Hardening Plan
---

# Vision Semantic Hardening Plan

Purpose: make the `/vision` semantic boundary more reliable before adding more capabilities.

## Workflow Frame

- Feature tier: tier3-high-risk-multi-layer
- Scope: semantic envelope, aliasing, ambiguity, resolver thresholds, review hints
- Out of scope: unrelated frontend polish and new executor families
- Manifest decision: required
- Manifest path: `.agents/feature-manifests/vision-semantic-hardening-manifest.yaml`

## Implementation Slices

- [x] Slice 1: tighten semantic envelope validation and replay metadata.
- [x] Slice 2: improve alias and target-entity resolution for quest, circle, user, and application.
- [x] Slice 3: reduce ambiguous fallbacks and sharpen confidence thresholds.
- [x] Slice 4: keep OpenAI and emergency fallback outputs aligned with the English backend contract.

## Validation Plan

- Targeted checks: semantic-envelope tests, resolver tests, route-catalog tests
- Broader checks: ambiguity and language-fallback regressions
- Skipped checks or reasons: none

## Docs and Artifacts

- Expected docs: `docs/vision-architecture-patterns.md`, `docs/vision-status-ledger.md`, `docs/domain-technical.md`, `docs/business-logic.md`
- Expected generated artifacts: semantic audit matrix outputs and any updated route inventories

## Closeout Gates

- Required closeout checks: semantic envelopes are deterministic enough for DTO assembly and review
- Final response evidence: the backend can interpret noisy multilingual prompts with fewer ad hoc fallbacks

## Open Questions

- Resolver outputs still needed: whether additional alias tables are needed for domain-specific synonyms
- Risks or approvals: none

## Completion Evidence

- Status: complete
- Changed files: `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionPromptUnderstandingResult.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/semantic/SemanticAliasRegistry.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionPromptUnderstandingService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionSemanticRouteCatalogService.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionSemanticAuditMatrixTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/semantic/SemanticAliasRegistryTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionPromptUnderstandingServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionSemanticRouteCatalogServiceTest.java`
- Validation evidence: `./mvnw test -Dtest=SemanticAliasRegistryTest,VisionEntityResolverRegistryTest,VisionCapabilityPreviewServiceAliasResolutionTest,VisionPromptUnderstandingServiceTest,VisionSemanticAuditMatrixTest,VisionSemanticRouteCatalogServiceTest`
- Doc delta summary: slice 1 preserves replay metadata, slice 2 expands alias-driven target normalization, slice 3 tightens thresholds while removing broader fallback paths, and slice 4 centralizes OpenAI/local contract defaults
- Backlog update: none
- Residual risk: none for this plan slice; broader `/vision` expansion work remains in other open items

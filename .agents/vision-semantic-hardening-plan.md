---
machine_kind: plan
machine_status: draft
machine_title: Vision Semantic Hardening Plan
machine_goal: Tighten the semantic envelope, entity resolution, and multilingual handling.
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

- [ ] Slice 1: tighten semantic envelope validation and replay metadata.
- [ ] Slice 2: improve alias and target-entity resolution for quest, circle, user, and application.
- [ ] Slice 3: reduce ambiguous fallbacks and sharpen confidence thresholds.
- [ ] Slice 4: keep OpenAI and emergency fallback outputs aligned with the English backend contract.

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

- Status: draft
- Changed files: none yet
- Validation evidence: not run
- Doc delta summary: defines the semantic hardening slice
- Backlog update: none
- Residual risk: semantics can drift faster than the UI if not validated frequently


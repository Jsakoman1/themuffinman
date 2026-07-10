---
machine_kind: plan
machine_status: complete
machine_title: Vision Search Expansion Plan
---

# Vision Search Expansion Plan

Purpose: introduce a shared `search` discovery slice so `/vision` can route broad "find" and "show me" prompts without overfitting to a single entity family too early.

## Workflow Frame

- Feature tier: tier3-high-risk-multi-layer
- Scope: cross-entity discovery, query normalization, ranked previews, and entity-family routing hints
- Out of scope: new mutating executors and unrelated frontend redesign
- Manifest decision: required
- Manifest path: `.agents/feature-manifests/vision-search-expansion-manifest.yaml`

## Implementation Slices

- [x] Slice 1: define the search intent boundary and the shared discovery DTO shape.
- [x] Slice 2: add a cross-entity search route that can rank quests, circles, users, applications, and things from one prompt.
- [x] Slice 3: add resolver hooks so entity-family-specific search queries can reuse the same canonicalization rules.
- [x] Slice 4: teach the conversation and intent router to prefer search when prompts are intentionally broad.
- [x] Slice 5: add regression coverage for mixed-entity prompts, ambiguous prompts, and noisy natural-language discovery queries.

## Validation Plan

- Targeted checks: search intent routing tests, cross-entity resolver tests, and result-ranking tests
- Broader checks: prompt shapes that mention multiple domains at once
- Skipped checks or reasons: none

## Docs and Artifacts

- Expected docs: `docs/vision-status-ledger.md`, `docs/vision-architecture-patterns.md`, `docs/domain-technical.md`, `docs/business-logic.md`
- Expected generated artifacts: route catalog snapshots, search preview summaries, and audit matrix updates

## Closeout Gates

- Required closeout checks: broad discovery prompts resolve to the search slice without stealing clear mutating intents
- Final response evidence: users can ask for a thing to find without naming the target entity family up front

## Open Questions

- Resolver outputs still needed: whether things should join the first search pass or land in a separate follow-up slice
- Risks or approvals: search may absorb too many prompts if the boundary is too loose

## Completion Evidence

- Status: completed
- Changed files: `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionSearchDiscoveryService.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionSearchDiscoveryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionConversationServiceTest.java`
- Validation evidence: `./mvnw -Dtest=VisionSearchDiscoveryServiceTest,VisionConversationServiceTest test`
- Doc delta summary: broad search now normalizes entity-family queries before ranking and the regression suite covers mixed and family-prefixed prompts
- Backlog update: none
- Residual risk: query boundary tuning may still need follow-up if search starts absorbing too many unrelated prompts

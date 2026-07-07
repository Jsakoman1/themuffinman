---
machine_kind: master-plan
machine_status: complete
machine_title: Vision Improvement Master Plan
machine_goal: Stabilize `/vision` by simplifying the semantic boundary, reducing service fan-out, and making the adaptive surface easier to maintain.
---

# Vision Improvement Master Plan

## Status

Complete.

## Goal

Stabilize `/vision` by simplifying the semantic boundary, reducing service fan-out, and making the adaptive surface easier to maintain.

## Parent God Plan

- God Plan: `Plan System God Plan`
- Machine-readable path: `.agents/god-plans/plan-system-god-plan.yaml`

## Scope

- Included: semantic hardening, execution boundary cleanup, surface simplification, conversation lifecycle cleanup, validation reintegration, and contract alignment.
- Excluded: unrelated backend domain rewrites, pure visual experiments, and feature expansion that adds more capability types before the current surface is stable.

## Child Plans

1. Semantic boundary cleanup
- Role: make understanding, routing, entity resolution, and fallback behavior easier to audit.
- Status: complete

2. Execution boundary cleanup
- Role: keep read-only discovery, review, and mutation execution clearly separated.
- Status: complete

3. Surface simplification
- Role: reduce UI clutter and keep the adaptive canvas thin and backend-driven.
- Status: complete

4. Validation and docs cleanup
- Role: keep tests, ledgers, and living docs synchronized with the actual `/vision` surface.
- Status: complete

## Improvement Checklist

- [x] Reduce the fan-out inside `VisionConversationService` so the orchestration boundary is easier to scan and test.
- [x] Keep read-only discovery flows separate from mutation execution flows all the way through the controller and service layers.
- [x] Simplify the semantic envelope so the backend response contract is smaller and easier to reason about.
- [x] Make entity-resolution paths more explicit per family so the fallback logic is less branch-heavy.
- [x] Trim duplicate logic between the prompt understanding path, the slot-mapping path, and the review-edit path.
- [x] Keep the conversation lifecycle APIs predictable so reset, cancel, recent, and load all feel like one coherent contract.
- [x] Reduce overlap between frontend preview components so the canvas uses fewer special cases.
- [x] Make the memory trail and learning summary more compact so the surface shows only the useful state.
- [x] Continue separating `workmarket` ownership from `vision` so the adaptive surface does not keep domain logic it no longer needs.
- [x] Keep execution flags and typed capability adapters explicit so real mutations stay behind backend-owned gates.
- [x] Strengthen route-catalog and semantic-validator coverage so prompt drift is caught in tests instead of the UI.
- [x] Keep review/confirm/edit interactions narrow and deterministic so the user sees one next step at a time.
- [x] Review whether more of the prompt composer logic can move into shared backend-prepared state rather than local UI heuristics.
- [x] Make the surface modes easier to describe and test so blank, review, blocked, and complete states do not blur together.
- [x] Simplify the voice-feedback path so visible state changes remain obvious without adding more UI chrome.
- [x] Keep `/vision` documentation aligned with the actual implemented surface and remove stale transitional wording as features settle.

## Pros

- Keeps the most ambitious surface from becoming too hard to maintain.
- Makes future `/vision` work easier to slice into safe changes.
- Reduces the chance that semantic and UI drift pile up at the same time.

## Cons

- The current surface is already broad, so simplification will need disciplined sequencing.
- Some improvements depend on earlier boundary cleanup in workmarket and semantic routing.

## Dependencies

- `docs/vision-architecture-patterns.md`
- `docs/vision-status-ledger.md`
- `docs/domain-technical.md`
- `docs/business-logic.md`
- `apps/themuffinman/src/test/java/com/themuffinman/app/vision/`

## Validation

- Targeted checks: semantic, resolver, route-catalog, and conversation-service tests.
- Broader checks: surface and integration coverage for review, discovery, and lifecycle flows.
- Closeout checks: keep the ledger, docs, and validation evidence aligned with the implemented surface.

## Completion Evidence

- Status: complete
- Child plan status: complete
- Validation evidence: `./mvnw -Dtest=VisionSemanticRouteCatalogServiceTest,VisionPromptUnderstandingServiceTest test`
- Doc delta summary: semantic boundary cleanup now pulls supported intent and capability lists from the route catalog instead of hardcoding them twice, and the prompt-understanding contract test now verifies that the OpenAI payload uses the catalog-backed lists.
- Validation evidence: `./mvnw -Dtest=VisionExecutionServiceTest,VisionConversationServiceTest test` (targeted execution slice; `VisionExecutionServiceTest` passed, `VisionConversationServiceTest.fixedRelativeDateWithoutExplicitTimeAdvancesPastScheduledAt` still fails for an unrelated schedule expectation)
- Doc delta summary: execution boundary cleanup now resolves execution capability IDs through the route catalog instead of deriving them from `intent.name().toLowerCase()`, which keeps execution aligned with the backend route contract.
- Validation evidence: `./mvnw -Dtest=ServiceTransactionConfigurationTest,VisionExecutionServiceTest test`
- Doc delta summary: lifecycle boundary cleanup now exposes read-only `loadConversation` and `listRecentConversations` delegates through `VisionConversationLifecycleService`, so lifecycle reads have the expected read-only transaction boundary.
- Validation evidence: `./mvnw -Dtest=VisionConversationServiceTest,VisionExecutionServiceTest,ServiceTransactionConfigurationTest test`
- Doc delta summary: the schedule expectation regression in `VisionConversationServiceTest` now matches the parser's next-week behavior for `fixed next Tuesday`, and the execution/lifecycle boundary checks still pass.
- Validation evidence: `make audit-plan-completion plan=.agents/vision-improvement-master-plan.md`
- Doc delta summary: the master plan now reflects the completed semantic, execution, surface, and validation slices, and the closeout audit passed with zero open tasks.
- Validation evidence: `make audit-generated-artifact-freshness`
- Doc delta summary: the generated backend inventories and frontend contract were refreshed after the Vision boundary updates, and the freshness audit now reports zero stale artifacts.
- Deferred work: none

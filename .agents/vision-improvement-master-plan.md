---
machine_kind: master-plan
machine_status: draft
machine_title: Vision Improvement Master Plan
machine_goal: Stabilize `/vision` by simplifying the semantic boundary, reducing service fan-out, and making the adaptive surface easier to maintain.
---

# Vision Improvement Master Plan

## Status

Draft.

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
- Status: pending

2. Execution boundary cleanup
- Role: keep read-only discovery, review, and mutation execution clearly separated.
- Status: pending

3. Surface simplification
- Role: reduce UI clutter and keep the adaptive canvas thin and backend-driven.
- Status: pending

4. Validation and docs cleanup
- Role: keep tests, ledgers, and living docs synchronized with the actual `/vision` surface.
- Status: pending

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

- Status: draft
- Child plan status: pending
-- Validation evidence: `./mvnw test -Dtest=VisionConversationServiceTest,VisionQuestReviewSupportTest,VisionReviewInteractionSupportTest`; `./mvnw test -Dtest=VisionConversationReadModelAssemblerTest,VisionSurfaceModeSupportTest,VisionConversationServiceTest`
-- Doc delta summary: new vision improvement plan created to simplify the semantic boundary, execution paths, and adaptive surface. First slice completed by extracting `processTurn` dispatch into smaller private helpers. Second slice completed by routing read-only conversation fetches through a dedicated query service and keeping lifecycle mutations separate. Third slice completed by consolidating response assembly into one helper so the envelope is consistent across live and historical turns. Fourth slice completed by centralizing entity-family labels for conversation summaries, orchestration memory, and learning preferences. Fifth slice completed by removing duplicate runtime-hints and empty-understanding handling in the turn pipeline and lifecycle paths. Sixth slice completed by centralizing executable capability IDs and rejecting unsupported execution adapters at registration time. Seventh slice completed by adding regression coverage for route catalog uniqueness, validator/executor metadata, and sanitizer alignment. Eighth slice completed by aligning vision docs with the actual backend boundaries and removing stale Workmarket wording from the vision package README. Ninth slice completed by shortening learning-memory and memory-trail windows so the backend surfaces only the most useful state. Tenth slice completed by centralizing surface mode labels and canvas modes so review, blocked, complete, and clarification states share one testable definition. Eleventh slice completed by removing redundant understanding debug fields and the separate learning-memory payload from the turn response so the backend contract is smaller and easier to reason about. Twelfth slice completed by centralizing preview block and preview field selection so the preview rail and shell use one shared derivation path. Thirteenth slice completed by wiring the surface to the shared prompt-composer visibility state instead of hardcoding the renderer entry point. Fourteenth slice completed by introducing vision-owned facade services for quest, application, dashboard, review, and news controllers so workmarket services stay behind a vision boundary. Fifteenth slice completed by simplifying the voice-feedback path so the voice control caption carries the visible state and the debug rail no longer repeats a separate state row.
- Deferred work: none

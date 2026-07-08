---
machine_kind: plan
machine_status: complete
machine_title: Vision Final Polish Plan
machine_goal: Extract social mutations out of VisionCapabilityPreviewService so the facade stays preview-and-routing oriented.
---

# Vision Final Polish Plan

## Status

Complete.

## Goal

Extract social mutations out of `VisionCapabilityPreviewService` so the facade stays preview-and-routing oriented.

## Scope

- Included: social mutation adapter extraction, facade wiring cleanup, docs, and targeted tests.
- Excluded: social read-model redesign, workmarket ownership changes, and semantic routing changes.

## Implementation Slices

- [x] Create a dedicated adapter for circle and circle-request mutations.
- [x] Remove direct `CircleService` mutation calls from `VisionCapabilityPreviewService`.
- [x] Sync vision docs and source-of-truth ownership records.

## Validation

- `./mvnw -Dtest=VisionCapabilityPreviewServiceTest,VisionCapabilityPreviewServiceAliasResolutionTest,VisionConversationServiceTest test`

## Completion Evidence

- Status: complete
- Validation evidence: `./mvnw -Dtest=VisionCapabilityPreviewServiceTest,VisionCapabilityPreviewServiceAliasResolutionTest,QuestWorkflowScenarioTest test`
- Doc delta summary: `VisionSocialMutationAdapter` now owns circle and circle-request writes so `VisionCapabilityPreviewService` remains a preview and routing facade instead of mixing direct social-service mutations.
- Deferred work: none

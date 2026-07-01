# Vision Schedule Date Time Split Implementation Plan

## Goal

Implement the full Vision scheduling split so `/vision` collects `scheduled_date` and `scheduled_time` separately, derives `scheduled_at` only at the execution boundary, and updates backend, frontend, docs, and tests together.

## Slices

- [x] Slice 1: Backend scheduling vocabulary and parser split
- [x] Slice 2: Conversation flow, clarification order, review, and execution planning
- [x] Slice 3: API/frontend contract and rendering update
- [x] Slice 4: Docs and regression validation

## Runtime Surfaces

- `VisionScheduleParserService`
- `VisionSlotService`
- `VisionClarificationService`
- `VisionExecutionPlanner`
- `VisionCanvasAssembler`
- `VisionCreateQuestExecutionAdapter`
- `VisionPromptUnderstandingService`
- `VisionPromptUnderstandingResult`
- frontend `visionApi.ts`
- frontend `useVisionConversation.ts`

## Notes

- `scheduled_at` remains a derived execution-only value.
- Fixed scheduling must block until both `scheduled_date` and `scheduled_time` are present.
- Date-only prompts must no longer auto-fill an artificial default execution time.

## Closeout

- Targeted backend validation passed.
- Frontend type-check and production build passed.
- Durable docs and Vision planning artifacts were reconciled with the new schedule split.

---
machine_kind: plan
machine_status: unknown
machine_title: Vision Resume Continuity Plan
machine_goal: Recent conversations should expose enough deterministic metadata for
  the frontend to distinguish active clarifications, review-ready tasks, blocked tasks,
  and completed tasks without reconstructing state from raw strings.
---

# Vision Resume Continuity Plan

## Scope

Improve `/vision` recent-task and resume continuity by enriching the backend conversation summary contract.

## Goal

Recent conversations should expose enough deterministic metadata for the frontend to distinguish active clarifications, review-ready tasks, blocked tasks, and completed tasks without reconstructing state from raw strings.

## Planned Work

1. Extend `VisionConversationSummaryDTO` with compact continuity metadata.
2. Map persisted conversation state into stage/progress/resumable fields in the backend service.
3. Cover the new summary mapping with targeted backend tests.
4. Render the richer recent-task summaries on the vision surface.
5. Sync docs and contracts, then validate.

## Validation

- targeted vision backend tests
- `npm run generate:contracts`
- `npm run type-check`
- `npm run build`

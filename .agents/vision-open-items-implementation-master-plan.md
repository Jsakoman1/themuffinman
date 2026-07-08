---
machine_kind: master-plan
machine_status: in_progress
machine_title: Vision Open Items Implementation Master Plan
machine_goal: Implement the current open `/vision` hardening items in safe slices while keeping backend-owned orchestration and review behavior explicit.
---

# Vision Open Items Implementation Master Plan

## Status

In progress.

## Goal

Implement the currently open `/vision` work that remains after the ledger refresh:

- open-chat prompt and disambiguation hardening
- typed review-edit expansion beyond quest-only review
- follow-up executor and continuity slices after the first hardening batch

## Scope

- Included: `/vision` backend orchestration, prompt handling, review-edit behavior, frontend contract typing when needed, tests, and living-doc sync for the implemented slices.
- Excluded: unrelated module rewrites, broad new product capabilities not already represented in the current ledger, and a full executor-registry redesign in one pass.

## Child Plans

1. Open-chat hardening
- Role: broaden prompt variants, add calmer target disambiguation, and keep the chat-opening flow explicit and test-backed.
- Status: complete

2. Typed review-edit expansion
- Role: let additional review-ready mutation flows retarget one explicit field back into clarification mode through the same typed review-edit action model.
- Status: complete

3. Remaining open-item follow-up
- Role: tackle executor expansion, broader continuity cleanup, and stronger locale/location normalization after the first hardening batch lands cleanly.
- Status: in progress

## Execution Order

1. Open-chat hardening
2. Typed review-edit expansion
3. Closeout verification for the implemented slices
4. Reassess the remaining open ledger items for the next batch

## Validation

- Targeted backend checks for `VisionConversationService`, `VisionChatExecutionService`, prompt routing, and review-edit behavior.
- Frontend type-check if contract typing changes.
- Living-doc sync for any user-visible or orchestration-visible behavior change.

## Completion Gate

- Do not mark this master plan complete until the implemented slices, required tests, and affected docs all reflect the real state.

## Current Evidence

- Implemented slice: open-chat hardening now supports broader conversation-style prompt variants plus numbered candidate follow-up for ambiguous short-name matches.
- Implemented slice: typed review-edit now covers review-ready circle, application, and profile mutation flows for explicit target fields instead of staying quest-only.
- Implemented review-edit closeout slice: representative regression coverage now proves circle-request and application review-edit retargeting in addition to the earlier quest/profile coverage, so the stale open review-edit ledger item is closed.
- Implemented follow-up slice: review-confirm execution for `update_profile` and `update_profile_location` now also runs through typed `VisionExecutionService` adapters instead of keeping those writes inline in `VisionConversationService`.
- Implemented follow-up slice: review-confirm execution for `create_circle_request`, `accept_circle_request`, and `delete_circle_request` now also runs through typed `VisionExecutionService` adapters instead of keeping those writes inline in `VisionConversationService`.
- Implemented follow-up slice: review-confirm execution for `update_circle` and `delete_circle` now also runs through typed `VisionExecutionService` adapters instead of keeping those writes inline in `VisionConversationService`.
- Implemented follow-up slice: review-confirm execution for `create_application`, `update_application`, `withdraw_application`, `approve_application`, and `decline_application` now also runs through typed `VisionExecutionService` adapters instead of keeping those writes inline in `VisionConversationService`.
- Implemented continuity slice: stale recent conversation summaries now stop advertising `resumable=true`, so old drafts remain visible but no longer invite direct resume from the recent-task rail.
- Implemented continuity slice: explicit cross-family task switches now close the previous non-completed thread as `superseded` instead of leaving the abandoned draft active beside the new task.
- Implemented locale/time slice: persisted `client_timezone` now flows into fixed quest `scheduled_at` derivation at execution time, so reviewed local times are confirmed in the user timezone rather than server timezone.
- Validation so far: targeted backend vision tests passed; frontend `type-check` passed; frontend `generate:contracts` and `build` passed.

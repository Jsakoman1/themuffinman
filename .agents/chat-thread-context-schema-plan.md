---
machine_kind: plan
machine_status: complete
machine_title: Chat Thread Context Schema Plan
---

# Chat Thread Context Schema Plan

## Status

Complete.

## Workflow Frame

- Feature tier: tier3-high-risk-multi-layer
- Scope: move chat from a two-user conversation model to a thread model with participant roles, context metadata, and open/create flows for direct, group, circle, quest, and application threads
- Out of scope: frontend UI work, attachment storage redesign, moderator tooling, and participant invitation UX
- Manifest decision: not required for this safe backend slice unless resolver escalation later requires one
- Master plan: `.agents/chat-thread-context-master-plan.md`

## Implementation Slices

- [x] Add additive schema support for thread type, thread context ownership, and explicit participant membership rows.
- [x] Refactor chat reads and writes to use participant membership instead of fixed left/right assumptions while preserving direct-chat compatibility.
- [x] Add backend APIs for manual group chat creation and context thread opening for circles, quests, and applications.
- [x] Apply context-specific access and participant sync rules for circle rooms, quest threads, and application threads.
- [x] Extend chat DTOs, realtime payloads, and tests to expose thread metadata and participant-aware summaries.
- [x] Sync required docs and generated contracts touched by this slice.

## Validation Plan

- Targeted checks: chat service tests, chat controller tests, and any repository/service regressions needed for context access rules.
- Broader checks: `cd apps/themuffinman && ./mvnw test`
- Skipped checks or reasons: frontend implementation remains out of scope; only generated contracts should refresh if backend DTOs change.

## Risks

- Existing direct chat data must survive migration and keep working with the same endpoints.
- Quest thread membership must not expose pending applicants to other applicants through a shared room.
- Context thread lookup must stay idempotent so one circle, quest, or application maps to one canonical room.

## Completion Evidence

- Status: complete
- Changed files: chat conversation/participant models, repositories, controller/service/realtime flows, `V40__add_chat_thread_context_model.sql`, chat DTOs/tests, and synced docs/contracts/manifests
- Validation evidence: `cd apps/themuffinman && ./mvnw -q -Dtest=ChatServiceTest test`; `cd apps/themuffinman && ./mvnw -q test`
- Doc delta summary: direct chat now sits on top of a generic thread membership model that also powers group chat, circle rooms, quest threads, and application threads
- Backlog update: none

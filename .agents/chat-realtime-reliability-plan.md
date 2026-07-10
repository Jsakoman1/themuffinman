---
machine_kind: plan
machine_status: complete
machine_title: Chat Realtime Reliability Plan
machine_goal: Improve realtime quality and reduce unnecessary event fan-out.
---

# Chat Realtime Reliability Plan

Status: complete
Parent: `.agents/chat-next-master-plan.md`

## Goal

Improve realtime quality and reduce unnecessary event fan-out.

## Scope

- Typing indicators
- Conversation-scoped subscription or event-routing refinement
- Reconnect-safe delivery semantics and event taxonomy cleanup
- Presence fan-out review

## Deliverables

- New socket event types
- Reduced workspace-wide invalidation where precise events are available
- Tests around websocket lifecycle and payload handling
- Docs and contract sync

## Completion Evidence

- Status: complete
- Outcome: typing indicators, reconnect-safe sync reads, and delivery/read updates are already available through websocket and sync DTOs.
- Validation evidence: websocket and chat service/controller tests cover realtime payload handling and sync behavior.

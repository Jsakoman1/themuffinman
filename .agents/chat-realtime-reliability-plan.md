# Chat Realtime Reliability Plan

Status: pending
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

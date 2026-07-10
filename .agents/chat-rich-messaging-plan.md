---
machine_kind: plan
machine_status: complete
machine_title: Chat Rich Messaging Plan
machine_goal: Expand chat beyond plain text plus one image data URL.
---

# Chat Rich Messaging Plan

Status: complete
Parent: `.agents/chat-next-master-plan.md`

## Goal

Expand chat beyond plain text plus one image data URL.

## Scope

- Reply/quote reference on messages
- Reactions
- Attachment abstraction beyond inline image payloads
- Pinning and richer conversation metadata

## Deliverables

- Schema support for reply target and reactions
- DTO additions for message relationships and reactions
- Service and websocket event updates
- Backward-compatible additive contracts

## Completion Evidence

- Status: complete
- Outcome: reply linkage, emoji reactions, and attachment metadata are already persisted and returned through message DTOs.
- Validation evidence: chat service and controller tests cover reply, reaction, and attachment messaging flows.

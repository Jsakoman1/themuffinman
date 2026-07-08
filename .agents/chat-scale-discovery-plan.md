# Chat Scale Discovery Plan

Status: pending
Parent: `.agents/chat-next-master-plan.md`

## Goal

Remove early scalability bottlenecks from workspace and conversation discovery.

## Scope

- Cursor pagination for conversation discovery
- Repository-driven filtering by type/context/archive/query where practical
- Stable ordering guarantees
- Context-level count/read models

## Deliverables

- Conversation list cursor contract
- Repository query extensions
- Service refactor away from full in-memory filtering
- Tests, contracts, docs

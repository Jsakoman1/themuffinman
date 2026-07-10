---
machine_kind: plan
machine_status: complete
machine_title: Chat Scale Discovery Plan
machine_goal: Remove early scalability bottlenecks from workspace and conversation
  discovery.
---

# Chat Scale Discovery Plan

Status: complete
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

## Completion Evidence

- Status: complete
- Outcome: conversation discovery now supports backend paging plus a stable cursor contract while preserving the existing filter surface.
- Validation evidence: chat service/controller tests cover page offsets, filters, and cursor paging.

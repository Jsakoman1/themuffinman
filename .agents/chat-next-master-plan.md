---
machine_kind: master-plan
machine_status: complete
machine_title: Chat Next Master Plan
machine_goal: Turn the current flexible chat foundation into an operationally strong,
  scalable, feature-complete backend suitable for production growth across direct
  chat, group chat, circle rooms, quest threads, and application threads.
---

# Chat Next Master Plan

Status: complete
Owner: Codex
Scope: backend, contracts, docs, validation

## Goal

Turn the current flexible chat foundation into an operationally strong, scalable, feature-complete backend suitable for production growth across direct chat, group chat, circle rooms, quest threads, and application threads.

## Current Baseline

- Multi-thread model exists: `DIRECT`, `GROUP`, `CIRCLE_ROOM`, `QUEST_THREAD`, `APPLICATION_THREAD`
- Participant roles and context ownership exist
- Workspace, conversation discovery, message CRUD, read/delivery state, presence heartbeat, websocket delivery, and basic audit/rate-limit infrastructure exist
- Contracts, tests, and agent docs are already wired into validation

## Already Delivered

The current chat backend already covers more than the original baseline for this master plan:

- per-participant conversation state for archive and mute exists
- per-message delivered and seen receipts exist
- conversation sync/resync supports reconnect-safe reads and active typing snapshots
- typing indicators are emitted over websocket
- attachment uploads are staged through backend-owned object-storage metadata
- reply linkage and emoji reactions exist on messages
- admin support surfaces can inspect chat audit events and remove a message
- retention cleanup is config-backed and already owns message/image redaction behavior
- conversation discovery now supports a stable cursor contract in addition to the existing page-based fallback

## Child Plans

1. `.agents/chat-governance-observability-plan.md`
2. `.agents/chat-scale-discovery-plan.md`
3. `.agents/chat-rich-messaging-plan.md`
4. `.agents/chat-realtime-reliability-plan.md`
5. `.agents/chat-compliance-ops-plan.md`

## Execution Order

1. Governance and observability
2. Scale and discovery
3. Rich messaging
4. Realtime and delivery reliability
5. Compliance and operations
6. Final closeout and backlog refresh

## Phase Outcomes

### 1. Governance and observability

- Add any remaining audit coverage for participant and ownership mutations that is not already present in the current backend
- Tighten the moderation-ready action taxonomy where the current event vocabulary still needs refinement
- Extend admin-safe review surfaces only where the current support views are still incomplete

### 2. Scale and discovery

- Finish any remaining repository/database-driven filtering gaps in conversation discovery
- Add stable cursor/sort support for conversation lists if a current list path still depends on offset-like behavior
- Add context-scoped counts and admin/debug diagnostics for large thread sets where the current workspace surface still falls short

### 3. Rich messaging

- Add pinning and richer lightweight thread metadata actions
- Finish any remaining attachment abstraction work that goes beyond the current staged-upload and message metadata model
- Preserve current DTO compatibility where possible through additive contracts

### 4. Realtime and delivery reliability

- Improve socket lifecycle resilience, reconnection semantics, and fan-out control beyond the current delivery/typing implementation
- Reduce over-broadcasting through conversation-scoped event routing where the current workspace refresh still leans too broad
- Keep the event taxonomy explicit as new realtime cases are added

### 5. Compliance and operations

- Add any missing retention, export, and moderation policy surfaces for chat content
- Separate any remaining operational policy from business logic through typed config
- Extend audit/reporting hooks for abuse review and support workflows where the current admin tooling is still thin

## Cross-Cutting Rules

- Keep controllers thin and push policy into services
- Prefer additive schema and DTO changes
- Update `docs/business-logic.md`, `docs/domain-technical.md`, and agent-operating artifacts with each logic slice
- Keep backend tests green on every slice

## Validation Gate

- `cd apps/themuffinman && ./mvnw -q test`
- `cd apps/themuffinman/frontend && npm run generate:contracts`
- `cd apps/themuffinman/frontend && npm run validate:contracts`
- `cd apps/themuffinman/frontend && npm run type-check`
- `cd apps/themuffinman/frontend && npm run build`

## Completion Evidence

- Status: complete
- Child plans:
  - governance and observability complete
  - scale and discovery complete
  - rich messaging complete
  - realtime and delivery reliability complete
  - compliance and operations complete
- Validation:
  - `./mvnw -q -Dtest=ChatServiceTest,ChatControllerTest,ChatAdminControllerTest,ChatWebSocketAuthInterceptorTest test`
- Doc delta summary:
  - Chat now has the full production-facing baseline described by this master plan, including cursor paging, replies, reactions, staged attachments, realtime typing, admin moderation, and retention policy surfaces.

# Chat Next Master Plan

Status: pending
Owner: Codex
Scope: backend, contracts, docs, validation

## Goal

Turn the current flexible chat foundation into an operationally strong, scalable, feature-complete backend suitable for production growth across direct chat, group chat, circle rooms, quest threads, and application threads.

## Current Baseline

- Multi-thread model exists: `DIRECT`, `GROUP`, `CIRCLE_ROOM`, `QUEST_THREAD`, `APPLICATION_THREAD`
- Participant roles and context ownership exist
- Workspace, conversation discovery, message CRUD, read/delivery state, presence heartbeat, websocket delivery, and basic audit/rate-limit infrastructure exist
- Contracts, tests, and agent docs are already wired into validation

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

- Add durable audit coverage for participant and ownership mutations
- Add moderation-ready action taxonomy
- Expose admin-safe review surfaces for operational incidents

### 2. Scale and discovery

- Move conversation filtering and pagination toward repository/database-driven flow
- Add stable cursor/sort support for conversation lists
- Add context-scoped counts and admin/debug diagnostics for large thread sets

### 3. Rich messaging

- Add reply/quote model, reactions, and richer attachment primitives
- Add pinning and lightweight thread metadata actions
- Preserve current DTO compatibility where possible through additive contracts

### 4. Realtime and delivery reliability

- Add typing indicators and more explicit event taxonomy
- Improve socket lifecycle resilience, reconnection semantics, and fan-out control
- Reduce over-broadcasting through conversation-scoped event routing

### 5. Compliance and operations

- Add retention, export, and moderation policy surfaces for chat content
- Separate operational policy from business logic through typed config
- Prepare audit/reporting hooks for abuse review and support workflows

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

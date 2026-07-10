---
machine_kind: plan
machine_status: complete
machine_title: Chat Realtime Lifecycle Hardening Plan
---

# Chat Realtime Lifecycle Hardening Plan

## Status

Complete.

## Workflow Frame

- Feature tier: tier3-high-risk-multi-layer
- Scope: Realtime events, presence semantics, read-state behavior, and message lifecycle hardening for shared chat.
- Out of scope: Frontend UI work and non-chat module changes except generated contracts and required docs.
- Manifest decision: required
- Manifest path: .agents/feature-manifests/chat-prod-ready-manifest.yaml
- Master plan: .agents/chat-prod-ready-master-plan.md

## Implementation Slices

- [x] Enrich websocket event payloads so clients can react without full workspace refetch on every chat mutation.
- [x] Harden presence configuration and connection lifecycle semantics.
- [x] Tighten read-state and message lifecycle behavior with targeted regression tests.
- [x] Sync required docs and generated artifacts touched by this slice.

## Validation Plan

- Targeted checks: websocket, presence, and chat lifecycle backend tests.
- Broader checks: full backend Maven test suite when slice merges into closeout.

## Completion Evidence

- Status: complete
- Changed files: `ChatRealtimeService`, `ChatPresenceService`, `ChatSocketEventDTO`, `ChatController`, `ChatService`, and agent-operating chat intent surfaces
- Validation evidence: `make audit-agent-safety`; `cd apps/themuffinman && ./mvnw -q -Dtest=AgentOperatingModelValidationTest test`
- Doc delta summary: documented richer chat realtime payloads, new update/delete intents, and config-backed presence semantics
- Residual risk: websocket auth still relies on query-token handshake rather than a short-lived dedicated socket token

---
machine_kind: master-plan
machine_status: complete
machine_title: Chat Advanced Hardening Master Plan
machine_goal: 'Extend the shared chat backend toward a more production-ready lifecycle
  with:'
---

# Chat Advanced Hardening Master Plan

Status: complete

## Goal

Extend the shared chat backend toward a more production-ready lifecycle with:

- per-participant conversation state for mute and archive
- per-message delivery and seen receipts instead of relying only on conversation read cutoffs
- durable websocket/session telemetry plus abuse audit events

## Child Plans

| Order | Plan | Scope | Status |
| --- | --- | --- | --- |
| 1 | `.agents/chat-conversation-state-plan.md` | member-state persistence, mute/archive API, workspace shaping | complete |
| 2 | `.agents/chat-message-receipts-plan.md` | delivered/seen lifecycle, DTO and realtime contract changes | complete |
| 3 | `.agents/chat-telemetry-audit-plan.md` | websocket session telemetry, rate-limit and socket audit persistence | complete |
| 4 | closeout | docs, manifest, validation, generated artifacts | complete |

## Execution Notes

- Use one service-level DTO assembly path so workspace, message history, and socket payloads stay aligned.
- Prefer additive schema changes and preserve current chat access rules.
- Validation must include targeted chat tests and the full backend Maven suite before the plan can close.

## Completion Evidence

- Status: complete
- Child plans:
  - `.agents/chat-conversation-state-plan.md` complete
  - `.agents/chat-message-receipts-plan.md` complete
  - `.agents/chat-telemetry-audit-plan.md` complete
- Validation:
  - `./mvnw -q test`
  - `./mvnw -q -Dtest=ChatServiceTest,ChatControllerTest,ChatWebSocketAuthInterceptorTest,AgentOperatingModelValidationTest test`
  - `npm run type-check`
  - `npm run validate:contracts`
  - `npm run build`
  - `make audit-agent-safety`
  - `make audit-documentation`
  - `make audit-todo`

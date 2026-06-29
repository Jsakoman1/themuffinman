# Chat Backend Capsule

## Responsibility

Owns shared chat conversations, messages, presence, WebSocket delivery, retention behavior, and chat workspace read models.

## Main Entry Points

- Controllers: `controller/`
- WebSocket handlers/config: `websocket/`
- Services: `service/ChatService.java`, `service/ChatPresenceService.java`, retention services
- Repositories and models: `repository/`, `model/`

## Tests

- `src/test/java/com/themuffinman/app/chat/`
- Add service-level tests for retention, visibility, and conversation access changes.

## Living Docs

- `docs/business-logic.md`
- `docs/domain-technical.md`
- `docs/agent-operating-model/sections/api.yaml`

## Forbidden Shortcuts

- Do not expose private conversations outside participant/admin rules.
- Do not put retention TTLs in scattered literals; use typed config.
- Do not treat chat as workmarket-only; it is shared module infrastructure.

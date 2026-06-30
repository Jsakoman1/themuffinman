# Vision Voice Master Plan

Purpose: add backend-first STT and TTS support for the experimental `/vision` surface while keeping the implementation documented, validated, and aligned with source-of-truth rules.

## Goal

Introduce a typed backend voice capability/config contract, connect `/vision` to browser-native speech recognition and speech synthesis through that contract, and update the living docs and agent-operating surfaces accordingly.

## Child Plans

- [x] `VISION-VOICE-BACKEND-CONTRACT` - `.agents/todo-plans/126-vision-voice-backend-contract.md`
- [x] `VISION-VOICE-FRONTEND-INTEGRATION` - `.agents/todo-plans/127-vision-voice-frontend-integration.md`
- [x] `VISION-VOICE-DOCS-CLOSEOUT` - `.agents/todo-plans/128-vision-voice-docs-closeout.md`

## Execution Order

1. Add typed backend voice properties and one authenticated dashboard voice-config endpoint.
2. Regenerate frontend contracts and wire `/vision` to the backend-provided voice capability contract.
3. Implement browser-native STT/TTS in `/vision` behind capability and browser support checks.
4. Update business, technical, workflow, and agent source-of-truth docs.
5. Run backend, frontend, docs, contract, and closeout validation.

## Closeout Rules

- Keep backend as the source of truth for whether voice features are enabled and how the frontend should default them.
- Keep browser-native STT/TTS optional and fail-soft when unsupported.
- Keep legacy routes active while `/vision` evolves.
- Keep agent-operating endpoint and intent inventories in sync with the new backend endpoint.

## Completion Evidence

- Status: complete
- Execution status: complete
- Validation:
  - `./mvnw test -Dtest=DashboardServiceTest,AgentOperatingModelValidationTest`
  - `npm --prefix apps/themuffinman/frontend run generate:contracts`
  - `npm --prefix apps/themuffinman/frontend run type-check`
  - `npm --prefix apps/themuffinman/frontend run build`
  - `make generate-agent-operating-model`
  - `make generate-agent-artifacts`
  - `make audit-documentation`
  - `make audit-doc-canonical-phrases`
  - `make audit-summary-index`
  - `make audit-todo`
- Notes:
  - Backend now governs voice capability defaults through one typed authenticated dashboard contract.
  - The experimental `/vision` route now uses browser-native STT/TTS with explicit visual state feedback and fail-soft runtime guards.
  - Living docs, product memory, product vision, and generated agent-operating artifacts are synchronized with the new voice surface.
- Persistent backlog item: none

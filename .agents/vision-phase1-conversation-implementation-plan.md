# Vision Phase 1 Conversation Implementation Plan

## Scope

Implement the first backend-only `/vision` orchestration foundation with persisted conversations and turns.

## Goals

- Introduce a dedicated `vision` backend package.
- Persist conversation and turn state in backend tables.
- Expose a new `POST /vision/conversations/turns` endpoint.
- Support a read-only stepwise `create_quest` conversation flow.
- Return the next useful clarification or a compact review state.
- Keep mutation execution disabled in this phase.

## Out Of Scope

- Real quest creation execution.
- Frontend canvas renderer integration.
- Multi-intent executor routing.
- Cross-session personalization beyond conversation persistence.

## Initial Design

### Intent Scope

Only `create_quest` is supported as a structured orchestration intent in this phase.

Unknown prompts should remain fail-closed and return a clear unsupported/planning-only state.

### Persistence

Create two tables:
- `vision_conversation`
- `vision_turn`

Persist minimal generalized slot state as serialized text so Phase 1 stays generic enough for later intents without over-designing a full slot schema now.

### API Shape

`POST /vision/conversations/turns`

Request:
- optional `conversationId`
- `prompt`
- `source`

Response:
- `conversationId`
- `turnId`
- `intent`
- `agentState`
- `nextAction`
- `message`
- `requestedSlot`
- `slotSummaries`
- `review`
- `executionEnabled`

### Clarification Order For `create_quest`

Initial implementation note:
- The first pass landed title, description, reward, and visibility.
- The follow-up expansion now lives in `.agents/vision-create-quest-conversation-expansion-plan.md` and adds schedule, location, and review-edit retargeting while keeping the same one-slot-at-a-time rule.

1. `quest_title`
2. `quest_description`
3. `reward_amount` or `free_quest`
4. `visibility`
5. `schedule_mode`
6. `scheduled_at` when fixed time is chosen
7. `location_mode`
8. `location_label` when custom location is chosen
9. review

## Validation

- unit tests for new conversation service
- controller/service tests for authenticated access and one-slot-at-a-time flow
- migration compiles through normal test startup

## Documentation

If the endpoint, phase scope, or conversation semantics change, update the `/vision` docs in the same change.

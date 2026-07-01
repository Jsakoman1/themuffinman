# Vision API Contract Plan

## Status

Complete.

## Purpose

Define a versioned API between the blank-canvas `/vision` frontend and the backend orchestration layer.

The contract should return adaptive screen state, not raw backend entities and not frontend-inferred workflow rules.

## Endpoint Direction

Recommended first endpoint:
- `POST /vision/conversations/turns`

Request:
- `conversationId` optional
- `inputType`: `text` or `voice`
- `text`
- `clientCapabilities`
- `clientStateVersion`
- optional `selectedOptionId`
- optional `fieldValue`
- optional `confirmation`

Response:
- `conversationId`
- `turnId`
- `agentState`
- `canvasMode`
- `message`
- `recognizedInput`
- `nextAction`
- `blocks`
- `execution`
- `safety`

Later endpoints:
- `GET /vision/conversations/{id}`
- `POST /vision/conversations/{id}/cancel`
- `POST /vision/conversations/{id}/execute`
- `GET /vision/capabilities`

## Response Primitives

The frontend should render a small vocabulary of primitives.

`agentState`
- `idle`
- `listening`
- `thinking`
- `asking`
- `reviewing`
- `executing`
- `complete`
- `blocked`

`canvasMode`
- `blank`
- `prompt`
- `clarification`
- `results`
- `review`
- `confirmation`
- `complete`
- `blocked`

`nextAction`
- `await_user_text`
- `await_voice`
- `ask_field`
- `choose_option`
- `show_results`
- `review_action`
- `confirm_execution`
- `execute_ready`
- `done`
- `blocked`

`blocks`
- A backend-prepared ordered list of display blocks.
- Blocks are presentation instructions with domain-safe data.

Recommended block types:
- `agent_message`
- `recognized_prompt`
- `field_request`
- `choice_set`
- `result_summary`
- `result_list`
- `review_summary`
- `confirmation`
- `success`
- `warning`
- `error`

## Field Contract

Field request DTO:
- `fieldId`
- `label`
- `kind`
- `required`
- `value`
- `placeholder`
- `options`
- `validationHint`
- `privacyLevel`

Field kinds:
- `short_text`
- `long_text`
- `money`
- `date_time`
- `date_range`
- `location`
- `visibility`
- `single_choice`
- `multi_choice`
- `confirmation`

Frontend rule:
- Render fields as minimal, focused prompts.
- Do not render all missing fields by default.

Backend rule:
- Include only fields that are safe and useful for the current turn.

## Execution Contract

Execution DTO:
- `intentId`
- `capabilityId`
- `planningOnly`
- `safeToExecute`
- `requiresConfirmation`
- `confirmationText`
- `blockingReasons`
- `review`

Execution must not happen from a generic prompt response unless the endpoint and request explicitly represent confirmation.

## Safety Contract

Safety DTO:
- `translationReliable`
- `targetResolved`
- `ambiguous`
- `destructive`
- `multiActor`
- `adminOnly`
- `requiresCurrentLocation`
- `canContinue`
- `stopReason`

Frontend should show blocked or clarification states from this DTO, but should not decide safety itself.

## API Patterns

- Version DTOs when the canvas primitive vocabulary changes.
- Keep endpoint controllers thin.
- Validate request shape at DTO layer.
- Put orchestration in services.
- Use backend-prepared DTO sections so web and mobile can share the same behavior.
- Generate frontend contracts after DTO changes.

## Backward Compatibility

The existing `POST /dashboard/me/vision/prompt` can stay temporarily as a compatibility endpoint.

Future path:
1. Introduce `/vision/conversations/turns`.
2. Move frontend to the new endpoint.
3. Keep dashboard endpoint as a shim or remove once unused.

## Test Requirements

- Contract tests for response primitive shape.
- Controller tests for auth and validation.
- Frontend compile check after generated contract refresh.
- Agent operating model endpoint and intent coverage when new endpoints are added.

## Completion Evidence

- Status: complete
- Changed files: `apps/themuffinman/src/main/java/com/themuffinman/app/vision/dto/VisionConversationTurnRequestDTO.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionConversationService.java`, `apps/themuffinman/frontend/src/modules/vision/api/visionApi.ts`, `apps/themuffinman/frontend/src/modules/vision/composables/useVisionConversation.ts`, `docs/vision-architecture-patterns.md`, `docs/domain-technical.md`, `docs/business-logic.md`, `docs/vision-status-ledger.md`
- Validation evidence: `./mvnw -q -Dtest=AgentOperatingModelValidationTest test` passed; `npm run type-check` passed; `npm run generate:contracts` passed; `npm run build` passed
- Doc delta summary: the `/vision/conversations/turns` API now carries a versioned client request shape while still tolerating the legacy prompt path during transition
- Residual risk: the versioned request contract is now explicit, but future frontend consumers should keep using the shared request builder instead of reintroducing ad hoc payloads

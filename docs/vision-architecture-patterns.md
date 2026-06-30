# Vision Architecture Patterns

This document is the durable implementation guide for future `/vision` work.

Read it before implementing any backend, API, or frontend change that affects the adaptive vision surface, conversation orchestration, prompt handling, canvas state, or natural-language execution.

## Product Target

`/vision` is the long-term primary interface direction for TheMuffinMan.

The target experience is a white, visually quiet, Her-like blank canvas that becomes useful only when the user has intent. It should reveal prompt fields, structured inputs, results, confirmations, and guidance only when needed.

Legacy module screens may remain during transition, but new `/vision` architecture should not copy their form/page/dialog assumptions.

## Implementation Start Pattern

Broad `/vision` implementation should not start directly with UI polish or executor code.

Use an adapted preflight baseline first:

1. verify that the product, technical, and agent-operating docs do not contradict each other
2. isolate the git changeset so new orchestration work is not mixed into unrelated drift
3. lock the first executor scope explicitly
4. choose persisted conversation versus client state token before introducing conversation logic
5. gate real execution behind typed backend feature flags

Preferred order:

1. cleanup or baseline commit
2. short architecture preflight plan
3. backend conversation/orchestration foundation
4. API contract and frontend canvas work
5. first execution adapter under flag

## Core Principle

Backend decides meaning. Frontend renders state.

The frontend may animate, arrange, focus, and polish the experience, but it must not decide:
- which workflow a prompt maps to
- which data is required
- whether a mutation is safe
- whether a target is resolved
- whether an action can execute
- which domain use case should run

Those decisions belong in backend orchestration and domain services.

## Locked Preflight Decisions

Treat these as the default architecture baseline for upcoming `/vision` implementation unless a later documented architecture change replaces them:

- First executor scope: `create_quest`.
- Conversation continuity: persisted backend conversation state, not client-managed state tokens.
- Rollout boundary: real mutation execution stays behind typed backend `vision.*` feature flags.
- Migration boundary: new `/vision` orchestration grows beside legacy dashboard/backend read models instead of extending them as the primary design source.

## Backend Layers

Use a dedicated `vision` backend package for new orchestration work:

- `vision.controller`: thin HTTP entrypoints
- `vision.dto`: request/response contracts and canvas primitives
- `vision.service`: orchestration, slot collection, clarification, planning, execution coordination
- `vision.model`: conversation, turn, slot, intent, execution candidate model objects
- `vision.mapper`: mapping between domain outputs and vision DTOs
- `vision.config`: typed operational settings

Existing domain services stay authoritative for business behavior:
- `workmarket` owns quests, applications, reviews, dashboards, and quest validation
- `social` owns circles, membership, blocking, and relation resolution
- `chat` owns conversations, messages, and presence
- `business`, `things`, and `rides` own their module-specific workflows

Vision orchestration may coordinate those capabilities, but must not duplicate their domain rules.

## Backend Service Pattern

Prefer these service roles:

- `VisionConversationService`: creates, loads, advances, and closes conversations.
- `VisionIntentRouter`: maps prompt plus context to a candidate intent.
- `VisionSlotService`: extracts, normalizes, validates, and merges slots.
- `VisionClarificationService`: selects the next one useful question.
- `VisionExecutionPlanner`: creates a non-mutating execution candidate.
- `VisionExecutionService`: executes reviewed and confirmed candidates.
- `VisionCanvasAssembler`: builds backend-prepared canvas state for clients.

Controllers call one facade service. They should not assemble workflows, choose intent, or inspect slots.

## Conversation Pattern

Every prompt or voice transcript is a turn in a conversation.

Conversation state should be backend-owned and persisted from the first real orchestration phase onward so text turns, voice turns, review states, clarifications, and execution confirmations share one durable timeline.

A turn should produce exactly one primary next step:
- ask for one missing field
- ask the user to choose one option
- show a compact result
- show an action review
- ask for confirmation
- execute a confirmed action
- stop with a clear blocked state

Default rule: ask one high-value question at a time.

Do not ask for a full form worth of data unless the user explicitly asks to review or edit all details.

## Slot Pattern

Use typed slots rather than ad hoc strings.

Recommended slot shape:
- `slotId`
- `kind`
- `value`
- `confidence`
- `source`
- `required`
- `validationState`
- `missingReason`

Common slot IDs:
- `quest_title`
- `quest_description`
- `reward_amount`
- `free_quest`
- `scheduled_at`
- `ends_at`
- `location_label`
- `visibility`
- `circle_ids`
- `target_user`
- `confirmation`

Slot extraction may use an LLM, but slot validation must use deterministic backend logic and domain services.

## Clarification Pattern

Clarifications are backend-chosen micro-prompts.

Good:
- "What should the quest be called?"
- "When should it happen?"
- "Should this be public or circles only?"
- "I have enough to create this. Review it?"

Bad:
- "Please provide title, description, reward, start time, end time, location, audience, images, and terms."

If multiple fields are missing, choose the next field by:
1. required for execution
2. highest ambiguity
3. highest user impact
4. shortest path to review

## Execution Pattern

Execution must be separated from interpretation.

Flow:
1. Interpret prompt.
2. Collect and validate slots.
3. Build execution candidate.
4. Show review.
5. Require explicit confirmation.
6. Execute through a domain use case or service.
7. Return compact result state.

Do not execute directly from a raw natural-language prompt.

The first production execution capability should be `create_quest`, because it offers useful end-to-end coverage without introducing destructive or multi-actor risk too early.

## Execution Adapter Pattern

Each capability should have a typed adapter:
- `VisionCreateQuestExecutionAdapter`
- `VisionApplyToQuestExecutionAdapter`
- `VisionUpdateProfileExecutionAdapter`
- `VisionSendChatMessageExecutionAdapter`

Adapter responsibilities:
- accept typed execution candidate
- check actor and permission context
- map slots to existing request DTO or command
- call existing domain use case/service
- map result to canvas response

Adapter must not:
- parse natural language
- decide UI layout
- skip domain validation
- silently coerce unsafe values

## Safety Pattern

Fail closed on:
- missing required data
- ambiguous target
- unreliable translation for execution-critical meaning
- destructive command without explicit confirmation
- multi-actor action without actor context
- admin-only intent from non-admin user
- current-location write without trusted coordinates
- domain validation failure

Read-only planning can be permissive. Mutation execution cannot.

## API Pattern

Prefer a vision-specific conversation API instead of dashboard-specific prompt endpoints.

Recommended future endpoint:
- `POST /vision/conversations/turns`

Response should be backend-prepared canvas state:
- `conversationId`
- `turnId`
- `agentState`
- `canvasMode`
- `nextAction`
- `recognizedInput`
- `message`
- `blocks`
- `execution`
- `safety`

Do not return raw entities and expect the frontend to infer what to show next.

The API contract should be able to represent read-only planning while execution remains disabled by feature flag, so rollout does not require separate frontend architectures for planning-only versus execution-enabled environments.

## Canvas Block Pattern

Use a small block vocabulary:
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

Blocks should be specific enough to render consistently, but not so generic that `/vision` becomes a JSON form renderer.

## Frontend Pattern

Recommended frontend shape:
- `VisionSurfaceModernView.vue`: route-level shell
- `useVisionConversation.ts`: client-side conversation state and API calls
- `VisionAgent.vue`: animated agent state
- `VisionPromptDock.vue`: prompt and voice input
- `VisionCanvasRenderer.vue`: renders backend blocks
- `blocks/*`: focused block components
- `visionApi.ts`: API client

The frontend should treat backend canvas state as the source of truth.

Frontend may own:
- animation
- focus
- transitions
- responsive layout
- audio recording/playback controls
- local optimistic UI for typing/listening states

Frontend must not own:
- business validation
- required field decisions
- mutation readiness
- domain permissions
- target resolution

## Visual Pattern

Default direction:
- white or near-white background
- visually empty by default
- central abstract agent
- prompt dock appears only when needed
- focused inline field reveal
- compact review state
- compressed success state

Avoid:
- dashboard chrome
- full-page forms
- nested cards
- generic modal-first flows
- permanent sidebars
- dense admin tables unless explicitly in comparison mode
- explanatory UI text that describes how the interface works

## First Capability Pattern

The first real executor should be `create_quest`.

Reason:
- high value
- authenticated actor owns the action
- no existing target resolution required
- easy to review before commit
- exercises slot collection and validation

Recommended sequence:
1. planning-only create quest conversation
2. create quest review
3. confirmed create quest execution
4. find/rank quests
5. apply to quest
6. edit own quest
7. profile/chat/thing/ride/business capabilities

Defer destructive, admin-only, and multi-actor execution until the adapter and safety patterns are proven.

## Documentation Pattern

When vision behavior changes, update:
- `docs/business-logic.md` for user-facing behavior
- `docs/domain-technical.md` for technical source of truth
- `docs/agent-operating-model.md` and YAML/sections for intent, endpoint, and safety changes
- this document when backend/API/frontend patterns change
- `.agents/*plan.md` when a planned rollout changes

## Validation Pattern

Backend orchestration changes:
- focused service tests
- `./mvnw test` when shared agent/domain behavior changes

API contract changes:
- backend tests
- generated frontend contracts
- `npm run type-check`
- `npm run build`
- endpoint and agent artifacts when relevant

Frontend canvas changes:
- `npm run type-check`
- `npm run build`
- visual smoke once the local dev server is used

Executor changes:
- full backend tests
- agent operating model validation
- domain use case tests
- explicit blocked-path tests

## Non-Negotiables

- No frontend-only workflow logic.
- No LLM-only mutation validation.
- No hidden execution without review and confirmation.
- No broad legacy frontend rewrite before the vision orchestration path is stable.
- No static long form as the default `/vision` interaction.

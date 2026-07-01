# Vision Backend Orchestration Plan

## Status

Complete.

## Purpose

Create a backend-first orchestration layer for the future blank-canvas `/vision` experience.

This layer should translate text or voice-derived text into a structured, stepwise conversation that can eventually execute real domain actions safely.

## Proposed Package Shape

Initial package:
- `com.themuffinman.app.vision.controller`
- `com.themuffinman.app.vision.service`
- `com.themuffinman.app.vision.dto`
- `com.themuffinman.app.vision.model`
- `com.themuffinman.app.vision.mapper`
- `com.themuffinman.app.vision.config`

Reason:
- Keeps `/vision` separate from legacy dashboard semantics.
- Allows future mobile and non-web clients to reuse the same orchestration API.
- Keeps existing workmarket/social/business services as domain capabilities, not UI drivers.

## Core Backend Concepts

`VisionConversation`
- Represents one adaptive user task session.
- Owns current intent, collected slots, missing slots, visible canvas mode, and execution readiness.

`VisionTurn`
- Represents one user message or voice transcript plus the backend response.
- Stores normalized prompt, detected intent, confidence, and next action.

`VisionIntent`
- A stable backend capability name such as `create_quest`, `find_quests`, `apply_to_quest`, `open_chat`, or `create_ride_offer`.
- Should map to agent operating model intent IDs where possible.

`VisionSlot`
- A typed missing or collected value required by an intent.
- Examples: `quest_title`, `quest_description`, `reward_amount`, `scheduled_at`, `location`, `visibility`, `target_user`, `confirmation`.

`VisionClarification`
- A question or choice set returned when the next safe step is to ask the user for one missing value.
- Should be small and focused.

`VisionExecutionCandidate`
- A fully or partially prepared command that can later call a domain use case.
- Must remain non-mutating until explicit review/commit.

`VisionCanvasState`
- Backend-prepared UI state that says what the frontend should render.

## Orchestration Flow

1. Accept prompt or voice transcript.
2. Normalize and translate prompt if needed.
3. Detect likely intent.
4. Load or create conversation state.
5. Merge new user input into collected slots.
6. Validate collected slots with domain-aware validators.
7. Choose next step:
   - ask for one missing required slot
   - show choices
   - show read-only results
   - show review
   - execute after explicit confirmation
   - stop with fail-closed error
8. Return `VisionCanvasState`.

## Slot Collection Policy

Default rule:
- Ask for one missing value at a time.

Exceptions:
- Ask for a small grouped choice only when the options are naturally tied, such as date and time.
- Show review with multiple fields only when the user has already provided enough data for a coherent draft.

Bad pattern:
- Asking for title, description, date, price, location, visibility, images, and confirmation all at once.

Good pattern:
- "What should the quest be called?"
- "When should it happen?"
- "Should it be open to everyone or only circles?"
- "I have enough to create the quest. Review?"

## First Capability Recommendation

Start with `create_quest`.

Minimum slots:
- title
- description or short task summary
- reward amount or free flag
- schedule mode
- visibility/audience
- location mode

Execution safety:
- No mutation until explicit review.
- Use existing quest validation and create use case.
- If schedule or reward is ambiguous, ask a clarification.
- If translation is unreliable, ask confirmation before execution.

## Service Boundaries

`VisionConversationService`
- Creates/loads conversation state.
- Applies turns.
- Returns canvas state.

`VisionIntentRouter`
- Converts normalized prompt plus context into a candidate intent.
- May use deterministic rules plus LLM classification.

`VisionSlotService`
- Extracts and merges slots.
- Tracks missing required slots.

`VisionClarificationService`
- Chooses the next one-step question.

`VisionExecutionPlanner`
- Builds execution candidates.
- Does not mutate.

`VisionExecutionService`
- Executes only reviewed and confirmed candidates.
- Calls domain use cases.

`VisionCanvasAssembler`
- Converts conversation state into API response DTOs for the frontend.

## Persistence Strategy

Phase 1 can choose between two approaches:

Option A: Stateless signed state token
- Faster to build.
- Good for proof of concept.
- Harder to inspect and recover.

Option B: Persisted conversation tables
- Better for real product behavior.
- Supports resume, audit, and future memory.
- Requires migration and cleanup policy.

Recommendation:
- Start with persisted conversations if implementation scope is accepted.
- Use retention config from the beginning.

## Required Config

Typed config under `config/`:
- conversation TTL
- max turns per conversation
- max prompt length
- OpenAI model profile selection
- execution enabled flag
- per-capability execution flags

## Testing

Backend tests should cover:
- blank prompt rejection
- intent detection fallback
- one-slot-at-a-time clarification
- create quest happy path to review
- create quest confirmation required before execution
- ambiguous target fail-closed behavior
- unreliable translation blocks execution or requires confirmation
- anonymous access forbidden

## Non-Goals For First Implementation

- Full memory/personalization.
- Multi-actor execution.
- Destructive execution.
- Admin automation.
- Replacing every legacy endpoint.

## Completion Evidence

- Status: complete
- Validation:
  - `./mvnw -q -Dtest=VisionExecutionServiceTest,VisionChatExecutionServiceTest,VisionConversationServiceTest,VisionExecutionPlannerTest,VisionPromptUnderstandingServiceTest,VisionIntentRouterTest test`
  - `make audit-todo`
- Notes:
  - The backend orchestration layer now persists conversations, understands `create_quest`, `discover_quests`, and `open_chat`, and routes confirmable mutation and bounded chat execution through dedicated service boundaries.
  - `create_quest` execution remains behind the review gate and typed execution flag, while `open_chat` resolves a target contact through the chat boundary and existing circle permissions.

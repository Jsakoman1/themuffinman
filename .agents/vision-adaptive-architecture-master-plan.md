# Vision Adaptive Architecture Master Plan

## Status

Planning only. Do not start implementation from this plan until the user explicitly asks for implementation.

## Goal

Build the future `/vision` architecture as the primary long-term product surface: a white, visually quiet, adaptive blank canvas that reveals prompt fields, guided inputs, results, confirmations, and visual structure only when the current task needs them.

The long-term direction is to remove the legacy frontend over time and leave one adaptive surface that can become work marketplace, sharing, rides, business, booking, chat, or account flows without looking like a classic windows/forms application.

## Current Reality

What exists today:
- `/vision` has a modern agent-centered UI and a bottom prompt composer.
- Text and voice can enter one backend prompt path.
- OpenAI-backed STT, TTS, prompt translation, and planning summaries are wired through backend services.
- `DashboardVisionPromptService` now reuses `AdminAgentPlaygroundService.analyzePrompt(...)` for planning.
- Agent operating docs model many intents, safety categories, resolution rules, and pre-executor constraints.

What does not exist yet:
- No production executor mutates quests, applications, circles, chat, or profile state from natural-language instructions.
- No stateful conversation session stores partial slot collection across turns.
- No backend contract tells the frontend exactly which transient fields, prompts, result cards, confirmations, or guidance fragments to show next.
- No stepwise clarification policy is implemented as a reusable orchestration engine.
- `/vision` still depends on parts of the old dashboard/read model for context.

## Architectural Position

Read `docs/vision-architecture-patterns.md` before implementing any child plan from this master plan.

Read `.agents/vision-preflight-plan.md` before starting the first runtime `/vision` execution phase.

Build a new vision orchestration layer beside the legacy module UI, not inside the legacy frontend model.

The new layer should reuse existing domain services and use cases for real business rules, but it should own:
- conversation state
- intent and capability routing
- stepwise clarification
- adaptive display instructions
- execution readiness
- final action review
- result compression

Legacy backend services should stay useful as stable domain capabilities. Legacy frontend screens should not dictate the new interaction model.

## Preflight Decisions

The following decisions are now treated as locked unless later evidence forces a deliberate architecture update:

- The first real executor scope is `create_quest`.
- Phase 1 conversation continuity should use persisted backend conversation state, not a client-managed state token.
- Real mutation execution must remain behind typed backend `vision.*` feature flags.
- New `/vision` implementation should grow from dedicated `vision` orchestration and API layers rather than extending legacy dashboard read-model assumptions.

## Child Plans

1. `.agents/vision-backend-orchestration-plan.md`
- Create the backend conversation and orchestration foundation.
- Define session state, turn state, slot collection, intent routing, and action readiness.

2. `.agents/vision-api-contract-plan.md`
- Define the API contract between `/vision` and the backend orchestrator.
- Make the response describe what the canvas should show, hide, ask, confirm, or execute next.

3. `.agents/vision-frontend-canvas-plan.md`
- Build the adaptive frontend shell around backend-prepared canvas state.
- Keep the UI visually blank, white, minimal, animated, and free of form-heavy layout unless precision is needed.

4. `.agents/vision-executor-rollout-plan.md`
- Introduce real execution safely, one domain capability at a time.
- Start with low-risk/create-only flows before destructive or multi-actor workflows.

5. `.agents/vision-validation-docs-plan.md`
- Define tests, safety docs, generated artifacts, and closeout rules for this architecture.
- Keep agent model, backend code, frontend contracts, and product docs aligned.

## Development Phases

### Phase 0: Architecture Freeze

Deliverables:
- Confirm the domain vocabulary: `conversation`, `turn`, `intent`, `slot`, `clarification`, `canvas state`, `execution candidate`, `review`, `commit`.
- Freeze the rule that `/vision` UI renders backend-prepared state and does not infer domain workflow rules.
- Freeze the rule that all mutation execution remains fail-closed until exact target resolution and required confirmation exist.

Exit criteria:
- Master and child plans reviewed.
- No production code required.

### Phase 1: Conversation Orchestrator

Deliverables:
- Backend package for vision orchestration.
- Persisted backend conversation session and turn history.
- Stepwise slot model that asks one useful question at a time.
- Read-only planning result that can produce canvas instructions.

Exit criteria:
- User can say or type "create a new quest..." and backend returns either a ready review state or the next missing field.
- No mutation execution yet.

### Phase 2: Adaptive Canvas Contract

Deliverables:
- Versioned DTOs for canvas response.
- Display primitives such as `agent_state`, `prompt`, `field_request`, `choice_set`, `summary`, `review`, `result`, `error`.
- Frontend renders these primitives without hardcoding quest-specific workflow decisions.

Exit criteria:
- `/vision` can show and hide prompt fields, field hints, summaries, and confirmations based on backend state.
- Frontend type-check/build passes.

### Phase 3: First Real Executor

Recommended first executor: `create_quest` with explicit review and confirmation.

Why:
- It is valuable.
- It exercises structured slot collection.
- It can be made safe because the actor is the authenticated user.
- It avoids destructive and multi-actor side effects.

Exit criteria:
- User can create a quest through a stepwise `/vision` conversation.
- Backend uses existing quest validation and use case boundaries.
- Frontend shows review before final commit.

### Phase 4: Capability Expansion

Add one capability at a time:
- find/rank quests
- create quest
- edit own quest
- apply to quest
- update own profile details
- start/complete quest
- chat open/send
- business profile edits
- thing listing
- ride offer

Defer until executor patterns prove stable:
- delete flows
- admin-only flows
- multi-actor flows
- "approve first applicant"
- current location writes
- circle-only scheduled automation

## Core Architecture Rules

- Backend owns business meaning.
- Frontend owns presentation, motion, and local interaction polish.
- API returns screen state, not just raw entities.
- The orchestrator asks one high-value question at a time unless a grouped review is genuinely clearer.
- LLM output may propose, summarize, classify, or transform language, but deterministic backend services decide permissions, validation, state transitions, and execution readiness.
- Any mutation must go through an explicit execution adapter that calls an existing use case or a new domain service with normal validation.
- The canvas should never become a generic JSON form renderer. It should render a small vocabulary of product-native adaptive primitives.

## Design Rules For `/vision`

- Default background should be white or near-white.
- The default state should feel empty on purpose.
- Prompt input should appear when the user engages, then collapse when not needed.
- Field entry should appear as focused inline guidance, not a full form page.
- Results should be compressed first, expanded only on demand.
- Buttons should be minimal and icon-led where possible.
- Avoid visible help text that explains the interface.
- Avoid window, modal, and form metaphors unless precision or confirmation requires a bounded surface.
- Use motion for state changes: listening, thinking, asking, ready, executing, complete, blocked.

## Risk Controls

- No autonomous destructive action.
- No multi-actor mutation without actor context and explicit authority.
- No hidden execution after ambiguous prompt interpretation.
- No frontend-only business rules.
- No LLM-only validation.
- No broad legacy rewrite during early phases.

## Open Questions Before Implementation

- Should the first executor create a persisted quest immediately after review, or create a draft first?
- Should `/vision` execution rollout be available to all authenticated users behind config, or restricted further to admin/developer cohorts first?
- Which data should the canvas remember across sessions beyond the conversation itself: recent prompt, preferred area, voice preference, task history, or none initially?

## Validation Strategy

- Backend unit tests for orchestrator slot policy, clarification flow, and execution readiness.
- Controller tests for API contract and authentication.
- Contract generation for frontend DTOs.
- Frontend type-check and build on every canvas contract change.
- Agent operating model updates whenever intents, safety rules, endpoint contracts, or executor behavior changes.
- Product docs updates whenever user-facing behavior changes.

## Definition Of Done For Future Implementation

- `/vision` can handle a user command as a conversation, not a one-shot form.
- Backend can return the next useful question.
- Frontend can render only the necessary UI for that turn.
- Execution remains fail-closed until exact required data is present.
- Tests and docs describe both happy path and blocked/clarification path.

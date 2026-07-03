---
machine_kind: master-plan
machine_status: active under `.agents/god-plans/vision-god-plan.yaml`
machine_title: Vision Adaptive Architecture Master Plan
machine_goal: 'Build the future `/vision` architecture as the primary long-term product
  surface: a white, visually quiet, adaptive blank canvas that reveals prompt fields,
  guided inputs, results, confirmations, and visual structure only when the current
  task needs them.'
---

# Vision Adaptive Architecture Master Plan

## Status

Active under `.agents/god-plans/vision-god-plan.yaml`.

The current implementation phase is no longer the original blank preflight. Persisted conversation state, backend-prepared canvas blocks, typed review edits, recent conversation recovery, voice feedback, and the first `create_quest` execution adapter now exist.

## Goal

Build the future `/vision` architecture as the primary long-term product surface: a white, visually quiet, adaptive blank canvas that reveals prompt fields, guided inputs, results, confirmations, and visual structure only when the current task needs them.

The long-term direction is to remove the legacy frontend over time and leave one adaptive surface that can become work marketplace, sharing, rides, business, booking, chat, or account flows without looking like a classic windows/forms application.

## Mandatory Preflight Pattern

Before starting runtime implementation from this master plan, run an adapted preflight pass for the current scope instead of jumping directly into code.

This is the preferred implementation-start pattern for broad `/vision` work because it reduces contradictions, keeps changesets legible, and forces the first executor and rollout boundaries to be explicit before orchestration code starts to spread.

The preflight pass should always cover:

1. lock the documentation baseline across `docs/product-vision.md`, `docs/vision-architecture-patterns.md`, `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, and the active plans
2. clean or logically isolate the git changeset before new orchestration work begins
3. define the first executor scope explicitly instead of aiming for a general agent that can do everything
4. decide persisted conversation versus client state token before conversation code is introduced
5. introduce or confirm backend feature flags that separate planning/clarification from real mutation execution

Recommended implementation-start order:

1. cleanup or baseline commit
2. short architecture preflight plan
3. backend conversation/orchestration foundation
4. API contract and frontend canvas renderer
5. first real executor behind feature flag

## Current Reality

What exists today:
- `/vision` has a modern agent-centered UI, animated agent, adaptive prompt composer, backend-driven canvas renderer, review block, field-request block, and recent conversation context.
- Text and voice can enter one persisted backend conversation path.
- OpenAI-backed STT/TTS and prompt understanding are wired through backend services, with local fallback when OpenAI is unavailable.
- Persisted conversation state, turn history, requested-slot tracking, applied-slot summaries, reset, cancel, resume, and recent conversation endpoints exist.
- The first `create_quest` execution adapter exists behind typed backend feature flags and explicit review confirmation.
- Review-confirmation execution now flows through a dedicated `VisionExecutionService` boundary around the `create_quest` adapter.
- Shared semantic focus mapping now covers prompt understanding fallback and typed review-edit routing.
- Read-only execution candidate planning now describes readiness, blockers, and the next required field for create_quest review turns.
- The frontend vision canvas now renders the execution candidate as a compact read-only block and reuses the planning summary in review state.
- Read-only quest discovery and backend-governed intent switching now share the same adaptive surface as create_quest.
- The modern surface now uses an inline idle composer, mode-specific hero states, and compact in-shell context reveal instead of stacked blank-state chrome.
- The route shell now keeps its adaptive surface-state calculations in a reusable composable so the view stays thin while the shell behavior stays consistent.
- Shared prompt semantics now also power the admin playground planning slice, so the same normalization and classification rules are reused across user-scoped and admin-scoped surfaces.
- The read-only execution-planning and canvas-execution slices are complete, so the review surface now stays read-only until an execution adapter is explicitly invoked.
- The long-session resume and continuity model now uses compact backend summaries as the memory source, so the shell can stay calm instead of reconstructing state from older transcript text.
- Agent operating docs model many intents, safety categories, resolution rules, and pre-executor constraints.

What does not exist yet:
- No mutation executors beyond `create_quest` exist.
- No generic multi-capability execution service exists yet.
- Intent routing still needs broader capability coverage beyond `CREATE_QUEST` and read-only discovery.
- Prompt understanding now supports generic planning metadata and discovery, but broader capability understanding still remains deferred.
- Recent-task continuation behavior is now summary-first, but broader capability coverage still needs to keep the same calm continuation model.
- Fixed scheduling still uses one overloaded `scheduled_at` slot; the date/time split follow-up now lives in `.agents/vision-schedule-date-time-split-master-plan.md`.

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
- Read-only execution planning should remain separate from mutation execution so the backend can describe readiness without creating a second commit path.

## Child Plans

1. `.agents/vision-backend-orchestration-plan.md`
- Create the backend conversation and orchestration foundation.
- Define session state, turn state, slot collection, intent routing, and action readiness.
- Status: complete

2. `.agents/vision-api-contract-plan.md`
- Define the API contract between `/vision` and the backend orchestrator.
- Make the response describe what the canvas should show, hide, ask, confirm, or execute next.
- Status: complete

3. `.agents/vision-frontend-canvas-plan.md`
- Build the adaptive frontend shell around backend-prepared canvas state.
- Keep the UI visually blank, white, minimal, animated, and free of form-heavy layout unless precision is needed.

4. `.agents/vision-executor-rollout-plan.md`
- Introduce real execution safely, one domain capability at a time.
- Start with low-risk/create-only flows before destructive or multi-actor workflows.

5. `.agents/vision-validation-docs-plan.md`
- Define tests, safety docs, generated artifacts, and closeout rules for this architecture.
- Keep agent model, backend code, frontend contracts, and product docs aligned.

6. `.agents/vision-generic-semantic-planning-plan.md`
- Add a generic semantic planning boundary above create_quest slot extraction.
- Prepare intent, capability, and confidence metadata before adding more executors.
- Status: complete

7. `.agents/vision-execution-planning-plan.md`
- Add a non-mutating execution planning boundary above the existing create_quest review path.
- Surface readiness, blockers, and execution preview data without introducing a new mutating executor.
- Status: complete

8. `.agents/vision-canvas-execution-surface-plan.md`
- Render the execution candidate in the frontend canvas as a compact read-only block.
- Keep review confirmation visually unified with the same adaptive surface rather than a separate popup.
- Status: complete

9. `.agents/vision-memory-and-context-master-plan.md`
- Harden the durable repo context for `/vision` implementation so future sessions do not have to rediscover architecture decisions, generated-artifact rules, repeated failures, or test setup patterns.
- Status: complete

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
- Status: complete for the first create_quest conversation/execution scope; mutation execution exists behind explicit review and backend feature flag.

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
- Status: initial create_quest executor exists behind `vision.execution-enabled`; hardening continues.

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

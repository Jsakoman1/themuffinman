# Vision Architecture Patterns

This document is the durable implementation guide for future `/vision` work.

Read it before implementing any backend, API, or frontend change that affects the adaptive vision surface, conversation orchestration, prompt handling, canvas state, or natural-language execution.

The compact companion set for daily work is:

- `docs/vision-context-gateway.md`
- `docs/vision-decision-record.md`
- `docs/vision-status-ledger.md`
- `docs/vision-presentation-contract.yaml`

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

Use `docs/implementation-control.md` and the owning work plan as the enforcement layer for this start pattern.

The machine-readable presentation policy lives in `docs/vision-presentation-contract.yaml`. Extend that contract before
adding new device-specific or user-visible Vision states.

Shared web surfaces must honor reduced-motion preferences, keyboard focus visibility, safe-area insets, and a minimum
touch target before a migration slice can be considered usable on phone-sized screens.

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

Frontend renderer shape should stay split into:
- one route shell
- one conversation composable
- one canvas router
- focused block components for review, field requests, result summaries, and shared status framing

The prompt composer should stay inside the adaptive canvas flow as an inline surface, not as a fixed floating dock that competes with the canvas contents.

Do not let one Vue file absorb the entire block vocabulary once the backend response shape expands.

Route-shell behavior should also stay blank-canvas oriented:
- avoid persistent page headers and module chrome when the task does not need them
- reveal status, memory, and support context through one compact contextual control only when useful
- keep the animated agent and prompt dock as the dominant anchors in the default state
- keep a compact route navigator available for stable module entry without turning the canvas into a dashboard
- treat shared-shell launches into `/vision` as typed handoffs rather than free-form redirects; the current shell contract uses route query fields for prompt, source, context label, and return target so the adaptive surface can explain where the handoff came from and how to get back
- auto-reveal state context when the backend enters review, blocked, or complete modes, because those are the moments when the user needs summary context most
- treat the backend runtime contract as the source of truth for device role, attention state, action hints, and audio or haptic cues so mobile and voice clients stay thin while still adapting their density
- treat the request id as the replay boundary for turn submission retries so the backend can safely return the last persisted turn without minting a duplicate conversation step
- treat `create_quest` as the gold-standard interaction pattern for both the terminal feed and the preview model: reveal only the next useful line, keep capability entry points inline and contextual, and avoid separate launcher surfaces that compete with the primary canvas
- extend the same terminal-first pattern to circles, applications, profile, chat, and request-style entity flows so they read like one evolving conversation surface rather than separate dashboard pages
- keep narrow self-profile mutations on the same terminal-first path by using backend-owned patch adapters that preserve unchanged identity and location state instead of sending sparse DTOs directly into broader profile-update services
- keep application creation on the same terminal-first path by resolving one backend-visible quest target before review, asking only for the next required slot, and letting quest pricing rules decide whether a proposed price slot is required
- keep application self-service updates and withdrawals on the same terminal-first path by resolving one exact pending self application first, showing the current application state in the textual preview, and requiring explicit confirmation before save or withdraw execution
- keep owner-side application approval and decline on the same terminal-first path by resolving one exact manageable quest and one exact pending applicant before review confirmation
- keep circle-request send/accept/delete on the same terminal-first path by resolving one exact person or one exact pending request before review confirmation
- keep owned-circle rename and delete on the same terminal-first path by resolving one exact owned circle before review confirmation and never reconstructing circle authority from frontend state
- keep profile-location updates on the same terminal-first path by limiting the draft to location mode and label while backend patch adapters preserve unrelated identity and location-sharing fields
- keep circle and circle-request writes on the same terminal-first path by routing them through one dedicated vision-side mutation adapter instead of direct social-service calls inside the preview facade
- keep confirmations, debug details, and request review surfaces inline in the feed unless a native browser modal is explicitly required for a destructive cross-route boundary
- when a route needs a preview, render it as a textual entity sketch that grows and reshapes with the current object instead of a modal, sheet, or split-pane shell
- circles, applications, and profile routes should prefer one linear feed with inline actions and text-based entity summaries over card stacks, grids, or profile panels
- keep prompt understanding on the low-cost semantic model by default, and reserve the heavier semantic model behind an explicit backend flag when a batch needs it
- keep the admin playground deterministic and local-only so OpenAI usage is reserved for the user-facing semantic and voice paths

Animated agent motion should feel layered and ambient:
- use slow field drift, halo breathing, and subtle spark motion instead of flat pulsing
- increase motion slightly when the backend is listening or processing
- keep the center readable and calm, not noisy or gimmicky

Prompt composer behavior should also stay blank-canvas oriented:
- keep the inline composer visible in the blank state as the primary entry point
- let the idle composer carry the first task invitation instead of adding a second launcher step
- expand automatically when the backend asks for richer input, the user is composing, or the flow needs review context

Idle hero text should not compete with active backend content:
- show the large intro only while the canvas is actually blank
- once response blocks exist, let the canvas and orb carry the visual focus

Surface controls should also stay sparse:
- hide the compact context control while the surface is fully idle and context-free
- show it again when a response, recent memory, or active voice interaction makes it useful

Surface tone should be adaptive but restrained:
- let CSS variables shift the wash and base background by canvas mode or voice state
- keep mood changes subtle enough that the surface still reads as one white canvas
- do not introduce extra panels just to express state

Shared surface tokens should stay centralized:
- route shell, prompt dock, canvas panel, and agent orb should consume the same surface palette variables
- prefer changing the shared tokens in the shell over re-tuning each child component independently
- keep tone shifts coordinated so the visual system reads as one surface with one mood

## Locked Preflight Decisions

Treat these as the default architecture baseline for upcoming `/vision` implementation unless a later documented architecture change replaces them:

- First executor scope: `create_quest`.
- Conversation continuity: persisted backend conversation state, not client-managed state tokens.
- Rollout boundary: real mutation execution stays behind typed backend `vision.*` feature flags.
- Migration boundary: new `/vision` orchestration grows beside legacy dashboard/backend read models instead of extending them as the primary design source.

The shorter immutable record for those decisions lives in `docs/vision-decision-record.md`.

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
When `/vision` needs domain-shaped previews or thin mutation handoff code, keep those pieces in explicit vision-side collaborators such as preview renderers or mutation adapters instead of growing one facade service into a second domain layer.
Apply that rule inside vision too: social previews, profile draft previews, profile patch assembly, feed previews, and workmarket preview handoff should stay in named collaborators instead of collapsing back into one large preview facade.

## Backend Service Pattern

Prefer these service roles:

- `VisionConversationService`: creates, loads, advances, and closes conversations.
- `VisionConversationLifecycleService`: handles reset, cancel, load, and recent-conversation entrypoints so read-only lifecycle access stays separate from the main turn-processing facade.
- `VisionIntentRouter`: maps prompt plus context to a candidate intent.
- `VisionSlotService`: extracts, normalizes, validates, and merges slots.
- `VisionClarificationService`: selects the next one useful question.
- `VisionExecutionPlanner`: creates a non-mutating execution candidate.
- `VisionExecutionService`: executes reviewed and confirmed candidates.
- `VisionCanvasAssembler`: builds backend-prepared canvas state for clients.
- `VisionLearningService`: records preference signals, feedback events, and compact memory summaries from completed turns, and computes decay-aware preference confidence for both learning and memory packing.
- `VisionMemoryCompactionService`: periodically rolls up preference and feedback history into summary rows.

Controllers call one facade service. They should not assemble workflows, choose intent, or inspect slots.

## Conversation Pattern

Every prompt or voice transcript is a turn in a conversation.

### Authority, Personal Context, And Client Boundaries

`/vision` is the adaptive, voice-ready client, while the Linear-inspired workspace is the direct Web interaction client. Shared shell handoffs may carry typed prompt, source, context-label, and return-target data, but they must not import Web workspace rails, boards, or command-center semantics into the Vision canvas.

Vision may use approved personal context to rank options or propose a next step. The backend owns context retrieval, consent checks, user scope, retention, explanation records, and any preference-learning update. Clients render the resulting compact context and explanation; they do not compose durable memory, infer authority, or turn an unverified transcript into a command.

The execution boundary is mandatory for every input mode: raw text, speech transcript, semantic-model output, and UI action all resolve into a backend-published typed capability. Deterministic validation, permission checks, review state, and explicit confirmation run before a mutation adapter executes. A recommendation remains non-mutating until that boundary is passed.

Conversation state should be backend-owned and persisted from the first real orchestration phase onward so text turns, voice turns, review states, clarifications, and execution confirmations share one durable timeline.
Persistent preference rows and feedback events should sit beside the conversation timeline so the backend can learn stable habits and correction history without promoting raw transcript replay into the semantic request.
Preference rows should carry both a stored confidence score and a last-confidence-update timestamp, because the backend needs a stable row plus a decay anchor rather than only a raw count.
When the backend selects learned preference signals for runtime memory or turn-level summaries, it should rank them by backend priority first and confidence second, because input mode and topic-family habits are more useful to surface than a flat confidence list.

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

See `docs/vision-decision-record.md` `VDR-002` and `VDR-005`.

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
- `scheduled_date`
- `scheduled_time`
- `ends_at`
- `location_label`
- `visibility`
- `circle_ids`
- `target_user`
- `confirmation`

For `/vision` fixed scheduling, prefer separate conversational slots for day and time and derive one absolute `scheduled_at` only at the execution boundary once both are present.

Slot extraction may use an LLM, but slot validation must use deterministic backend logic and domain services.
The LLM layer should stay separate from slot validation as a semantic understanding step that can return normalized prompt text, a focus slot, and extracted slot candidates, while deterministic backend services still decide which values are accepted.
Do not let fallback heuristics treat every prompt as description, reward, or location text just because the user spoke in one sentence.
When the conversation already has a requested slot, the backend should use that slot as the fallback semantic focus if the model response does not name one explicitly.
Prefer one shared semantic-mapping service for prompt understanding, fallback focus, and review-edit follow-up routing so the same turn-level rules do not drift across services.
Semantic understanding should also return a generic semantic plan before slot extraction becomes capability-specific. The current first slice carries `candidateIntent`, `candidateIntentConfidence`, `capabilityId`, and a short planning note above create_quest slots so intent routing can move away from raw prompt heuristics without expanding execution authority.
That semantic slice now also needs to carry required-slot metadata, a clarification flag, and replay-oriented record data so missing context is explicit before DTO assembly begins.
Target lookup should stay separate from action intent: the backend may resolve one target entity family for clarification and DTO mapping with its own confidence threshold even when the action itself belongs to a different family.
For example, a request such as “Show me the lawn-mowing job Frank posted yesterday” must use the same backend pipeline for typed text and speech: resolve the permitted person, quest family, title/intent, and time constraint; return one authorized quest; then emit a typed `workspace-v1` handoff to the canonical Web Work detail route. Ambiguous, inaccessible, or stale results must produce clarification or recovery, never a guessed route or a private-existence leak.
The current discovery slice adds read-only `DISCOVER_QUESTS` routing and a quest-discovery canvas payload, but it still keeps mutation execution scoped to typed backend adapters.
The first non-quest capability-expansion slice should follow the same pattern: route read-only profile, circles, and applications through the main conversation surface and render backend-prepared textual previews instead of navigating to separate route views.
Shared prompt semantics should be reused across Vision and Admin Playground when the same normalization and intent-classification rules apply, so both surfaces consume one backend prompt-understanding boundary instead of maintaining separate local heuristics.
OpenAI-backed semantic orchestration should receive a backend-owned request contract instead of a free-form prompt only. The request must include the raw prompt, current conversation context, user context, allowed route catalog, and expected response contract.
The route catalog is published by backend services per turn. The model may select only routes, capabilities, DTO fields, and slots present in that catalog, and backend services must reject anything outside it.
After parsing model output, backend services should first fail-close any capability or focus-slot selection outside the published response contract, then sanitize the semantic payload against the same route catalog before intent routing, slot merging, or review planning continue.
Maintain a semantic audit matrix with representative quest, circle, application, profile, chat, and detail prompts so route drift and fallback regressions surface in tests instead of user sessions.
User context should carry available locale, language, country, locality, and timezone hints so STT transcripts and typed text can be interpreted in the user's real operating context. When only a country is known, the backend may provide a conservative timezone default, such as `Europe/Zurich` for `CH`, but final scheduling validation remains deterministic.
Locale and timezone should be resolved by explicit backend precedence instead of whichever hint arrives first. Prefer explicit persisted user preferences when they exist, then durable user-specific history, then client runtime hints, and finally backend country defaults or global defaults. The chosen source should be included in the semantic request so later capability slices can reason about confidence.
The semantic request should also carry a compact memory pack split into user memory, session memory, and recent turn snapshots, because multi-topic conversations are easier to orchestrate when stable preferences stay separate from the active thread. That memory pack should also expose recent entity families so vague follow-ups can stay anchored to the active topic unless the user clearly switches domains. Low-confidence ambiguous follow-ups should stay in the current thread unless the prompt clearly signals a different entity family. Strong preference signals may also slightly shape clarification wording, but they should not bypass policy or route selection. The turn response should also carry a compact memory trail for frontend debug and adaptive preview use, plus a learned-memory payload with the latest compact summary and structured preference signals so the UI can show what was learned without walking the full memory pack.
Fallback focus should not override a clearly switched entity family, because stale requested-slot memory can otherwise pin the user back to the previous topic even after the new prompt clearly changes domain.

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

When `/vision` and admin operator surfaces share runtime abstractions, keep the split explicit:
- `Vision` stays user-scoped and must not gain cross-user execution authority
- admin surfaces may gain broader execution only through separate admin-scoped policy gates
- shared policy abstractions may standardize capability checks, but they must not blur trust boundaries

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

## Review Edit Pattern

Review corrections should use typed backend actions instead of natural-language edit heuristics.

The stable shape is:
- action says the user wants a review correction
- a typed review target identifies one editable field family
- backend maps that target to one requested slot
- clarification resumes for one field only

Natural-language review-edit detection should not exist in the review-ready backend path once typed review controls are available.

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

See `docs/vision-decision-record.md` `VDR-003` and `VDR-007`.

## API Pattern

Prefer a vision-specific conversation API instead of dashboard-specific prompt endpoints.

Recommended future endpoint:
- `POST /vision/conversations/turns`

The request contract is versioned and should carry:
- `conversationId`
- `inputType`
- `text`
- `clientCapabilities`
- `clientStateVersion`
- optional `selectedOptionId`
- optional `fieldValue`
- optional `confirmation`

The backend may continue to accept legacy prompt fields during transition, but the versioned request shape should be treated as the primary contract.

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

Current standardized implementation:
- `VisionSurfaceModernView.vue`: thin route shell and top-level layout
- `components/VisionAgentOrb.vue`: central animated agent surface
- `components/VisionPromptDock.vue`: prompt and voice dock
- `components/VisionCanvasRenderer.vue`: backend-driven block and review rendering
- `useVisionConversation.ts`: orchestration-facing frontend state
- `visionApi.ts`: contract boundary

## Reuse And Failure Memory

Future `/vision` work should prefer the local context and failure-memory path before broad repo rediscovery:

- use `docs/vision-context-gateway.md` for location of backend, API, frontend, tests, and docs
- use the owning `docs/work/*.yaml` plan to record contract, documentation, and test validation
- use reusable fixtures under `apps/themuffinman/src/test/java/com/themuffinman/app/vision/testing/` for conversation builders, slot presets, location candidates, and schedule phrases

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
- `docs/implementation-control.md` and YAML/sections for intent, endpoint, and safety changes
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

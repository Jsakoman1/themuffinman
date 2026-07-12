---
machine_kind: master_plan
machine_status: draft
machine_closeout_contract: 2
machine_baseline_ref: 0cf598d
machine_delivery_mode: coordination
machine_title: Vision Adaptive Mobile-First Master Plan
machine_goal: Reconcile the voice-first, automation-first product vision with a minimal iPhone/watch-first experience and a calm, capable web workspace without moving workflow meaning into the frontend.
---

# Vision Adaptive Mobile-First Master Plan

## Status

Draft. This is a program-level direction and ordering document. It deliberately does not authorize a broad frontend rewrite. Each listed child plan is now expanded into its own checkbox-based plan; Plan 1 must be reviewed before implementation starts.

## Master Plan Frame

- Purpose: turn TheMuffinMan into one coherent adaptive social-utility product: Vision handles semantic, voice, and guided tasks; module workspaces provide calm, deterministic browsing and known-detail work.
- Shared context: the backend already owns intent routing, semantic normalization, slot collection, permission checks, execution planning, explicit confirmation, device role, runtime cues, and persisted conversation continuity. The frontend must become a sparse renderer of that state rather than a terminal plus a permanent support/debug surface.
- Plan inventory: seven ordered child plans, described below and intentionally not yet created. These explicit paths are the authoritative child inventory:
  - `.agents/vision-ia-presentation-contract-plan.md`
  - `.agents/vision-shared-design-system-plan.md`
  - `.agents/vision-canvas-declutter-plan.md`
  - `.agents/vision-mobile-watch-contract-plan.md`
  - `.agents/web-workspace-modernization-plan.md`
  - `.agents/workspace-mobile-read-model-contract-plan.md`
  - `.agents/adaptive-surface-migration-closeout-plan.md`
- Ordering and dependencies: product interaction contract first, then shared presentation primitives, then Vision simplification, then mobile/device adaptation, then module workspaces and missing read-model/API work, followed by migration and quality closeout.
- Final review gate: one real task must feel coherent from voice/text intake through review and completion on phone, watch companion, and web, while module spaces remain useful without Vision and all task meaning remains backend-owned.

## Initial Analysis

### Product Intent Recovered From Current Sources

- The product is a Social Useful Network: people offer, find, coordinate, schedule, share, and communicate around useful local activity.
- `/vision` is the long-term primary adaptive interface, not a generic chatbot and not a replacement for every browse/list/detail surface.
- Voice and text are two inputs to the same persisted backend conversation. Speech transcription, semantic understanding, deterministic validation, target resolution, confirmation, and execution must remain separate backend concerns.
- The interface should feel like a quiet, white, Her-like environment that changes shape only when user intent requires more structure.
- Stable workspaces remain necessary: `My Work`, `Find Work`, `Chat`, `Calendar`, `Business`, `Circles`, and `Profile` give users orientation, known-object access, and deterministic scans. They must be thin clients over backend-prepared read models, not a second workflow engine.

### Current Implementation Strengths To Preserve

- `POST /vision/conversations/turns` already accepts typed client/device/locale input and returns backend-prepared canvas state, runtime context, review data, summaries, and actions.
- Vision state is persisted, replay-safe by request id, and supports text/voice continuity, clarification, typed review edits, confirmation, and typed execution adapters.
- Semantic routing is backend-published and fail-closed; frontend code does not determine capability, permissions, target resolution, validation, or mutation readiness.
- The current shell has a central route registry, Vision handoff utility, backend-backed read APIs, and one generic surface component. These are viable consolidation points.
- Existing device-role and runtime-cue contracts are the correct basis for iPhone and watch clients. A native client should render the same backend turn contract, not reproduce Vision rules locally.

### UX Findings

1. The Vision support rail contradicts the canvas model.
   - `VisionSurfaceModernView.vue` turns on a second grid column whenever a response has review, result, completion, blocked, discovery, execution, or memory data.
   - `VisionFlowDebugPanel.vue` permanently exposes route, slot, and transcript whenever a response exists. This is implementation/debug context, not default user-facing guidance.
   - `VisionIntentPreviewPanel.vue` duplicates a changing task model as a terminal sheet. It repeats fields, summary, and status already available in the main canvas and forces the task surface to shrink.
   - Result: a conversation that should grow one useful line at a time becomes a split-pane pseudo-terminal with competing information hierarchy.

2. The main canvas still emits too much operational metadata.
   - `VisionCanvasRenderer.vue` renders device role, attention state, session anchor, action hints, consent, resume, audio, and haptic cues as visible header and pill rows.
   - These are valuable runtime-contract fields, but most are renderer hints. They should be selectively projected into a compact user-facing state, not copied verbatim into normal UI.

3. The shared web shell is structurally useful but text-heavy and repetitive.
   - Desktop navigation includes label plus descriptive paragraph for every route. The header repeats route context, description, quick prompt, Vision explanation, contextual link, and account controls.
   - `shellDefinitions.ts` embeds phase commentary and implementation explanations as user-facing description and list content. `SurfaceContentView.vue` then renders them as hero copy, action descriptions, cards, row descriptions, and status notes.
   - This makes current live module routes read like an architecture demo rather than a finished product. Placeholder/example action routes such as fixed entity ids must not survive into production UI.

4. Shared components exist, but the visual grammar has not yet been reduced to a small product system.
   - `VisionTerminalRow` is doing both conversation feedback and structured preview work.
   - `SurfaceContentView` makes every module look like the same text-card page regardless of whether the user is scanning work, managing a schedule, reading chat, or editing profile.
   - There is no explicit density policy for phone, watch, or desktop, even though the backend already provides the device-role contract.

5. The product needs two complementary experiences, not one terminal everywhere.
   - Vision should be the adaptive task layer: initiate, understand, clarify, compare, review, confirm, and complete.
   - Module workspaces should be the dependable orientation and management layer: scan known work, open a known conversation, inspect bookings, view calendar pressure, and edit a stable profile/business page.
   - The handoff between them should be typed and compact: context label, return target, and optionally an initial task. It must not show a persistent “handoff explanation” panel on every Vision route.

### Design Decisions Proposed For Review

- Default UI rule: one dominant task, one primary action, and one compact state signal. Details appear only after deliberate reveal or when required to safely proceed.
- Use `glance -> act -> inspect` across every surface. Phone and watch optimize for glance/act; web can expose inspect beside the primary task only when it improves a real decision.
- Keep typed backend runtime context, but define a backend-prepared `presentation` projection with a small public vocabulary such as `quiet`, `listening`, `working`, `needs_input`, `review`, `complete`, and `blocked`. Do not expose raw diagnostics by default.
- Preserve the terminal/feed character only as a lightweight conversational texture. It must not dictate layout, add duplicate side sheets, or become the information architecture for browse-heavy modules.
- Replace generic cards and explanatory copy with a limited archetype set: adaptive canvas, compact state strip, result list, comparison sheet, review strip, conversation thread, schedule timeline, and profile/business identity sheet.
- Use a shared product design system with centrally owned tokens, layout primitives, iconography, state color/motion, accessibility behavior, and compact copy rules. No module may invent its own shell or loading/empty/error patterns.
- Make native readiness an API-contract goal, not a frontend emulation goal: stable DTOs and backend-owned sections/actions must be usable by Vue, iPhone, and watch without deriving product logic on any client.

### Scope Boundaries

In scope:

- Vision canvas simplification and support-context redesign.
- A shared, centralized design system and surface archetypes.
- A mobile-first responsive web implementation that is deliberately compatible with an iPhone app and watch companion.
- A web workspace model for work, chat, profile, business, scheduling, and booking entry points.
- Backend/API additions only where a client cannot render the correct product state without frontend inference.
- Documentation, contracts, tests, and generated artifacts needed to keep behavior deterministic.

Out of scope for this program:

- Replacing every domain service or duplicating domain rules in Vision.
- Broad autonomous execution beyond the typed adapters, permissions, validation, review, and feature flags already required by the vision architecture.
- Treating Apple Watch as a full app or form editor. It is a companion for status, triage, confirmation, quick voice capture, and handoff.
- Visual redesign based on screenshots alone without validating behavior against real backend states.
- A monolithic one-shot rewrite. Every migration must preserve an independently usable route and contract.

## Target Interaction Model

| Surface | Primary job | Default density | Allowed structure | Must not become |
| --- | --- | --- | --- | --- |
| Vision canvas | Understand and guide an active task from voice/text to outcome | One task and one next action | Inline transcript acknowledgement, one field request, compact result/list, review strip, conditional context reveal | Dashboard, debug terminal, permanent split pane, long form |
| iPhone app | Fast personal action and continuity | One-thumb, short sessions | Bottom navigation, full-screen task flow, compact sheet only when task requires it | Desktop shell compressed to a phone |
| Watch companion | Glance, confirm, capture, hand off | One decision or status | Status, next action, confirm/decline, voice capture, open-on-phone handoff | Browse-heavy module client or multi-field editor |
| Web workspace | Scan, compare, manage known entities | Medium, intentional density | Calm navigation, list/timeline/thread/identity archetypes, detail pane only where it improves a decision | Static card dashboard or documentation page |

## Plan Breakdown

- [ ] Plan 1: Product IA and presentation-contract specification.
  - Define user-visible surface states, copy budget, public versus diagnostic runtime fields, target navigation model, and the exact Vision-to-workspace handoff contract.
  - Produce a compact UX decision record and a state-to-archetype matrix for each priority journey.
  - Lock acceptance criteria for iPhone, watch, and web before components are changed.

- [ ] Plan 2: Shared design system and frontend architecture foundation.
  - Centralize color, type, spacing, safe-area, motion, elevation, focus, and density tokens.
  - Create shared primitives for app frame, compact state strip, action row, entity row, section header, empty/loading/error state, review strip, and contextual disclosure.
  - Split display-only components from Vision conversation concerns; remove presentation duplication without moving business logic to the client.

- [ ] Plan 3: Vision canvas declutter and support-context replacement.
  - Remove the default preview/debug rail and stop reshaping the canvas into two columns for routine states.
  - Convert raw runtime/debug details into an explicit developer-only inspection path; ship a compact user-facing contextual disclosure that is hidden while idle and appears only for review, blocked, completion, active voice, or explicit user request.
  - Render transcript acknowledgement, active slot, applied value, review, confirmation, and completion as one inline progressive feed with no duplicate preview model.
  - Preserve typed review actions, explicit confirmation, accessibility, voice cues, and replay behavior.

- [ ] Plan 4: iPhone-first and Watch-companion presentation contract.
  - Turn backend device role, attention state, action hints, audio cues, haptic cues, and `watchFriendly` data into a documented client presentation policy.
  - Define responsive breakpoints and safe-area behavior for the web client, then add contract fixtures for phone/watch states.
  - Specify native screen inventory, notification/deep-link handoff, offline boundaries, voice capture lifecycle, privacy/consent, and exact watch action limits.
  - Do not build native applications in this plan unless separately approved; make their implementation straightforward from shared DTOs and state contracts.

- [ ] Plan 5: Web workspace modernization and real route ownership.
  - Reduce the shell to compact navigation, current location, one contextual action, and account access; remove explanatory product/architecture copy and duplicate Vision launch controls.
  - Replace generic `SurfaceContentView` card pages with backend-backed archetypes matched to the task: work list, inbox/thread, schedule timeline, booking queue, business identity/offerings, and profile/settings.
  - Deliver priority routes in this order: Home/My Work and Find Work; Chat; Profile; Business owner workspace; public business page and external booking journey; Calendar cross-module lens.
  - Replace hard-coded demo entity actions and architecture notes with real empty states, permissions, and backend-prepared next actions.

- [ ] Plan 6: Backend read-model, API, and contract completion for workspace and mobile clients.
  - Audit each priority route for data that the frontend currently derives, hides, or fakes; add viewer-specific backend read DTOs, sections, actions, and pagination/cursor policies where needed.
  - Add missing public-business and external booking contracts so a non-owner can discover a business, inspect offerings/availability, book, receive status, and coordinate without frontend business logic.
  - Keep semantic task understanding, permissions, validations, workflow transitions, scheduling, and mutations in backend services and typed adapters.
  - Regenerate contracts and add API/domain tests as every new read or action contract is introduced.

- [ ] Plan 7: Migration, accessibility, performance, and consistency closeout.
  - Migrate one route family at a time behind clear ownership, delete superseded support/placeholder surfaces, and update the router/route registry only after the replacement works.
  - Validate keyboard, screen-reader, contrast, reduced-motion, dynamic type, touch target, mobile safe-area, and voice failure paths.
  - Run backend, API-contract, frontend type/build, visual/mobile smoke, and cross-route consistency checks; update product, technical, architecture, operating-model, and regression documentation.

## Child Plan Status

| Plan | Manifest | Depends On | Status | Completion Audit |
| --- | --- | --- | --- | --- |
| `.agents/vision-ia-presentation-contract-plan.md` | `.agents/feature-manifests/vision-ia-presentation-contract-manifest.yaml` | none | draft, not started | not run |
| `.agents/vision-shared-design-system-plan.md` | `.agents/feature-manifests/vision-shared-design-system-manifest.yaml` | Plan 1 | draft, not started | not run |
| `.agents/vision-canvas-declutter-plan.md` | `.agents/feature-manifests/vision-canvas-declutter-manifest.yaml` | Plans 1-2 | draft, not started | not run |
| `.agents/vision-mobile-watch-contract-plan.md` | `.agents/feature-manifests/vision-mobile-watch-contract-manifest.yaml` | Plans 1-3 | draft, not started | not run |
| `.agents/web-workspace-modernization-plan.md` | `.agents/feature-manifests/web-workspace-modernization-manifest.yaml` | Plans 1-2 | draft, not started | not run |
| `.agents/workspace-mobile-read-model-contract-plan.md` | `.agents/feature-manifests/workspace-mobile-read-model-contract-manifest.yaml` | Plans 1, 4, 5 | draft, not started | not run |
| `.agents/adaptive-surface-migration-closeout-plan.md` | `.agents/feature-manifests/adaptive-surface-migration-closeout-manifest.yaml` | Plans 1-6 | draft, not started | not run |

Child plan paths belong only in `Plan inventory` and `Plan Breakdown`. The table tracks delivery status but does not add implicit child dependencies.

## Priority Journeys And Acceptance Criteria

| Journey | Vision role | Workspace role | Phone/watch success condition |
| --- | --- | --- | --- |
| Find work | Interpret natural request, clarify constraints, rank and compare | Persisted browse/filter list and known quest entry | Phone shows top options and next action; watch can notify and hand off, not browse full results |
| My work | Explain or act on a task from intent | Scan owned quests, applications, attention items | One tap to the next item; watch exposes only urgent action/summary |
| Profile edit | Guided semantic edit with typed review | Stable identity/settings review and direct edit entry | Short edits are one-flow; sensitive changes retain explicit review |
| Chat | Resolve unknown person or plan outreach | Inbox and known thread ownership | Phone continues conversation; watch supports notification reply/hand-off |
| Business owner | Guided planning or semantic help | Profile, offers, booking queue, availability, calendar | Phone performs daily ops; watch surfaces booking triage only |
| External appointment booking | Explain and resolve ambiguity | Public business page, offering selection, availability, booking confirmation | Guest booking is direct, accessible, and never depends on private owner-shell state |

## Cross-Plan Constraints

- Backend decides meaning. Frontend and future native clients render backend-prepared state.
- One primary next step per Vision turn remains non-negotiable.
- No duplicate task state: each fact has one visual home at a time. A contextual disclosure may summarize, but must not mirror an entire preview sheet.
- User-facing copy must state the immediate value, status, or action. Architecture explanations, route ownership commentary, raw provider status, internal labels, and debug state stay out of production UI.
- No permanent sidebars or split panes in Vision. Web detail panes are allowed only for a comparison or management decision with a demonstrated need.
- Voice is first-class but never voice-only; every voice state has concise visual confirmation and an accessible text equivalent.
- Watch uses the same backend conversation/action contract but exposes only safe, short, explicitly supported actions.
- New frontend behavior must use generated contracts. No direct client reconstruction of permissions, validation, target matching, or workflow transitions.
- Every new domain workflow or read model updates `docs/business-logic.md`, `docs/domain-technical.md`, affected agent-operating source sections, regression scenarios, tests, and contracts in the same implementation slice.

## Evidence And Validation Strategy

- Discovery evidence: state inventory for all Vision modes, route inventory, copy inventory, API/read-model inventory, and a before/after information-hierarchy audit for priority journeys.
- Backend/API evidence: focused JUnit tests, domain use-case tests, semantic route/contract tests, generated frontend contracts, and endpoint callsite checks.
- Frontend evidence: `npm run type-check`, `npm run build`, route-level visual smoke on desktop and narrow mobile viewport, plus keyboard/reduced-motion/accessibility checks.
- Mobile/watch readiness evidence: contract fixtures for every device-role/attention-state combination and explicit native handoff/deep-link tests where applicable.
- Program evidence: `make audit-frontend-route-surfaces`, `make audit-frontend-state-logic-duplication`, `make audit-frontend-usage-graph`, architecture/documentation drift audits, and a regenerated context check after the current `codex-context` registry defect is fixed.

## Risks And Mitigations

- Risk: removing the rail removes needed user orientation. Mitigation: measure each rail datum, retain only user-relevant state in a compact conditional disclosure, and keep diagnostics in a developer-only tool.
- Risk: “minimal” becomes an empty or opaque interface. Mitigation: preserve clear state, visible current action, inline transcript confirmation, and explicit contextual entry points.
- Risk: generic shared components flatten meaningful module differences. Mitigation: standardize primitives and state behavior, not every content layout; use the smallest matching archetype per task.
- Risk: native-readiness creates speculative API work. Mitigation: add DTO fields only when a defined phone/watch/web renderer cannot faithfully express the backend-owned state.
- Risk: workspace modernization duplicates Vision workflows. Mitigation: use the route-ownership matrix and typed handoffs; browse/known-detail stays in a workspace, semantic/guided work stays in Vision.
- Risk: local context tooling can drift when a documented operator surface loses its generator or Make target. Mitigation: `control-start` is restored as a non-recursive compact snapshot and the context gateway now validates it through its normal provider path.

## Cross-Plan Consistency Review

- Shared assumptions: a calm product is not a textless product; it has a strict information hierarchy. Phone and watch reduce density, while web adds only decision-relevant context. The backend remains the only source of workflow truth.
- Overlaps to prevent: Plan 3 may not invent shared tokens that belong in Plan 2. Plan 5 may not add endpoint-specific derived logic that belongs in Plan 6. Plan 4 may not fork presentation semantics from Vision or web.
- Simplification test for every slice: if an element does not help the user understand where they are, what happened, or what to do next at this moment, remove it or hide it behind explicit reveal.
- Deferred decisions: final visual identity details, native framework choice, and any new module beyond the listed priority journeys remain separate decisions after the first shared system and canvas pass prove themselves.

## Closeout Gates

- Required closeout checks: all plan files complete, implementation evidence recorded, validation evidence recorded, docs synced, generated contracts refreshed where needed, superseded UI removed, and final consistency review complete.
- Final response evidence: route and state inventory, validated API/read-model contracts, priority-journey screenshots or smoke evidence at phone and web density, test/build outputs, documentation delta, and explicit residual risks.
- Backlog follow-up rule: keep this master plan open until every plan is complete. Record any newly discovered deferred product work with a stable ID in `docs/implementation-backlog.md` before a slice closes.

## Completion Evidence

- Status: draft
- Baseline ref: `0cf598d`
- Child plan audit summary: seven child plans exist in draft state; none has started implementation or run a completion audit.
- Changed files: this master plan and seven linked draft child plans under `.agents/`.
- Validation evidence: source review plus focused frontend route, state-logic, usage-graph, and API-contract audits; `make codex-context` completed with zero provider failures.
- Doc delta summary: decomposed the program direction into bounded child plans only; no product behavior, API contract, or domain rule changed.
- Backlog update: none. This plan is the active program record; detailed deferred items will be created only when a plan slice is expanded.
- Residual risk: the master plan must be reviewed and decomposed into executable plan files before implementation. Current UI assessment is code/documentation-based and still needs visual validation against a running authenticated app.

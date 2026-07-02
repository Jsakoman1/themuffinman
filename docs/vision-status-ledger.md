# Vision Status Ledger

This file is the compact continuity ledger for `/vision`.

It should stay short, factual, and updated when a vision batch materially changes current capability or intentionally defers a capability.

## Done

- persisted backend conversation state is the default interaction model for `/vision`
- first executor scope is `create_quest`
- text turns can drive stepwise slot collection and review assembly
- voice-related architecture direction is OpenAI-based, while the conversation contract remains shared with text turns
- create-quest review can retarget one field at a time instead of forcing full-form re-entry
- review-confirmation execution now flows through a dedicated `VisionExecutionService` boundary instead of calling the quest adapter directly from conversation orchestration
- reset and cancel lifecycle actions exist
- recent conversation recovery and load endpoints exist
- custom location handling supports parsed addresses, lookup-backed candidates, and explicit candidate confirmation
- schedule parsing now covers ISO and European date-time input plus a broader deterministic HR/EN spoken set, including tomorrow/day-after-tomorrow, next weekdays, noon/midnight, hour-only day-period phrases, and half-past phrasing
- schedule parsing supports basic typed dates plus common spoken relative phrases
- the modern vision frontend surface is split into a route shell, animated agent component, prompt dock, and backend-driven canvas renderer
- review-ready quest corrections now use typed backend review-edit actions with explicit review targets instead of depending on frontend-generated natural-language edit prompts
- review-ready backend turns no longer reinterpret free-text phrases like "change reward" as slot-edit commands; review edits must come through typed review actions
- multi-candidate custom location clarification now exposes ranked candidate wording and an explicit keep-typed-location fallback so the user can understand why candidate choices differ
- recent `/vision` task summaries now expose stage and progress metadata so the surface can distinguish resumable clarifications, review-ready tasks, blocked tasks, and completed tasks without frontend inference
- recent `/vision` tasks are now grouped into active, review-ready, blocked, and completed sections, with stale markers and disabled resume for completed work
- the frontend canvas renderer now delegates review, field-request, result-summary, and shared status framing to focused block components instead of growing one monolithic renderer
- the frontend vision shell now wraps state summary, execution candidate, canvas blocks, and prompt dock in one unified adaptive surface instead of exposing separate top-level panels
- the `/vision` route shell now behaves more like a blank canvas by hiding state and recent-task context behind one secondary reveal control instead of keeping header chrome and task lists always open
- the route shell now auto-reveals state context for review, blocked, and complete modes while staying quiet in routine clarification mode
- the route shell now pulls surface-state calculations into a reusable composable so the view stays thinner and closer to a true shell
- `VisionConversationSummaryDTO` now acts as the compact long-session memory source for resume, recent-task grouping, stage/progress labels, stale marking, and pending-slot context instead of forcing the frontend to rebuild state from raw turn text
- the animated agent surface now uses layered drift, halo, and spark motion to read as a living presence instead of a flat indicator
- the blank `/vision` state now uses the inline prompt surface as the primary entry point instead of reserving a separate idle hero or launcher step
- the blank-canvas hero heading now steps aside once the backend has real response content so the canvas can dominate active states
- active `/vision` states now promote a mode-specific hero signal for review, discovery, blocked, complete, and clarification turns instead of reusing one generic blank-canvas heading
- the compact context rail stays hidden in a fully idle empty state and only appears when there is real context, a response, or an active interaction
- the revealed context region now behaves like a compact inline state rail with lighter continuation cards instead of reading as a second panel inside the shell
- the context reveal affordance now lives inside the shell as a small utility chip instead of a separate top-level control row
- the blank canvas now shifts subtle background tone by response mode or voice state so review, blocked, complete, listening, and processing feel distinct without adding new chrome
- the route shell, prompt dock, canvas panel, and agent orb now share a centralized surface token set so blank-canvas tone shifts stay coordinated instead of being tuned per component
- `VISION-BLANK-CANVAS-001` has been completed as the route-shell, prompt-dock, canvas, and orb blank-canvas pass
- prompt understanding now carries a generic semantic plan with candidate intent, confidence, capability id, and planning note above create_quest-specific slot extraction
- new conversations can route intent from semantic planning metadata before falling back to deterministic prompt heuristics
- existing conversations now pass conversation context into prompt understanding so requested-slot fallback focus is available in the real turn flow
- create_quest review turns now surface a read-only execution candidate that describes readiness, blockers, and the next required field without introducing a new mutation path
- `/vision` now also supports a read-only `DISCOVER_QUESTS` capability that can switch into ranked quest recommendations inside the same adaptive surface
- intent switching now allows a prompt to start a new conversation when the active thread's intent no longer matches the user's current task
- shared prompt semantics now power both Vision and Admin Playground normalization/classification without collapsing the authority boundary
- read-only execution planning and the canvas execution candidate surface now exist above the create_quest review flow
- the durable vision memory layer now has its own context gateway, decision record, failure memory, feature-slice checklist, generated-artifact policy, and status ledger
- `OPEN_CHAT` is now a first-class routed vision capability and can open a chat with an explicit circle contact through the existing chat boundary
- quest and application detail entry points now have Vision-native routes, with legacy detail paths redirected into the Vision surface
- profile, settings, circles, and chat entry points now resolve through Vision-native routes, and the old legacy view files have been removed from the route graph
- profile and settings are now direct Vision route surfaces instead of nested profile dialogs, keeping identity and location editing inside the same route-level mental model as the rest of Vision
- the main Vision surface now exposes Vision-native routes for profile, circles, applications, and chat, and the current user's applications now have a Vision-native list route
- the main Vision surface now keeps capability entry points inline inside the terminal feed instead of rendering a separate launchpad panel, and the preview model is being tuned to the `create_quest` slot-by-slot reveal pattern
- Vision detail routes are being collapsed toward a terminal-first shell so circles, applications, chat, and profile surfaces stop reading like legacy popup pages
- circles, applications, and profile are now rendered as linear text feeds with inline actions instead of dashboard cards and sidebars
- chat, application detail, settings, onboarding, quest composer, and quest detail edit now also use the same terminal/feed presentation instead of the old dialog-and-panel shell
- the shared Vision feed styling now standardizes line-by-line blocks, inline actions, and textual summaries across the remaining Vision detail surfaces
- Vision now has a profile onboarding route for completing identity, avatar, and location setup before falling back to the fuller settings surface
- profiles without a saved location auto-focus the onboarding location section so the first-time setup path opens on the missing piece
- the legacy admin login handoff now redirects into Vision instead of pointing at a removed frontend route
- the legacy admin frontend shell and its route-level entry pages have been removed from the active frontend tree
- the `/vision/conversations/turns` request now carries a versioned client contract with input type, text, client capabilities, and client state version fields instead of relying only on legacy prompt semantics

## In Progress

- create-quest conversation hardening is still expanding around ambiguity wording, richer review editing, and executor confidence boundaries
- additional capability expansion is being prepared behind the shared semantic boundary so it does not depend on raw string intent checks
- open-chat resolution still needs broader prompt variants and a richer target-user disambiguation path before it feels as calm as create_quest

## Deferred

- additional mutation executors beyond `create_quest`
- stale-task cleanup beyond passive marking and broader multi-task continuity rules
- typed slot-edit intent models beyond the current create-quest review loop
- stronger locale-aware schedule parsing and broader location normalization
- a full multi-capability execution service

## Blocked By Design

- broad "agent can do everything" execution is intentionally blocked until each capability has a typed adapter, explicit slot contract, permission boundary, review path, and deterministic execution gate
- frontend-only workflow inference is intentionally blocked because `/vision` is backend-governed by design

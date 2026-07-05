# Vision Status Ledger

This file is the compact continuity ledger for `/vision`.

It should stay short, factual, and updated when a vision batch materially changes current capability or intentionally defers a capability.

## Done

- persisted backend conversation state is the default interaction model for `/vision`
- first executor scopes are `create_quest` and `create_circle`
- text turns can drive stepwise slot collection and review assembly
- voice-related architecture direction is OpenAI-based, while the conversation contract remains shared with text turns
- create-quest review can retarget one field at a time instead of forcing full-form re-entry
- review-confirmation execution now flows through a dedicated `VisionExecutionService` boundary that dispatches typed capability adapters for `create_quest` and `create_circle` instead of calling a single quest adapter directly from conversation orchestration
- reset and cancel lifecycle actions exist
- recent conversation recovery and load endpoints exist
- custom location handling supports parsed addresses, place-prefixed labels, lookup-backed candidates, ordinal candidate choice, and explicit candidate confirmation
- custom location handling also now tolerates simple place-prefixed labels and ordinal candidate selection before review continues
- schedule parsing now covers ISO and European date-time input plus a broader deterministic HR/EN spoken set, including tomorrow/day-after-tomorrow, next weekdays, noon/midnight, hour-only day-period phrases, and half-past phrasing
- schedule parsing now also covers basic Croatian relative phrases like `sutra`, `prekosutra`, `sljedeći tjedan`, and weekday names such as `ovaj petak`, plus Croatian day-period phrasing like `u 7 navečer` and `u 7 ujutro`
- schedule parsing now also covers relative offsets like `in two weeks`, `za dva tjedna`, `week after next`, and weekend phrases such as `next weekend`
- semantic sanitization now drops prompt-like location labels and keeps short place-like labels, so a full create-quest command no longer leaks into the location preview
- schedule parsing now checks explicit am/pm phrasing before hour-only fallback, so combined weekday-and-time turns stay anchored to the correct evening or morning hour
- location parsing now understands simple postal-code-plus-locality and locality-plus-postal-code fragments, which keeps short Swiss-style location input structured
- schedule parsing now also recognizes explicit evening phrasing such as `in the evening` when an hour is already present
- schedule parsing now also resolves plain weekday references such as `this Friday` into the next matching calendar day
- the local emergency fallback is English-only and fail-closes on non-English prompts
- schedule parsing supports basic typed dates plus common spoken relative phrases
- create-quest fallback parsing now keeps description as a core task summary and strips reward, schedule, and location noise before the value reaches the preview
- create-quest review now stays in clarification mode when the semantic intent confidence is still too weak, even after the visible slots are otherwise complete, so noisy prompts do not jump straight into review
- the semantic audit matrix now explicitly covers the settings snapshot and chat workspace routes alongside the other non-quest read surfaces
- the modern vision frontend surface is split into a route shell, animated agent component, prompt dock, and backend-driven canvas renderer
- review-ready quest corrections now use typed backend review-edit actions with explicit review targets instead of depending on frontend-generated natural-language edit prompts
- review-ready backend turns no longer reinterpret free-text phrases like "change reward" as slot-edit commands; review edits must come through typed review actions
- multi-candidate custom location clarification now exposes ranked candidate wording and an explicit keep-typed-location fallback so the user can understand why candidate choices differ
- recent `/vision` task summaries now expose stage and progress metadata so the surface can distinguish resumable clarifications, review-ready tasks, blocked tasks, and completed tasks without frontend inference
- recent `/vision` tasks are now grouped into active, review-ready, blocked, and completed sections, with stale markers and disabled resume for completed work
- the frontend canvas renderer now delegates review, field-request, result-summary, and shared status framing to focused block components instead of growing one monolithic renderer
- the frontend vision shell now wraps state summary, execution candidate, canvas blocks, and prompt dock in one unified adaptive surface instead of exposing separate top-level panels
- the shared Vision presentation maps for entity-family labels and preview field ordering now live in one frontend config module instead of being duplicated across the conversation composable and preview panel
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
- fallback `VisionPromptUnderstandingResult.empty()` turns now still carry replay metadata, so unsupported or placeholder paths keep a minimal audit trail
- family alias normalization now covers extra quest, circle, application, and user synonyms so target entity queries canonicalize more consistently before resolution
- mutating entity-resolution thresholds are now stricter, and some broad circle/application query fallbacks were removed so ambiguous prompts clarify sooner
- OpenAI and emergency fallback contract defaults are now centralized so missing semantic fields still resolve to the English backend contract shape before envelope assembly
- new conversations can route intent from semantic planning metadata before falling back to deterministic prompt heuristics
- existing conversations now pass conversation context into prompt understanding so requested-slot fallback focus is available in the real turn flow, but explicit entity-family switches no longer inherit the old slot as fallback focus
- create_quest review turns now surface a read-only execution candidate that describes readiness, blockers, and the next required field without introducing a new mutation path
- `/vision` now also supports a read-only `DISCOVER_QUESTS` capability that can switch into ranked quest recommendations inside the same adaptive surface
- intent switching now allows a prompt to start a new conversation when the active thread's intent no longer matches the user's current task
- shared prompt semantics now power both Vision and Admin Playground normalization/classification without collapsing the authority boundary
- read-only execution planning and the canvas execution candidate surface now exist above the create_quest review flow
- the durable vision memory layer now has its own context gateway, decision record, failure memory, feature-slice checklist, generated-artifact policy, and status ledger
- `OPEN_CHAT` is now a first-class routed vision capability and can open a chat with an explicit circle contact through the existing chat boundary
- quest and application detail entry points now have Vision-native routes, with legacy detail paths redirected into the Vision surface
- `VIEW_QUEST_NEWS` is now a routed read-only Vision capability, with a dedicated quest-news preview and semantic route catalog entry for the authenticated user
- `VIEW_NOTIFICATIONS` is now a routed read-only Vision capability, backed by the quest-news inbox read model and a dedicated notifications preview
- profile and settings are now direct Vision route surfaces instead of nested profile dialogs, keeping identity and location editing inside the same route-level mental model as the rest of Vision
- the main Vision surface now exposes Vision-native routes for profile, circles, applications, and chat, and the current user's applications now have a Vision-native list route
- the main Vision surface now keeps capability entry points inline inside the terminal feed instead of rendering a separate launchpad panel, and the preview model is being tuned to the `create_quest` slot-by-slot reveal pattern
- Vision detail routes are being collapsed toward a terminal-first shell so circles, applications, chat, and profile surfaces stop reading like legacy popup pages
- circles, applications, and profile are now rendered as linear text feeds with inline actions instead of dashboard cards and sidebars
- chat, application detail, settings, onboarding, quest composer, and quest detail edit now also use the same terminal/feed presentation instead of the old dialog-and-panel shell
- the shared Vision feed styling now standardizes line-by-line blocks, inline actions, and textual summaries across the remaining Vision detail surfaces
- login and registration now use the same minimal route-level shell approach as the Vision transition, and the leftover shared brand/component stylesheet layer has been reduced to a small global reset plus Vision color tokens
- the floating Vision preview is no longer quest-hardcoded; it now renders backend-provided capability preview items so profile, circles, applications, chat, and detail snapshots can reuse the same changing-model surface
- profile, circles, and applications now behave as same-thread workspace families, so switching from read-only snapshot prompts into related mutation prompts reuses the current Vision conversation instead of forcing a new thread
- Vision now has a profile onboarding route for completing identity, avatar, and location setup before falling back to the fuller settings surface
- profiles without a saved location auto-focus the onboarding location section so the first-time setup path opens on the missing piece
- the `/vision/conversations/turns` request now carries a versioned client contract with input type, text, client capabilities, and client state version fields instead of relying only on legacy prompt semantics
- OpenAI prompt understanding now receives a typed Vision semantic orchestration request that includes raw prompt, conversation context, user locale/location/timezone hints, backend-published allowed routes, and response contract metadata
- the Vision semantic orchestration request now also carries backend-owned memory context split into user memory, session memory, and recent turn snapshots so multi-topic conversations can keep stable preferences separate from the current thread
- the memory context now also carries recent entity families and the current session family so vague follow-up turns can stay anchored to the active domain unless the user clearly switches topics
- low-confidence ambiguous follow-up turns now stay inside the current thread unless the prompt clearly signals a different entity family
- the turn response now exposes a compact memory trail with the active family and recent family/intent context for debug and adaptive preview use, plus a visible topic-switch hint when the active family changes
- recent conversation summaries now also carry entity-family and topic-switch hints so the resume rail can show conversation continuity across entity changes
- the turn response memory trail now also includes a hidden sheet-friendly session summary, open-question list, and recent-action list so the frontend can show durable context without dumping raw debug text
- the Vision learning loop now also exposes compact explainability records for preference ranking, intent selection, and slot-focus decisions so learned habits can be explained without raw replay
- the Vision memory pack now also derives an explicit retrieval focus from the recent topic rail when learned entity-family confidence is weak, so task-family and circle-family recall stay grounded in the latest durable context
- the first semantic route catalog publishes current Vision routes for `create_quest`, `discover_quests`, and `open_chat` so model routing is constrained by backend-owned capabilities
- the first user context pack derives available locale and timezone hints from profile location data, including `CH` to `de-CH` and `Europe/Zurich`
- backend sanitization now hard-rejects model-selected capabilities, focus slot ids, and extracted slot payloads that fall outside the published Vision route catalog and slot schema
- the semantic request now also carries runtime client locale/timezone hints and records which backend source supplied the final locale and timezone values
- `/vision` now has terminal-first read-only semantic routes for self profile, circles, and applications inside the same conversation surface
- `/vision` now also has terminal-first read-only semantic routes for settings, user profile detail, circle detail, quest detail, and application detail, and the remaining route-level detail entry points now redirect into the shared conversation surface instead of dedicated page shells
- `/vision/chat` now resolves through the shared conversation surface as a read-only chat workspace snapshot, while person-specific chat opening remains on the `open_chat` terminal flow
- the current session memory snapshot now persists on the conversation row so the backend can keep a durable session context file in step with the live turn history
- the persisted session snapshot now feeds back into the semantic memory pack on the next turn so the model can see the durable session rail directly, including structured summary, open-question, and recent-action fields
- the semantic understanding layer now fail-closes any OpenAI response that picks a capability id or focus slot outside the backend-published contract before routing and slot sanitization continue
- the backend semantic model remains the primary interpreter for `/vision` across quests, circles, applications, profiles, and chat, with the deterministic local parser reduced to an English-only emergency path for safe read surfaces and fail-closed mutation fallback
- turn and dashboard prompt responses now expose prompt-understanding provider/status metadata so degraded local emergency handling is visible instead of implicit
- the semantic prompt audit matrix now covers representative quest, circle, application, profile, chat, and detail prompts so drift shows up in tests before the route catalog widens again
- the semantic prompt audit matrix now also covers group-style circle creation, submit-application phrasing, profile editing, and direct-message phrasing so the model-first route selection stays explicit across the main entity families
- the semantic response validator now rejects extracted slots and generic semantic fields that are not exposed by the selected backend route before sanitization runs
- `/vision` now also has a first request-style mutation pilot for `create_circle`, including one-slot draft collection, review-ready state, and explicit confirmation before execution
- `/vision` now also has a broad cross-entity `search` capability that can rank quests, circles, users, applications, and things inside the shared canvas surface
- `/vision` now also has a read-only `VIEW_THINGS` capability that exposes available shared things as a first-class Vision snapshot
- `/vision` now also has a first application mutation pilot for `create_application`, including deterministic applyable-quest resolution, message collection, price collection only for paid quests, and explicit confirmation before execution
- `/vision` now also has narrow pending-application self-service mutation pilots for `update_application` and `withdraw_application`, including deterministic current-application resolution, textual previews of current and changed values, and explicit confirmation before execution
- `/vision` now also has a safe self-profile mutation pilot for `update_profile`, with username and profile-description draft collection, review-ready state, explicit confirmation, and a backend adapter that preserves existing email, avatar, and location state
- `/vision` now also has owner-side mutation pilots for `approve_application` and `decline_application`, with exact manageable-quest resolution, exact pending-applicant resolution, textual previews, and explicit confirmation before execution
- `/vision` now also has circle-request mutation pilots for `create_circle_request`, `accept_circle_request`, and `delete_circle_request`, with exact person or pending-request resolution, textual previews, and explicit confirmation before execution
- `/vision` now also has owned-circle mutation pilots for `update_circle` and `delete_circle`, with exact owned-circle resolution and explicit confirmation before execution
- `/vision` now also has a safe `update_profile_location` pilot, with location-mode and location-label draft collection plus a backend adapter that preserves unrelated profile and sharing state

## In Progress

- create-quest conversation hardening is still expanding around ambiguity wording, richer review editing, and executor confidence boundaries
- additional capability expansion is being prepared behind the shared semantic boundary so it does not depend on raw string intent checks
- open-chat resolution still needs broader prompt variants and a richer target-user disambiguation path before it feels as calm as create_quest
- the semantic orchestration layer still needs broader route catalog coverage and integration tests against non-create_quest capability expansion
- request-style and mutating capability expansion beyond `create_circle`, `create_circle_request`, `accept_circle_request`, `delete_circle_request`, `update_circle`, `delete_circle`, `create_application`, `update_application`, `withdraw_application`, `approve_application`, `decline_application`, `update_profile`, and `update_profile_location` still remains

## Deferred

- additional mutation executors beyond `create_quest` and `create_circle`
- stale-task cleanup beyond passive marking and broader multi-task continuity rules
- typed slot-edit intent models beyond the current create-quest review loop
- stronger locale-aware schedule parsing and broader location normalization

## Blocked By Design

- broad "agent can do everything" execution is intentionally blocked until each capability has a typed adapter, explicit slot contract, permission boundary, review path, and deterministic execution gate
- frontend-only workflow inference is intentionally blocked because `/vision` is backend-governed by design

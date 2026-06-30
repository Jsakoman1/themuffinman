# Vision Status Ledger

This file is the compact continuity ledger for `/vision`.

It should stay short, factual, and updated when a vision batch materially changes current capability or intentionally defers a capability.

## Done

- persisted backend conversation state is the default interaction model for `/vision`
- first executor scope is `create_quest`
- text turns can drive stepwise slot collection and review assembly
- voice-related architecture direction is OpenAI-based, while the conversation contract remains shared with text turns
- create-quest review can retarget one field at a time instead of forcing full-form re-entry
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
- the `/vision` route shell now behaves more like a blank canvas by hiding state and recent-task context behind one secondary reveal control instead of keeping header chrome and task lists always open
- the route shell now auto-reveals state context for review, blocked, and complete modes while staying quiet in routine clarification mode
- the animated agent surface now uses layered drift, halo, and spark motion to read as a living presence instead of a flat indicator
- the prompt dock now starts collapsed on the blank canvas and opens through a small launcher instead of occupying the default idle state
- the blank-canvas hero heading now steps aside once the backend has real response content so the canvas can dominate active states
- the compact context rail stays hidden in a fully idle empty state and only appears when there is real context, a response, or an active interaction
- the blank canvas now shifts subtle background tone by response mode or voice state so review, blocked, complete, listening, and processing feel distinct without adding new chrome
- the route shell, prompt dock, canvas panel, and agent orb now share a centralized surface token set so blank-canvas tone shifts stay coordinated instead of being tuned per component
- `VISION-BLANK-CANVAS-001` has been completed as the route-shell, prompt-dock, canvas, and orb blank-canvas pass

## In Progress

- create-quest conversation hardening is still expanding around ambiguity wording, richer review editing, and executor confidence boundaries
- vision memory and workflow standardization are being pushed into repo-persistent docs, checklists, and fixtures

## Deferred

- additional mutation executors beyond `create_quest`
- stale-task cleanup beyond passive marking and broader multi-task continuity rules
- typed slot-edit intent models beyond the current create-quest review loop
- stronger locale-aware schedule parsing and broader location normalization
- full replacement of legacy frontend surfaces with vision-only navigation

## Blocked By Design

- broad "agent can do everything" execution is intentionally blocked until each capability has a typed adapter, explicit slot contract, permission boundary, review path, and deterministic execution gate
- frontend-only workflow inference is intentionally blocked because `/vision` is backend-governed by design

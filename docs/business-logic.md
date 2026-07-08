# Business Logic

This document explains the product in user-facing terms. It is meant to stay aligned with the code and serve as a future FAQ source for humans and chatbots.

For stable cross-module terminology, start with `docs/cross-domain-glossary.md`.
For long-term product direction and interaction principles, see `docs/product-vision.md`.
For future `/vision` implementation patterns, see `docs/vision-architecture-patterns.md`.

## Scope

Current covered modules:
- identity and profiles
- work marketplace
- business hub
- thing sharing
- voluntary car sharing
- circles and relationships
- chat between circle contacts
- location-aware quest flows

## Product Interaction Direction

- The long-term interface direction is an adaptive Social Useful Network surface rather than a fixed collection of forms and menus.
- Voice should be supported with parallel visual feedback so users can confirm what was heard, what is happening, and what data is being discussed.
- Large result sets should be summarized and filtered by default, then expanded only when the user asks for more detail.
- Complex information should be shown visually when that is clearer than explaining it only through audio.
- The authenticated frontend now also includes an experimental `/vision` screen that demonstrates this direction with a centered animated agent and an inline prompt surface inside the same adaptive canvas.
- The `/vision` shell is intentionally terminal-first: the main conversation rail stays compact and left-aligned, while richer preview content is reserved for the right-side panel only when it adds real user value.
- `/vision` keeps descriptive helper text intentionally short in the active rails, so voice and text turns read like a concise command log instead of a second narrative UI.
- `/vision` uses short slot labels such as `Name`, `Details`, and `Place` in the active rail when the current flow line already carries the entity family, so the same screen stays readable across quests, circles, profiles, and applications.
- `/vision` now uses the same compact terminal row grammar in the flow debug rail and the intent preview rail, so the right side reads like one consistent log instead of two different UI patterns.
- `/vision` now also uses that same row grammar for main canvas discovery, summary, and choice output, so the entire screen reads like one terminal rather than a mix of cards and lines.
- `/vision` now uses backend-managed speech transcription, speech synthesis, and prompt decoding so typed text and voice input can feed the same backend processing path, while prompt understanding defaults to the low-cost `gpt-4o-mini` semantic model unless an explicit upgrade flag is enabled.
- `/vision` now carries separate user memory, session memory, and turn memory into semantic understanding so the system can keep stable preferences, current thread state, and recent turns apart when users switch topics mid-conversation.
- `/vision` now also tracks recent entity families in memory, so short or vague follow-up turns can stay anchored to the active topic unless the user clearly opens a different one.
- `/vision` now keeps weak ambiguous follow-up turns inside the active conversation thread instead of switching topics too eagerly, while still allowing clear new-topic requests to open a new thread when they are explicit enough.
- `/vision` now avoids reusing the previous requested slot as fallback focus when the new prompt clearly switches entity families, so fresh circles, applications, profile, or chat requests are not pinned back to an old quest slot.
- `/vision` now also gives direct chat prompts a short numbered list of candidate people when the name fragment is ambiguous, so the user can answer with an exact name, email, or a candidate number instead of hitting a generic failure.
- `/vision` now exposes a compact memory trail in its turn response so the frontend can show the active entity family, recent families, and the last remembered turn state when that context is useful.
- `/vision` now also exposes a topic-switch hint when the active entity family changes, so the preview can make a quest-to-circle or profile-to-chat transition visible instead of hiding it in debug text.
- `/vision` now also marks recent conversation summaries with their entity family and a short topic-switch hint, so the resume rail reads like one evolving conversation history instead of a list of unrelated tasks.
- `/vision` now also surfaces those recent conversation summaries as a clickable resume strip in the main shell, so the next task is visible before the user opens any detail panel.
- `/vision` now also exposes a compact hidden memory sheet with session summary, open questions, and recent actions so the frontend can stay calm while still showing the durable context when needed.
- `/vision` now also persists a compact session memory snapshot on the conversation row, so the backend can carry the current thread context forward even when the live turn object is not on screen.
- `/vision` now feeds that persisted session memory snapshot back into prompt understanding as the durable session rail for the next turn, with structured summary, open-question, and recent-action fields instead of one opaque blob.
- `/vision` now also captures persistent preference signals and feedback events from turns, so repeated corrections, input-mode habits, and recent topic patterns can influence future understanding without changing business rules.
- `/vision` now periodically compacts learned preference signals and feedback history into backend summary rows, so the resume rail and semantic prompts can rely on a short durable memory instead of raw turn replay.
- `/vision` now returns that learned memory summary on the turn response itself, so the frontend can show what the system has learned without reconstructing it from raw turn history.
- `/vision` now tracks per-user preference confidence with backend decay, so older habits naturally cool down unless the user keeps reinforcing them.
- `/vision` now uses learned input preference confidence to shape some clarification wording, so a user who consistently uses voice can get shorter spoken-friendly follow-up prompts.
- `/vision` now ranks learned preference signals by backend priority before exposing them to runtime memory and learning snapshots, so the most actionable habits surface first instead of being treated as a flat confidence list.
- `/vision` now also exposes compact explainability records for preference ranking, intent selection, and slot-focus decisions, so the UI can show why a habit or route won without replaying raw turn data.
- `/vision` now also derives an explicit retrieval focus from recent topic families when the learned entity-family confidence is too weak, so topic switching and memory recall stay grounded in the most recent durable rail.
- `/vision` now inserts a dedicated semantic understanding step between speech transcription and slot merging so one prompt can fill multiple explicit quest fields without dumping everything into description or location by default.
- `/vision` now asks the semantic layer to map entity phrases to the backend-published slot schema first, using route examples, slot aliases, slot anti-examples, route slot descriptions, and allowed values as the source of truth before any deterministic fallback parsing runs.
- `/vision` now resolves follow-up entity text through a shared route-slot priority plus semantic-plan fallbacks such as `targetUserQuery` and `searchQuery`, so the same draft can keep filling the intended target without a pile of family-specific parser branches.
- `/vision` now merges semantic slot values with confidence-aware overwrite rules, so high-confidence values can replace draft state, medium-confidence values only fill missing fields, and low-confidence values stay out of the draft.
- `/vision` now applies slot-specific normalization only at the final merge layer, so money, date, time, and location values are cleaned after semantic extraction instead of hardcoding punctuation rules into the parser.
- `/vision` now runs a focused repair pass for weak or partial slot extraction when the backend sees a clear missing field, so the system can ask for one more targeted semantic extraction instead of treating every weak answer as final.
- `/vision` now records repair and correction signals in learning memory, so later turns can see which slots were repaired, which feedback type happened, and which habits were reinforced by user corrections.
- `/vision` now treats the backend semantic model as the primary interpreter for prompt understanding across quests, circles, applications, profiles, and chat, while the deterministic local parser is reduced to an English-only emergency path for safe read surfaces and fail-closes mutation-style prompts when OpenAI understanding is unavailable.
- `/vision` now resolves target entities through backend resolver families and confidence thresholds, so a prompt like a circle request can keep its action domain in one family while still resolving the actual target person, quest, circle, or application separately before clarification or DTO mapping continues.
- `/vision` turn and dashboard prompt responses now expose the prompt-understanding provider and understanding status, so the frontend can tell whether a turn came from primary OpenAI understanding, OpenAI-plus-local rescue, or degraded local emergency handling.
- `/vision` now fail-closes any OpenAI semantic response that selects a capability or focus slot outside the backend-published response contract before routing continues.
- `/vision` now rejects any OpenAI semantic response that tries to extract slots or generic semantic fields not exposed by the selected backend route, so model output stays aligned with the actual route contract before sanitization.
- `/vision` now keeps create-quest descriptions as a core task summary and strips reward, schedule, and location noise from the fallback description so the preview stays readable.
- `/vision` now also supports a read-only quest discovery path, so browse/search prompts can return ranked open quests inside the same adaptive surface without entering the create-quest execution flow.
- `/vision` can now also open a chat with an existing circle contact when the user explicitly names the target, using the same adaptive surface instead of a separate chat launcher.
- `/vision` can now also show the current user's profile, circles, and applications as read-only terminal snapshots inside the same main conversation surface instead of requiring separate route navigation.
- `/vision` can now also create a circle through the same conversation flow by collecting the circle name, showing a short review, and requiring explicit confirmation before execution.
- An explicit create-circle prompt like "make new circle" now wins over the circles snapshot view, so the user can leave the read-only list and start a draft in one turn.
- The same explicit-action override also lets circle requests, application actions, profile updates, and direct chat requests escape from their matching read-only snapshots when the prompt is clearly a mutation.
- `/vision` can now also create a quest application through the same conversation flow by resolving one applyable quest, collecting the application message, collecting a proposed price only for paid quests, and requiring explicit confirmation before execution.
- `/vision` can now also update or withdraw one of the current user's pending quest applications through the same conversation flow by resolving one exact pending application first, then requiring explicit confirmation before saving changes or withdrawing it.
- `/vision` can now also update the current user's own profile username and profile description through the same conversation flow, while preserving the stored email, avatar, and location settings behind the backend mutation boundary.
- Voice recording and speech playback use backend-provided limits so long recordings, oversized audio uploads, and overly long speech synthesis text fail early instead of exhausting local memory.
- `/vision` now also has a dedicated persisted conversation backend foundation at `POST /vision/conversations/turns`, so the system can ask one missing field at a time and keep the same task state across turns.
- `/vision/conversations/turns` now uses a versioned request shape with `inputType`, `text`, `clientCapabilities`, and `clientStateVersion`, so the frontend can declare what it can do instead of relying only on legacy prompt fields.
- The active `/vision` surface should prefer that persisted conversation path for stepwise task guidance, treat `cancel`, `stop`, and `reset` as immediate conversation-control shortcuts that short-circuit to the lifecycle flow, and allow `Esc` as a local cancel shortcut in the composer while exposing recent task resume behavior and keeping the older dashboard prompt endpoint as a compatibility/planning path during transition.
- The active `/vision` surface should treat compact backend summaries as the memory source for resume and recent-task continuity, rather than reconstructing the current state from older transcript text.
- Recent `/vision` summaries may still show stale older tasks for continuity, but the backend no longer marks those stale tasks as resumable, so the surface should not offer one-click continuation for old drafts.
- When the user clearly switches from one task to a different task family, `/vision` now closes the abandoned non-completed thread as superseded instead of leaving the older draft looking active beside the new task.
- Fixed-time quest execution now also uses the persisted client timezone from the active conversation when it derives the final scheduled timestamp, so confirmation no longer silently reinterprets the reviewed local time through server timezone.
- The persisted `/vision` path now returns backend-prepared canvas blocks such as agent message, recognized input, field request, collected summary, review summary, and warnings, so the frontend can stay visually adaptive without deciding workflow rules itself.
- The first real `/vision` quest flow now collects title, description, reward, visibility, schedule choice, and location choice step by step instead of pushing the user into one big form.
- The supported `/vision` mutation drafts now show live form-like preview cards with the recognized fields and additional draft details so the user can review circles, circle requests, quests, applications, and profile changes before confirmation instead of seeing only the next slot prompt.
- Review-ready `/vision` profile, circle, and application mutation drafts can now also send the user back to one explicit field through the same typed review-edit path that already existed for quest review.
- That same typed review-edit path is now regression-covered across representative circle-request and application review surfaces as well, so backend-owned review correction is no longer only proven on quest and profile flows.
- Follow-up turns on `/vision` now keep the active slot and draft snapshot visible to the semantic layer, so the backend can continue filling the same draft instead of re-reading the whole prompt as a fresh task.
- If the create-quest semantic intent is still too weak after the visible slots are filled, `/vision` now asks for a clearer title or task before it moves into review, so noisy prompts do not look ready too early.
- The same `/vision` quest flow now understands a wider set of date and time phrases such as ISO date-time, European date-time, tomorrow, tonight, next week, next weekday, relative offsets like `in two weeks` and `za dva tjedna`, weekend phrases like `next weekend`, am/pm, noon, midnight, and a small spoken-time vocabulary, while collecting the day and time as separate conversation details when the user does not provide both at once.
- When the user gives a custom quest location on `/vision`, the backend may suggest one or more more precise matched places, and the surface must ask whether to use one of those resolved candidates, keep the typed location, or pick a candidate by number before the quest review continues.
- The custom location parser also accepts simple place-prefixed labels such as `address: ...`, `place: ...`, `location: ...`, `near ...`, and `at ...` before it resolves structured address fragments.
- `/vision` now rejects location labels that look like full prompts or task descriptions, while still accepting short place-like labels such as city names, square names, and address fragments.
- `/vision` now also understands simple postal-code-plus-locality and locality-plus-postal-code location fragments, so short Swiss-style place labels can stay structured without needing a full picker.
- `/vision` now parses explicit `am` / `pm` phrases before the generic hour-only fallback, so combined weekday-and-time input stays on the day/time split instead of drifting into the wrong hour.
- `/vision` now also understands explicit evening phrasing such as `in the evening` when the hour is already present.
- `/vision` now also understands a plain weekday reference such as `this Friday` without requiring the user to say `next Friday` first.
- The deterministic local emergency fallback is English-only and still fail-closes on non-English prompts, but it can now keep simple English create-quest, create-circle, create-application, and profile-update turns alive when OpenAI is unavailable instead of forcing them into unsupported state.
- The same `/vision` review state can now send the user back to one named field such as reward, schedule, or location, so corrections stay conversational instead of reopening a legacy edit surface.
- The `/vision` conversation turn API now accepts explicit backend actions for review-to-execution confirmation and review-field correction targets, while reset and cancel still have dedicated lifecycle endpoints and also accept the common shortcut phrases `cancel`, `stop`, and `reset` during an active conversation.
- When `app.vision.execution-enabled` is on, the same persisted `/vision` conversation may create the quest or circle after the review state and an explicit confirmation from the user.
- The long-term `/vision` goal is to keep the remaining authenticated runtime blank-canvas adaptive so it reveals fields, prompts, results, and confirmations only when the current task needs them.

## Identity

### What the module does

- Identity manages registration, login, user roles, profile basics, and the shared user record used across other modules.
- The same user profile also carries profile avatar, profile description, and location-sharing settings.
- Identity also provides richer read models for public profile viewing and admin user inspection.

### Main surfaces

- register
- login
- current-session user
- profile edit
- public profile view
- admin user detail
- admin user management

### Identity subdomains

- `authentication`: register, login, token issue, current-session lookup
- `user account management`: create, update, delete, list, uniqueness and validation rules
- `profile and profile view`: profile fields, profile statistics, relation-aware public profile view
- `admin user detail`: admin-only view with role, circles, contacts, and option sets
- `security integration`: JWT parsing, authenticated principal loading, role mapping

### Detailed subdomain notes

### Authentication

- Registration creates a normal user account, not an admin account.
- Login is based on email plus password.
- Email comparison is case-insensitive after normalization.
- The authenticated session payload exposes the current user's identity and profile basics.
- JWT tokens are issued on register and login, but not on the `/auth/me` refresh-style read.

### User account management

- Email is required, normalized, validated, and must be unique.
- Username is required and must stay within the configured length rules in code.
- Password is required for account creation and optional for normal profile edits.
- Admin edits can also change role and reset password.
- A user cannot delete their own account through the admin-style delete endpoint.
- The last remaining admin cannot be deleted.

### Profile and profile view

- A profile is more than identity basics; it also includes avatar, rich-text description, and location settings.
- Profile description is sanitized before being returned.
- Avatar input is normalized before save.
- Current-location updates are a distinct capability from free-text address edits and require trusted device coordinates or reverse lookup before save.
- Public profile view is relation-aware, so the same profile can expose different primary actions depending on who is viewing it.
- Profile view also includes open quest count, recent open quests, and separate employer or worker rating summaries.

### Admin user detail

- Admin detail is a management read model, not just the base user DTO.
- It combines user data with option lists, circle assignments, and contact data for the target user.
- Non-admin users cannot access this surface.

### Security integration

- Security uses JWT bearer tokens whose subject is the normalized user email.
- Role authorities are derived from the stored user role and default to `USER` when role is missing.
- Authentication loading resolves the full `AppUser` entity and uses it as the request principal in controllers.

## Location

### What the module does

- Location manages saved user location settings, quest location behavior, address lookup, and exact-location sharing rules.
- It supports both profile-level discoverability and quest-level location display.
- It also records provider usage so admin tools can inspect lookup activity and storage impact.

### Main surfaces

- profile location settings
- quest location selection
- address lookup
- reverse lookup from current coordinates
- nearby discovery support
- admin location debug status

### Location subdomains

- `user location settings`: saved profile location mode, radius, address metadata, exact-sharing rules
- `quest location behavior`: inherit or override profile location, visibility summaries, exact or approximate display
- `lookup and reverse lookup`: provider-backed address search and coordinate reverse search
- `exact visibility and nearby discovery support`: who can see exact addresses and who can appear in nearby flows
- `provider metrics and audit`: cached lookups, rate limiting, monthly usage counters, lookup event records

### Detailed subdomain notes

### User location settings

- A user can turn location off, share only approximate area, or enable exact location.
- Radius is normalized into an allowed range instead of trusting raw client input.
- Saving location while mode is on requires usable coordinates.
- Turning profile location off clears stored coordinates and address metadata.
- Exact location can be visible to nobody, everyone, selected circles, or selected people.

### Quest location behavior

- A quest can hide location, show approximate area, show exact location, or inherit the creator's profile behavior.
- A quest can either reuse the creator's profile location or store a custom quest-specific address.
- If custom quest location is used, the address must resolve to coordinates before save.
- If profile-based location is requested while the creator's profile location is off, the quest cannot expose profile location.
- Even when a quest stores exact address data, the viewer may still only see approximate location depending on the creator's sharing rules.

### Lookup and reverse lookup

- Address lookup is provider-backed but can be disabled by configuration.
- Short or empty search queries do not call the external provider.
- Reverse lookup resolves coordinates into a candidate address when provider integration is enabled.
- Repeated lookups are cached to reduce provider calls.

### Exact visibility and nearby discovery support

- Nearby discovery depends on saved user coordinates and a non-off location mode.
- Exact user location can only be shared with existing circle contacts when the selected-people mode is used.
- Circle-based exact sharing depends on circles owned by the location owner.
- Quest exact-location visibility is derived from the creator's location-sharing rules when the quest asks for exact display.
- Exact-location access is a backend policy decision that always allows the owner, then applies the configured scope.

### Provider metrics and audit

- The system records each provider lookup and reverse lookup as an event row.
- Admin status exposes request counts, cache hits, current-month provider usage, and database footprint context.
- Per-actor rate limits apply to both search and reverse lookup actions.

## Shared platform rules

### What this covers

- These are the cross-module rules that keep API behavior, input cleanup, auth wiring, and operational jobs consistent.
- They are not one business module on their own, but they shape how every module behaves.
- Shared backend concepts now define reusable actor identity, ownership, circle-visibility selection, and scheduling-window rules so modules do not copy these basics as the product grows.
- Lightweight backend domain events separate mutation workflows from cross-cutting side effects such as workmarket news notifications, while keeping delivery synchronous for current local behavior.

### API and validation contract

- Backend services raise consistent HTTP-style business errors such as bad request, forbidden, conflict, not found, and unauthorized.
- Validation errors are returned in a structured API error format with optional field-level details.
- Malformed JSON and invalid request parameter types are normalized into readable bad-request responses.
- Complex read surfaces use backend-named fetch profiles so dashboard, quest detail, application detail, and thing-sharing views receive complete DTO data without client-side reconstruction.

### Input cleanup and rich text rules

- Email input is normalized before identity operations.
- Search queries are trimmed and leading `@` prefixes are removed before search matching.
- Profile avatar values must be image data URLs and are rejected when too large.
- Rich text is sanitized rather than stored raw, and blocked tags like scripts or iframes are removed.
- Links and images inside allowed rich text are filtered through safe URL rules.

### Security and session rules

- The backend is stateless and uses bearer JWT authentication.
- Public API access is intentionally narrow: register, login, and websocket handshake entry are open, while the rest is authenticated or admin-only.
- CORS and websocket allowed origins are controlled from shared security configuration.

### Operational rules

- Retention jobs clean up old notifications and chat content on configured cron schedules.
- Location provider behavior, JWT lifetime, and seed users are driven by typed config objects rather than scattered string lookups.
- Optional bootstrap admin and seeded local users are environment-controlled, not always-on behavior.

## Internal Sandbox

### What it is for

- Internal sandbox flows exist for fast generation of test data and synthetic multi-actor scenarios.
- Internal sandbox flow is a separate capability from production-like voice flow.
- They are meant for developer or admin use, not for normal end users.
- They help populate dashboards, quests, applications, circles, reviews, and edge-case states without manual multi-account clicking.

### Safety boundary

- Sandbox flow must stay separate from real-life product flow.
- sandbox behavior must stay separate from production mutation semantics
- Production-like automation must only perform real allowed user actions.
- Sandbox flow may create synthetic users, synthetic applications, and synthetic lifecycle progress for testing.
- Sandbox flow must never be silently mixed into normal user-facing commands.
- Synthetic entities should stay clearly marked so they can be recognized as non-real data later.
- Sandbox-generation planning is kept in a separate backend package from production admin planning, and it still only contributes safe planning warnings and workflows rather than executing mutations.

### Example sandbox use

- Create 20 users with circles and quests.
- Create a quest plus synthetic applications from selected workers.
- Push a quest through approve, start, complete, and review states for UI and reporting tests.

## Admin Agent Playground

- Admins can use a lightweight agent playground to try prompts against a backend planning surface.
- The current playground is still planning-first, but it now also exposes one guarded direct execution capability for synthetic quest batch generation after explicit confirmation.
- The direct execution slice is admin-only, target-user-specific, and limited to synthetic quest creation for sandbox/operator workflows.
- It does not rely on the ChatGPT consumer app.
- Production admin prompt planning and sandbox-generation planning are structurally separate so synthetic test-data rules stay auditable.
- The playground now classifies the raw prompt directly on the local path and expects English-only operator input there; broader language support depends on a backend-managed provider.
- Local fallback translation has been removed from the admin path, so the admin planner no longer rewrites prompts before workflow classification.
- The planner now also returns structured resolution, clarification, and execution-readiness contracts instead of only free-form warnings.
- The semantic contract now also records required slots, missing slots, and replay metadata so clarification is deterministic and reproducible instead of inferred from noisy user text.
- The same planner response now also tells the admin UI whether the first guarded execution capability is available for the current prompt.
- Vision and Admin Playground now share one backend prompt-semantics boundary for normalization and intent classification, while still keeping user-scoped and admin-scoped authority separate.
- The same agent-safe pattern is now expected across quest updates, application self-service, circle-group management, outgoing request cancellation, and chat read actions.
- The same agent-safe pattern now also covers profile self-update, notification read actions, and admin-side application correction or deletion.
- The operating model now also explicitly covers common read surfaces such as dashboard, circle overview, quest detail, application detail, chat history, and admin debug status instead of leaving them implicit.
- The admin agent playground now stays deterministic and local-only for planning and translation; it no longer calls OpenAI for planning summaries.
- Admin playground prompt handling still translates and classifies prompts into backend planning language, but it does so through local deterministic rules rather than a live provider hop.
- The response should separate the free-form summary from deterministic planner output such as matched signals, unresolved inputs, warnings, and suggested workflows.
- The provider-written summary should stay bounded by the deterministic planner output instead of inventing new workflow ids or missing-input requirements.

## Chat

### What the module does

- Chat is the direct messaging layer between connected users.
- It is built on top of accepted social relationships, not on free platform-wide messaging.
- It provides conversation summaries, message history, presence, websocket updates, and retention cleanup.
- The `/chat` module route is now a standalone workspace for conversation, contact, unread, and online-contact overview.

### Main surfaces

- chat workspace
- open conversation action
- conversation message history
- send message
- mark conversation as read
- presence heartbeat
- websocket-driven workspace refresh

### Chat subdomains

- `conversation access and lifecycle`: who can open or access a conversation and how pairs are normalized
- `message flow`: sending text or image messages, read tracking, previews
- `workspace and contact read model`: conversations, contacts, circles, unread counts, and online counts
- `presence and realtime`: online window, websocket register/unregister, push updates
- `retention and cleanup`: image redaction, message deletion, preview refresh

### Detailed subdomain notes

### Conversation access and lifecycle

- A direct chat can only exist between users who are connected through the social circle relation model.
- Users cannot start a chat with themselves.
- Pending circle requests do not create chat eligibility.
- Conversation pairs are normalized so one pair of users maps to one conversation.
- Opening a conversation now also goes through a backend per-user rate limit.
- Each participant now keeps their own conversation state for archive and mute without changing the counterpart's view.
- Opening a conversation clears that participant's archive state and refreshes their last-opened marker.
- Losing circle eligibility also removes effective chat access to the conversation.
- Existing conversation history does not preserve chat access after the accepted relation is lost.

### Message flow

- A chat message can contain text, an image, or both.
- Message history is paginated instead of forcing the whole conversation timeline into one response.
- Send-message supports an optional client message id so safe client retries do not create duplicate messages.
- Image uploads are still carried in the current data URL payload, but the backend now enforces allowed image types and decoded byte limits.
- Sending a message updates the conversation summary fields immediately.
- Message delivery and seen receipts are now tracked per message instead of relying only on one conversation-level read cutoff.
- Open-conversation, send-message, read-tracking, and presence heartbeat now enforce backend rate limits.
- Delivery receipt updates are also rate-limited on the backend.
- Read tracking only applies to incoming unread messages for the current viewer.
- Read tracking can stop at a specific message id instead of always marking the whole thread.
- Delivery tracking can also stop at a specific message id, which lets mobile or web clients acknowledge partial history safely.
- Users can edit only their own non-deleted messages inside the configured edit window.
- Users can soft-delete only their own messages inside the configured delete window.
- Deleted messages no longer expose their original body or image payload through read DTOs.
- Image-only messages fall back to a generated preview label instead of raw empty text.

### Workspace and contact read model

- Workspace combines recent conversations, contact list, owned circles, unread conversation count, and online contact count.
- Workspace now excludes archived conversations by default and can explicitly include them when the caller asks for archived state.
- Contacts are derived from the current user's circle memberships plus existing conversation counterparts.
- Chat workspace only includes current accepted circle contacts.
- Contact and conversation read models now expose deterministic resolution hints for future agent-safe chat targeting.
- The workspace is already shaped for UI consumption and is not just a raw join of tables.
- The same workspace DTO backs the standalone `/chat` module route as well as the shared app-shell chat experience.

### Presence and realtime

- Presence is activity-window based, not permanent online state.
- The online window is configuration-backed instead of a hardcoded constant.
- Websocket connect and disconnect events update presence and notify connected circle contacts.
- Websocket auth accepts the bearer token from the standard authorization header, websocket subprotocol fallback, or legacy query-token fallback.
- Websocket clients can now send explicit delivered and seen receipt acknowledgements in addition to presence pings.
- Realtime events now carry richer chat payloads for message create, update, delete, and read transitions in addition to workspace invalidation.
- Chat now also stores durable websocket auth-failure, connect, disconnect, ping, invalid-payload, and rate-limit audit events for operational abuse review.

### Retention and cleanup

- Old chat images can be redacted before old messages are fully deleted.
- Cleanup also refreshes affected conversation previews so list views stay consistent after retention jobs.

## Social

### What the module does

- Social is the relationship and grouping layer for users.
- It handles connection invites, accepted circle relationships, blocking, owned circle groups, and organizing contacts into those groups.
- It also provides the discovery surfaces that later feed quest visibility, chat eligibility, and exact-location sharing.

### Main surfaces

- circles overview
- incoming and outgoing invites
- contact list
- circle group management
- search and invite candidates
- blocked users
- nearby people discovery
- admin circle overview

### Social subdomains

- `relationship lifecycle`: invite, accept, delete, block, unblock, and relation-state lookup
- `circle groups`: user-owned named groups
- `circle memberships`: assigning connected users into owned circles
- `contacts and overviews`: overview counters, contact list shaping, incoming/outgoing request lists
- `search and nearby discovery`: candidate search, blocked list, invite candidates, nearby users
- `presentation and action contracts`: relation labels, badges, primary actions, and response shaping

### Detailed subdomain notes

### Relationship lifecycle

- A relationship starts as no relation, then may become an outgoing or incoming request, an accepted connection, or a blocked relation.
- A user cannot invite themselves.
- A user cannot block themselves.
- If a blocked relation exists, new invites are rejected.
- Accepting an invite turns the request into an accepted connection instead of creating a separate entity.
- Unblocking removes the blocked relation record instead of mutating it back into a normal connection.

### Circle groups

- A circle group is owned by one user and named by that owner.
- Circle names are unique per owner, not globally.
- Circle groups are used to organize already connected people.
- Admins can inspect and delete circles at the platform level.

### Circle memberships

- Memberships connect a contact to one of the owner's circles.
- A user can only be organized into circles if the two users are already connected.
- Bulk update operations add or remove one owned circle across multiple connected people.
- Unassigned contacts are valid and visible as a first-class state.

### Contacts and overviews

- The overview summarizes connection count, unassigned connections, incoming requests, and outgoing requests.
- Contacts are shaped from accepted relations plus membership information owned by the current user.
- Request lists are split into incoming and outgoing views with different action labels.

### Search and nearby discovery

- General user search is relationship-aware and shows different actions depending on relation status.
- Future agent flows must stop on ambiguous recipient matches instead of guessing which person a partial name means.
- Invite candidates are simply users with no current relation.
- Blocked users are shown only when the current user initiated the block.
- Nearby discovery only works when the current user has a discoverable saved location.
- Nearby discovery excludes blocked relations and shows approximate location plus distance.

### Presentation and action contracts

- Social responses already include labels, badge classes, summary labels, and primary or secondary actions.
- The frontend is not expected to infer relation semantics from raw timestamps alone.
- Profile and search actions are relation-aware, including invite, accept, cancel, block, unblock, and open-circles behaviors.
- Search and relation read models now also carry deterministic resolution hints so future automation can identify exact targets without rebuilding labels client-side.
- Outgoing request rows and owned circle rows also carry deterministic resolution hints so future automation can cancel or delete exact targets without guessing.
- Admin and direct user read models now also carry deterministic resolution hints so future automation can target exact accounts without rebuilding labels client-side.

### Who can do what

- Any authenticated user can send an invite, accept an incoming invite, cancel or decline an accessible invite, block another user, and unblock a user they blocked.
- Only the recipient can accept an incoming invite.
- Only the user who created a block can remove that block.
- A user can only organize another person into circles after the two users are already connected.
- Admins get a broader overview and can delete circle groups at platform level.

### Cross-module role

- Social is a dependency domain for `workmarket`, `chat`, and parts of `location`.
- Quest visibility can depend on accepted circle relationships or selected circles.
- Chat eligibility depends on being connected through the social relation model.
- Exact location sharing to selected people or circles also depends on social data.

## Workmarket

### What the module does

- Workmarket is the task marketplace inside TheMuffinMan.
- Users create quests, other users apply, owners approve applicants, and the quest moves through its lifecycle.
- The module also provides dashboards, notifications, and application review surfaces.

### Main surfaces

- Quest list and search
- Quest detail
- Quest application views
- Dashboard
- Quest news / notifications
- User reviews after completion

### Quest lifecycle in plain language

- A quest starts as `OPEN`.
- When an owner approves applicants, the quest can move to `ASSIGNED` once all required worker spots are filled.
- A quest can then move to `IN_PROGRESS` when execution begins.
- After completion it moves to `COMPLETED`.
- An owner can reopen an assigned quest back to `OPEN` through the owner flow.
- Some time changes are not immediate and must be confirmed by the approved worker.

### Who can do what

- A quest owner can create, edit, delete, start, complete, and manage most quest settings.
- A quest owner can also approve or decline applicants.
- An admin can do the same and can also reassign creator ownership when editing.
- An applicant can create, edit, withdraw, and view their own application while it is still pending.
- An approved worker can confirm or reject certain quest term changes.
- Quest and application permissions are backend policy decisions, not frontend-only checks.

### Circles and requests

- Users can create named circles that they own.
- Users can send circle requests to other users, accept incoming circle requests, and cancel or decline pending requests.
- The terminal-first Vision surface can now send one exact circle request, accept one exact incoming circle request, or remove one exact pending circle request after review confirmation.
- Circle-request removal may represent declining an incoming request or cancelling an outgoing invite, depending on who owns the pending request.

### Quest visibility

- A quest can be visible to everyone or limited to circles.
- If circle-specific visibility is used, the quest is only visible through matching circle relationships.
- If no explicit visible circles are selected, visibility falls back to the creator's circle relationship with the viewer.
- Owners can choose whether approved applicants are shown publicly on the quest detail.

### Applications

- Users apply to open quests with a message and a proposed price.
- Only one application per user per quest is allowed.
- Owners and admins can approve or decline pending applications.
- The terminal-first Vision surface can now approve or decline one exact pending application by first resolving one manageable quest and then one exact applicant username before review confirmation.
- Owner-side application views now expose deterministic pending-selection metadata so future automation can know whether pending applications exist and which pending application is the oldest.
- Approved applications fill worker slots up to the quest assignee target.
- If all spots are filled, the quest becomes `ASSIGNED` and remaining pending applications are declined automatically.
- Owners and admins can only decline applications while the quest is still open and the application is still pending.
- Applicants can update or withdraw only while the application is still pending.
- The quest detail view now exposes the same pending applicant self-service actions, including withdraw, directly on the user's own application card.
- Pending-application self-service should resolve one exact current pending application before update or withdrawal.
- Notification item read should resolve one exact current news item before mutation, while mark-all-read stays scoped to the authenticated user's own feed.

### Dashboard

- The dashboard groups the user's own quests, available quests, applications, recent news, circles, and admin-only user lists.
- Dashboard navigation labels and tab descriptions are included in backend-prepared sections so clients render the screen contract instead of duplicating dashboard copy rules.
- Summary counters are used to show open quests, assigned quests, waiting confirmation quests, active work, and unread news.
- For admins, the dashboard also exposes broader user management context.

### News and notifications

- Quest news is the product's notification feed for quest and circle events.
- Typical events include application created, updated, approved, declined, quest started, quest completed, quest reopened, and term changes.
- News items can be marked as read one-by-one or in bulk.
- Unread count is used in the dashboard and synced to realtime listeners.

### Search and sorting

- Quest search supports text search, status filters, audience filters, date filtering, image filtering, scheduled-only filtering, location radius filtering, and sorting.
- The default sort is recommendation-based.
- Recommendation favors relevance, distance, timing, images, reward amount, and gives a slight boost to flexible timing over fixed timing.

### Completion and reviews

- Reviews exist as a separate layer after work has been completed.
- The review module is part of the workmarket domain and should be documented together with quest completion behavior.

### Workmarket subdomains

- `quest core`: creating, editing, showing, deleting, scheduling, and locating quests
- `applications`: applying, approving, declining, withdrawing, and exposing applicant data
- `execution and term negotiation`: start, complete, reopen, and confirm or reject time changes
- `dashboard and notifications`: summary counters, grouped views, planner items, recent news
- `reviews and ratings`: post-completion feedback between employer and approved worker
- `options and filters`: enum-like frontend options, search defaults, and visibility choices

### Detailed subdomain notes

### Quest core

- Quest creation requires title, description, award amount, and valid time/location input.
- By default a quest is circle-oriented unless the creator explicitly opens it to everyone.
- Owners can edit most quest fields.
- Future automation must resolve exactly one owned quest before owner-side actions such as approve, decline, or delete.
- The same read-before-write rule should be reused for owner-side quest update, owner-circle update or delete, outgoing circle-request cancellation, and chat read actions.
- The terminal-first Vision surface can now rename or delete one exact owned circle after review confirmation instead of relying on legacy circle dialogs.
- Deleting a quest is a destructive owner action and should require explicit confirmation after exact quest resolution.
- Admins can additionally change quest ownership and perform broader status updates.

### Profile location

- Users can keep profile location off, approximate, or exact.
- A terminal-first Vision profile-location update may change only the location mode and label while preserving the rest of the user's identity fields and existing visibility/radius settings.
- When the location label changes, stored resolved provider and coordinate data should be cleared so backend location resolution can rebuild it from the new label.
- Quest detail is not just raw data; it is assembled with viewer-specific actions and presentation fields.
- Quest list is available both as free search and as backend-defined presets like available work, my visible quests, and my active quests.
- Nearby search only works when the viewer has a usable saved location.
- Quest location can either reuse the creator profile location or use a custom quest-specific address.
- Exact quest address visibility is still limited by the creator's profile-sharing rules.
- Even when the same quest is loaded by two different users, the backend can return different actions and UI hints depending on who is viewing it.

### Applications

- Applications only exist for open quests.
- The quest creator cannot apply to their own quest.
- A user can only have one application per quest.
- Applicants send two meaningful pieces of input: a message and a proposed price.
- Pending applications are the only ones applicants can still edit or withdraw.
- Owners and admins only manage pending applications; they do not re-open declined or withdrawn applications through the normal flow.
- Approved applications are not just labels; they directly affect whether the quest still has open worker slots.
- When a quest already has approved workers, the application view intentionally separates approved workers from the rest of the applicant list.
- Owners can reveal or hide non-approved applications depending on quest state.
- Admins also get a separate search surface for applications across the whole system.
- Application detail is its own backend-assembled view with quest context and navigation hints, not just the raw application row.

### Execution and term negotiation

- Approval and execution are distinct concepts.
- A quest can have approved workers before actual work starts.
- Starting work requires an assigned quest.
- Completing work requires an in-progress quest.
- If the owner changes timing on an active quest, the system can move the quest into a waiting-confirmation state.
- The approved worker then confirms or rejects the proposed term change.
- Owners, admins, and approved workers can start or complete execution once the quest is in the correct status.
- Confirming a pending term change applies the queued time fields and returns the quest to its previous active status.
- Rejecting a pending term change discards the queued time fields and also returns the quest to its previous active status.
- Owners and approved workers can both participate in execution, but they do not get the same actions in every state.
- Reopening a quest is not just a status flip; it can also reactivate prior non-withdrawn applications.
- Admins can bypass some of the owner-only flow and apply status or term corrections directly.
- Quest detail exposes this subdomain through explicit execution and term-change sections so the frontend does not have to derive the flow itself.

### Example voice-assisted work flow

- A future voice assistant may help a user say: "Create a quest for tomorrow at 15:00 only for my selected circle friends and prepare it to start."
- `Tomorrow at 15:00` must be interpreted in the caller's timezone and converted into an absolute schedule before save.
- `Selected circle friends` must resolve to real accepted connections, not just loosely matching names.
- If some selected people are not yet confirmed connections, the safe behavior is to stop or branch into invite-and-wait flow, not to pretend the audience is already valid.
- If the user wants the quest prepared all the way to start, the system still needs real applications from the worker side and real owner-side approval before start is allowed.
- If unexpected applicants appear outside the intended selected worker set, the safe automation path is to leave them for manual review or decline them explicitly, never to approve them by mistake.
- If the owner later changes the time after assignment or while work is active, that change can pause into worker confirmation flow instead of applying immediately.
- After the quest is completed, the workflow can hand off into employer-worker review creation, but only for real approved participants of that quest.
- Workflow state-machine contracts for quests, applications, circle relations, chat access, thing borrowing, and future bookings are cataloged in `docs/workflow-state-machines.yaml`.

### Dashboard and notifications

- Dashboard is a product workspace view built from quests, applications, circle requests, and quest news.
- It separates visible owned work, active worker-side work, pending applications, waiting confirmation items, and recent notifications.
- Planner items are split between scheduled work and flexible work.
- Notifications are trimmed to recent items and also expose unread items.
- Dashboard summary is role-aware and includes admin-only context when the viewer is an admin.
- Anonymous dashboard requests do not fail; they return empty sections plus shared options.
- Notifications are navigable items, not just messages. They can point the user to a quest, an application, or the circles area.
- Circle request notifications can also expose direct accept/decline affordances in the response layer.

### Reviews and ratings

- Reviews are only available after quest completion.
- A worker can review the employer only if the worker was approved for that quest.
- An employer can review a worker only if that worker was approved for that quest.
- Users cannot review themselves.
- The same reviewer can update their existing review for the same quest-target pair.
- Reviews are tied to a concrete quest, not just to a user pair in the abstract.
- Ratings can later be summarized on a user profile separately for employer and worker roles.
- Comment text is optional after normalization; the meaningful required part of a review is the star rating plus a valid target.

### Options and filters

- The API exposes ready-to-use option lists for statuses, audiences, sort modes, location modes, and visibility settings.
- Search defaults include a default radius and whether nearby search should be enabled immediately for the current user.
- These options form part of the backend contract, not just frontend convenience data.
- Presentation data also comes from the backend: status labels, badge classes, helper text, action labels, and visibility booleans.
- The frontend is expected to render many workmarket states from server-prepared labels and flags instead of re-deriving business meaning on the client.

### Cross-module dependencies

- Workmarket depends on identity for the acting user, role checks, creator ownership, and user profile read models.
- Workmarket depends on social for circle-only quest visibility, selected-circle visibility, incoming circle request actions inside notifications, and profile navigation context.
- Workmarket depends on location for nearby search, quest location inheritance, custom quest address resolution, and exact-vs-approximate location display.
- Workmarket depends on shared platform rules for validation errors, rich-text sanitization, normalized search handling, and paginated list contracts.
- Workmarket also feeds other modules: user profiles reuse open-quest and review summaries, and chat or future assistants can rely on quest DTOs as the canonical task representation.

## Business Hub

### What the module does

- Business Hub lets an authenticated user publish one business profile tied to their account.
- Active business profiles appear in a directory and can be opened as a public business page.
- A business may also publish bookable offerings, availability rules, booking policy defaults, gallery images, and owner schedule reads.
- Booking workflow is backend-owned so the same contract can later support web, iPhone, and `/vision` clients without duplicating rules in the UI.

### Main surfaces

- Business directory
- Public business page
- Current user's business profile editor
- Current user's offering list and booking policy editor
- Current user's availability editor
- Public availability read and customer booking flow
- Owner booking list, owner booking actions, and owner dashboard summary
- Owner calendar projection for backend-prepared day-by-day schedule review

### Business profile rules

- Each account can own at most one business profile.
- Business name is required.
- Slug is unique across business profiles and may be auto-generated from the business name when left blank.
- Inactive profiles stay editable by their owner but do not appear in the public directory or public slug lookup.
- Business descriptions are sanitized before being returned.
- Booking can be enabled or disabled per business profile.
- Business timezone is required before owner availability or real bookings can be used.

### Business booking rules

- A business offering is the actual bookable service root, not the profile row itself.
- Offerings may be fixed-duration, customer-selected duration, or all-day offerings.
- Offerings may allow one booking per slot or shared-capacity booking with multiple parallel reservations.
- Availability is entered in the local business timezone through recurring rules plus exception rows.
- Booking persistence stores absolute `startsAt` and `endsAt` timestamps and still returns timezone context in DTOs.
- DST handling belongs to the backend availability and booking rules, not to frontend-only logic.
- `INSTANT` offerings create `CONFIRMED` bookings immediately.
- `REQUEST` offerings create `PENDING_CONFIRMATION` bookings that still reserve capacity immediately.
- Customer booking create supports idempotency keys so retries do not easily create duplicate reservations.
- Customer and owner booking lists start with backend pagination and filter contracts even before the UI needs the full surface.
- Booking DTOs return backend-prepared `allowedActions`, `statusLabel`, and optional `blockingReason`.
- Booking presentation and read paths stay side-effect-free; missing policy rows are created only by mutation flows.
- Customer cancellation depends on business policy and the configured cancellation window.
- Owner confirmation, rejection, cancellation, completion, and no-show marking are explicit backend transitions.
- Historical bookings keep offering title, price, duration, and timezone snapshots so later offering edits do not rewrite history.
- Offering removal is archival-safe deactivation rather than destructive history loss.
- Owner schedule data can also be projected into a calendar-shaped backend read model that groups bookings by the business's local day so owner dashboards, booking lists, and mobile clients do not derive their own schedule buckets.
- Gallery images are business-owned metadata rows and can be shown on the public page without changing booking workflow rules.
- Owner dashboard and owner booking list both rely on the same owner schedule summary read-model so "what is happening today" stays consistent.
- Future notification vocabulary is reserved around booking created, confirmed, rejected, cancelled, and reminder-style follow-up events.
- `/vision` consumes business capabilities such as `view_business` and `view_business_availability` from the same backend-owned business model, and future booking mutations should reuse the same business policy, availability, and snapshot rules.

## Thing Sharing

### What the module does

- Thing Sharing lets authenticated users list items they are willing to lend.
- Other users can browse available things and send one pending borrow request per thing.
- Owner-side approval, scheduling, handoff, and return tracking are not implemented in this first slice yet.

### Main surfaces

- Available things directory
- Current user's lending listings
- Borrow request action for available listings

### Thing sharing rules

- Listing title is required.
- Listing descriptions are sanitized before being returned.
- Owners cannot request to borrow their own things.
- Borrow requests require the listing to be currently available.
- A borrower can have only one pending request for the same thing.

## Voluntary Car Sharing

### What the module does

- Voluntary Car Sharing lets an authenticated user publish a ride offer with origin, destination, departure time, and seat count.
- Ride offers can be public to authenticated users or scoped to circles owned by the driver.
- Passenger reservation, approval, cancellation, and trip completion are not implemented in this first slice yet.

### Main surfaces

- Visible ride offer list
- Current user's ride offers
- Ride offer creation form

### Ride sharing rules

- Origin, destination, departure time, and seat count are required.
- Departure time must be in the future.
- Seat count must stay between 1 and 8.
- Circle-scoped visibility can only reference circles owned by the driver.
- Ride notes are sanitized before being returned.

## Frontend and API contract shape

### What the frontend treats as source of truth

- The frontend does not hand-maintain most DTO shapes; it imports generated contracts from the shared contract layer.
- Module API files are thin wrappers over backend endpoints, not a second business-logic layer.
- Shared axios and API-error helpers define how auth headers and backend error messages surface to the UI.
- The generated frontend contract is checked during the normal frontend build, so backend DTO or agent-model changes must refresh the contract before the build passes.

### Route and page model

- `/login` and `/register` are the only unauthenticated entry pages.
- `/vision` is the primary authenticated user surface.
- Vision-native quest and application detail routes carry the main detail flows.
- Vision-native profile, settings, circles, and chat routes carry the user-scoped continuation flows.
- Admin capability work is currently accessed through the Vision surface rather than a separate dedicated frontend route.
- Logged-in users land on `/vision`.
- Legacy route-era user entry routes such as `/work`, `/quests`, `/app-users`, `/business`, `/things`, and `/rides` are no longer active product entry points.

### API client model

- `authApi` owns login, register, and current-session lookup.
- `visionApi` is the primary frontend gateway and combines the conversation, quest, application, dashboard, news, user, circle, and location clients used by Vision routes.
- Each underlying client is endpoint-focused and mostly forwards server DTOs directly to the page or composable layer.
- The remaining backend `workmarket` domain stays available through the Vision-facing client layer.

### Error and session behavior

- Shared axios logic only auto-clears the session on authenticated `/auth/me` failure.
- Most other backend failures are surfaced back to the UI for local handling instead of forcing logout.
- The frontend prefers backend-supplied field or top-level error messages through `getApiErrorMessage`.

## Core Concepts

### User

- A user owns profile data, may create quests, may belong to circles, and may chat with people inside their circle graph.

### Circle

- A circle is a named group owned by a user.
- Circle names are unique per owner.
- Circles are used to control visibility, access, and chat eligibility.
- Users can be connected, requested, or blocked relative to each other.

### Quest

- A quest is a task or job that a user creates for other people to take on.
- The creator owns the quest.
- A quest can be public to everyone or limited to selected circles.
- A quest can have a schedule, a deadline, images, a reward amount, and a target number of assignees.
- A quest reward amount can also be `0`, which means the quest is free.
- A quest can also carry location data and visibility rules that affect who can see it.

### Chat

- Chat now supports direct chats, manual group chats, circle-wide rooms, quest threads, and application threads.
- Direct chat still requires an accepted circle relationship between the two users.
- Group chat can include more than two participants, and the backend keeps explicit membership and role data for each participant in the thread.
- Group chat participant roles are `OWNER`, `ADMIN`, and `MEMBER`.
- Group chat owners and admins can rename the thread and manage membership, but only the owner can transfer ownership.
- Removing or leaving a group chat never mutates circle room, quest thread, or application thread membership because those context-owned threads are synced from their parent record.
- When a group owner leaves, ownership transfers automatically to an existing admin first and otherwise to another remaining participant.
- Circle room chat belongs to one circle and includes the circle owner plus the current circle members.
- Quest thread chat belongs to one quest and includes the quest owner plus the currently approved applicants for that quest.
- Application thread chat belongs to one quest application and includes the quest owner plus that one applicant.
- Canonical context-owned threads can now also be read through dedicated non-mutating backend routes when the thread already exists, while the existing open endpoints still create the thread on demand.
- The backend now also exposes a filtered conversation-list surface so clients or agents can browse only direct chats, group chats, or one specific context-owned thread family without reconstructing that filter from the whole workspace payload.
- Conversation-list responses now expose page and has-more metadata so backend consumers can page large chat sets without loading the full workspace surface.
- Messages now support optional reply linkage, attachment metadata, and per-message emoji reactions.
- Chat attachments can now be pre-validated through a dedicated backend upload endpoint before message creation.
- Conversations now expose a dedicated sync surface so reconnecting clients can fetch message deltas and current typing participants after a websocket gap.
- Chat now emits lightweight typing events over websocket for already authorized conversation participants.
- Admin support tooling now exposes filtered chat audit events plus one conversation support view with recent messages and recent moderation history.
- Admin tooling can now also remove one chat message through the backend moderation surface.
- Every chat thread now carries backend context ownership metadata so the system can tell whether the thread belongs to a circle, quest, application, or to an ad hoc direct/group conversation.

## Current Quest Rules

- A quest title is required.
- A quest description is required and must contain meaningful rich text content.
- A quest reward amount is required and cannot be negative.
- A quest with reward amount `0` is treated as a free quest.
- A quest can contain up to 10 images.
- Quest images must be image data URLs.
- The assignee target defaults to `1` and cannot be less than `1`.
- Quest audience is either `CIRCLES` or `EVERYONE`.
- If the audience is not `CIRCLES`, circle visibility is cleared.
- If the term is fixed, create requires a `scheduledAt`.
- Custom quest location requires at least one location field.
- A quest using custom location must resolve to usable coordinates before save.
- Applications for paid quests must include a proposed price of at least `0.01`.
- Applications for free quests must not include a proposed price.

## Time and Term Rules

- If the term is fixed, a scheduled time is required.
- A scheduled start time cannot be in the past.
- An end time must be after the start time.
- When a quest is already active, time changes may require confirmation from the approved worker.
- If a quest is reopened, previously affected applications are reactivated into the pending state.
- Owners can only switch their quest between open and assigned in the dedicated owner flow.
- Approved workers can confirm or reject queued term changes.
- Voice or batch automation must not fake applicant-side actions, circle-request acceptance, or term confirmation without real authenticated actor context.

## Circle Relationship Rules

- Circle relationships can be:
  - none
  - circle
  - incoming request
  - outgoing request
  - blocked
- Chat and some visibility rules depend on the relationship status between users.
- Quest visibility may also fall back to the creator's circle relationship with the viewer.

## Current FAQ Draft

### What is a quest?

A quest is a task posted by one user so other users can apply, coordinate, and complete it.

### Why can some quests be circle-only?

Because the creator may want the task visible only to trusted people or selected groups.

### Why do some quests show as free and hide the application price?

Because a quest with reward amount `0` is treated as free, so applicants do not negotiate or submit a price for it.

### Why does chat require circles?

Because chat is currently restricted to users who are connected through the circle system.

### Why does a time change sometimes need confirmation?

Because an active quest can already have an approved worker, so changing the schedule can affect an existing commitment.

## Update Rule

- If a business rule changes in code, update this file in the same change.
- Add short FAQ entries when the same user question appears more than once.

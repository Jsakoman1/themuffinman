# Business Logic

## VisionForWeb assistant behavior

VisionForWeb is available from the authenticated shell and can open canonical module surfaces from text or voice input. OpenAI owns production semantic interpretation; a provider outage pauses Vision and preserves retryable conversation state. Local deterministic routing exists only for explicitly labeled development/test fixtures. Opening a surface never bypasses backend permission, mutation review, or confirmation rules.

Supported navigation families share one backend-published action contract: Work, Circles/People/Profile, Things, Rides, Business, Chat, Notifications, and Activity. Vision can open an entry surface from any authenticated shell context and can open a detail surface only after the backend resolves an authorized entity. A user without a business owner profile can still ask to find businesses; that request opens the public Business directory rather than incorrectly requiring the private dashboard.

This document explains the product in user-facing terms. It is meant to stay aligned with the code and serve as a future FAQ source for humans and chatbots.

## Authenticated Web completion rules

- Work has a dedicated Find Work surface and a direct Create new work action. Creating a work item requires the user to review whether its terms are fixed; the Web form exposes this as the `Fixed terms` choice instead of silently guessing.
- Worker assignment management exposes backend-owned allowed actions and typed stale-state conflict codes. If a quest, approved worker, or replacement application changed while the manager was acting, the client receives a recoverable code and refresh guidance instead of guessing whether the action succeeded.
- Chat supports inbox loading, direct chat, group chat, and message sending from the Web surface. A group chat must include at least two other accepted circle contacts.
- Chat realtime connections announce a structured `CONNECTED` state and whether a resync is required. The Web client displays the transport state and consumes workspace events through the `/ws/chat` endpoint; after reconnect, clients use the existing conversation sync boundary rather than assuming that missed events were delivered.
- Chat attachments can be refreshed through the backend after an external URL expires. The server re-checks conversation or upload ownership and returns `AVAILABLE`, `EXPIRED`, or `UNAVAILABLE` with safe metadata; clients never refresh a URL directly or bypass chat authorization.
- Before creating a group chat, clients can ask the backend whether the selected people are eligible. The response explains a typed reason such as `MINIMUM_PARTICIPANTS`, `SELF_INCLUDED`, `PARTICIPANT_NOT_FOUND`, or `CIRCLE_ACCESS_REQUIRED`, and supplies the next allowed action; group creation still rechecks eligibility authoritatively.
- Leaving a group returns a typed membership transition. It identifies the conversation, confirms `LEFT`, and reports the replacement owner when the leaving user was the owner, so every client can close or refresh the stale thread safely.
- An uploaded chat attachment has a backend-owned lifecycle. It starts `READY_TO_SEND`, can be explicitly `CANCELLED` before sending, becomes consumed exactly once when sent, and expires when its upload TTL ends. Removing an attachment from the Web composer also asks the backend to cancel its pending upload; local removal remains safe if the network is unavailable.
- Circles, People, and Chat are connected through accepted-contact consent. Sending an invite does not by itself authorize direct or group chat; the recipient must accept first.
- Calendar is a visual month, week, and day workspace. Business calendar data is optional for users without a business profile; available work events remain visible and the unavailable business portion is explained as recoverable.
- The authenticated Web workspace uses one persistent app-like module rail. Home, Work, Chat, Calendar, Business, Circles, Things, and Rides remain available as stable destinations; relevant child destinations and attention signals are prepared by the backend, while the browser controls only presentation and route highlighting.

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
- Vision quest creation exposes its workflow state and allowed next actions as structured data. While the draft is incomplete the user provides input or cancels; once review is ready the user can confirm, edit, or cancel; completed and failed states expose their own safe next actions. The Web does not infer these actions from assistant wording.
- Large result sets should be summarized and filtered by default, then expanded only when the user asks for more detail.
- Vision discovery returns a typed result state: `RESULTS`, `EMPTY_QUERY`, or `NO_MATCHES`. Empty states also provide a backend-owned recovery action (`ENTER_QUERY`, `REFINE_QUERY`, or `REMOVE_FILTER`) so the user gets a clear next step without the client guessing.
- Vision quest discovery also returns the current page, page size, and a page-aware `hasMore` value, so the Web client can offer “Show more” only when another slice exists and can recover from an empty or no-match request without looping.
- Vision discovery can be narrowed by an explicit entity family (`quest`, `circle`, `user`, `application`, or `thing`). The backend validates the filter, returns only authorized matches, and supplies the canonical detail route for each result so a spoken request can hand off to the correct detail surface.
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
- `/vision` and VisionForWeb use OpenAI as the only production interpreter for prompt understanding across quests, circles, applications, profiles, chat, and Web navigation. A deterministic local parser may be used only in an explicit development/test profile for synthetic fixtures and contract tests.
- `/vision` now resolves target entities through backend resolver families and confidence thresholds, so a prompt like a circle request can keep its action domain in one family while still resolving the actual target person, quest, circle, or application separately before clarification or DTO mapping continues.
- If OpenAI understanding is unavailable, Vision pauses input processing, preserves the current conversation and retryable input, and shows a recoverable provider-unavailable state. It does not silently translate, classify locally, or report the request as unsupported.
- `/vision` now fail-closes any OpenAI semantic response that selects a capability or focus slot outside the backend-published response contract before routing continues.
- `/vision` now rejects any OpenAI semantic response that tries to extract slots or generic semantic fields not exposed by the selected backend route, so model output stays aligned with the actual route contract before sanitization.
- `/vision` now keeps create-quest descriptions as a core task summary and strips reward, schedule, and location noise from the fallback description so the preview stays readable.
- `/vision` now also supports a read-only quest discovery path, so browse/search prompts can return ranked open quests inside the same adaptive surface without entering the create-quest execution flow.
- `/vision` must use the same backend semantic and resolver path for typed and voice discovery. A request such as “Show me the lawn-mowing job Frank posted yesterday” should resolve an authorized quest and return a typed handoff to its canonical Web Work detail route; ambiguous, inaccessible, or stale matches must ask for clarification or show recovery instead of guessing.
- `/vision` can now also open a chat with an existing circle contact when the user explicitly names the target, using the same adaptive surface instead of a separate chat launcher.
- `/vision` can now also show the current user's profile, circles, and applications as read-only terminal snapshots inside the same main conversation surface instead of requiring separate route navigation.
- `/vision` can now also create a circle through the same conversation flow by collecting the circle name, showing a short review, and requiring explicit confirmation before execution.
- An explicit create-circle prompt like "make new circle" now wins over the circles snapshot view, so the user can leave the read-only list and start a draft in one turn.
- Members can leave an accessible circle through a confirmed self-leave action; owners cannot leave their own circle because that would violate circle ownership invariants.
- Product notifications are delivered through the backend-owned notification fan-out and can be received in the Vision notification inbox through `VIEW_NOTIFICATIONS`; realtime push delivery and native-client delivery remain separate capabilities.
- When any protected Web API request returns `401`, the client clears the stale local session and returns to login; public login, registration, recovery, and reset requests keep their own validation errors instead of triggering a redirect loop.
- An authenticated owner or member can view an accessible circle detail through the shared circle read model. Non-members are not told whether a private circle exists, and the Vision surface exposes only the privacy-safe detail returned by the backend.
- The same explicit-action override also lets circle requests, application actions, profile updates, and direct chat requests escape from their matching read-only snapshots when the prompt is clearly a mutation.
- `/vision` can now also create a quest application through the same conversation flow by resolving one applyable quest, collecting the application message, collecting a proposed price only for paid quests, and requiring explicit confirmation before execution.
- `/vision` can now also update or withdraw one of the current user's pending quest applications through the same conversation flow by resolving one exact pending application first, then requiring explicit confirmation before saving changes or withdrawing it.
- `/vision` can now also update the current user's own profile username and profile description through the same conversation flow, while preserving the stored email, avatar, and location settings behind the backend mutation boundary.
- Voice recording and speech playback use backend-provided limits so long recordings, oversized audio uploads, and overly long speech synthesis text fail early instead of exhausting local memory.
- `/vision` now also has a dedicated persisted conversation backend foundation at `POST /vision/conversations/turns`, so the system can ask one missing field at a time and keep the same task state across turns.
- `/vision` now also replays a retried first submit by `clientRequestId` even before a browser has received a conversation id, so transient network failures do not duplicate the first action.
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
- Development/test fixtures may use deterministic English parser input, but production Vision never uses a local emergency fallback when OpenAI is unavailable.
- The same `/vision` review state can now send the user back to one named field such as reward, schedule, or location, so corrections stay conversational instead of reopening a legacy edit surface.
- The `/vision` conversation turn API now accepts explicit backend actions for review-to-execution confirmation and review-field correction targets, while reset and cancel still have dedicated lifecycle endpoints and also accept the common shortcut phrases `cancel`, `stop`, and `reset` during an active conversation.
- When a Vision mutation reaches review-ready state, the Web renderer exposes explicit `Confirm and create` and `Cancel` controls; a review message alone is never treated as execution evidence.
- When `app.vision.execution-enabled` is on, the same persisted `/vision` conversation may create the quest or circle after the review state and an explicit confirmation from the user. The local `dev` profile enables this guarded path by default for end-to-end development; the base/production default remains disabled unless explicitly configured.
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

### Activity recovery

- Activity is viewer-scoped and backend-prepared. Each item provides its source, route, timestamp, and primary action label; the Web client does not infer action meaning from item type.
- A resumable Vision item can be dismissed for the current user. Dismissal is idempotent and does not alter the underlying conversation or task.
- Resumable Chat activity follows the same viewer-scoped dismissal rule; dismissing a Chat suggestion hides that suggestion without changing the conversation or messages.
- Things borrow requests are also projected into activity for the authorized owner or borrower. Owner rows open My things, borrower rows open Things, and each role receives a distinct backend-owned resume key.
- Empty activity is a valid state, while load and dismiss failures remain visible and retryable. Invalid resume keys return a client error instead of an opaque server failure.

### Worker assignment recovery

- Only the quest owner or an administrator can release or replace an approved worker. The backend rechecks the quest lifecycle and application status at action time.
- Releasing an approved worker restores an open slot when the quest no longer meets its assignment target. Replacing a worker requires a different pending application for the same quest.
- If another action changed the quest or application first, the backend returns a conflict and the Web application refreshes the application list before the user tries again.

### Chat continuity

- Chat sync is authorized by the backend for every conversation and compares the server cursor with the client cursor. A changed or server-empty latest message state requires refresh; the client does not infer message freshness locally.
- Invalid conversation IDs or cursors return client errors. If membership or access changes and sync returns forbidden/not found, Web clears the stale thread, refreshes the inbox, and returns to the inbox when the conversation is no longer available.
- Group creation and membership changes remain circle/contact-authorized server actions. Leaving a group preserves the minimum-participant and owner-transfer rules; the Web surface refreshes its conversation list after the action.

### Chat media

- Attachments are uploaded through the backend flow, validated against configured MIME and size limits, and consumed once when attached to a message.
- Message reads return attachment availability metadata. If storage cannot resolve an attachment, the conversation remains readable and Web shows an unavailable-media explanation instead of a broken link.
- Attachment URLs may be short-lived. Current Web preserves the message metadata and safe unavailable state; external URL refresh and richer preview behavior remain open capability gaps.

### Identity subdomains

- `authentication`: register, login, token issue, current-session lookup, and account recovery
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
- Logout calls `POST /auth/logout`; the backend stores only a SHA-256 token hash until the JWT expiry, rejects that token on later requests, and periodically cleans expired revocation records using the central retention schedule. The browser still clears its local session in a `finally` path so logout remains safe if the network is unavailable.
- Password recovery requests always return a generic accepted response so an unknown email cannot be enumerated. Known accounts receive a short-lived, single-use recovery event; the actual email/SMS delivery provider and abuse controls are not implemented yet.
- Password reset accepts only an unexpired, unconsumed recovery token and replaces the stored password hash. A consumed or expired token is rejected without changing the account.
- Recovery requests are throttled server-side per normalized email and source address using typed account-recovery configuration. Distributed coordination and delivery-provider retry semantics remain deployment follow-ups.
- Authentication requests are validated at the controller boundary and login failures use one generic unauthorized message so account existence is not disclosed. Native-client acceptance remains a later roadmap phase.

Location lookup recovery:

- Forward location lookup returns a backend-owned `resolutionStatus`: `NOT_CONFIGURED`, `NO_RESULTS`, `RESOLVED`, or `PROVIDER_UNAVAILABLE`.
- A provider failure does not erase saved location settings. Web shows a retry/manual-correction explanation and keeps the existing context intact.
- Reverse lookup returns a service-unavailable response when the provider cannot answer; clients must not treat that as permission to expose or persist raw coordinates.

### Profile visibility and consent

- Profile description and profile picture visibility are independent backend-owned fields. Each can be `PUBLIC`, `PRIVATE`, or `CIRCLES`.
- `CIRCLES` requires explicit circle IDs in the update request. The backend accepts only circles owned by the profile owner and checks viewer membership before returning the protected field.
- Selected circle IDs are returned only to the profile owner. Other viewers receive the field value or a redacted null, never the owner's recipient list.
- The Web settings surface explains that circle sharing is consent-based; it does not decide access locally. Browser and Vision runtime acceptance remain open inventory gaps.

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
- Profile Settings provides a direct location editor. Users can turn location off, choose approximate or exact mode, set a default nearby-search radius, resolve a place by search, or resolve the browser's current coordinates. Enabled location modes require a resolved candidate; denied browser permission falls back to manual search.
- Profile Settings also provides inline editing for the username and profile description through the same backend-owned save action as location settings. Validation errors preserve the last valid profile and location state.
- Exact location visibility now exposes selectable owned circles or accepted circle contacts in Profile Settings. Changing the location text clears any previously resolved candidate, and scoped exact visibility cannot be saved without at least one selected recipient.
- Public profile view is relation-aware, so the same profile can expose different primary actions depending on who is viewing it.
- Public profile responses are viewer-redacted by the backend: another user's email, exact visibility recipients, raw coordinates, and street-level fields are not exposed unless the location policy explicitly permits the viewer.
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
- Booking reschedule requests use the same backend availability and capacity rules as creation. Customer and owner policy windows are enforced server-side, conflicts leave the original booking unchanged, and accepted changes create an audit event.
- Public booking uses `POST /business/public/{slug}/booking-preview` to obtain the backend-derived end time, duration, and timezone before submitting a booking. Clients must not derive booking duration locally.
- Destructive and consequential web actions use the shared action dialog with click-outside and explicit confirmation; browser-native confirmation and alert dialogs are outside the product interaction contract.
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

- Location settings expose an explicit resolution state: `OFF`, `RESOLVED`, or `NEEDS_RESOLUTION`. An enabled location without usable coordinates is never presented as ready.
- Search responses expose whether the configured provider is available. The Web surface preserves saved settings when a provider is unavailable and offers retry/manual correction instead of silently overwriting them.
- Browser geolocation permission denial is recoverable: the user can search for a place, choose another result, or clear the current candidate and correct it before saving.

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

- The backend uses bearer JWT authentication without server sessions. Logout is server-authoritative through short-lived token revocation records; expired revocation records are safe to clean up and never contain raw tokens.
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
- `/vision` can mark the currently opened chat as read only after an explicit confirmation; it cannot mark an arbitrary conversation without an opened chat context.
- `/vision` can mark all notifications for the authenticated user as read only after explicit confirmation; individual notification marking remains a separate capability.
- `/vision` can mark one explicitly identified notification as read only after explicit confirmation; missing or unauthorized notification ids fail safely through the backend news service.
- `/vision` can refresh the currently opened chat context after a stale or disconnected session and returns the latest authorized message window through the same backend chat read service.
- The workspace activity feed may include recent authorized Chat conversations. Each Chat item is backend-prepared with its canonical route, preview, and `Open` action; the Web does not derive Chat access from the activity payload.
- Vision execution failures expose typed `failureCode` and `retryable` metadata alongside the human explanation. Clients can show a retry or correction action from backend state instead of parsing message text.
- Quest creation failures are contained at the Vision adapter boundary: invalid drafts return a non-retryable validation state, while unexpected creation failures return a retryable execution-failure state without exposing backend exception text.
- `/vision` can reopen an assigned quest owned by the authenticated user after explicit confirmation; the existing Work state transition remains responsible for eligibility, status, and applicant workflow effects.
- `/vision` can create a thing listing from a reviewed title through the existing Things sharing service; ownership, sanitization, availability, and persistence remain backend-owned.
- `/vision` can add, update, or delete business gallery image metadata after explicit confirmation; owner authorization and image URL validation remain backend-owned.
- `/vision` can create, update, and delete owner business availability rules and exceptions after explicit confirmation; timezone, range, ownership, and exception validation remain backend-owned.
- `/vision` can request to borrow an identified thing listing after explicit confirmation; availability, self-borrow prevention, duplicate pending requests, and borrower identity remain backend-owned.
- `/vision` can show the authenticated user's own Things borrow requests and loans through a read-only backend snapshot; request status and listing identifiers are returned without exposing another user's private data.
- Thing owners can update listing metadata or archive a listing through confirmed Vision actions; archiving makes the listing unavailable and removes it from the public catalog.
- `/vision` can open one shared Thing listing by id through the same privacy-aware detail service used by the web detail surface; availability and owner-safe listing fields are returned read-only.
- `/vision` can show the business owner's booking calendar through the existing owner-authorized calendar read service; booking visibility and ownership remain backend-controlled.
- `/vision` can edit an own chat message within the backend edit window, reply to a message, and add an emoji reaction after explicit confirmation; chat membership, ownership, rate limits, and message validation remain backend-owned.
- `/vision` can create a business profile or update its business name after explicit confirmation; profile ownership, slug rules, sanitization, and persistence remain backend-owned.
- `/vision` can confirm an owner booking or cancel a customer/owner booking after explicit confirmation; booking role resolution and status validity remain in the shared Business use-cases.
- The Vision business bookings snapshot includes the owner's pending booking requests, so the owner can review incoming appointments without bypassing the owner-only read boundary.
- `/vision` can reject, complete, or mark an owner booking as no-show after explicit confirmation; the Business use-cases enforce owner authorization and valid status transitions.
- `/vision` can archive an owned business offering after explicit confirmation; archiving is implemented as the existing backend active-state transition.
- `/vision` can rename an owned work quest after explicit confirmation; ownership and title normalization remain backend-owned.
- `/vision` can set profile location visibility to off, approximate, or exact after review; the location policy remains backend-authoritative.
- `/vision` can create a business offering from a reviewed title and rename an owned offering while preserving its existing pricing, duration, capacity, and booking configuration.
- `/vision` can request an appointment for an offering using an offering id and explicit start/end instants; availability conflicts and customer authorization remain in the shared booking use-case.
- `/vision` can cancel a borrower's pending request, approve or decline an owner's pending request, and mark an approved borrowed thing returned after explicit confirmation; authorization, valid state transitions, and listing availability remain backend-owned.
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
- Owners can rename or remove a circle from the Circles workspace; removal is explicit and is not treated as a silent archive because the current backend does not preserve an archived group state.
- The Circles workspace exposes incoming acceptance, outgoing cancellation, person discovery, and privacy explanation together; detailed circle-member management remains a separate open capability.
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
- Personal workspace pins can save an accessible quest for quick return. Pins are private to the viewer and disappear if access is lost.
- Compact list previews for accessible quests and thing listings; previews keep the list in context and link to the canonical detail page.
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
- From a known Work quest detail, applicants can submit directly in the web UI; Vision remains available when application creation starts from natural-language intent or needs guided correction.
- Only one application per user per quest is allowed.
- Owners and admins can approve or decline pending applications.
- The terminal-first Vision surface can now approve or decline one exact pending application by first resolving one manageable quest and then one exact applicant username before review confirmation.
- Owner-side application views now expose deterministic pending-selection metadata so future automation can know whether pending applications exist and which pending application is the oldest.
- Approved applications fill worker slots up to the quest assignee target.
- If all spots are filled, the quest becomes `ASSIGNED`; remaining pending applications stay available for owner review so an approved worker can later be released or replaced without losing the candidate pool.
- Owners and administrators can release an approved worker or replace that worker with a pending applicant through backend-owned worker-management commands. A released assignment becomes `RELEASED`, reopens an available slot, and notifies the affected users.
- The Web applications surface exposes release and replacement controls for approved workers, while Vision exposes the same commands as reviewed `RELEASE_WORKER` and `REPLACE_WORKER` intents. Both clients use the shared backend authorization and lifecycle rules.
- Owners and admins can only decline applications while the quest is still open and the application is still pending.
- Applicants can update or withdraw only while the application is still pending.
- Withdrawal from the web applications workspace requires explicit user confirmation before the backend mutation is sent.
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
- Rides and Things now expose backend-owned `allowedActions` in their response contracts. The web surface uses those actions for edit, borrow, join, leave, start, complete, cancel, approve, decline, and return controls; local UI state remains limited to selection, dialogs, loading, and feedback.
- Vision transport uses the generated contract at the API boundary and a named presentation adapter for nullable canvas/view-model details, keeping text/voice presentation concerns portable for future native clients.

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

- Public business discovery, offering reads, and availability reads are anonymous read-only surfaces. Creating, reading, or changing a booking requires an authenticated registered user; guest booking is intentionally outside the current MVP contract because booking ownership, verification, cancellation, and notification identity must remain accountable to an AppUser.
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
- The booking write surfaces are implemented by these backend use cases:
  - `business/service/BusinessCreateBookingUseCase.java`
  - `business/service/BusinessCancelBookingUseCase.java`
  - `business/service/BusinessConfirmBookingUseCase.java`
  - `business/service/BusinessCompleteBookingUseCase.java`
  - `business/service/BusinessRejectBookingUseCase.java`
  - `business/service/BusinessNoShowBookingUseCase.java`
- Booking DTOs return backend-prepared `allowedActions`, `statusLabel`, and optional `blockingReason`.
- Booking presentation and read paths stay side-effect-free; missing policy rows are created only by mutation flows.
- Vision's `view_business_bookings` surface is a read-only guidance entry for business booking review, booking requests, and capacity-aware next steps; it must not invent booking state on the client.
- Customer cancellation depends on business policy and the configured cancellation window.
- Owner confirmation, rejection, cancellation, completion, and no-show marking are explicit backend transitions.
- Historical bookings keep offering title, price, duration, and timezone snapshots so later offering edits do not rewrite history.
- Offering removal is archival-safe deactivation rather than destructive history loss.
- Things borrowing currently supports listing discovery, detail, request, owner decision, borrower cancellation, and return. Listing update/archive remain separate Things concerns; voluntary car sharing is already offered through the Rides surface with optional circle-scoped visibility, matching, and an explicit trip lifecycle.
- Owner schedule data can also be projected into a calendar-shaped backend read model that groups bookings by the business's local day so owner dashboards, booking lists, and mobile clients do not derive their own schedule buckets.
- Gallery images are business-owned metadata rows and can be shown on the public page without changing booking workflow rules.
- Thing borrow responses now explain their backend-owned lifecycle state and expose typed possible actions; clients must still submit mutations to the Thing service, which rechecks ownership, availability, and current status.
- Future iPhone and Watch clients receive explicit presentation limits and unavailable/offline behavior from the backend. The Watch contract is glance-oriented and read-only while disconnected; domain confirmation and permissions remain server-side.
- Owner dashboard and owner booking list both rely on the same owner schedule summary read-model so "what is happening today" stays consistent.
- Future notification vocabulary is reserved around booking created, confirmed, rejected, cancelled, and reminder-style follow-up events.
- Appointment rescheduling is offered for eligible customer and owner bookings. The selected time is rechecked against availability and capacity, policy windows are enforced, and the original booking remains unchanged if validation fails. Clients must use the reschedule endpoint rather than creating a second booking.
- Notification preferences are user-owned per category (`chat`, `work`, `booking`, `circle`, `location`, `system`) and delivery level (`in-app`, `push`, `email`). System notices cannot be disabled; other preferences are saved atomically. Supported Work/Circle news remains in the notification inbox for history, while a disabled `in-app` preference suppresses the realtime unread-update signal for that category.
- An authorized viewer may receive the effective exact address presentation, but never the owner's selected-circle/user allow-list, provider place identifier, or raw coordinates. Those remain owner-only policy metadata.
- Current-location resolution has typed recovery outcomes: browser unavailable or permission denied, provider not configured, provider unavailable, and coordinates not resolved. A failed lookup never clears already saved location settings, and the user can retry or choose a manual search result.
- Location-aware Web, quest, and Vision behavior uses the same effective user context: mode, resolved/unresolved state, locality, and approximate label remain consistent across those surfaces.
- Vision can change one notification category and delivery level at a time after showing the requested enable/disable action for explicit confirmation. The same backend preference service enforces required system notices for Web and Vision.
- Owners can now cancel or pause an active quest from the quest detail surface or backend lifecycle endpoints. Cancellation preserves quest history and notifies affected participants; pause preserves the previous active state and resume restores it. Worker reassignment remains design-gated in `docs/work/work-quest-lifecycle-contract.yaml`.
- `/vision` consumes business capabilities such as `view_business` and `view_business_availability` from the same backend-owned business model, and future booking mutations should reuse the same business policy, availability, and snapshot rules.

## Thing Sharing

### What the module does

- Thing Sharing lets authenticated users list items they are willing to lend.
- Other users can browse available things and send one pending borrow request per thing.
- Owners can review pending requests and approve or decline them. Approval makes the listing unavailable until the borrower marks the item returned; decline leaves the listing available.

### Main surfaces

- Available things directory and dedicated listing detail surface
- Current user's lending listings
- Borrow request action for available listings

### Thing sharing rules

- Listing title is required.
- Listing descriptions are sanitized before being returned.
- Owners cannot request to borrow their own things.
- Borrow requests require the listing to be currently available.
- A borrower can have only one pending request for the same thing.
- Listing detail is loaded from the backend and carries the viewer's pending-request state; the browser does not reconstruct listing availability or request ownership.
- Only the listing owner can approve or decline a pending request.
- Only the borrower can mark an approved request as returned.
- An approved request makes the listing unavailable; returning it makes the listing available again.
- Cross-module search can present Things, Work, and Business result families together. Each result keeps its family and source route; ranking, visibility, pagination, empty states, and errors remain backend-owned.
- Comparing search results remains valid after pagination: a permitted result selected from a later page is rechecked against the full authorized candidate set before comparison, rather than being silently dropped because page zero was reloaded.

## Voluntary Car Sharing

### What the module does

- Voluntary Car Sharing lets an authenticated user publish a ride offer with origin, destination, departure time, and seat count.
- Ride offers can be public to authenticated users or scoped to circles owned by the driver.
- Passenger reservation and lifecycle actions are explicit consent actions: join, leave, driver cancellation, start, and completion. Driver approval is not required; circle visibility and passenger confirmation are the consent boundary.

### Main surfaces

- Visible ride offer list
- Current user's ride offers
- Ride offer creation form
- Ride discovery, join/leave, and driver lifecycle controls

### Ride sharing rules

- Origin, destination, departure time, and seat count are required.
- Departure time must be in the future.
- Seat count must stay between 1 and 8.
- Circle-scoped visibility can only reference circles owned by the driver.
- Ride notes are sanitized before being returned.
- A ride moves through `OPEN`, `FULL`, `IN_PROGRESS`, `COMPLETED`, or `CANCELLED`; a full ride starts only after explicit driver action.
- The implementation records lifecycle audit events and sends relevant ride events to the shared in-app news surface without expanding ride visibility.
- Vision can list visible rides and execute ride actions only after collecting required fields and receiving explicit confirmation; Web exposes the same actions directly.

## Frontend and API contract shape

### What the frontend treats as source of truth

- The frontend does not hand-maintain most DTO shapes; it imports generated contracts from the shared contract layer.
- Module API files are thin wrappers over backend endpoints, not a second business-logic layer.
- Shared axios and API-error helpers define how auth headers and backend error messages surface to the UI.
- The generated frontend contract is checked during the normal frontend build, so backend DTO or agent-model changes must refresh the contract before the build passes.

### Route and page model

- `/login` and `/register` are the only unauthenticated entry pages.
- The authenticated frontend now uses a shared shell entry model with `/home`, `/work`, `/chat`, `/calendar`, `/business`, `/circles`, and `/profile` as stable user-facing navigation routes.
- The shared shell now uses one standard entry grammar across those routes: header and actions in the hero, shared metric strip, shared list-row vocabulary, and calm desktop/mobile navigation chrome.
- `Home` currently stays intentionally thin and only consumes the backend-prepared dashboard summary instead of assembling a cross-module frontend dashboard.
- `Home` exposes direct `Find work` and `Create new work` actions; users do not need to enter Vision or infer the Work route before starting either journey.
- `Work` now reads from the existing dashboard read model for discover, my quests, and applications. `/work/find` is the explicit discovery route, `/work/quests` is the owned-quest route, and known quest/application details remain deterministic Work routes; Vision remains available for semantic discovery and guided correction flows.
- `Chat` now owns deterministic workspace browsing under `/chat` and `/chat/:conversationId`, while Vision remains the semantic handoff path when the user starts from intent instead of a known thread.
- The web Chat workspace can create a named group by searching and explicitly selecting participants through the existing people-search boundary; group creation remains backend-authorized and does not infer membership from frontend state.
- Members can leave a group conversation from its open web thread after explicit confirmation. Direct conversations do not expose the leave action.
- `Calendar` now acts as a visual month/week/day coordination workspace over backend-prepared work planner and owner booking calendar sections; source modules still own event details and mutations.
- `Business`, `Circles`, and `Profile` now each render stable backend-owned entry surfaces instead of placeholder shell chrome.
- `/people` and `/people/:userId` are the canonical Web Find People and trust-aware profile routes. Search and profile actions are backend-provided for the viewer, including connection invite and block-boundary actions. `/business/find` is the canonical authenticated public business directory route and links into public business detail before booking.
- `/vision` remains the premium authenticated deep-work surface for guided, semantic, review-gated, and cross-module tasks.
- Business Profile now includes owner gallery management: owners can upload image files through the backend storage boundary or add an external image URL, set alt text/order, hide or publish an image, and remove it with explicit confirmation. Uploads are limited to images up to 10 MB, and the public business page continues to show only active gallery images; unavailable storage returns a recoverable error.
- Vision-native flows can still resolve quest and application targets, but the normal web UI owns known quest and application detail routes so users retain predictable navigation and context.
- Vision-native profile, settings, circles, and chat routes carry the user-scoped continuation flows.
- Admin capability work is currently accessed through the Vision surface rather than a separate dedicated frontend route.
- Logged-in users now land on `/home` for orientation, while `/vision` stays available as the guided assistant route.
- In the shared-shell phase, `Work` owns deterministic browse and known detail entry points (`/work/quests/:id` and `/work/applications/:id`); Vision is used when the user needs semantic target resolution or guided execution.
- Settings stays nested under `/profile/settings`, and applications stay nested under `Work` instead of becoming top-level navigation.
- Shell-to-Vision handoff now uses one explicit route-query contract with `prompt`, `autorun`, `context`, `source`, and `returnTo`, so quick launches, contextual launches, and route return links stay typed instead of ad hoc.
- When a user explicitly asks Vision to open a supported workspace destination (for example, “open notifications in workspace”), the backend may return an allowlisted `workspace-v1` target such as `/notifications`; read-only prompts without an explicit navigation request remain inside Vision and unsupported targets return no link.

### API client model

- `authApi` owns login, register, current-session lookup, and backend logout/revocation.
- `userShellApi` is a thin authenticated entry-surface client that reads existing backend summaries for dashboard, chat workspace, circles, business owner surfaces, and profile views without adding a frontend business-logic layer.
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
- Conversation-list responses now expose page, cursor, and has-more metadata so backend consumers can page large chat sets without loading the full workspace surface.
- Messages now support optional reply linkage, attachment metadata, and per-message emoji reactions.
- Chat attachments can now be pre-validated through a dedicated backend upload endpoint before message creation.
- Conversations now expose a dedicated sync surface so reconnecting clients can fetch message deltas and current typing participants after a websocket gap.
- The detailed browser Chat surface uses that sync contract on conversation load and when the tab becomes visible or the browser returns online. It merges recovered messages by message id and keeps a manual Sync action available when recovery needs to be retried.
- Opening a conversation marks the latest loaded message as read through the backend read-state endpoint. Users can reply to an exact loaded message, edit or delete their own message, toggle a reaction, and open an attachment URL when one is available.
- Vision can now show one attachment from an authorized conversation participant by message id. It returns the backend-issued expiring URL and expiry metadata; Vision upload remains a separate capability until a binary-upload handoff is defined.
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

### How do long lists work?

Work applications and chat conversations load in bounded pages. The user can request more items without losing the current context, while Vision presents a compact first result set and exposes whether additional matches exist.

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
- A user may own multiple business profiles. Selecting a business changes the profile being edited; favorites are separate user-owned shortcuts and do not grant private business access.
- A business owner may archive an owned business. Archived businesses leave public discovery and favorite lists, while the owner can still inspect the archived profile and its existing records.
- Ride matching is an advisory filter over circle-visible active rides. It may use approximate route text and bounded departure windows, but it never creates a ride or exposes exact location without a separate user action.
- Commute matching is disabled by default. Enabling it requires explicit consent plus approximate home/work areas, weekdays, and a departure time; pausing or revoking it removes the opt-in without creating or joining a ride.
- Universal search is a permission-filtered discovery surface across supported object families. A saved search stores only a bounded query and scope owned by the creator; it can be paused, resumed, expired, or deleted without copying private source records.
- Trust actions are private: a user may block a relationship or submit a report, but the affected user does not receive the report contents. Ratings remain tied to eligible completed work interactions and cannot be self-created.
- Onboarding is optional and resumable. It stores only a bounded step state; skipping setup does not disable core modules, and reset returns the user to the welcome step.
- Activity entries are viewer-scoped projections. A resume suggestion can reopen a safe route or be dismissed; dismissing it never mutates the underlying work, ride, booking, chat, or Vision workflow.
- Vision can show the same viewer-scoped activity projection through `VIEW_ACTIVITY`; it may summarize and resume safe destinations but cannot infer or mutate the underlying source workflow.
- Things can now be discovered at `/things`, offered by the current user, and requested through a borrow message. The backend remains authoritative for availability, owner restrictions, duplicate pending-request prevention, owner decisions, and return state.
- A borrower can cancel their own pending Things request from the listing surface. Cancelled requests are no longer treated as pending; another user cannot cancel someone else's request.
- Owners can approve or decline pending requests from My things. Approval moves the request to `APPROVED` and makes the listing unavailable; a borrower can move it to `RETURNED`, which makes the listing available again.
- Circle membership is a trust boundary, not a blanket data-sharing permission. A connection does not automatically reveal another person's exact address or private activity; each module applies its own visibility and consent rules.
- Chat supports selecting an attachment, uploading it through the backend, and sending it as part of a message. The composer shows upload/send errors and lets the user remove a selected upload before sending; attachment access remains controlled by the backend.
- Image attachments show a local preview before sending and release that browser object URL when replaced, removed, sent, or the Chat view closes. The preview is only a local selection aid; backend upload and access rules remain authoritative.
- When exact location mode is enabled, Profile Settings exposes the supported visibility scopes: nobody, everyone, selected circles, or selected people. The backend validates circle membership and selected-person eligibility before saving.
- Multi-field creation and profile setup can use guided intake: the Web asks one meaningful question at a time, while the backend owns step order, validation, draft transitions, and the final review boundary. A partial guided draft is not a created entity.
## Main surface behavior

Home is an orientation dashboard. Sidebar navigation owns module discovery, so Home does not duplicate module quick-action links; dashboard metrics and attention rows lead to the relevant destination when one exists.

Find Work is external discovery. It uses the backend `AVAILABLE` preset and must not show the current user's own quests. My quests uses `MY_VISIBLE`, so an owner's newly created OPEN quest remains visible in the owner's work surface.
## Attention center

The Notifications surface includes a compact attention center assembled from the viewer-scoped notification and activity read models. It shows the unread count and only backend-provided safe destinations; it does not invent cross-module state in the browser.

## Authenticated navigation shortcuts

The authenticated desktop shell separates global destinations, module navigation, and personal shortcuts. Activity and saved searches are personal because they are viewer-scoped backend products; they are not generic favorites. The shell does not show a generic star, bell, workspace switcher, or recent-object control until the product has a matching user-owned backend contract. On narrow screens, all primary destinations and the expanded module list stay keyboard reachable without horizontal navigation scrolling.

Business booking and ride lists use the same compact workspace-row language as the rest of the authenticated app. Their status, visibility, capacity, and available actions still come from the relevant backend DTOs; changing the row presentation never grants an action or changes a booking or ride lifecycle.

Vision may receive a typed shell handoff with an optional context label and safe return path. It explains that context and can offer the return link, but the handoff does not choose a capability, bypass review, or execute an action.
### Vision target selection

When a user asks Vision to open a specific work item or application, Vision uses
the user's current authorized candidates to understand the request. A candidate
ID is not permission: the backend checks the target again before opening it. If
several items match, or if an item is stale or no longer accessible, Vision asks
for a clearer or currently accessible target instead of opening a guessed page.

## UI action behavior

The Web workspace exposes only actions that are valid for the current viewer and
entity state. A Home metric and the collection it opens use the same backend query
scope; for example, `Open work` means visible open work and does not silently become
the user's owned quests. Collection screens keep one obvious primary action and move
secondary controls into compact context surfaces. Preview is a short inspection step,
not a duplicate detail page. Activity is one timeline where each event appears once;
Rides separates finding a ride, offering a ride, and commute preferences so discovery
does not require reading or completing settings first.

Runtime closeout validates meaningful route and state transitions rather than page-load screenshots or broad smoke traces.

# Business Logic

This document explains the product in user-facing terms. It is meant to stay aligned with the code and serve as a future FAQ source for humans and chatbots.

## Scope

Current covered modules:
- identity and profiles
- work marketplace
- circles and relationships
- chat between circle contacts
- location-aware quest flows

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

### Provider metrics and audit

- The system records each provider lookup and reverse lookup as an event row.
- Admin status exposes request counts, cache hits, current-month provider usage, and database footprint context.
- Per-actor rate limits apply to both search and reverse lookup actions.

## Shared platform rules

### What this covers

- These are the cross-module rules that keep API behavior, input cleanup, auth wiring, and operational jobs consistent.
- They are not one business module on their own, but they shape how every module behaves.

### API and validation contract

- Backend services raise consistent HTTP-style business errors such as bad request, forbidden, conflict, not found, and unauthorized.
- Validation errors are returned in a structured API error format with optional field-level details.
- Malformed JSON and invalid request parameter types are normalized into readable bad-request responses.

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

### Example sandbox use

- Create 20 users with circles and quests.
- Create a quest plus synthetic applications from selected workers.
- Push a quest through approve, start, complete, and review states for UI and reporting tests.

## Chat

### What the module does

- Chat is the direct messaging layer between connected users.
- It is built on top of accepted social relationships, not on free platform-wide messaging.
- It provides conversation summaries, message history, presence, websocket updates, and retention cleanup.

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
- Losing circle eligibility also removes effective chat access to the conversation.
- Existing conversation history does not preserve chat access after the accepted relation is lost.

### Message flow

- A chat message can contain text, an image, or both.
- Sending a message updates the conversation summary fields immediately.
- Read tracking only applies to incoming unread messages for the current viewer.
- Image-only messages fall back to a generated preview label instead of raw empty text.

### Workspace and contact read model

- Workspace combines recent conversations, contact list, owned circles, unread conversation count, and online contact count.
- Contacts are derived from the current user's circle memberships plus existing conversation counterparts.
- Chat workspace only includes current accepted circle contacts.
- The workspace is already shaped for UI consumption and is not just a raw join of tables.

### Presence and realtime

- Presence is activity-window based, not permanent online state.
- Websocket connect and disconnect events update presence and notify connected circle contacts.
- Realtime events notify users when conversations, presence, or unread news state changes.

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
- Invite candidates are simply users with no current relation.
- Blocked users are shown only when the current user initiated the block.
- Nearby discovery only works when the current user has a discoverable saved location.
- Nearby discovery excludes blocked relations and shows approximate location plus distance.

### Presentation and action contracts

- Social responses already include labels, badge classes, summary labels, and primary or secondary actions.
- The frontend is not expected to infer relation semantics from raw timestamps alone.
- Profile and search actions are relation-aware, including invite, accept, cancel, block, unblock, and open-circles behaviors.

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

### Quest visibility

- A quest can be visible to everyone or limited to circles.
- If circle-specific visibility is used, the quest is only visible through matching circle relationships.
- If no explicit visible circles are selected, visibility falls back to the creator's circle relationship with the viewer.
- Owners can choose whether approved applicants are shown publicly on the quest detail.

### Applications

- Users apply to open quests with a message and a proposed price.
- Only one application per user per quest is allowed.
- Owners and admins can approve or decline pending applications.
- Approved applications fill worker slots up to the quest assignee target.
- If all spots are filled, the quest becomes `ASSIGNED` and remaining pending applications are declined automatically.
- Owners and admins can only decline applications while the quest is still open and the application is still pending.
- Applicants can update or withdraw only while the application is still pending.

### Dashboard

- The dashboard groups the user's own quests, available quests, applications, recent news, circles, and admin-only user lists.
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
- Admins can additionally change quest ownership and perform broader status updates.
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

## Frontend and API contract shape

### What the frontend treats as source of truth

- The frontend does not hand-maintain most DTO shapes; it imports generated contracts from the shared contract layer.
- Module API files are thin wrappers over backend endpoints, not a second business-logic layer.
- Shared axios and API-error helpers define how auth headers and backend error messages surface to the UI.

### Route and page model

- `/login` and `/register` are the only unauthenticated entry pages.
- `/work` is the main workmarket entry for normal users.
- `/work/:id` and `/applications/:id` are the main detail routes for quest and application flows.
- `/users/:id` and `/settings` are the main profile and profile-settings routes.
- `/admin/work`, `/admin/quests`, `/admin/users`, `/admin/applications`, and `/admin/circles` are the admin workspaces.
- Logged-in admins are redirected away from the normal `/work` route to the admin workspace.

### API client model

- `authApi` owns login, register, and current-session lookup.
- `workmarketApi` is an aggregated frontend gateway that combines quest, application, dashboard, news, user, circle, admin, and location clients.
- Each underlying client is endpoint-focused and mostly forwards server DTOs directly to the page or composable layer.
- Location lookups used in profile settings currently flow through the same aggregated `workmarketApi`, even though the backend routes live under `/location`.

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
- A quest can also carry location data and visibility rules that affect who can see it.

### Chat

- Chat is a private conversation between two users.
- A chat conversation can only exist between users who are in the same circle relationship graph.
- Chat is currently designed for direct, ongoing coordination around work and social connections.

## Current Quest Rules

- A quest title is required.
- A quest description is required and must contain meaningful rich text content.
- A quest reward amount is required and must be at least `0.01`.
- A quest can contain up to 10 images.
- Quest images must be image data URLs.
- The assignee target defaults to `1` and cannot be less than `1`.
- Quest audience is either `CIRCLES` or `EVERYONE`.
- If the audience is not `CIRCLES`, circle visibility is cleared.
- If the term is fixed, create requires a `scheduledAt`.
- Custom quest location requires at least one location field.
- A quest using custom location must resolve to usable coordinates before save.

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

### Why does chat require circles?

Because chat is currently restricted to users who are connected through the circle system.

### Why does a time change sometimes need confirmation?

Because an active quest can already have an approved worker, so changing the schedule can affect an existing commitment.

## Update Rule

- If a business rule changes in code, update this file in the same change.
- Add short FAQ entries when the same user question appears more than once.

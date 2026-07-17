# Web UI Functional Completion Audit

Date: 2026-07-17
Baseline: `4715898f78e0baa9a06393df5148646d80c3a200`
Scope: authenticated Web UI entry points, module surfaces, backend contracts, and browser-runtime acceptance.

## Executive finding

The current Web application has a mixture of real routes, generic shell projections, and direct feature views, but it does not yet behave as one complete production client. The reported symptoms are consistent with four different gap classes:

1. **Discoverability gaps** — a route or feature exists but is not exposed as a clear primary action or canonical module surface.
2. **Surface completeness gaps** — a route renders a summary/list projection where the product requires a usable workspace, such as the calendar.
3. **Contract/runtime reliability gaps** — the client calls real backend endpoints, but a single failed request collapses an entire surface or the browser has not proven the authenticated happy path.
4. **Missing product contracts** — Find People and Find Business are not represented as complete route + read-model + action surfaces.

This is not safe to close with isolated button additions. The work needs one ordered Web completion program.

## Evidence collected

### Work

- `apps/themuffinman/frontend/src/router.ts` contains `/work`, `/work/quests`, `/work/quests/new`, quest details, and applications.
- `WorkQuestCreateView.vue` contains a functioning create form and calls `POST /quests`.
- `shellDefinitions.ts` contains a `New quest` action for the Work surface, and the Work discovery page now exposes a persistent, unmistakable `Create new work` action.
- Work discovery has search, sorting, scheduled filtering, pagination, a Vision handoff, and a retryable error state; authenticated browser acceptance remains open.

### Chat

- `ChatSurfaceView.vue` calls `/chat/conversations`, then separately loads messages for a selected conversation.
- The inbox error state is driven by one `getChatConversations` failure and displays only `Could not load conversations.`.
- Chat has real backend mappings for workspace, conversation list, direct open, group creation, messages, sync, attachments, reactions, and read state in `ChatController`.
- The client does not use the workspace read model even though `/chat/workspace` exists, so both endpoint paths remain part of the contract until the client migration is deliberate.
- Authenticated PostgreSQL probing initially returned 401 for workspace and conversations. Server logs showed the real cause: the fetch query used `select distinct` with `order by coalesce(last_message_at, created_at)`, which PostgreSQL rejects because the ordered expression is not in the distinct select list. The query now fetches without database ordering and the service applies the existing deterministic ordering comparator.

### Circles

- `CirclesView.vue` now loads groups, incoming requests, outgoing requests, and blocked users independently with partial-failure diagnostics.
- A failure in one secondary request no longer blanks the other usable circle sections; full failure still exposes a retryable page state.
- Backend routes exist for all four reads, plus search, connections, groups, membership, requests, and blocks.
- The page keeps circle management separate from the canonical Find People route, while search and trust actions remain backed by the same Circles contracts.

### Calendar

- `/calendar` maps to `WorkspaceSurfaceView.vue` and the generic `SurfaceContentView` calendar renderer.
- `shellSurfaceData.ts` assembles scheduled work, flexible work, and business calendar rows.
- The renderer now provides month/week/day grids, date navigation, event links, local timezone presentation, and explicit empty/error/retry handling.
- A failed optional business-calendar read leaves available work schedule data visible; a failed primary dashboard read produces a retryable Calendar error.
- Business owner calendar backend data exists at `/business/bookings/owner/calendar`; the general calendar therefore needs a deliberate cross-module calendar read model and visual UI contract, not another list section.

### Find Work, Find People, Find Business

- Find Work is now represented by the canonical `/work/find` route and `WorkDiscoveryView.vue`, while `/work` remains the top-level Work entry and `/work/quests` owns the user's quests.
- Find People is represented by `/people` and `/people/:userId`, with backend viewer-aware search/profile reads and trust actions.
- Find Business is represented by `/business/find`, backed by the public business directory query and links to `/business/public/:slug` for booking handoff.
- These surfaces have source/API evidence and recoverable states; authenticated browser interaction remains the outstanding closeout gate.
- A route-order audit also found `/work/quests/new` declared after `/work/quests/:questId`; the static create route was therefore vulnerable to being interpreted as quest id `new`. The static route now precedes the dynamic route, and Home/Work expose direct create actions.

## Root-cause findings and remaining runtime work

The first Chat hypothesis was verified and fixed:

- Chat failed because of a PostgreSQL-specific `SELECT DISTINCT` plus `ORDER BY coalesce(...)` query incompatibility, not because the JWT or Chat authorization contract was missing. The backend now sorts the fetched conversations in `ChatService`, and the frontend includes endpoint/status diagnostics.
- Circles previously hid which secondary request failed; independent loading and warning states now expose that boundary.
- Existing build/type-check success proves compilation only. It does not prove a logged-in browser can load each surface, perform mutations, or recover from failures.

## Current runtime check

- Backend unit/integration tests pass in the H2 test configuration, including the Chat service/controller slice.
- After restarting the local dev JVM with PostgreSQL reachable, the same authenticated JWT returned `200` for `/auth/me`, `/dashboard/me`, `/chat/workspace`, `/chat/conversations?limit=20&page=0`, `/circles/groups`, and `/business/profiles`.
- The runtime probe used the seeded `test@test.com` account and confirmed the Chat read-path defect is fixed against the real PostgreSQL schema. Browser-level interaction, mutations, and the remaining calendar/discovery closeout gates are still open.

## Required design decisions

- Web UI and Vision remain equal production clients. Vision is a complement, not a fallback for missing Web UI.
- Every canonical surface must define: route, entry action, backend read model, loading state, success state, empty state, error/retry state, permission state, and mutation confirmation.
- Cross-module ranking, visibility, consent, permissions, and state transitions stay backend-owned.
- Calendar must be a visual calendar workspace, while lists remain supporting views.
- Find People and Find Business must not be faked with local filtering over unrelated screens.
- Browser-runtime evidence is required before claiming a surface functional.

## Planned implementation order

1. Establish a route/surface contract and browser smoke harness.
2. Make shell navigation and module entry actions explicit, starting with Create new work and Find Work.
3. Diagnose and harden Chat and Circles with per-request observability, partial-failure handling, and backend integration coverage.
4. Build canonical Find People and Find Business contracts and surfaces.
5. Replace the generic Calendar timeline with a visual month/week/day calendar workspace.
6. Sweep every authenticated route for complete loading, empty, error, permission, and mutation states.
7. Run backend tests, frontend checks, browser runtime scenarios, documentation audits, and `make work-verify` for the implementation plan.

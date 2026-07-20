# Web Optimization Analysis

## Current findings

The global workspace palette is centralized in `frontend/src/styles/base.css`,
but it only defines a dark `:root` palette and `color-scheme: dark`. Typography
uses small global values (`0.75rem` labels and `0.8125rem` metadata), while many
module views repeat even smaller route-local text rules. Controls and touch targets
are already tokenized, so a central scale change is possible without changing
domain behavior.

The authenticated shell already provides the correct application frame: a left
module rail, compact header, global search, Vision entry, and Home route. The
important landing bug is in `LoginView.vue` and `RegisterView.vue`, which push to
`/vision` after authentication even though `/` already redirects to `/home`.

Theme selection has no current appearance preference contract. The existing
persisted workspace preference pattern (`PersonalShortcutController` and
`WorkspaceRailPreference`) is a suitable architectural precedent, but theme must
be a separate preference with a typed value such as `SYSTEM`, `DARK`, or `LIGHT`.

The direct Work create view rendered title, description, award, and terms in one
form and submitted a local DTO. The implementation now exposes a shared
`POST /guided-intake/step` contract and a reusable `GuidedIntakePanel`; Work,
Business, Things, Rides, Social, and Identity entry flows consume it before their
canonical backend services perform final validation and persistence.

## Vision deployment boundary

The future Vision console is a separate application. This Web optimization plan
must not redesign its canvas, make `/vision` the Web landing route, or use console
runtime evidence as proof that the Web shell is complete.

The Web scope is the authenticated shell plus `GlobalVisionEntry` and
`VisionForWebHost`: an inline assistant that sends text/voice input to the shared
backend Vision runtime and renders backend-approved navigation, context, review,
and execution results inside Web. The backend semantic, personal-context,
capability, permission, and guided-intake contracts remain shared so the future
console, iPhone, and Apple Watch can consume them without duplicating logic.

## Target behavior

- The app is readable at default desktop scale without browser zoom.
- Dark remains the default-compatible theme; Light uses the same semantic tokens,
  not route-specific CSS overrides.
- Theme choice is available from Settings/account controls, persists across
  sessions, applies before or during shell render, and respects `SYSTEM` when
  explicitly selected.
- Login and registration land on Home. Inline VisionForWeb remains available from
  every authenticated surface; the future standalone console is not the Web
  landing route or a dependency of the Web shell.
- Create flows show one meaningful step at a time with a compact draft summary,
  inline validation, Back/Next, and a final review/submit state. Conditional
  fields are introduced only when backend rules say they are relevant.

## Non-negotiable boundaries

- Standardization and reuse are the primary design constraints: prefer shared
  backend services, DTOs, API contracts, frontend primitives, and tokens over
  route-specific implementations.
- Vue owns presentation state only; backend owns required-field order, conditional
  visibility, validation, permissions, and state transitions.
- All domain and workflow logic stays behind the backend/API boundary so Web,
  inline VisionForWeb, the future standalone Vision console, iPhone, and Apple
  Watch can reuse it.
- Theme tokens stay centralized in the app shell; module views must not invent
  independent dark/light palettes.
- Home is an orientation/read model, not a second Vision dashboard.
- Web theme and scale changes apply to the inline VisionForWeb host only; the
  standalone console owns its later presentation system.
- Guided-intake metadata is a shared backend/API contract, while Web and the future
  standalone console may render different client-specific surfaces.
- The first migration must not silently change create semantics or submit partial
  domain DTOs to existing services.

## Plan-by-plan readiness analysis

### `web-optimization-visual-scale.yaml`

This is the correct first slice because every later Web surface inherits the
semantic tokens. It is implementation-ready: change shared values and shell
components first, then audit touched route CSS for tiny overrides. Completion means
readability and frontend type-check/build; it must not include domain or Vision
console work.

### `web-optimization-theme.yaml`

This is the third slice and depends on the scale and surface baseline. It needs a new
backend-owned appearance preference, migration, authenticated read/write contract,
and root-level application before the settings control is complete. System, Dark,
and Light are explicit values. The Web implementation includes inline
VisionForWeb, not the future standalone console. Backend tests, frontend
type-check, and build are required.

### `web-optimization-large-screen-surfaces.yaml`

This is the second slice and closes the current frontend-wide coverage gap. The
5K requirement is not “make everything wider”: it establishes a readable content
measure, intentional multi-column/detail behavior, and consistent shared surface
primitives. It explicitly audits every authenticated module family and records
exceptions instead of pretending that one shared component covers all routes.
It also protects the inline VisionForWeb host while excluding the standalone
Vision console. Its implementation must be verified at wide desktop and narrow
responsive widths.

For an Apple Studio Display, validation should cover both the common macOS
effective viewport around 2560x1440 and a browser viewport exposing the full 5K
width. The target is not to fill every pixel: primary reading/work content should
keep a comfortable measure, while extra width can host a detail/utility rail,
comparison context, or deliberate breathing room. The plan should also check
browser zoom and text scaling so the result remains usable when the display is
configured differently.

### `web-optimization-guided-intake.yaml`

This plan now covers the complete multi-field entity workflow set in serial slices.
The first task creates the portable backend step contract; the following tasks
consume it in Work, Business, Things, Rides, Social, and Identity. The contract
must expose one current step, draft summary, validation, transition state, and
review boundary. The frontend cannot infer requiredness or submit a locally
invented workflow. The same contract remains available to the future Vision
console and mobile clients, but this plan does not change their UI.

The boundary is intentional: chat messages, notifications, search filters,
collection/detail pages, and single-action confirmations are not fake wizards.
They remain direct interactions unless a later analysis proves that they are
genuinely multi-field workflows.

### `web-optimization-home-landing.yaml`

This plan is correctly after guided intake because it is the Web navigation
closeout, not an independent Vision redesign. Login, registration, root, and
recovery behavior must converge on Home while preserving intended deep links.
Inline VisionForWeb stays in the authenticated shell and contextual handoffs. The
standalone console is neither the landing route nor a Home dependency.

### `web-optimization-runtime-closeout.yaml`

This is the only finalization slice. It must prove the current Web implementation:
readable shell, dark/light persistence, Home post-auth, inline VisionForWeb, and
one guided intake runtime for each Work, Business, Things, Rides, Social, and
Identity family. `/vision` console screenshots or console behavior do not satisfy
this plan. Temporary screenshots/traces are disposable; canonical runtime JSON,
plan status, living docs, and validation commands are retained.

## Current implementation and runtime evidence

The visual scale, large-screen surface system, persisted appearance preference,
guided-intake contract and all six entity-family migrations, plus Home routing,
are verifier-marked complete in the serial inventory. The current runtime probe
confirmed Home HTML reachability, authenticated `SYSTEM → LIGHT` persistence, and
the first backend-selected step for all six guided flows without recording a
credential or token.

The runtime artifact intentionally records that authenticated browser rendering
for 5K, dark/light visual output, and inline VisionForWeb was not captured because
this workspace has no browser automation harness and Safari automation was not
available. The master therefore remains open until a real browser trace is
captured; API probes and source inspection are not substituted for that evidence.

## Goal-pursuing decision

The implementation queue is complete through Home. The only remaining item is
the runtime closeout. It must follow the twelve-item serial contract and may only
close after its browser evidence is current and complete.

The final runtime closeout must also prove that the shared step contract is reused
across those entity families rather than implemented as six unrelated frontend
flows.

# Services human-understandable UX analysis

## Decision

Services is the customer-facing place for discovering another person's business,
understanding what it offers, checking real availability, and booking one service.
The public business route is therefore a small business website with an embedded
appointment journey, not a generic Business admin detail and not a booking form
surrounded by technical metadata.

The target sequence is:

```text
Find a service
  -> understand the business and its rating
  -> compare services and prices
  -> choose one service
  -> see server-backed available days
  -> choose a time
  -> answer service-specific questions
  -> review price and confirmation terms
  -> submit
  -> see the durable booking outcome
```

## Canonical product constraints

- Services stays selected in the global navigation throughout discovery, favorites,
  public profile viewing, and booking.
- Business remains the owner's private management workspace.
- The public page must show the business identity first: hero image, name, headline,
  description, published address/service area, contact methods, website, and gallery.
- Ratings and review text must come from persisted reviews tied to completed bookings;
  the UI must not show invented ratings or unverified testimonials.
- Availability, pricing, capacity, timezone conversion, and booking permission remain
  backend-owned.
- A service must be selected before availability is meaningful because offerings can
  have different duration, capacity, resources, fulfillment rules, and prices.
- The UI follows the Apple-inspired product contract: clarity before density, stable
  navigation, neutral readable surfaces, progressive disclosure, direct manipulation,
  strong keyboard/focus behavior, and restrained materials.
- Desktop and mobile must both complete the direct booking flow; Vision is complementary
  and does not replace this Web journey.

## Current implementation assessment

### What already works

- `/business/find` and `/business/favorites` are canonical Services routes.
- `/business/public/:slug` now carries the Services shell context.
- The public page DTO already contains the profile fields needed for a real mini-site:
  hero image, name, headline, description, public address label, email, phone, WhatsApp,
  website, timezone, offerings, and gallery images.
- The backend already calculates valid availability windows and applies business
  timezone, booking policy, resources, capacity, exceptions, and existing bookings.
- Quote, preview, idempotent booking creation, and durable completion state already
  exist.
- Business reviews do not yet exist. Work reviews cannot be reused directly because
  their eligibility and uniqueness belong to quests and user roles, not completed
  bookings and a business profile.

### What is difficult to understand

- Discovery results look like operational rows rather than inviting service-provider
  cards; the repeated sentence about status, prices, and area is mechanical.
- The public page oscillated between an oversized promotional hero and an overly reduced
  booking-first view. Neither expresses the intended “business website plus booking”
  model.
- Profile identity, trust information, service choice, date selection, and booking
  questions compete in one vertical stack.
- The current native date input does not communicate which days are available before
  selection.
- Availability times appear only after choosing a date, so users must guess and retry
  unavailable days.
- Technical concepts such as timezone, slot capacity, pricing type, and schema steps
  can appear before they help the user make a decision.
- The current public page owns too much state and presentation in one Vue file, making
  the hierarchy hard to evolve safely.

## Target information architecture and tabs

Services retains its module tabs, `Find service` and `Favorites`. An opened public
business gets a subordinate local tab row:

- `Overview`
- `Services & prices`
- `Reviews`

These are sections of one public page, not new global destinations. The business
identity remains visible above the tabs, and the selected section is represented in the
URL query so links, refresh, and Back preserve context.

Booking is not a fourth tab. `Book appointment` opens `Services & prices`; selecting a
service reveals its calendar directly below or beside that service. This keeps service,
price, duration, and availability causally connected.

### Services discovery

- Stable local tabs: `Find service` and `Favorites`.
- One plain search field with a human prompt such as “What service or business do you
  need?”
- Optional secondary filters only when backed by real data.
- Visual provider cards with hero thumbnail, business name, one-line headline, area,
  booking availability, and a clear `View services` action.
- Empty, loading, and failure states explain the next useful step.

### Public business page

The page has five calm parts:

1. **Persistent business identity**
   - hero image or intentional fallback;
   - business name and headline;
   - published address/service area;
   - verified average rating and review count;
   - save action;
   - direct contact/website actions when published.
2. **Overview tab**
   - readable description;
   - compact gallery;
   - address and contact details.
3. **Services & prices tab**
   - cards showing service title, summary, duration, understandable price, and booking
     mode;
   - fixed, from, free, and quote-required prices use honest human language;
   - selecting one service updates the booking calendar without leaving the page.
4. **Reviews tab**
   - average rating, count, and rating distribution;
   - chronological verified-booking reviews with stars, optional text, service snapshot,
     and date;
   - an honest empty state when no reviews exist.
5. **Book an appointment**
   - selected service summary;
   - month calendar with available/unavailable day states;
   - time slots for the selected day;
   - service questions/options after a time is chosen;
   - review panel with local time, duration, price/quote state, confirmation expectation,
     and one submit action.

### Desktop composition

Use a readable content column for the public profile and a booking workspace below it.
When a service is selected, the calendar and booking summary may form a two-column
surface: calendar/time selection on the left and the current selection/actions on the
right. The right side may be sticky only within the booking section and must not obscure
profile content.

### Mobile composition

Use one task column:

1. profile identity;
2. contact actions;
3. services;
4. month calendar;
5. available times;
6. questions;
7. review and submit.

The selected service and time remain visible in a compact summary. The mobile layout
must not squeeze a desktop calendar or create horizontal scrolling.

## Availability projection

The existing range endpoint returns absolute windows. A customer calendar needs
business-local day identity, day availability, and the slots for the selected day.
Deriving this from instants in the browser would duplicate timezone and DST knowledge.

Add one backend-prepared calendar projection for a bounded visible range:

- business profile and offering identity;
- business timezone;
- requested local start and end dates;
- one ordered day entry per visible date;
- `available` state and available slot count;
- safe slot windows for available days;
- an explicit empty/unavailable result without exposing owner resources or private
  capacity details.

The service reuses `BusinessAvailabilityReadService` and its computation boundary.
The controller stays thin. The client renders the projection and never infers whether
a day or slot is bookable.

## Interaction states

| State | User-facing behavior |
|---|---|
| No service selected | Show services and explain that availability depends on the service. |
| One service exists | Preselect it and load the current visible calendar range. |
| Several services exist | Keep the calendar inactive until one service is selected. |
| Loading availability | Preserve the selected service/month and show an in-place loading state. |
| No days available | Explain that the visible month has no times and allow month navigation. |
| Day available | Selecting it reveals backend-returned time buttons. |
| Slot becomes stale | Preserve answers, refresh availability, explain the conflict, and return focus to time selection. |
| Quote required | Show that the business will confirm price; do not fabricate a numeric total. |
| Booking succeeds | Show persisted status, service, local time, price snapshot/quote state, and My bookings link. |
| Booking fails | Keep the user's recoverable input and show one retry or reselection path. |
| No reviews | Show “No reviews yet” without a zero-star visual that resembles a bad rating. |
| Review eligible | A customer with a completed booking can create one 1–5 star review with optional text. |
| Review already exists | The customer updates that booking's review instead of creating a duplicate. |
| Review not eligible | Do not show a submit control; the backend also rejects the request. |

## Business review contract

Business reviews use a Business-owned model rather than the Work quest review table:

- one review per completed booking;
- reviewer must be the booking customer;
- reviewed target is the booking's business profile;
- 1–5 stars are required;
- normalized optional review text has a bounded length;
- the displayed service name comes from the booking's historical snapshot;
- public reads expose only safe reviewer identity, rating, text, service snapshot, and
  timestamps;
- summary average, count, and distribution come only from persisted reviews;
- owners cannot create a customer review for their own business.

The review's optional text is the customer comment. Separate threaded comments, owner
replies, reactions, reporting, and moderation workflows remain outside this first slice.

## Visual language

- Use existing global tokens and shared controls; do not create a Services-only token
  dialect.
- Hero imagery supports identity, while booking controls remain neutral and legible.
- Use whitespace, typography, and grouping for hierarchy rather than many bordered
  cards.
- Purpose color may orient the Services destination but never encode availability,
  price, permission, or booking status.
- Use icons only with text labels for contact and calendar controls.
- Gallery media uses stable aspect ratios and accessible alternative text.

## Accessibility and usability acceptance

- Calendar implements a labeled grid with keyboard navigation, visible focus, selected
  date state, available/unavailable semantics, and a clear month title.
- Disabled dates are not represented by color alone.
- Time slots are real buttons with selected state.
- Loading and booking outcomes use appropriate live regions without repeatedly
  interrupting screen-reader users.
- Contact actions have understandable accessible names.
- Focus moves to the next meaningful region after service/date selection and returns to
  time selection after a stale-slot conflict.
- Desktop light/dark and mobile light/dark screenshots must be reviewed visually.
- Browser traces must cover discovery, profile content, service selection, month
  navigation, slot selection, review, success, empty month, and stale-slot recovery.

## Scope boundaries

This program does not add staff selection, map search, proximity ranking, guest booking,
recurring appointments, attachments, third-party recipients, payments, threaded review
comments, owner replies, reactions, reporting, moderation, or owner configuration.

Existing verified booking validation, quote, preview, creation, favorites, owner
calendar, and navigation behavior are baseline-only. The residual work is the Services
customer experience, a backend-prepared public availability calendar projection, and
fresh route-specific runtime evidence.

## Recommended delivery order

1. Verify atomic task hardening and serial inventory.
2. Improve Services discovery without changing search authority.
3. Add verified-booking Business ratings and reviews.
4. Restore the complete public mini-site with Overview, Services & prices, and Reviews.
5. Add the backend public availability calendar projection.
6. Render the interactive customer month calendar.
7. Integrate the guided booking and recovery journey.
8. Add review reading and completed-booking review submission.
9. Close documentation and contract truth.
10. Verify responsive/accessibility behavior.
11. Capture desktop and mobile browser evidence.

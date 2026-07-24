# Structural redesign scope freeze — 2026-07-24

This is the scope boundary for the next goal-pursuing implementation. It is a hard-cut frontend redesign with backend/API parity, not an incremental card or toolbar refresh.

## In scope

- Apple-inspired, calm, standardised Web product language that can later map to Swift iPhone and Apple Watch clients.
- One authenticated shell, one header hierarchy, one tab primitive, one preview primitive, one form grammar, one token/CSS system, and one persistent VisionForWeb composer.
- Clear Home sections, Work scope separation with adjacent preview, isolated per-business workspaces, professional public business pages, simple booking, all-module Calendar, and per-business calendars.
- Circles as people/groups/visibility/privilege/chat/event context; Things as low-friction lending plus shareable wishlist; Rides as consent-aware background matching and suggestions.
- Attention, Saved searches, Profile, Settings, Chat, responsive behavior, accessibility, loading/empty/error/conflict states, cleanup, and retirement of duplicate surfaces.
- Backend-owned rules, DTOs, permissions, aggregation, pricing, capacity, matching, and VisionForWeb semantics exposed through portable API contracts.

## Explicitly out of scope

- Native Swift implementation, deployment changes, provider replacement, unrelated domain expansion, and visual decoration without a comprehension benefit.
- Keeping old and new UI systems as parallel canonical experiences.
- Frontend recreation of ownership, permission, pricing, capacity, matching, or workflow rules.

## Non-negotiable acceptance gates

1. No task closes without an observable outcome, exact paths, leaf validation, and evidence boundary.
2. No route is accepted if it only renames or re-labels an unscoped dataset.
3. No legacy surface is deleted before its replacement and runtime evidence exist.
4. VisionForWeb remains available everywhere as a quiet Siri-like contextual composer; repeated Ask Vision controls are removed only after parity evidence.
5. Calendar aggregates all module event sources while each business retains an isolated business calendar.
6. CSS and component cleanup is measured and reviewable; new one-off grammars are rejected.
7. Backend/API parity is proven before frontend task closeout, and the final runtime matrix passes on desktop, mobile, keyboard, permission, empty, conflict, and recovery journeys.

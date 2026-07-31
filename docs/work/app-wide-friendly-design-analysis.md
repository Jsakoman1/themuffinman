# App-wide friendly design analysis

Date: 2026-07-29  
Baseline: `a18b811e65001f68d174b42d1574f6c0f2ffc18d`

## Product intent

The Home redesign establishes the desired language: a person should first understand
where they are, then see one or a few meaningful choices, and finally receive compact
follow-up or recovery. Colour is a purposeful destination cue, never a substitute for
domain status, permissions, urgency, prices, availability, or workflow state.

The responsive Web client is the current mobile client. This program improves its
390px layouts and preserves backend-owned data, actions, and permission decisions for
future native clients. It does not claim to build a separate native iOS application.

## Evidence reviewed

- Current authenticated screenshots at 1440px and 390px for Work, Things, Rides,
  Calendar, Chat, People, Circles, Profile, Activity, Business overview, and Service
  discovery.
- The verified `human-first-all-surfaces` and `human-usability-business-parity`
  programs, treated as interaction and behavioural baseline only.
- The verified friendly Home launcher and follow-up-card programs, treated as the new
  visual language baseline.

## What is already working

- The shared shell provides stable desktop rail, fixed mobile tabs, and a More drawer.
- Home demonstrates clear purpose selection and compact secondary cards in both themes.
- Things already has direct, understandable borrowing calls to action.
- Business booking and owner journeys are already governed by separate human-first
  evidence and are not reimplemented here.
- Existing typed read models, route ownership, and action permissions are sufficient
  for visual simplification; no client-side business rules are required.

## Material gaps found

| Surface family | Current gap | Target outcome |
| --- | --- | --- |
| Shared collections | Dense neutral rows, inconsistent heading/CTA spacing, and repeated page-local panels make otherwise simple decisions feel administrative. | One shared friendly collection grammar: orienting header, one primary action, readable summary rows/cards, and secondary controls after the first decision. |
| Work | Desktop repeats “Offer work”; mobile places the same action in a callout and header while discovery rows remain table-like. | One primary creation handoff; a lighter discovery digest with clear reward, timing, and location hierarchy. |
| Things | The direct borrow action is clear, but collection rows are still visually dense and secondary condition text competes with the listing identity. | Friendly listing cards with a concise identity, owner/context, condition, and one request action. |
| Rides | Route entry, optional matching, form fields, and suggestions are exposed together. | A purpose-led route card, essentials-first route form, collapsed regular-commute setup, and compact suggestion cards. |
| People, Circles, Profile | Action clusters and raw compact rows obscure the personal decision. Empty Circles lacks a warm first step. | Intent cards for connect/manage/privacy, clear empty-state invitation, and profile summary retained as the calm personal base. |
| Activity and notifications | Repeated rows create a long undifferentiated action feed. | A visible highest-priority recovery plus grouped secondary updates, without changing notification semantics. |
| Calendar | Desktop grid is correct for planning, but controls and source scope compete with the next event; on mobile the week grid occupies the first screen. | Today/next agenda leads mobile; desktop keeps grid with a quieter agenda rail and progressively disclosed controls. |
| Chat | Desktop split pane is appropriate; mobile still shows both panes at once, leaving an unusable partial detail surface. | Mobile switches deliberately between inbox and thread, with a clear Back to inbox recovery; desktop stays split-pane. |
| Service discovery and Business overview | Discovery preserves collection context well on desktop, but its selection/context rail survives as awkward stacked content on mobile. Business overview is a raw list. | Mobile opens or reveals one selected business at a time; discovery lists and owner overview get a purpose-led, readable collection treatment. |
| Detail and creation screens | They have established workflows but inherit inconsistent dense headers, action bars, and utility copy. | Apply only shared header, action hierarchy, spacing, and responsive detail conventions; preserve all existing domain-specific steps. |

## Design rules for implementation

1. Keep the first decision visible; do not add a colourful card solely as decoration.
2. Use semantic purpose tones only for entry, follow-up, or empty-state choice cards.
3. Keep consequences, statuses, prices, availability, and permissions in their existing
   semantic status language.
4. Prefer one whole-card route/action surface over an adjacent card plus duplicate CTA.
5. On mobile, use one task column, purposeful horizontal controls only where they are
   genuinely sibling choices, and never keep a hidden desktop detail pane visible.
6. On desktop, retain list/detail and calendar-grid patterns where comparing context is
   valuable; do not convert every screen into a tile dashboard.
7. Do not change canonical routes, backend contracts, booking workflows, or mutations
   unless a visual simplification proves a missing backend-prepared presentation field.

## Program boundary

The program includes every current authenticated route family and responsive shell
behaviour. Identity entry routes and Vision canvas are reviewed only for visual
compatibility: they retain their separately verified interaction models and need no
duplicate recreation of their workflows. Business owner setup and public booking remain
baseline-only except for shared collection/header/mobile-detail presentation alignment.

## Proposed serial sequence

1. Harden the plan and establish shared collection primitives.
2. Apply the collection grammar to Work, Things, and Rides.
3. Apply personal and relationship hierarchy to People, Circles, Profile, Activity, and
   notifications.
4. Repair responsive Calendar, Chat, and Service/Business discovery composition.
5. Align detail, creation, account, and remaining secondary surfaces to the shared
   header/action grammar.
6. Capture fresh light/dark desktop and mobile evidence, then close out documentation.

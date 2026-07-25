# Apple desktop product reference contract

Status: canonical design-direction reference for the deep-cut Apple desktop-app redesign.
Goal pursuit note: complete redesign acceptance requires rendered visual evidence, not structural markers alone.

Goal-pursuit preflight reviewed: 2026-07-24. This reference remains the primary design authority for every queued redesign task.
Reference-contract adoption reviewed: 2026-07-24.
Mental-model contract reviewed: 2026-07-24.
System-behavior, trust, and continuity contract reviewed: 2026-07-24.
Screenshot comparison matrix: `docs/apple-app-screenshot-comparison-2026.md`.

This document translates publicly documented Apple platform patterns into portable
TheMuffinMan requirements. It is not permission to copy Apple trademarks, exact
screens, proprietary icons, SF Symbols files, Apple copy, or Apple source code.
The target is a product that feels native to the same ecosystem through hierarchy,
behavior, interaction grammar, and restraint.

## Direct references

### Platform and system model

- [Designing for macOS](https://developer.apple.com/design/human-interface-guidelines/designing-for-macos/)
- [Windows](https://developer.apple.com/design/human-interface-guidelines/windows)
- [Menus and actions](https://developer.apple.com/design/human-interface-guidelines/menus-and-actions)
- [Toolbars](https://developer.apple.com/design/human-interface-guidelines/toolbars)
- [Navigation and search](https://developer.apple.com/design/human-interface-guidelines/navigation-and-search)
- [Sidebars](https://developer.apple.com/design/human-interface-guidelines/sidebars)
- [Split views](https://developer.apple.com/design/human-interface-guidelines/split-views)
- [Search fields](https://developer.apple.com/design/human-interface-guidelines/search-fields)
- [Searching](https://developer.apple.com/design/human-interface-guidelines/searching)
- [Popovers](https://developer.apple.com/design/human-interface-guidelines/popovers)
- [Sheets](https://developer.apple.com/design/human-interface-guidelines/sheets)
- [Focus and selection](https://developer.apple.com/design/human-interface-guidelines/focus-and-selection/)
- [File management](https://developer.apple.com/design/human-interface-guidelines/file-management)
- [Settings](https://developer.apple.com/design/human-interface-guidelines/settings)
- [Writing](https://developer.apple.com/design/human-interface-guidelines/writing)
- [Generative AI](https://developer.apple.com/design/human-interface-guidelines/generative-ai)
- [Managing notifications](https://developer.apple.com/design/human-interface-guidelines/managing-notifications)
- [Undo and redo](https://developer.apple.com/design/human-interface-guidelines/undo-and-redo/)
- [Accessibility](https://developer.apple.com/design/human-interface-guidelines/accessibility/)
- [Privacy](https://developer.apple.com/design/human-interface-guidelines/privacy/)
- [Alerts](https://developer.apple.com/design/human-interface-guidelines/alerts)
- [Drag and drop](https://developer.apple.com/design/human-interface-guidelines/drag-and-drop)
- [App Shortcuts and App Intents](https://developer.apple.com/design/human-interface-guidelines/app-shortcuts)
- [Snippets](https://developer.apple.com/design/human-interface-guidelines/snippets)

### Apple desktop application references

- [Mail User Guide for Mac](https://support.apple.com/guide/mail/welcome/mac)
- [Notes User Guide for Mac](https://support.apple.com/guide/notes/welcome/mac)
- [Reminders User Guide for Mac](https://support.apple.com/guide/reminders/welcome/mac)
- [Calendar User Guide for Mac](https://support.apple.com/guide/calendar/welcome/mac)
- [Use the Finder on Mac](https://support.apple.com/guide/mac-help/mchlp2605/mac)
- [Customize your Mac with System Settings](https://support.apple.com/guide/mac-help/change-system-settings-mh15217/mac)
- [How to use Siri on Mac](https://support.apple.com/guide/mac-help/how-to-use-siri-mchl6b029310/mac)
- [Pages User Guide for Mac](https://support.apple.com/guide/pages/welcome/mac)
- [Use tags to organise files on Mac](https://support.apple.com/guide/mac-help/mchlp15236/mac)

## Derived product contract

### 1. TheMuffinMan is one workspace, not a set of unrelated pages

The authenticated experience uses one stable application frame:

- functional sidebar for global context and module navigation;
- toolbar for back/forward, current context, local search, and a small number of
  high-value actions;
- main content canvas with a readable list, grid, editor, or calendar;
- split-view detail/preview when simultaneous context helps;
- persistent bottom Vision composer, visually quiet and never a second dashboard;
- settings as a dedicated pane/window-style surface, not an overloaded toolbar.

The browser remains the delivery container. We do not fake a macOS title bar, Dock,
menu bar, Finder chrome, or native window controls. A future Swift client may map
the same backend contracts to real platform surfaces.

### 2. Every command has one discoverable home

The same action model must be reachable through the appropriate combination of:

- visible primary action;
- toolbar action for frequent contextual work;
- More/context menu for secondary actions;
- Cmd/Ctrl+K command palette for navigation and power use;
- menu-equivalent command registry for future native clients.

No action may exist only behind Vision, only behind an unexplained icon, or only in
a page-local toolbar. The command registry is permission-aware and backend-prepared.
Destructive or consequential actions require explicit confirmation and recovery.

### 3. Sidebar/list/detail is the default desktop grammar

Use the Finder/Notes/Mail-style hierarchy when a user needs to browse and inspect:

- sidebar: context, folders, businesses, circles, saved views, filters;
- list: compact rows, unread/attention/availability state, selection;
- detail: editor, conversation, event, booking, service, or Thing preview.

Selection remains visible, keyboard navigation does not unexpectedly change context,
and a preview closes back to the same row and scroll position. New full pages are
reserved for genuinely different workflows, not ordinary inspection.

### 4. Search is one coherent system

Global search, local collection search, command search, and Vision input are distinct
but related modes. Search must:

- have one obvious entry point in the current toolbar/context;
- preserve the previous surface when dismissed;
- provide suggestions, recent searches, scopes, and clear empty states where useful;
- search backend-owned indexed data rather than only the currently loaded array;
- deep-link directly to the relevant preview or action;
- never be duplicated as several competing header buttons.

### 5. App-specific lessons to adopt

| Apple reference | Adopt as TheMuffinMan behavior |
|---|---|
| Mail | Triage-first list/detail; unread and needs-attention hierarchy; search and filtering; compose in context; actions such as archive/mark/follow-up are reversible and close to the selected item. Apply to Chat, Work, bookings, and Attention. |
| Messages/iMessage | Conversation-first continuity; one primary compose action; inline attachments and contextual actions; related Work/Business/Booking context stays attached to the conversation; avoid making users navigate away to complete a social action. |
| Notes | Quick capture with immediate focus; autosave; rich text/editor focus; folders, tags, pinned items, and smart collections; preview list before opening detail; collaboration and visibility are explicit. Apply to Vision notes, Business content, Things wishlist, and shared Circle material. |
| Reminders | Simple completion gesture; lists and sections; subtasks only when needed; tags/smart lists; scheduled items visible in Calendar; templates for repeatable flows; recoverable deletion. Apply to Home attention items and guided Work/Business flows. |
| Calendar | Multiple sources with understandable identity; day/week/month views; Today anchor; arrows; event preview; all-day/timed distinction; reminders and bookings in the same temporal model; per-Business isolation plus authorized global aggregation. |
| Finder | Sidebar favorites and locations; browse/list/grid/preview modes; tags and smart collections; contextual actions; selection continuity; Quick Look-like preview before committing to an open/detail route. Apply to Things, Business discovery, Work, and files/media. |
| System Settings | Grouped panes, stable sidebar, concise labels, search across settings, current pane title, restore last pane, and settings that are not mixed into everyday toolbar commands. |
| Siri | Available from any context by text and future voice; clear idle/listening/thinking/result/error states; concise response bubble; suggested next actions; disclose AI; confirm significant mutations; provide Edit/Undo/Retry and explain limitations. |
| Pages | Template-first creation; focused editor; rich text/media/layout support where the domain needs it; inline editing; autosave and version/recovery model; collaboration permissions; inspector/detail controls stay contextual instead of crowding the main canvas. Apply to Business landing pages and rich gallery/content editing. |
| Dock/taskbar | Do not reproduce the OS Dock in Web. Provide equivalent high-value entry points through persistent app navigation, Favorites, recent contexts, command palette, and deep links. Future native clients may expose safe quick actions through the real platform Dock/Shortcuts surfaces. |

### 6. Logic and backend requirements

The visual patterns are not sufficient without the matching authority model:

- backend owns context identity, permissions, visibility, search scopes, event
  aggregation, booking state, pricing, workflow transitions, and Vision actions;
- every list/detail surface has a typed read model and stable deep link;
- every command has permission, availability, confirmation, idempotency, and
  recovery metadata;
- autosave/draft behavior is explicit and must not silently commit a domain mutation;
- search and smart collections are server-backed and bounded;
- selection, pane widths, view preferences, and dismissed presentation state are
  presentation preferences, never business truth;
- notifications distinguish passive, actionable, time-sensitive, and system states;
- stale, deleted, forbidden, conflict, retry, and offline/browser-boundary states
  retain the user’s context and offer a safe next action;
- API contracts must remain portable to iPhone and Apple Watch clients.

### 7. Apple-like acceptance test for every screen

Before a redesigned screen is accepted, answer all of these:

1. What is the current context and where is it visible?
2. What is the one primary action?
3. Can the user inspect the selected item without losing the collection?
4. Where are secondary and destructive actions, and can they be undone or recovered?
5. Is search discoverable and correctly scoped?
6. Does keyboard navigation preserve focus and selection?
7. What happens at loading, empty, stale, forbidden, conflict, failure, and success?
8. Does the screen remain clear in dark mode, zoom, forced colors, and reduced motion?
9. Is the logic owned by a backend contract rather than duplicated in Vue?
10. Would the same domain contract make sense to a future Swift client?

## Mental model and information-flow contract

The primary Apple advantage is not visual styling; it is the user’s mental model:
the system reveals structure gradually, keeps orientation stable, and lets people
move between many entities without forcing all data into the first view.

### Overview first, detail on demand

- Every collection starts with a compact overview: title, identity, status, one
  useful metadata line, and the next available action.
- Detail is opened by selection, preview, drawer, popover, or deliberate route;
  it is never loaded as a wall of fields for every row.
- Advanced fields, raw IDs, audit metadata, pricing formulas, permissions, and
  technical state remain behind an intentional Details/More/Inspector affordance.
- “Hide”, “Dismiss”, and “Show” are never used as unexplained primary navigation.
  The product should use named tabs, grouped sections, disclosure chevrons,
  previews, or a clear More menu so the user knows what is being hidden and why.

### Few entities and many entities use the same navigation grammar

- One to five entities: show a calm list or compact cards with persistent
  selection, then inspect the selected entity beside the list.
- Many entities: show bounded pages/virtualized results, useful grouping, sorting,
  search suggestions, filters, and a meaningful empty/partial result state.
- Never render 1,000 jobs, businesses, Things, rides, or messages as an undifferentiated
  wall. The first view is a digest; the user chooses the next slice.
- Sorting and categorization must be human-readable: status, recency, relevance,
  date, source, business, distance, or user-created groups. Technical IDs are not
  default sort labels.
- Selection, scroll position, active filters, and current group persist while a
  user previews or returns from an entity.

### Settings are a lightweight configuration journey

- Settings are not a permanent half-screen dashboard. Use a settings icon or
  command to open a focused pane/popover/sheet for the relevant scope; use a
  dedicated settings window/pane only when the setting family needs navigation.
- Group settings into named categories with short summaries and current values;
  show the detail editor only after selection.
- Search settings when the category tree is large, restore the last viewed pane,
  and keep everyday commands out of Settings.
- Apply safe presentation preferences immediately where possible; preserve drafts,
  explain consequential changes, and make reset/recovery explicit.

### Browse, inspect, edit, commit

Every high-volume flow should have these distinct states:

1. Browse a summarized collection.
2. Inspect one selected entity in context.
3. Edit only the fields relevant to the current decision.
4. Review the consequence, then commit or cancel.
5. Return to the same collection position with visible success/recovery.

This applies to “My work”, Find Work, Businesses, Calendar events, Things,
Circles, Rides, Chat, settings, and Vision-generated actions. Vision may shorten
the path, but it must not remove orientation, review, permission, or confirmation.

### Module-level application rule

The shared Apple mental model must be visible in every product module, not only in
the shell:

- Work: compact owned/discovery/application summaries, preview before full quest,
  clear scope tabs, saved searches, filters, and review/commit flows.
- Business: customer versus owner context, service-first public page, progressive
  booking, rich editor/gallery preview, per-business workspace and Calendar.
- Calendar: all-module source aggregation, per-business isolation, range navigation,
  event preview, and explicit source identity.
- Chat: conversation list/detail continuity, unread/attention triage, attachments
  preview, related-object context, and one focused composer.
- Circles: people-first relationships, groups, visibility/permission summaries,
  club/event context, and no unexplained social graph terminology.
- Things: quick listing, browse/preview, wishlist, availability, borrow/return
  state, trust context, and low-friction request flow.
- Rides: background matching suggestions, route/time/seat summaries, circle trust,
  low-pressure opt-in, and clear change/cancellation consequences.
- Attention and Saved Searches: grouped actionable queues, urgency-aware status,
  compact rows, snooze/mute/filter controls, and no notification wall.
- Profile and Settings: identity/context summary, grouped panes, privacy and
  visibility explanations, search, and inline edit/recovery.

## System behavior, trust, and continuity contract

Apple-like quality also comes from making the system safe to explore and easy to
resume. TheMuffinMan must treat these behaviors as product contracts, not polish:

- Undo/Redo is available for safe reversible mutations, uses meaningful action
  names, and restores/highlights the affected object. Backend mutations expose an
  operation identity, inverse/recovery capability, and idempotent replay boundary.
- Drafts autosave where safe. Closing a preview or switching context does not lose
  text, Business rich content, booking choices, Thing listings, or Vision input.
  Committed domain mutations remain explicit and are not confused with drafts.
- Activity and history explain what changed, by whom, when, and what can be
  reversed. Notifications are urgency-aware and never use “urgent” language for
  marketing or low-value information.
- Deep links restore the relevant context, collection, selection, preview, and
  action state. A user returning from a notification, Vision response, shared link,
  or future native client should land directly at the useful object.
- Search results, calendar events, Things, Businesses, Work, circles, and chat
  messages have stable backend entity identity and action capabilities so future
  App Intents/Shortcuts-style clients can expose safe actions without duplicating
  business logic.
- Quick Look-like previews are preferred for attachments, gallery media, Business
  pages, Thing details, and rich content before opening or committing a larger
  workflow.
- Multi-select and batch actions are available where they reduce work, but the
  selected count, affected scope, preview of consequences, partial failures, and
  undo/retry path are always visible.
- Collaboration exposes participants, ownership, edit/view rights, activity, and
  conflict resolution. “Shared” is never treated as “public”.
- Permission requests are contextual and explain why data is needed before asking;
  denied, expired, revoked, and partial-consent states have a useful fallback.
- Browser limitations, offline reads, native handoff, notifications, and external
  provider behavior are explicitly represented as capabilities and boundaries;
  the UI never promises a platform feature that the backend/runtime cannot prove.

## Scope boundary

These references strengthen the redesign direction and implementation precision.
They do not claim native macOS runtime, Dock integration, offline mutation,
Spotlight indexing, SiriKit/App Intents integration, iCloud sync, or Apple platform
permissions. Those remain explicit future-client or external-provider boundaries.

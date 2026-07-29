# Human-first all-surfaces analysis

## Objective

Apply the proven Business interaction discipline to every authenticated product surface without copying Business booking concepts into unrelated domains. The shared rule is: orient the person, show what needs attention, make one next action obvious, preserve context while inspecting detail, reveal advanced configuration only when needed, and let the backend remain authoritative for actions and state.

## Proven Business pattern to reuse

1. A calm, named workspace with an explicit scope or context.
2. Stable sibling tabs only when a module has genuine peer areas.
3. Attention items before history or secondary lists, with count badges only for actionable backend state.
4. A list plus adjacent detail/utility surface where comparing multiple objects matters.
5. A short consequence statement next to each allowed action.
6. Guided essentials first; advanced configuration is contextual and collapsed.
7. Completion, validation, and empty states explain the next useful step.

## Current surface review

| Surface family | What is already good | Main human gap | Target pattern |
| --- | --- | --- | --- |
| Home and notifications | Viewer-scoped summaries, attention centre, canonical links | Home can still read as module inventory rather than a personal operating day | Today, needs attention, next commitments, then calm entry actions |
| Work | Search, quest detail, applications and typed actions already exist | Owner/applicant role and next decision are split across routes and controls are dense | My work as an operating queue; detail with consequence-aware application decisions |
| Things | Search, listing preview, request actions and owner/borrower roles are available | My listings, incoming requests and borrower requests compete in one view | Separate attention queue from inventory; retain preview/detail pattern |
| Rides | Clear consent boundary, matching preference form, previews and lifecycle actions | Setup, discovery and personal ride operations appear together | Guided commute setup once; My rides as operating queue; Find rides as discovery |
| People and Circles | Trust model, circle workspace and requests are meaningful | Contacts, requests and circles need clearer task-based separation | People = connections and requests; Circles = group membership and visibility context |
| Calendar | Source-aware projection, day/week/month and adjacent preview exist | Global planning must make source and next action more legible than mode/filter controls | Today/next first, source-aware agenda and detail handoff |
| Chat | Existing canonical threads and shared shell | Conversation discovery, unread attention and empty-start guidance need one consistent mental model | Inbox of conversations, selected thread, compact composer and recovery state |
| VisionForWeb | Backend-owned semantic routing, review gates and inline assistant host exist | It must stay a calm contextual helper, not become a second competing workspace | Contextual entry, explicit review/consequence, canonical route handoff and bounded recovery |
| Profile and account | Stable account routes and setup surfaces | Settings can expose too much account configuration without priority | Personal essentials, privacy, notifications and advanced account controls in intent groups |
| Search and saved search | Backend-owned query and saved intent state | Search concepts are technical when no active need is expressed | Ask for a need, present results, then offer a saved intent only after value is clear |

## Non-goals

- Do not merge domain workflows or move authorization/state transitions into the frontend.
- Do not force two-column layouts onto simple forms or focused reading tasks.
- Do not replace canonical routes, existing backend read models, or product-specific language with generic Business terminology.
- Do not treat the existing shared components as sufficient evidence of a human workflow; each surface needs a route-level review and runtime proof.

## Delivery sequence

1. Harden the shared pattern and audit route ownership.
2. Home, notifications, Work and applications: highest frequency of attention and decisions.
3. Things and Rides: ownership/request and discovery/operations separation.
4. People, Circles and Profile: relationship and personal-settings clarity.
5. Calendar, Chat, Search and VisionForWeb: cross-module context and recovery.
6. Mobile, accessibility, browser runtime proof and documentation closeout.

## Evidence rule

Every child plan must use only bounded source paths, retain backend-provided actions/statuses, run the frontend type-check and production build, and attach a browser trace for changed primary flows. A child may not claim broad module completion from static component reuse alone.

## Execution mapping

The serial inventory maps every user-visible outcome to exactly one child-plan task. Foundation is the only shared-primitive slice; later items are bounded to their named route family and cannot borrow another module's runtime evidence.

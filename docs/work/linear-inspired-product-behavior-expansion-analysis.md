# Linear workspace and Vision adaptive-system convergence analysis

## Decision summary

The product should be a deliberate hybrid, not a single visual imitation:

- The authenticated Web workspace should use the **Linear-inspired operating model**: stable navigation, dense but scannable collections, object-first detail, explicit state, small contextual controls, and fast keyboard continuation.
- `/vision` should use the **Her-like adaptive model**: one quiet conversational room, progressive disclosure, a typed backend-led next step, and compact review rather than a dense management console.
- The two systems meet through explicit handoffs, canonical routes, backend-owned DTOs, and shared identity/action contracts. They must not collapse into one generic command surface.

The verified redesign master establishes the visual and interaction grammar. The behavior-expansion master should now add the minimum missing backend contracts that make that grammar feel like a real product: viewer-owned personal shortcuts, a coherent attention projection, adopted previews, command catalog entries, and safe realtime reconciliation.

## Evidence reviewed

- The current frontend audit reports **43 routes**, **42 concrete route surfaces**, no detached route surface, and existing backend client coverage for the main product domains.
- The stale-surface audit explicitly reports `ObjectPreviewPanel.vue` and `useObjectActions.ts` as currently detached. This is the highest-confidence UX gap because the primitive already exists but has not yet reached a real collection.
- Current screenshots show four relevant Linear patterns: a persistent rail with personal shortcuts, dense list scanning, object detail with a compact property rail and activity stream, and a board/view mode. Only the first three have a safe near-term mapping to TheMuffinMan.
- Existing architecture already provides backend-owned Work list/detail/action DTOs, notification/news reads, activity/attention projections, Chat sync recovery, typed Vision conversations, direct routes, and generated frontend contracts.
- `docs/vision-architecture-patterns.md` and `docs/vision-presentation-contract.yaml` require `/vision` to remain backend-orchestrated, persisted, progressive, and context-sensitive. They explicitly reject a dashboard-shaped or modal-heavy Vision shell.

## Target operating model

| Concern | Workspace: Linear-inspired | Vision: Her-like | Shared rule |
| --- | --- | --- | --- |
| Primary job | Browse, compare, inspect, and execute known product workflows | Express intent, clarify, review, and execute typed plans | Both resolve to the same domain services and authorization rules |
| Navigation | Persistent rail, explicit route, personal shortcuts | Compact return/handoff context only when useful | Canonical routes remain stable |
| Object access | Dense rows, preview, detail, utility rail | Textual entity sketch in a linear conversation | No object data is reconstructed in the client |
| Input | Search, Create, field forms, direct actions | Inline natural-language/voice composer and typed review chips | Input mode is visible; no hidden mode switch |
| State | Selected, unread, active, blocked, loading, empty | Listening, processing, asking, review, complete, blocked | Backend state remains authoritative |
| Motion | Small state-transition feedback and focus cues | Calm ambient signal plus meaningful processing/listening feedback | Reduced motion preserves the information, not the animation |
| Failure | Inline retry, retained route/context, explicit refresh | Inline recovery line and next safe action | Never silently keep stale authority |

## What should not be copied

The screenshots contain features that do not yet map safely to our product:

- **Board drag/drop and arbitrary status columns** need a stable, domain-level ordering and mutation contract. Work, rides, bookings, and lending have different state machines; a generic board would falsely imply that all states are editable in the same way.
- **Arbitrary favorites, subscriptions/bells, and shared saved views** require user ownership, target allow-lists, expiry, privacy, delivery policy, and removal semantics. They cannot be added as local booleans.
- **Agent side-panel output** is not a pattern to clone into Vision. Vision should remain a typed conversation with evidence and review. A narrow utility panel is appropriate only for a known workspace object with a canonical detail route.
- **Fake realtime** (optimistic local status or simulated presence) would be worse than a modest refresh because the product has permissions, visibility boundaries, and multi-user workflows.

## Current-state comparison

### Already aligned with the workspace model

- Persistent authenticated shell with primary module navigation, personal routes, responsive fallback, named icons, search, and create entry points.
- Backend-owned Work membership filtering, sorting, details, actions, application flows, and direct canonical routes.
- Shared dialog, action dialog, card/row, keyboard, density, loading, error, and focus primitives.
- Detail screens that distinguish narrative/main content from a utility/action rail.
- Chat split layout with message sync, reconnect/visibility recovery, attachment handling, reply/edit/delete, and direct conversation routes.
- Notification/activity surfaces with existing viewer-scoped reads and read mutations.
- Vision’s persisted conversation, backend-selected next step, typed slots, review gate, transcript feedback, and route handoff.

### Partially aligned or deliberately unfinished

- Personal shell shortcuts currently cover only Activity and saved searches; there is no viewer-owned pinned-object contract.
- Work state survives a supported route return, but the shared preview primitive is not connected to a collection and no supported object preview has a backend-prepared payload.
- Global search and Create are useful entry points but not yet one typed command catalog with availability/recovery semantics.
- Notifications and activity are adjacent surfaces rather than one consistent inbox projection with an explicit source/state/action vocabulary.
- Chat can recover by request sync but no shared typed event/reconciliation protocol exists for attention plus Chat.
- Vision’s interaction design is substantially more mature than its Web parity in some domain capabilities; the workspace must not compensate by turning Vision behavior into local UI guesses.

## Review of the verified interaction master

### 1. Interaction contract audit — complete and still binding

This was the most important completed plan. It correctly made backend membership, actions, and visibility authoritative and recorded blocked gates. Keep it as the root policy document. The next audit must add concrete ownership decisions for pins, command entries, and attention items, not relax existing gates.

### 2. Shell navigation — complete foundation

The shell now has the right density and hierarchy. Its remaining gap is semantic rather than visual: personal shortcuts are only routes, while the reference behavior includes a bounded personal object layer. That gap belongs in the personal-shortcuts child, not another shell CSS pass.

### 3. Object interaction system — complete primitive, incomplete adoption

`AppCard`, preview behavior, `ObjectPreviewPanel`, and `useObjectActions` establish the correct vocabulary. Audit evidence says the latter two are detached. The new preview-adoption child must either connect them to actual Work/one-other-family flows or remove them. Leaving a speculative primitive in the codebase would undermine the reuse goal.

### 4. Command and Create — correct separation, incomplete catalog

The current shortcuts correctly distinguish Search, Create, and Vision and suppress themselves in editable controls. This is exactly the right boundary. The next stage is not a free-form universal command system: it is a catalog of stable navigation commands plus backend-authorized object actions with existing confirmation rules.

### 5. Collection views — strong foundation, no generic board

Compact rows, filters, density control, and accessible load states match the reference list behavior well. The screenshots’ board is intentionally not a next step because no cross-domain grouping/order/mutation contract exists. Collection enhancement should focus on richer backend-provided metadata, selection/preview, and preserved context.

### 6. Detail/preview panels — layout complete, activity context incomplete

The split main/utility pattern is appropriate. The reference’s strongest additional trait is a compact, object-specific activity narrative. The attention-inbox and preview work may surface existing backend event summaries where safe, but must not invent a generic audit timeline from frontend interactions.

### 7. Performance and state continuity — correct conservative posture

The current Work return state is intentionally scoped to supported query parameters and session-local display state. This is good. Do not introduce cached object authority, prefetch, virtualized lists, or optimistic transitions before data and performance evidence justify them.

### 8. Inbox, Chat, and Vision — correct semantic separation

Chat already has an anchored direct composer and recovery behavior. Notifications have safe read behavior. Vision exposes typed process states. The opportunity is one shared visual vocabulary for selection/loading/recovery while preserving three different meaning models: attention item, message stream, and conversation turn.

### 9. Polish/runtime closeout — complete baseline, must be repeated after behavior changes

Focus, Escape dismissal, reduced motion, and static contracts are now in place. The new runtime closeout must explicitly trace permissions, stale/removed objects, command suppression in inputs/dialogs/Vision, and reconnect. It must stop only agent-started services and never kill a user’s existing `make dev` stack.

## Detailed review of the new behavior-expansion master

### A. Behavior contract audit

**Why it exists:** All remaining high-value Linear-like behaviors need product ownership decisions before implementation. The current audit says “blocked”; this plan turns selected gates into precise, narrow contracts.

**Required output:** For every proposed behavior record object family allow-list, actor ownership, persistence schema, read projection, canonical route, DTO, permission/recheck behavior, stale-object handling, event/recovery policy, and non-goals.

**Risk:** If this step is skipped, the rest of the master will create local convenience state that silently diverges from permissions and visibility.

**Recommendation:** Complete this as a docs/API inventory pass before any migration. It should explicitly decide whether initial pins support only Work quests and business profiles, or another minimal pair. Do not choose every object family.

### B. Personal shortcuts and pinned object navigation

**Linear value:** High. Personal pinning makes the left rail a useful working memory rather than a static sitemap.

**Our adaptation:** A shortcut persists only `{viewer, supported object family, object id, ordered position}`. The server derives label, icon/type, and canonical route from an authorized read. The client may never store arbitrary URLs, copied names, private descriptions, or assumed action permissions.

**Vision relationship:** Vision can suggest a known item or hand off to it, but it should not silently pin it. Pinning is an explicit workspace preference mutation.

**First safe scope:** Work quests and one already-public/read-safe family such as business profiles, subject to the audit. Do not pin Chat messages, temporary Vision turns, or opaque routes.

**Critical tests:** unauthorized after pin, deleted object, visibility changed, duplicate pin, ordering race, pin/unpin failure, keyboard reachability, and narrow-rail behavior.

### C. Attention inbox

**Linear value:** High. The reference Inbox is not merely a notification list; it is an ordered queue of “what needs attention now,” with clear origin and continuation.

**Our adaptation:** Build a viewer-scoped projection that normalizes existing News/Activity and only those domain fan-out sources already authorized for the viewer. Every item needs a source kind, timestamp, unread/read state when applicable, title/summary, canonical route, and typed permitted actions. Existing one-item and mark-all read actions remain the only read mutation baseline.

**Vision relationship:** Vision may summarize or open the same attention items through its current backend capability; it does not create a separate private inbox.

**Critical caution:** Do not equate all activity with notification. A passive history row, resumable Vision task, and actionable lifecycle update need visibly distinct state and action semantics.

### D. Route-backed preview adoption

**Linear value:** High and low-risk. Side preview makes dense lists faster to use without a route transition for every inspection.

**Our adaptation:** On a supported collection, `P` or a named preview affordance opens a panel with a compact authorized read DTO. Enter/click still opens the canonical route. Escape closes preview. Selection and scroll return persist only in the collection context and vanish if the object disappears from the returned backend list.

**Vision relationship:** Vision does not use the panel. Its equivalent is a textual object sketch in the conversation feed, because a side panel would conflict with the adaptive room metaphor.

**Initial scope recommendation:** Work discovery first, then Things detail/listing if the audit confirms it has a simple owner/availability read. Do not begin with Chat, because the split conversation workspace already has a primary focus pane.

### E. Typed command catalog

**Linear value:** Medium-high. Keyboard flow becomes fast only when users can discover destinations and repeat known actions without navigating a hierarchy.

**Our adaptation:** A command entry is typed, named, grouped, shortcut-described, and either:

1. a local stable navigation destination;
2. a server-provided personal shortcut; or
3. an action whose backend eligibility and confirmation path are already known.

The command palette never resolves natural language, makes an action decision, or constructs a domain mutation. Those are Vision’s job.

**Vision relationship:** The palette can include “Open Vision” with an explicit context handoff. Vision’s composer must never be replaced by Ctrl/Cmd+K, and shortcuts remain disabled in the Vision composer, forms, and dialogs.

**Critical caution:** Do not make command search depend on every module endpoint in its first version. Static destinations plus validated pins and a tiny set of direct actions is enough.

### F. Realtime continuity

**Linear value:** Medium. Immediate feedback and current activity make the workspace feel alive, but correctness is more valuable than animation.

**Our adaptation:** Reuse current Chat sync and domain notification fan-out. A typed event only says that a viewer-visible resource or projection may have changed; the client reconciles via an existing authorized read. This yields safe behavior for reconnect, duplicate event, changed visibility, and stale-object removal.

**Vision relationship:** Vision conversations stay persisted and refresh through their existing lifecycle/read endpoints. They should not subscribe to generic workspace events as if they were a live dashboard.

**Critical caution:** The first slice should use explicit refresh hints and measured polling/reconnect behavior, not a new global event bus or optimistic UI system.

### G. Runtime closeout

**Why it exists:** The visible quality difference between a prototype and a desktop product appears under error, permission change, reconnect, keyboard, and narrow layout — not in the happy path.

**Required browser cases:** cold load, empty, failed load/retry, filtered return, preview open/close, direct route after preview, pin authorization change, attention read/update, command while input/dialog/Vision is focused, Chat reconnect, reduced motion, and narrow fallback.

**Operational rule:** Start only the runtime services needed for the test. On shutdown kill only process ids created by that test and check ports. If `make dev` finds an existing user process, leave it untouched and record that external runtime evidence was not created by this slice.

## Recommended implementation sequence

1. Contract audit.
2. Preview adoption, in parallel with the personal-shortcut data contract after the audit decides the allow-list.
3. Attention projection, because it reuses existing backend reads and gives the shell/action system useful state.
4. Personal shortcut client surface after its API and invalidation behavior are proven.
5. Command catalog using stable routes, validated pins, and existing authorized actions.
6. Realtime reconciliation after the attention read model is stable.
7. Runtime closeout and audit cleanup.

This order is deliberately more conservative than copying the reference product’s visual feature order. It closes an existing detached primitive first, creates one trustworthy personal-memory layer, then adds acceleration.

## Success definition

The product will feel more like a calm, fast desktop application when a user can keep orientation, inspect without losing context, resume what needs attention, return to meaningful objects, and use the keyboard without wondering which system has authority. It will remain recognizably TheMuffinMan when Vision continues to feel like an adaptive, evidence-led conversation rather than a duplicate of the workspace.

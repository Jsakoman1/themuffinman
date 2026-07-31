# Human-first Web experience analysis

## Decision

The verified shell, Home hierarchy, Business flows, local tabs, and shared
components are a usable baseline. The remaining work is not another general
redesign. It is a bounded completion program for the places where a non-technical
person still encounters hidden navigation, duplicated concepts, technical labels,
unpredictable object opening, unfamiliar Chat behavior, difficult Calendar
controls, mixed visual themes, or frontend-owned product defaults.

## Readiness result

The first draft was directionally correct but not safe enough to activate. The
hardening pass made four structural corrections:

1. It reuses and extends the existing `ClientActionDTO` instead of introducing a
   competing `ActionDescriptor` abstraction.
2. It treats `docs/work/assets/logo_transparent.png` as reference source and
   places the optimized runtime logo in `frontend/public`, removing the
   documentation folder from the production asset boundary.
3. It splits Chat feedback, Calendar layout/responsiveness, cross-domain action
   rollout, and API decomposition into independently verifiable outcomes.
4. It keeps Activity and Saved-search routes/data as compatibility surfaces while
   removing ordinary navigation promotion; no persistence is deleted.

The resulting queue contains 38 strict serial items. Each item owns one
observable outcome and has one predecessor, exact required paths, leaf
validation, and a bounded evidence type.

## Product model

- Notifications are events that happened. They are not resumable work.
- Needs-your-action items belong on Home and in their owning module.
- Continue-where-you-left-off belongs on Home.
- A normal collection row opens its canonical object on the first activation.
- A split review pane is reserved for repeated operational decisions.
- Chat uses an inbox/thread mental model.
- Calendar preserves Day, Week, and Month while simplifying controls and keeping
  timezone placement authoritative.
- Business setup options and action presentation are backend-prepared so future
  mobile clients do not duplicate product logic.

## Residual findings

1. Desktop hides Personal and Account destinations behind two nested disclosures.
2. Activity overlaps Notifications and Chat; Saved searches stores an intent but
   has no complete Web creation and matching workflow.
3. Attention count and Attention items are assembled from different semantic
   sources. The smallest safe migration is to make the existing Attention
   endpoint a compatibility notification projection over the news stream while
   Activity remains a separate Home-resume source.
4. SurfaceRow exposes click, preview, and open behavior; routes consume those
   events inconsistently.
5. Generic preview panels repeat row content and require a second click for detail.
6. The authenticated sand shell can still mix with system-dark controls and uses
   undeclared semantic tokens.
7. The supplied logo contains large transparent margins; increasing its CSS box
   does not proportionally increase visible identity.
8. Chat backend capability is stronger than its current card-like presentation.
9. Calendar controls expose implementation-oriented filters, and event placement
   uses browser-local Date operations instead of the projection timezone.
10. Business offering defaults, setup steps, enum choices, and action consequence
    copy remain duplicated in Vue, even though Business already proves the useful
    shared backend pattern through `ClientActionDTO`.
11. userShellApi, shellSurfaceData, and SurfaceContentView remain oversized
    cross-domain modules.

## Sequence rationale

Information architecture and notification semantics come first because every
later surface depends on how users enter and leave it. The row contract follows
because Chat, Calendar, and discovery should not inherit a broken preview model.
The visual foundation then stabilizes tokens and identity before large route
redesigns. Chat and Calendar are implemented as specialized interaction systems.
Backend UI metadata follows those high-value surfaces, then behavior-preserving
module decomposition reduces the cost of future Web and mobile work. Static and
fresh browser evidence close the final implementation state.

## Plan-by-plan disposition

| Child plan | Hardened boundary | Main dependency risk |
| --- | --- | --- |
| Navigation and Notifications | Visible IA, compatibility-only Activity/Saved searches, one news-backed notification projection | Shell route registry and generated notification contract must move together |
| Interaction contract | Direct-open rows by default; ReviewPane only for repeated decisions | Return query/filter/scroll state must survive navigation |
| Visual foundation | Closed tokens, frontend-owned optimized logo, shared SVG icons | Explicit dark mode may remain only as a complete independent token scope |
| Chat | Decomposition, inbox, thread, presence/read state, then message actions/recovery | Existing realtime contracts must be reused rather than reimplemented |
| Calendar | Backend source metadata, toolbar, timezone placement, grid behavior, mobile modes, event popover | Browser timezone must never determine projection day placement |
| Backend UI contracts | Business setup DTO and staged extension of `ClientActionDTO` | Allowed-action services remain authoritative for permission |
| Modularization | Three API extraction slices, then data and renderer decomposition | Compatibility facades prevent a big-bang import rewrite |
| Closeout | Full static suite plus fresh desktop/mobile browser evidence | Existing screenshots cannot count as evidence |

## Explicit exclusions

- No literal clone of Instagram, Facebook, iMessage, or the reference images.
- No change to core permission, booking, pricing, quote, or workflow authority.
- No deletion of Saved-search persistence until a separate product decision.
- No parallel action-presentation DTO while `ClientActionDTO` remains suitable.
- No mascot or unrelated illustration generation.
- No whole-application rewrite.

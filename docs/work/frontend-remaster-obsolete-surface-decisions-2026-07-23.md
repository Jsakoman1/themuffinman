# Frontend App Remaster — CSS and Obsolete Surface Decisions

Date: 2026-07-23  
Status: reviewed and implemented for serial closeout

## CSS ownership

`styles/base.css` owns global tokens, reset/accessibility primitives, compatibility aliases, and low-level shared controls. `style.css` owns the remaster-level composition helpers: surface rhythm, state stacks, control transitions, reduced-motion behavior, and responsive section padding. Domain view styles remain local until the same semantic pattern is proven across at least two mapped surfaces.

The current shared additions are intentionally small. The repeated toolbar control-group grammar now lives in the shared `.ui-cluster` and `.ui-cluster--end` helpers and is consumed by `CollectionToolbar`; domain-specific selectors remain module-owned where their layout semantics differ. No broad selector rewrite is justified by the audit alone, and every changed CSS behavior must be checked at desktop, compact tablet, and mobile widths.

## Stale candidate disposition

| Candidate | Audit result | Decision | Reason / next owner |
| --- | --- | --- | --- |
| `ActivityRail.vue` | likely unused, no importers | Retain temporarily; mark dormant | Activity is a mapped personal-context capability. Remove only after a route/registry audit proves no planned shell reactivation depends on it. Owner: personal-context plan. |
| `AppActionMenu.vue` | likely unused, no importers | Retain temporarily; do not add new consumer | Existing action hierarchy is now provided by mapped surface controls. Revisit after final action audit. Owner: shell cleanup. |
| `GlobalVisionEntry.vue` | likely unused, no importers | Retain detached and review | Do not reintroduce a global Vision entry. VisionForWeb remains inline and the detached terminal remains unreachable from Web UI. Owner: Vision boundary audit. |
| `QuickSwitcher.vue` | likely unused, no importers | Retain temporarily; no route authority | Search/command center uses mapped `GlobalSearchEntry` and existing command catalog. Revisit after personal-context closeout. |
| `WorkspaceContextNav.vue` | likely unused, no importers | Retain temporarily; no duplicate navigation | Shared shell rail/context surfaces are the active grammar. Revisit after final route inventory. |
| `WorkspaceKeyboardHelp.vue` | likely unused, no importers | Retain temporarily; document as optional utility | Keyboard help remains useful only if the active shell exposes a real shortcut contract. Do not invent shortcuts. |
| `VisionSurfaceModernView.vue` | route detached | Keep detached | This is the standalone Vision console boundary and must not be linked from Web UI. |
| `styles/base.css` | review needed | Keep as global token/reset owner | The file is active through `style.css` import; the audit’s importer heuristic does not represent CSS import ownership. |

## Closeout checks

- The stale audit is evidence for review, not permission to delete by filename.
- No candidate is reconnected merely to eliminate an audit warning.
- Shared CSS changes preserve focus rings, forced colors, reduced motion, responsive stacking, and current route/component ownership.
- Final runtime closeout must explicitly test that `/vision` Web behavior uses the inline host contract and that no shell/menu link reaches the detached terminal.

## Serial implementation result

- CSS ownership is unchanged: `styles/base.css` remains the token/reset owner and `style.css` remains the remaster composition owner.
- The shared toolbar control-group pattern was consolidated without changing route or action ownership.
- All six likely-unused components remain retained and explicitly dormant; none was deleted or reconnected without a product decision.
- `VisionSurfaceModernView.vue` remains detached, while authenticated Web continues to use VisionForWeb inline.
- Shared focus ownership is now explicit: `styles/base.css` owns `.ui-focusable`, consumed by both `AppButton` and `AppIconButton`; domain controls keep their local layout semantics.

## Recovery closeout evidence — 2026-07-24

The recovery implementation was re-run after the partial-implementation audit. `AppButton` and `AppIconButton` now consume the shared focus helper, while the six dormant candidates remain retained and unlinked. Playwright foundation evidence confirms the authenticated shell, responsive collection toolbar, reduced-motion mode, and `/vision` redirect into the inline VisionForWeb host with no detached terminal surface.

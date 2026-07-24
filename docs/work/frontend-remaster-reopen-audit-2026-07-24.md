# Frontend App Remaster — Reopen Audit

Date: 2026-07-24  
Status: master reopened; prior implementation verification invalidated

## Why the previous closeout was insufficient

The prior run proved that the repository could type-check, build, pass static frontend audits, load protected routes with a seeded account, and produce screenshot files. It did not prove that the 12 child scopes were implemented. The strict verifier accepted a changed `required_path` and runtime artifact as sufficient evidence even when the child’s domain views, interactions, or visual acceptance criteria had not changed.

The prior child evidence must therefore be retained as baseline/diagnostic evidence, not completion evidence. It is especially weak where the runtime result was an unauthenticated redirect, or where the final route matrix only proved route reachability and absence of page errors.

## Current disposition

| Child | Disposition |
| --- | --- |
| 01 System Map reconciliation | Verified analysis baseline remains valid. |
| 02 UX architecture decisions | Verified decision record remains valid. |
| 03 Shared foundation | Reopened: shared primitives need real consumers and visual adoption. |
| 11 Formatting/state | Reopened: residual local formatters and state vocabulary remain. |
| 12 Identity/onboarding | Reopened: only login pending feedback changed. |
| 04 Work | Reopened: authenticated discovery/detail/create/action journey not implemented/proven. |
| 05 Chat | Reopened: workspace/composer/realtime/recovery implementation not proven. |
| 09 Personal context | Reopened: Home hint is not a command-center remaster. |
| 10 Profile/location | Reopened: large settings form remains complex and unproven. |
| 06 Module sweep | Reopened: module-specific modernization and actions remain incomplete. |
| 07 CSS/stale surfaces | Reopened: candidates were documented, not resolved; CSS cleanup is incomplete. |
| 08 Runtime closeout | Reopened: visual review found clipped mobile navigation and interaction coverage is incomplete. |

## New completion gates

Every reopened implementation child must satisfy all of these, in addition to its existing plan acceptance:

1. At least one meaningful source implementation change must occur in each named primary surface family; changing only an ARIA attribute, evidence file, or plan file is insufficient.
2. The changed behavior must be exercised with an authenticated seeded Chromium journey. A redirect to `/login` is boundary evidence only and cannot close an authenticated child.
3. The journey must include a user-visible interaction appropriate to the child: navigation, selection, inline edit, dialog/sheet, search/filter, create/edit/action, recovery/retry, or VisionForWeb handoff.
4. Runtime evidence must record the exact route, action sequence, final state, page errors, failed API responses, viewport, and whether the interaction was SPA/no-reload.
5. Visual evidence must be inspected, not merely generated. It must explicitly record overflow, clipping, hierarchy, action reachability, focus visibility, reduced-motion behavior, and responsive layout at the child’s required widths.
6. Accessibility evidence must include keyboard traversal/focus restoration for changed dialogs/sheets or controls, semantic names/roles, and a contrast/touch-target review. Static audits alone do not count.
7. A child cannot claim completion while its own acceptance table still contains “requires seeded runtime,” “temporary retain,” “boundary only,” or equivalent unresolved language.
8. Final closeout must validate the whole route matrix plus at least one real interaction per remastered module family. Route loading without interaction is not enough.

## Known findings carried forward

- The mobile closeout screenshot shows clipped/duplicated bottom-navigation labels such as `Home Home` and `Work Work`.
- The desktop Home surface has excessive unused vertical space and still needs stronger application-shell density/hierarchy.
- Six stale components remain only documented as temporary candidates; none has been safely removed or reactivated.
- Local formatter residuals remain in Chat, Notifications, Business, Work, and calendar surfaces.
- The detached `/vision` redirect boundary is correct, but inline VisionForWeb interaction must still be tested from a real authenticated surface.

## Restart rule

The next executable slice is child 03. Start it with `make work-start` only after the plan status and inventory status are pending as recorded here. Do not mark the master verified again until the reopened inventory is sequentially verified and this audit’s gates are evidenced.


# Frontend remaster — partial implementation recovery

Date: 2026-07-24  
Status: active remediation

## Finding

The previous closeout proved that required files changed and that static/runtime checks passed, but it did not prove that each implementation child delivered the planned UX scope. Several children therefore passed the control verifier with only a small markup, CSS, formatter, or status-state change.

The master is reopened. Plans 01 and 02 remain valid analysis/decision slices, and plan 03 remains a valid shared-foundation implementation. Plans 04, 05, 06, 07, 09, 10, 11, 12, and 08 are reopened for implementation recovery and final re-verification.

## New anti-partial rule

An implementation child cannot be marked verified from a changed required path alone. It must provide:

1. A before/after implementation record naming the concrete UI behavior changed on every in-scope surface.
2. At least two production source surfaces changed for a module plan, unless the plan explicitly proves why one shared source is sufficient.
3. A browser scenario for every named journey and action family in the plan, including the visible result or recovery state.
4. Desktop and mobile screenshots of the changed surface, with visual review recorded in the child plan.
5. Evidence that the change is visible in the rendered DOM or computed layout, not only present in source.
6. A deferred-scope list for every planned item not implemented; deferred items keep the child open rather than allowing a partial closeout.

## Recovery scope

| Child | Required recovery focus |
| --- | --- |
| 04 Work | Implement discovery/context/detail, creation, application review, and action-feedback grammar across Work surfaces. |
| 05 Chat | Implement two-pane continuity, composer, attachment/reply/edit/reaction feedback, reconnect/recovery, and mobile transition behavior. |
| 06 Modules | Implement representative Circles/People, Things, Rides, and Business surface migrations, not only Things loading. |
| 07 CSS | Resolve stale ownership decisions and consolidate repeated shared patterns with measured adoption; document every retained exception. |
| 09 Personal | Implement Home, Attention, Activity, Search, Saved Search, and Create command-center continuity. |
| 10 Profile | Implement guided profile/settings information architecture, visibility/consent explanation, location recovery, and mobile editing. |
| 11 State | Finish the full formatter/state matrix and migrate all residual local formatter/state vocabulary drift. |
| 12 Identity | Implement the shared identity form grammar across login, register, recovery, reset, and onboarding with real recovery journeys. |
| 08 Closeout | Re-run only after the recovered children pass; test each named journey and inspect all changed visuals. |

## Closeout rule

The master is not complete when the YAML verifier is green if a child’s implementation record, browser journey matrix, or deferred-scope list shows incomplete planned work. “Verified” must mean implemented, rendered, interacted with, visually inspected, and fully scoped.

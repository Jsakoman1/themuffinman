# App-wide friendly design preflight

Date: 2026-07-29  
Baseline: `a18b811e65001f68d174b42d1574f6c0f2ffc18d`

## Scope classification

- Baseline-only: verified Home launcher/follow-up work, stable shell navigation,
  Business owner/public booking workflows, existing backend permissions and routes,
  and verified human-first functional behaviour.
- New: friendly collection grammar; Work, Things, Rides, People, Circles, Profile,
  Activity, notifications, Calendar, Chat, Business overview, Service discovery, and
  remaining detail/create visual alignment; fresh responsive evidence.
- Explicit boundary: responsive Web is the mobile client in this repository. This plan
  prepares it for future native clients through backend-owned contracts; it does not
  claim a separate iOS build.

## Architecture decision

The program is presentation-first. It reuses existing typed read models, canonical
routes, permitted actions, and mutations. Shared Vue components may standardize
orientation, card hierarchy, and responsive composition, but cannot derive workflow
state, availability, permission, price, or urgency locally.

Desktop retains list/detail, Calendar grid, and Chat split-pane where comparison helps.
Mobile deliberately collapses those into one task surface at a time, preserving an
explicit path back to the list or inbox. Semantic pastel roles communicate a chosen
purpose only; they do not replace established status colours.

## Atomic-review readiness

The execution inventory contains thirteen serial atoms: plan hardening, a shared
primitive, eight bounded route-family repairs, one detail-shell alignment, two visual
audits, and closeout. Each implementation task has bounded required paths, a leaf
frontend validation command, dependencies, and a separate evidence boundary.

## Activation conditions

1. Control-source, atomic-hardening, coverage, and recursion audits must pass.
2. Only the hardening task may start first.
3. Every later task starts and verifies singly through the shared execution inventory.
4. Runtime capture uses the owned `make dev` stack and ends with `make dev-stop`.

## Completed checks

- Control-source validation: passed (173 YAML files).
- Atomic-task hardening audit: passed (13 inventory atoms).
- Plan coverage audit: passed (no open inventory plans).
- Work-plan recursion audit: passed (no recursive validation commands).

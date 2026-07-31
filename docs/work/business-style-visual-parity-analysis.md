# Business-style visual parity analysis

## Decision

Use Business and Home as the presentation reference, not as literal templates. Every
authenticated screen should share the same hierarchy: stable branded shell, local tabs
only for sibling tasks, a plain-language orientation header, one clear next action,
and neutral factual work surfaces.

## Current gaps

1. `FriendlyCollectionHeader` is used by Business, People, Circles, Activity, and
   discovery screens, while Calendar, People discovery, and many detail routes still
   use the separate `SurfaceHeader` grammar.
2. Several collection routes have matching local tabs but different header spacing,
   action placement, preview boundaries, and mobile collapse behavior.
3. Detail/create routes use a mix of legacy headers, `DetailSurface`, and direct
   local CSS; their task context is less visible than the Business owner workspace.
4. The warm brand shell is shared, but it needs fresh cross-route visual proof rather
   than assuming the Home screenshot proves dense and narrow task routes.

## Rules for the repair

- Do not turn factual statuses, prices, permissions, or lifecycle state into brand
  colours.
- Do not add tabs to a page unless it has genuine sibling destinations.
- Preserve backend-owned route, action, booking, visibility, and workflow behavior.
- Use warm colour only for shell identity and destination/task context; lists, forms,
  previews, and detail information remain quiet and readable.

## Scope groups

| Group | Screens | Repair |
| --- | --- | --- |
| Shared grammar | `SurfaceHeader`, `FriendlyCollectionHeader`, `FriendlySummaryCard`, `TaskSurface` | one compatible header/action/spacing language |
| Collections and workspaces | Work, Things, Rides, Services, People, Calendar, Chat | Business-style orientation, local context, predictable preview boundary |
| Details and creation | Work, Thing, Ride, Person, applications, profile, service setup | consistent contextual header and progressive disclosure |
| Proof | representative Home, Business, Work, Things, Calendar, Chat, Profile desktop/mobile | browser screenshots and overflow/error check |

## Explicit exclusions

No backend model, permission, API, booking, pricing, availability, or navigation
contract changes are part of this visual parity program.

## Readiness decision

The planned presentation work is safe to execute as a serial frontend-only program:
every route family has one bounded task, and the final browser checks cover the only
new runtime proof required by this scope.

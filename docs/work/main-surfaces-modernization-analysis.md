# Main surfaces modernization analysis

## Confirmed findings

- Home duplicates shell navigation through quick actions and renders informational rows as if they were actionable.
- `ShellSurfaceMetric` and several `ShellSurfaceRow` instances have no destination, so visible affordances do not respond.
- `SurfaceContentView` accepts `note` but does not render it, which hides useful fallback and ownership explanations.
- Business landing composes several safe requests and can become visually empty when owner read models are missing or partially unavailable.
- Find Work calls `/quests/search` without the backend `AVAILABLE` preset, so it can include the authenticated user's own quests.
- Generic rendering branches for Home, Work, Business, Chat, and Calendar are too dense to communicate each surface's interaction model.

## Design direction

Sidebar navigation owns destinations. Home owns orientation. Work owns discovery and owned-work management. Business owns owner setup and operations. Shared primitives own interaction feedback; backend DTOs own permissions, visibility, statuses, and allowed actions.

## Implementation order

1. Correct Home, Business, and Work data contracts.
2. Standardize clickability and action hierarchy.
3. Extract explicit surface archetypes.
4. Verify browser flows and documentation.

# Discovery information-architecture analysis

## Findings

- The product has route families for Work, People, Business, Things, and Rides, but users can still encounter generic module labels instead of intent-led entries.
- Backend presets already distinguish external work (`AVAILABLE`) from owned work (`MY_VISIBLE`); the same separation should guide other modules.
- Empty, forbidden, and partial states are as important as route existence because a discoverable dead-end is still a broken flow.

## Design decisions

- Use explicit labels: Find Work, Offer Work, Find People, Find Business, Find Rides, Things to borrow.
- Keep manage surfaces separate: My quests, My businesses, My rides, My things, and My bookings.
- Define a single card contract: title/preview opens the detail surface; one primary action is shown only when backend allows it.

## Risks and mitigations

- Too many sidebar entries create the same crowding problem: group by Find, Create/Offer, and Manage with responsive disclosure.
- A label change can break smoke selectors and user expectations: keep route aliases and update machine-readable runtime scenarios.
- Frontend filters must not drift from backend visibility: all discovery queries use named backend presets or DTO contracts.

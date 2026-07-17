# Commute ride matching analysis

## Findings

- Rides already have lifecycle, circle, capacity, and visibility contracts that must remain the final authority.
- Existing location settings distinguish approximate/exact visibility; commute matching must never require revealing exact addresses to another user.
- Recurring commute matching is a recommendation system, not an automatic ride booking or automatic location-sharing feature.

## Contract proposal

- Store opt-in commute preference with coarse home/work areas, recurring weekday/time windows, role preference, flexibility, and revocable consent versions.
- Match only users satisfying circle/visibility/block/safety constraints and return bounded suggestion DTOs with explainable compatibility reasons.
- Keep suggestions ephemeral or retention-limited; do not retain raw matching inputs in notification payloads.
- Require explicit review and confirmation before creating a ride offer or join request.

## Risks and mitigations

- Location and work schedule are sensitive: coarse geospatial cells, encrypted/restricted storage, strict viewer DTOs, and audit events are required.
- Matching can imply employment or routine: use approximate time/location language and user-controlled pause/revoke.
- Suggestions can become noisy: bounded result count, deduplication, cooldown, and explicit feedback should be backend-configured.

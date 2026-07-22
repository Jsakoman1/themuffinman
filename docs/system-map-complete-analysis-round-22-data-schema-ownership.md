# Round 22: Data Schema and Persistence Ownership

Status: source-trace analysis. Reviewed: 2026-07-22.

## Conclusion

Persistence remains inside the modular monolith. Flyway migrations V1–V76 establish
the schema history; 49 repositories operate over domain-owned entity aggregates.
`AppUser` is the principal shared identity reference. Cross-module relations should
be treated as explicit dependencies, not a reason to move business rules into a
shared repository or frontend code.

## Ownership map

| Owner | Core persisted responsibilities | Cross-domain dependencies |
|---|---|---|
| Identity | user, auth revocation, profile, preferences, recovery | identity is referenced by nearly every actor-owned record |
| Workmarket | quest, application, news, review | identity, circles, location, notifications |
| Business | profiles, offerings, availability, booking, audit, gallery | identity, storage, notifications |
| Chat | conversations, participants, state, messages, reactions, attachments, audit | identity, social relationship eligibility, storage |
| Social | circle groups, memberships, requests | identity; governs chat and visibility eligibility |
| Sharing/Rides | listings, borrow requests, offers, participants, commute/audit | identity, circles/location visibility |
| Vision | conversations, turns, memory, preference, feedback | identity and delegated domain execution |
| Cross-module support | activity dismissal, notification preference, saved search, native handoff, safety report, location events | identity and owning domain targets |

## Persistence controls

- Schema changes require new Flyway migrations; existing migrations are historical
  evidence and must not be edited.
- Repositories are domain-owned query boundaries. The fetch audit identifies 14
  high-risk and 24 medium-risk methods as review candidates, not measured defects.
- Booking, rides, chat uploads, and Vision state contain explicit audit, version,
  request-id, expiry, or lifecycle records in selected paths. Their semantics are
  evaluated in later workflow and side-effect rounds.

## Boundary

This registry maps declared entities, repositories, and migration ownership. It does
not validate a live database schema, migration application history, row counts, or
production storage behavior.

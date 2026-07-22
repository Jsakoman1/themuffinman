# System Map Deepening — Round 8: Data Ownership and Domain Dependencies

Date: 2026-07-22  
Status: **observed analysis**  
Scope: backend entities, tables, repositories, services, events, migrations, and cross-module dependencies

## Executive finding

The application is package-organized by domain, but persistence ownership is
cross-cutting. `identity.AppUser` is the primary shared identity anchor; nearly
every product domain stores a direct foreign key to it. `social.CircleGroup` is
the main access-scope anchor for circle-based visibility and participation.
`workmarket.Quest` is a high-centrality product aggregate because reviews,
applications, news, location lookup, chat context, and Vision execution all
reference or interpret it.

The resulting architecture is a modular monolith with strong local ownership
inside most domains and several intentional shared-kernel dependencies. The
largest hidden coupling is not the database itself; it is the service layer,
especially Vision, which imports and orchestrates almost every product domain.

## Observed ownership model

| Domain | Primary owned aggregates/tables | Main external references | Ownership assessment |
|---|---|---|---|
| Identity | `app_user`, profile visibility, gallery, preferences, onboarding, auth recovery, revoked tokens, shortcuts | referenced by all user-owned product records | canonical owner of account identity and user-scoped settings |
| Social | `circle_group`, `circle_membership`, `circle_request` | `app_user`; visibility join tables; Quest/Ride audiences | canonical owner of circles, membership, and circle relationship state |
| Workmarket | `quest`, `quest_application`, `quest_news_item`, `user_review`, `quest_image` | users, circles, location, chat, Vision | canonical owner of work/quest lifecycle and application workflow |
| Business | profile, offering, availability, booking, policy, favorite, gallery, booking audit | users; booking-specific Vision adapters | locally cohesive booking/product domain with user ownership |
| Things | `thing_listing`, `thing_borrow_request` | users; Vision adapters | listing owns the borrow relationship; user owns actor identity |
| Rides | `ride_offer`, `ride_participant`, preferences, audit | users, circles | ride owns lifecycle and participants; circles constrain audience |
| Chat | conversations, participants, member state, messages, reactions, presence, attachments, audit | users; Quest context; activity and Vision | chat owns message/conversation state but consumes product context |
| Vision | conversation, turns, memory/preference/feedback | users and most product aggregates through services/adapters | owns assistant state and orchestration, not product-domain truth |
| Supporting domains | activity, notification, location, search, trust, storage, native handoff | users plus selected product domains | cross-cutting support capabilities with different persistence roles |

The table/entity counts observed in the source tree are: identity 8, social 3,
workmarket 4, business 9, chat 8, vision 5, rides 4, things 2, and one each in
activity, location, native handoff, notification, search, and trust. These are
source counts of classes annotated with `@Entity`, not a claim that they are the
complete database object count.

## Data ownership rules found in code

### Identity is the shared root, not a generic dumping ground

`AppUser` owns authentication identity and profile-level data, including profile
description/avatar visibility, while separate one-to-one or one-to-many records
hold appearance, onboarding, gallery, shortcuts, recovery, token revocation,
notification, and commute preferences. Product domains reference `AppUser` by
foreign key rather than duplicating account records.

This is a sound ownership split, but `AppUser` also has direct many-to-many
visibility relations to circles and users. That makes profile visibility a
cross-domain boundary: identity owns the policy-bearing profile field, while
social owns the referenced circle membership universe.

### Social owns access scopes, but consumers enforce behavior

`CircleGroup`, `CircleMembership`, and `CircleRequest` form the social relation
model. Quest and ride entities keep circle audience relations, and profile
visibility uses dedicated join tables. The database therefore preserves the
scope references, but the effective authorization decision is distributed across
identity, social, workmarket, rides, and chat services.

This means “circle ownership” and “circle visibility” are not the same fact:
social owns membership and relationship state; each consuming domain owns the
decision whether its resource may be shown or acted on for a viewer.

### Product aggregates own lifecycle state

Workmarket owns Quest and QuestApplication state transitions. Business owns
booking state, policy, availability, and audit history. Things owns listing and
borrow-request state. Rides owns offer, participant, and ride audit state. These
domains use explicit workflow services and enums rather than putting lifecycle
rules in controllers.

The database reinforces this with foreign keys and unique constraints. Examples:

- booking requires profile, offering, and customer user;
- an offering and availability rule belong to a business profile;
- a borrow request belongs to one listing and one borrower;
- a ride participant is unique per ride/passenger;
- a Quest application is unique per quest/applicant;
- audit rows are children of their owning lifecycle aggregate.

### Chat and Vision are context consumers with their own state

Chat persists conversations, participants, messages, reactions, presence,
attachments, and audit records. Some conversations have product context, and
chat services import Quest/application concepts. This is a context dependency,
not ownership transfer: chat should not become the source of truth for Quest
state.

Vision persists conversations, turns, memory, preferences, and feedback. Its
execution adapters call domain services such as booking, Quest, borrow, circle,
profile, and chat services. Vision is therefore an orchestration layer over
domain-owned state. It must not directly become the owner of those aggregates.

## Dependency graph

The practical dependency graph is:

```text
                 identity / AppUser
                /    |       |      \
               /     |       |       \
          social   work   business  chat
             |       |        |       |
        circles  quests   bookings  messages
             |       |        |       |
          rides   reviews  audits  activity
                \    |      /       /
                 \   |     /       /
                     Vision
              (resolution + orchestration)
```

The diagram is conceptual: Vision is not downstream in a data ownership sense;
it is a high-fan-in application service layer. The direct source evidence is the
large set of Vision service imports of workmarket, business, social, things,
rides, chat, identity, activity, and notification types.

## Coupling hotspots

1. **Vision service fan-in.** Vision has over one hundred service classes in the
   package scan and many execution adapters are named after concrete product
   operations. This gives a clear orchestration surface but makes Vision the
   most sensitive place for domain contract drift.
2. **AppUser fan-out.** Every product area carries user foreign keys for owners,
   actors, participants, viewers, or customers. Identity schema changes have a
   broad migration and test blast radius.
3. **Circle visibility joins.** Profile, Quest, Ride, and likely chat presentation
   each interpret circle access. A new visibility rule must be audited across all
   consumers, not only the social package.
4. **Quest context reuse.** Workmarket concepts are consumed by activity, chat,
   location, notification, identity review surfaces, and Vision. Quest lifecycle
   changes therefore have non-local read and notification consequences.
5. **Snapshot fields.** Business booking stores presentation snapshots such as
   offering title. This is an intentional historical-read compromise, but it
   creates two representations that must be distinguished from current offering
   truth.
6. **Event-based side effects.** Business booking events and domain event support
   decouple some notifications/audit behavior, but the event graph needs a
   separate inventory of publishers, handlers, transaction boundaries, and
   failure behavior in Round 10.

## Boundary quality assessment

### Strong boundaries

- Entity, repository, service, DTO, mapper, and controller packages are generally
  grouped by domain.
- Product lifecycle rules are primarily expressed in backend services/use cases.
- Foreign keys and unique constraints encode important ownership and cardinality
  rules in the database.
- Audit entities exist for several high-value workflows instead of relying only
  on mutable current state.

### Soft or porous boundaries

- Cross-domain imports are common in services and especially concentrated in
  Vision.
- Shared user and circle references make authorization behavior distributed.
- `QuestNewsItem` uses IDs and snapshot values rather than a direct aggregate
  relation, which is useful for feed durability but weakens navigable ownership
  evidence.
- Supporting domains such as activity, notification, location, and search read
  product concepts without always owning them, so their dependency direction
  must be kept explicit.

## Evidence gaps carried forward

- No single generated ownership registry currently maps every entity to its table,
  owning domain, foreign-key parents, repository, and primary service.
- The static import graph is not yet normalized into domain-to-domain edge counts.
- Event publishers and handlers are not fully mapped with transaction/failure
  semantics.
- Runtime database schema evidence was not used in this docs-only round; the
  migration and JPA relationship mapping are the current source evidence.
- Cross-domain authorization decisions need a dedicated security/privacy pass in
  Round 11.

## Recommendations for the next rounds

1. Round 9 should map endpoint ownership to this data graph and flag endpoints
   where controller package, service owner, and persistence owner differ.
2. Round 10 should trace domain events, scheduled jobs, storage providers, and
   failure recovery across these ownership boundaries.
3. Round 11 should build a matrix for user, circle, exact-location, conversation,
   and product visibility decisions.
4. Round 12 should make the ownership graph mechanically checkable or at least
   maintain a canonical evidence registry.

## Source evidence

- `apps/themuffinman/src/main/java/com/themuffinman/app/**/model/*.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/**/repository/*.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/**/service/*.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/**/event/*.java`
- `apps/themuffinman/src/main/resources/db/migration/*.sql`
- `docs/domain-technical.md`
- `docs/workflow-state-machines.yaml`

## Conclusion

Round 8 confirms a modular monolith whose domain packages are meaningful, but
whose true architecture is defined by shared identity, circle access, Quest
context, and Vision orchestration. Future changes should be evaluated against
those four dependency axes. The round is ready for strict verification.

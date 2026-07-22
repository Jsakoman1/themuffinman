# System Map Third Pass — Round 16: Read Models, Query Shapes, and Performance

Date: 2026-07-22  
Status: **observed analysis**

## Executive finding

The backend has a deliberate read-model architecture: read services are commonly
`@Transactional(readOnly = true)`, repositories use explicit fetch joins and
projections, and DTO assemblers prepare viewer-specific responses. Pagination is
present in high-volume surfaces such as Quest applications, Quest search, news,
reviews, circles, and chat messages.

The main performance risk is not an absence of query discipline. It is the
combination of large fetch graphs, collection joins, cross-domain read assembly,
and high-fan-in dashboard/Vision surfaces. The repository audit identifies 14
high-risk and 24 medium-risk fetch methods; these are review candidates, not proof
of runtime regressions.

## Read pipeline

```text
controller
  -> read service (@Transactional(readOnly = true))
  -> repository query/fetch plan
  -> domain policy + viewer context
  -> assembler/mapper
  -> backend-prepared DTO/section/page response
```

This pattern protects lazy relations and centralizes client-facing semantics. It
also means query cost can be distributed across repository fetches, service
assembly, and nested helper calls rather than visible in one controller method.

## Observed query strategies

### Fetch joins

Workmarket Quest list/detail queries fetch creator, images, and visible circles.
Quest application queries fetch Quest, creator, and applicant. Business offering,
availability, profile, booking, and gallery repositories fetch the ownership
chain needed for authorization and presentation. Things, rides, and circles use
similar targeted fetch joins.

This reduces lazy-loading failures and avoids obvious N+1 paths, but collection
fetch joins can multiply rows and require `distinct` or careful pagination.

### Native/geospatial queries

Workmarket uses a native PostGIS distance query to first obtain nearby Quest IDs,
then resolves detailed entities. This two-step pattern can reduce the cost of
fetching full graphs for out-of-radius records, but it must preserve ordering and
pagination semantics.

### Pagination and page windows

Shared page factories and page-window utilities normalize page, size, total, and
item metadata. Quest search, application dashboards, news, reviews, circle
collections, and chat messages use bounded reads or `Pageable`/page-like shapes.

### Read projections

Business owner schedule/calendar, dashboard, booking presentations, workspace
navigation, activity, and Vision response assemblers are explicit read models.
They often combine multiple domain sources into one client response, which is
useful for thin clients but creates a query fan-out hotspot.

## High-risk query categories

The repository fetch audit identifies recurring categories:

1. Business booking detail queries fetching offering, profile, customer, and
   owner.
2. Chat conversation detail queries fetching participants, owner, creator, last
   sender, and related state.
3. Chat message/attachment lookups with authorization-sensitive joins.
4. Business offering and availability reads joining profile and owner.
5. Workmarket application lists joining Quest, creator, and applicant.
6. Social circle/request reads joining owners, members, requester, recipient, and
   blocked-by actor.

These graphs are justified by read-model needs, but should be measured for row
explosion, memory use, and count-query behavior under realistic data volumes.

## Module observations

### Workmarket

Workmarket separates Quest list/detail/preview/application/dashboard reads and
uses dedicated response factories. Search can combine text, status, audience,
dates, schedule, radius, and viewer context. The main risk is the number of
filters and related application/visibility reads assembled for one result set.

### Business

Business has separate read services for bookings, owner schedule, calendar,
dashboard, public pages, availability, and previews. This is semantically strong,
but owner dashboards and calendars can aggregate overlapping booking, offering,
profile, policy, and availability data. Shared presentation services reduce drift
but need query-count/runtime measurement.

### Chat

Chat intentionally uses bounded page sizes, sync windows, and separate workspace,
conversation, message, attachment, and admin support reads. The detailed
conversation fetch graph is high-risk because it is both realtime-adjacent and
authorization-sensitive.

### Social and identity

Social relationship/circle reads use multiple fetch joins because viewer-specific
membership and relation state are needed. Identity profile reads combine user
fields, visibility, gallery, location, review summaries, and navigation/action
metadata. These are likely hot read surfaces and should be profiled as complete
use cases, not repository methods alone.

### Vision

Vision read models combine conversation state, turns, semantic plan, candidate
context, previews, action contracts, and learning/memory signals. The query cost
may be acceptable for a single conversation, but the orchestration fan-in creates
latency and failure propagation risk.

## Performance control strengths

- explicit read-only transaction annotations are widespread;
- repository fetch methods document intended relation graphs;
- page/window helpers prevent unbounded client responses in key surfaces;
- mapper/read-service audits exist;
- backend DTO assembly prevents clients from triggering arbitrary lazy traversal;
- repository fetch audit identifies review candidates automatically.

## Gaps and risks

1. Static fetch-risk audits do not measure SQL count, duration, row count, memory,
   or production cardinality.
2. Collection fetch joins and `distinct` may produce expensive intermediate
   result sets.
3. Cross-domain dashboards may issue several sequential repository calls with no
   explicit latency budget.
4. Some read-like services have class-level or absent read-only transaction
   evidence in the audit output and deserve targeted review.
5. There is no canonical per-surface query budget or performance evidence artifact.
6. Cache strategy is not a universal read-model policy; most evidence is direct
   database reads.

## Recommended next actions

1. Add query-count and timing traces to representative local runtime scenarios.
2. Profile Quest search/detail, Business owner calendar, Chat conversation sync,
   Social circle overview, and Vision conversation load.
3. Record expected cardinalities for collection fetch joins and pagination.
4. Introduce per-surface latency/query budgets before adding broad caching.
5. Audit the nine read-like surfaces identified without explicit method-level
   read-only transaction evidence.
6. Keep read-model assembly centralized while moving expensive cross-domain work
   behind explicit services or projections.

## Source evidence

- `scripts/audits/audit-repository-fetch.rb`
- `scripts/audits/audit-mapper-usage.rb`
- `scripts/audits/audit-read-surface-inventory.rb`
- `apps/themuffinman/src/main/java/com/themuffinman/app/**/repository/*.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/**/service/*ReadService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/common/pagination/*`
- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/WorkmarketQuestReadService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/business/service/*ReadService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/chat/service/ChatSyncService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionConversationQueryService.java`

## Conclusion

Round 16 confirms a mature read-model direction with explicit fetch plans and
bounded surfaces. The next maturity step is empirical query/latency evidence,
especially for high-centrality dashboard, chat, social, Business calendar, and
Vision reads.

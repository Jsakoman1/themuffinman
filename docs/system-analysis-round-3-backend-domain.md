# System Analysis Round 3 — Backend and Domain Architecture

Status: completed analysis snapshot, 2026-07-22. This round examines the current modular-monolith backend, package coupling, transaction/read boundaries, workflow ownership, and cross-module orchestration. It is an architectural analysis, not a refactoring proposal.

## 1. Executive finding

The backend is a domain-oriented modular monolith with a recognizable layered shape, but not a set of isolated bounded contexts. The strongest boundaries are responsibility and service naming; the weakest boundaries are package-level dependency direction and cross-module read orchestration.

```text
HTTP/WebSocket controllers
          ↓
domain services / use cases / read services
          ↓
repositories + JPA entities
          ↓
PostgreSQL/Flyway schema

cross-cutting authority: identity, common concepts, circles/visibility,
location, notification, activity, Vision semantic orchestration
```

The code has deliberately moved toward separate read services and mutation/use-case services. This is visible in classes such as `WorkmarketQuestReadService`, `WorkmarketQuestApplicationReadService`, `BusinessBookingReadService`, `BusinessOwnerScheduleReadService`, `ActivityReadService`, and `ThingPreviewReadService`, alongside explicit mutation classes such as `BusinessCreateBookingUseCase` and `WorkmarketQuestStateTransitionService`.

However, package imports show that domain packages are not independent: Vision imports most user-facing domains; Work imports identity, location, circles, notifications, and chat; Identity imports Work, Social, and Location for profile summaries and visibility; Activity imports Chat, Things, Vision, Work, and Notification. This is appropriate for a shared product platform, but it means the architecture is a coordinated modular monolith rather than a candidate for naive service extraction.

## 2. Backend shape and scale

The current Java source inventory is approximately:

| Area | Files |
|---|---:|
| Controllers | 38 |
| Services | 124 |
| Repositories | 49 |
| DTO classes | 273 |
| Mapper/`Mgr` classes | 16 |
| Flyway migrations | 76 |

Largest domain packages by Java source count are Vision (195), Workmarket (139), Business (109), Chat (65), Identity (64), Social (44), and Location (34). The size distribution confirms that Vision and Workmarket are orchestration-heavy domains, while Business is a substantial operational subsystem rather than a thin future placeholder.

The application root uses component scanning, `@ConfigurationPropertiesScan`, and `@EnableScheduling`. The runtime is one Spring context and one database schema. There is no separate module build, Gradle/Maven subproject, service deployment, or independent persistence boundary per domain.

## 3. Domain responsibility map

### Platform and identity

`identity` owns accounts, authentication, profile data, profile visibility, onboarding, personal shortcuts, galleries, and viewer-specific user summaries. `security` owns JWT filtering/configuration. `location` owns preferences, provider lookup, exact-location access policy, and resolution states. `trust` owns safety reports.

Identity is not a completely low-level package: it imports Workmarket and Social because profile views include ratings/reviews and relationship/visibility context. This is a product-level read dependency and should remain explicit rather than being mistaken for a generic identity primitive.

### Workmarket

`workmarket` owns quests, applications, lifecycle transitions, worker assignment, news, dashboards, reviews, and work-specific read models. It depends on identity for actors and profiles, location for quest location, social for visibility circles, notification/news, and some shared chat/context behavior.

The important architectural split is:

- read services assemble viewer-scoped DTOs and allowed actions;
- mutation services/use cases perform ownership checks and transitions;
- `WorkmarketQuestStateTransitionService` centralizes quest lifecycle changes;
- worker management rechecks manager permission, lifecycle, approved worker, and replacement state within the action boundary.

### Business

`business` is the largest operational domain after Work/Vision. It owns profiles, public pages, offerings, availability rules/exceptions, booking policies, booking lifecycle, calendar projections, favorites, galleries, and owner dashboards.

The package has a relatively clear use-case/read split: booking create/confirm/reject/cancel/complete/reschedule classes are transactional mutations, while public/profile/booking/calendar/dashboard reads use read-only services. Repositories expose owner-scoped and customer-scoped methods such as `findDetailedByIdAndCustomerUserId` and `findDetailedByIdAndOwnerId`, which makes authorization part of the fetch boundary rather than only a later service check.

### Chat and storage

`chat` owns conversations, membership, messages, presence, realtime sync, typing, moderation, attachments, and retention. It depends on identity, social eligibility, common concepts, config, storage, and some Work/context relationships.

The backend has both REST and WebSocket boundaries. `ChatWebSocketAuthInterceptor` authenticates `/ws/chat`; `ChatSyncService` remains the authoritative resync boundary after reconnect or missed events. Storage is abstracted behind local/S3-compatible providers and attachment metadata/availability is represented in DTOs rather than inferred by the client.

### Social/circles

`social` owns circles, relationship requests, memberships, blocking, discovery, and access/visibility relationships. It is a shared authorization primitive for Work, Chat, Location, and profile visibility. This makes Social a high-fan-out dependency even though its own feature surface is smaller.

### Things and rides

`things` owns lending listings and borrow-request lifecycle. `rides` owns commute preferences, ride offers, schedule windows, and optional circle-scoped visibility. Both rely on identity/common concepts and feed shared search/activity surfaces.

### Activity, notification, search, native handoff

These packages are cross-module read/integration capabilities:

- `activity` projects authorized resumable items from Work, Chat, Things, and Vision into a workspace feed;
- `notification` owns notification preferences and attention/read state;
- `search` owns saved search intents and search support;
- Vision search services own cross-family discovery and canonical detail routes;
- `nativehandoff` creates bounded contracts for future mobile/Watch consumers.

They should not be treated as ordinary feature modules. They are integration/read-model infrastructure and naturally depend on multiple domains.

## 4. Dependency graph findings

The package import graph reveals these high-value coupling patterns:

| Source package | Main imported domains | Interpretation |
|---|---|---|
| Vision | identity, Work, Business, Semantic, Things, Social, Chat | Vision is a cross-domain orchestration facade, not an isolated chatbot feature. |
| Workmarket | common, identity, location, social, notification, chat | Work embeds visibility, location, lifecycle notifications, and coordination. |
| Activity | identity, Chat, Things, Vision, Work, Notification | Activity is a cross-module projection layer. |
| Identity | common, Work, Social, Location | profile views and visibility are product-contextual, not purely account-local. |
| Chat | identity, common, config, Social, storage, Work | Chat authorization and attachments require shared platform boundaries. |
| Business | common, identity, config, storage | Business is comparatively contained; it still shares actors, storage, and common scheduling primitives. |
| Social | identity, common, Work, Location | circles are a shared visibility/relationship primitive. |

There are apparent bidirectional package relationships, especially Identity ↔ Work/Social and Identity ↔ Location. These are not automatically defects: they often arise because a profile read model needs domain-specific summaries while the domain needs actor identity. The risk is that package imports can slowly turn into service calls in both directions without a declared read-model or port boundary.

The practical architectural rule should therefore be: cross-domain reads are allowed when they are explicit, viewer-scoped, and assembled in a named read service; cross-domain mutations should remain behind the owning domain's use case/service.

## 5. Transaction and read-boundary architecture

The application sets `spring.jpa.open-in-view=false`, which is a strong boundary choice: lazy entity relations cannot be accidentally traversed from an HTTP view after the service transaction closes. The code responds with explicit `@Transactional(readOnly = true)` read services and repository methods with detailed fetch queries.

The repository-fetch and read-surface audits exist because this architecture has a real failure mode: a read DTO mapper can access an unfetched relation or a service can return entities beyond the transaction boundary. The latest historical inventory audit reported 10 read-like methods without explicit read-only transaction annotations. That is a diagnostic queue, not proof that all ten are broken.

Positive patterns:

- read service classes commonly declare class-level read-only transactions;
- owner/customer authorization is frequently encoded in repository method names and queries;
- detail reads use `findDetailed...` methods where relation materialization matters;
- DTO/read-model assembly is generally done inside the service transaction;
- mutation services use explicit `@Transactional` boundaries.

Risk patterns requiring future inspection:

- mapper helpers (`Mgr`) can obscure where entity traversal occurs;
- cross-domain read composition can call several repositories and assemble a large DTO in one transaction;
- some service methods rely on class/method annotation inference instead of explicit read-only declaration;
- repository fetch strategy can be correct for one sibling read surface and incomplete for another.

This validates the standing repository rule: whenever one LazyInitialization/DTO mapping path changes, sibling read surfaces using the same mapper or fetch pattern must be audited together.

## 6. Workflow and state ownership

The backend mostly owns state transitions, not controllers or frontend clients. Examples include:

- Work quest lifecycle through `WorkmarketQuestStateTransitionService`;
- Work application decisions and worker replacement through Workmarket services;
- Business booking lifecycle through separate transactional use cases and booking policy/audit services;
- Things borrow lifecycle through `ThingSharingService`;
- Chat membership and message state through `ChatConversationStateService` and `ChatService`;
- Vision conversation workflow through persisted statuses, turns, allowed actions, and execution result types.

The presence of many `setStatus(...)` calls is not by itself a defect. The key question is whether the call occurs in the owning transition service and whether preconditions, authorization, notifications, and audit events are handled in the same boundary. `workflow-state-machines.yaml` and the regression scenario catalog provide the intended cross-check.

Business also has explicit domain events (`BusinessBookingCreatedEvent`, `BusinessBookingRescheduledEvent`, `BusinessBookingStatusChangedEvent`) and event handlers for notifications/projections. Work similarly has quest/application news event handling. This decouples immediate mutation from secondary communication, but introduces eventual-consistency and event-handler failure observability requirements.

## 7. Cross-cutting authority boundaries

The backend authority model is visible in four repeated patterns:

1. **Actor resolution:** services receive the authenticated `AppUser` and resolve ownership/role in the backend.
2. **Scoped repository fetch:** many repository queries include owner, customer, participant, or visibility identifiers.
3. **Backend-prepared actions:** DTOs carry allowed actions, primary actions, recovery codes, and route/detail metadata.
4. **Typed failure/recovery:** Vision and Chat expose typed states/codes so clients do not parse arbitrary text to decide whether to retry, refresh, or confirm.

Shared concepts under `common` include actor identity, scheduling windows, circle visibility, pagination, normalization, time, search, rich-text validation, and structured errors. This is the correct place for cross-module primitives, but it must not become a dumping ground for domain rules that belong to Business, Work, Chat, or Social.

## 8. Persistence and schema coupling

The database is one PostgreSQL schema managed by 76 Flyway migrations. Schema history reflects incremental expansion from the work marketplace into Business, Chat, profile visibility, location, activity, storage metadata, and workspace preferences.

This provides operational simplicity but creates monolith coupling:

- one migration stream controls all modules;
- shared `AppUser` and circle tables are referenced broadly;
- cross-module projections use relational joins or multiple repository reads;
- extracting a module would require explicit ownership of shared tables and event/API contracts.

The correct future extraction path, if ever needed, is contract-first and ownership-first: identify aggregate/table owners, publish cross-domain read/event contracts, and only then move deployment boundaries. Package moves alone would not create independent services.

## 9. Architecture risks and follow-ups

### High-value risks

- Vision is the highest-coupling package and can become a universal dependency sink if new domain meaning is implemented there instead of in domain services.
- Activity, Search, Notification, and native handoff are integration layers with broad read dependencies; their query limits and failure behavior need explicit observability.
- Identity profile reads depend on domain modules, creating a potential cycle if profile assembly becomes a place for arbitrary feature data.
- Read-only transaction gaps and mapper/fetch coupling remain a concrete LazyInitialization risk class.
- Event handlers create secondary behavior outside the original transaction; failures and retries need explicit operational treatment.

### Recommended controls

- Keep domain mutations in the owning domain; let Vision call typed adapters/use cases.
- Keep cross-module read aggregation in named integration/read services with bounded queries and explicit DTOs.
- Maintain repository fetch audits when changing mappers or viewer-role read models.
- Maintain one state transition owner per aggregate/workflow.
- Add tests for event publication/handler effects where notification or activity behavior is part of the contract.
- Treat `common` additions as architectural decisions, not convenience imports.

## 10. Round 3 conclusion

The backend architecture is coherent as a modular monolith: domain packages, layered services, explicit read/mutation separation, backend-owned authorization, and typed DTO contracts are real strengths. Its main architectural fact is that modules are product boundaries inside one coordinated graph, not isolated bounded contexts. The most important future work is dependency discipline and read-model/fetch observability, not premature service extraction.

No production code or domain rule was changed in this analysis round. The findings are candidates for future focused implementation/control slices only when a concrete defect or policy gap is selected.

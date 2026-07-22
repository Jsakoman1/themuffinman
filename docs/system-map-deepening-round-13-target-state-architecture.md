# System Map Deepening — Round 13: Target-State Architecture and Evolution

Date: 2026-07-22  
Status: **observed-to-target analysis**  
Scope: product vision, product memory, target capability catalog, current inventory, domain architecture, Web/Vision clients, native handoff, and service extraction options

## Executive finding

The current system is a substantial Spring Boot modular monolith with a shared
authenticated Web shell, a backend-owned Vision orchestration path, and planned
native handoff. This is compatible with the product direction. The target does
not require immediate microservice extraction; it requires stronger contracts,
ownership registries, cross-client parity, runtime evidence, and operational
boundaries first.

The safest evolution is incremental: keep the modular monolith as the system of
record, harden domain ownership and API contracts, add native clients against the
same backend capabilities, then extract only domains whose operational and data
boundaries are already explicit.

## Current state versus target direction

| Dimension | Current observed state | Target direction | Gap |
|---|---|---|---|
| Product shape | one Spring Boot app with domain packages | one coherent Social Useful Network surface | shell and backend composition are progressing; cross-client parity remains |
| Web | shared authenticated workspace shell and module views | direct, complete production client | some runtime scenarios remain pending |
| Vision | persisted conversational state and typed orchestration over domains | primary adaptive assistant and safe execution client | production provider/runtime and full parity evidence remain |
| Native | handoff token/presentation boundary | iPhone/Watch clients consuming same contracts | native implementation/device evidence not established |
| Modules | Work, Business, Things, Rides, Chat, Circles plus support domains | product modules with shared users, circles, scheduling, consent, messaging | ownership graph is clear enough for modular monolith, not yet extraction-ready |
| Backend | domain services, repositories, DTOs, events, Flyway | backend-centric reusable rules for Web, Vision, native | endpoint/evidence registry and operational contracts needed |
| Operations | strong local configuration and runtime evidence | deployable observable multi-client platform | topology, metrics, alerting, recovery gaps |
| Control | YAML plans, verifier, inventories, audits | repeatable autonomous delivery and analysis | universal reference/finding registry still missing |

## Product target model

The target capability catalog defines platform, Vision, Web UI, mobile, watch, and
API surfaces across platform, Vision, work, circles, chat, business, sharing, and
cross-module modules. This is broader than the currently visible product routes:
the catalog expresses production-ready scope, while `docs/capability-inventory.yaml`
records current implementation and evidence.

The product vision makes two client grammars explicit:

1. **Web workspace:** stable navigation, collections, details, search, actions,
   and reliable workflow completion.
2. **Vision console/assistant:** adaptive, voice/text-led, context-aware
   interpretation, review, and backend-confirmed execution.

They are complementary production clients. Vision cannot compensate for an
incomplete direct Web capability, and a Web route cannot compensate for missing
Vision support where the target requires it.

## Recommended architectural shape

```text
                 shared contracts and policy
          identity | circles | consent | scheduling
                         |
  Web shell ---- backend domain services ---- Vision assistant
       |                    |                    |
  native handoff ----- API/DTO/event boundary --- voice/providers
                         |
                       Postgres
                  + object storage
```

The shared core should remain small and explicit:

- identity and actor context;
- circle membership and relationship scope;
- visibility/consent primitives;
- scheduling/time semantics;
- notification/activity contracts;
- messaging context and safe attachment access;
- typed capability/action contracts for all clients.

Product domain services should retain ownership of lifecycle rules. The shell,
Vision, and future native clients should consume prepared read models and invoke
backend-owned actions.

## Module evolution assessment

### Workmarket

The most developed product lifecycle boundary: Quest, applications, workers,
reviews, news, and state transitions have dedicated services and read models.
It is a candidate for future extraction only after notifications, chat context,
circle visibility, location, and Vision adapters are represented as stable ports.

### Business

Business has strong local cohesion around profile, offerings, availability,
booking, policy, and audit. It is the best candidate for an eventual bounded
service because booking rules are substantial and operationally meaningful, but
booking still depends on identity, scheduling, notifications, storage, and
Vision. Extraction should follow contract stabilization, not precede it.

### Things and Rides

These are smaller product domains with clear aggregates and lifecycle services.
They are good candidates for modular hardening and native client consumption.
Premature service extraction would add deployment complexity before their
cross-module visibility and runtime evidence are complete.

### Chat

Chat is both a product module and a platform capability. Its WebSocket,
conversation, membership, message, attachment, presence, audit, rate-limit, and
retention concerns make it a poor early extraction candidate unless realtime,
identity, storage, and moderation contracts are first isolated.

### Vision

Vision should remain an orchestration/application layer over domain services. It
should not own Quest, booking, circle, borrow, ride, or chat business truth. A
future separate Vision application is plausible at the client boundary, while
the backend semantic and execution services can remain co-located until provider,
latency, cost, and security requirements justify separation.

### Shared services

The repository has `services/` as a planned shared-backend location, but the
current implementation is concentrated in `apps/themuffinman`. Shared concepts
should first be extracted as explicit libraries/contracts or internal packages,
not as empty service shells or speculative microservices.

## Safe evolution sequence

### Phase A — contract and evidence hardening

- complete endpoint consumer/evidence registry;
- complete ownership and visibility matrices;
- keep generated contracts fresh from canonical source sections;
- close or explicitly defer pending runtime scenarios;
- define provider, storage, job, and WebSocket operational metrics.

### Phase B — client parity

- complete direct Web capability paths;
- expose the same backend action contracts to Vision;
- implement native handoff consumers and device evidence;
- validate iPhone/Watch constraints without moving business rules into clients.

### Phase C — operational readiness

- document deployment topology and environment promotion;
- rehearse migration, backup/restore, retention, and provider failure;
- add observability and alerting;
- establish cost, quota, and abuse-control operations.

### Phase D — selective extraction

Extract only a domain that has:

- explicit aggregate/table ownership;
- stable API and event contracts;
- no hidden direct repository access from other domains;
- clear authorization and consent boundaries;
- independent runtime/operational requirements;
- migration, recovery, and observability evidence.

Business booking or selected provider-facing capabilities are plausible first
extraction candidates. Identity, circles, chat, and Vision should be treated as
shared/high-centrality boundaries and extracted later, if at all.

## Architecture decisions to preserve

1. Keep backend business rules, permissions, validation, and transitions in
   services.
2. Keep clients thin and backend-contract driven.
3. Keep Web and Vision as equal clients with explicit parity records.
4. Keep Vision as an orchestrator, not a replacement domain owner.
5. Keep circles and consent as shared policy inputs with consumer-specific
   enforcement.
6. Prefer internal modular boundaries before network boundaries.
7. Use events for side effects only when transaction and failure semantics are
   explicit.
8. Do not use target catalog status to imply current implementation status.

## Risks in the target evolution

- treating a future native client as proof before device evidence exists;
- extracting a domain while its Vision adapters remain hidden coupling;
- making the Web shell a second business-logic layer;
- turning Vision into a universal privileged bypass;
- creating shared libraries that become unowned dumping grounds;
- scaling without job idempotency, WebSocket topology, or provider observability;
- allowing target-state aspirations to overwrite current-state inventory facts.

## Follow-up roadmap for the system map

1. Add endpoint-to-client-to-evidence registry from Round 9.
2. Add data ownership and visibility registries from Rounds 8 and 11.
3. Add runtime topology/observability model from Round 10.
4. Add canonical source-reference and finding registry from Round 12.
5. Use those registries to decide whether any domain is extraction-ready.
6. Reassess target catalog coverage only after current evidence is refreshed.

## Source evidence

- `docs/product-vision.md`
- `docs/product-memory.md`
- `docs/target-capability-catalog.yaml`
- `docs/capability-inventory.yaml`
- `docs/domain-technical.md`
- `docs/system-map.md`
- `apps/themuffinman/src/main/java/com/themuffinman/app/**`
- `apps/themuffinman/frontend/src/**`
- `apps/themuffinman/src/main/resources/db/migration/**`
- `services/`
- `AGENTS.md`

## Conclusion

The current modular monolith is not an architectural failure relative to the
target. It is the correct integration stage while ownership, contracts,
consent, client parity, and runtime operations are still being hardened. The
next architectural move should be evidence-backed modularization and client
parity, followed by selective service extraction only where real operational
independence exists.

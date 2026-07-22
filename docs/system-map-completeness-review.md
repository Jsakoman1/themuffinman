# System Map Completeness Review

Status: baseline review. Reviewed: 2026-07-22. This is an engineering evidence
assessment after the completed analysis programs through Round 37; it is not a
claim that every production operation has been observed.

## Executive assessment

The current system is a coherent Spring Boot/Vue modular monolith with a strong
documentation and control layer. The architecture is appropriate for the current
product stage: the backend is the authority for domain rules, permissions, state
transitions, and prepared read models, while the shared frontend shell and Vision
surface consume those contracts. The system map now covers the main product,
domain, implementation, evidence, runtime, security, delivery, and drift-control
relationships.

Overall engineering system score: **7.2/10**.

This score measures map quality and engineering readiness, not product-market fit
or visual polish. The score is held below 8 because several important areas are
described or designed but not empirically proven: production operations, actual
performance, durable side-effect replay, native/device behavior, field-level data
classification, and a complete endpoint review.

| Dimension | Score | Assessment |
|---|---:|---|
| Architecture and modular boundaries | 8.0 | Modular monolith is coherent and extraction is not yet justified. |
| Domain authority and workflow model | 8.0 | Backend ownership is clear; some cross-domain policy and booking-owner drift remains. |
| Documentation and control system | 8.5 | Strong canonical-source, work-plan, verifier, and drift-control model. |
| Interface and client traceability | 7.0 | Registries exist, but 67 non-Web endpoint rows remain unclassified and native proof is absent. |
| Data, privacy, and retention mapping | 7.0 | Entity and retention relationships are mapped; field-level sensitivity and deletion proof are incomplete. |
| Runtime, reliability, and observability | 5.5 | Requirements and risk surfaces are mapped, but measurements, replay, and deployed behavior are not proven. |
| Release and operational readiness | 4.5 | Local controls are visible; CI, deployment, backup, rollback, and incident evidence are external unknowns. |

## What is good

- The product, domain, target capability, current implementation, verification,
  runtime evidence, and audit layers have distinct owners.
- The modular monolith is represented honestly. `services/` is not mistaken for
  deployed extracted services, and the shared app-shell is not mistaken for a
  product domain.
- Backend-centric authority is consistently documented across permissions,
  visibility, validation, workflow transitions, and DTO assembly.
- Flyway, typed operational configuration, regression scenarios, runtime acceptance,
  capability evidence, and strict work-plan verification provide useful control
  points for future implementation work.
- The later rounds added relationship registries instead of copying business rules
  into another narrative document. This reduces semantic duplication and makes
  change impact easier to reason about.
- Unknown external, native, performance, and operational facts are explicitly
  marked unknown or pending rather than being promoted from static inspection.

## What is weak or risky

- The map has grown into a healthy registry network, but discoverability is still
  partly manual. A reader needs the system map plus several registries to answer
  one feature question.
- Endpoint traceability is materially better but not closed: the discovered set
  includes 214 endpoints, with 140 Web-linked and 67 non-Web rows still requiring
  classification; administrative/agent and native-handoff cases are separate.
- The workflow model is strong as a source, but ownership wording can drift from
  implementation. Booking ownership is the clearest known example.
- Event, notification, realtime, and scheduled-job mechanisms are present, yet
  universal after-commit behavior, durable delivery, replay, and operator ledgers
  are not evidenced.
- Performance is currently a catalog of measurements to collect, not a catalog of
  observed latency/query/resource results.
- Runtime acceptance has passed and pending scenarios; pending scenarios include
  authentication recovery, Vision execution/recovery, Chat recovery, notification,
  location visibility, and native/device contracts.
- Production CI, deployment topology, promotion, rollback, backup/restore, and
  incident response are not represented by repository evidence.

## Analysis of the map itself

The system map is well layered and its ownership rule is correct: it is a durable
navigation index, while canonical documents and machine-readable registries own
facts. The relationship diagram explains the direction from product meaning to
implementation and evidence, and the control/evidence table gives a useful entry
point for feature work.

The main optimization issue is not missing prose; it is missing coverage metadata.
Before this review, there was no single machine-readable answer to “which system
dimensions have actually been mapped, and which are still unknown?” The new
`docs/system-map-coverage-registry.yaml` supplies that index without duplicating
the underlying rules. It classifies every major dimension as `mapped`, `partial`,
or `unknown`, and records owner, source links, evidence class, and the remaining
gap.

The map is detailed enough for repository-guided feature analysis. It is not yet a
complete operational model of a deployed product. That distinction is now explicit
and should remain visible in every future round. The next structural improvement
should be a generated module dependency/ownership graph, followed by dedicated
registries for error recovery, integrations/configuration, data classification,
jobs, and test topology. These should be added only when their evidence model is
defined; the coverage registry prevents premature claims in the meantime.

## Coverage still missing or only partial

The full classification is in `docs/system-map-coverage-registry.yaml`. The highest
value gaps are:

1. Package/module dependency direction and code ownership.
2. A unified error, failure, retry, user-recovery, and operator-action taxonomy.
3. External provider contracts, data egress, configuration/environment, and secret
   lifecycle mapping.
4. Field-level sensitivity classification, deletion evidence, and retention proof.
5. Scheduled-job execution, idempotency, replay, and operator runbook coverage.
6. Test topology linking unit, integration, browser, device, fixture, and runtime
   evidence to capabilities.
7. Deployment topology, CI promotion, rollback, backup/restore, and incident
   response evidence.
8. Actual observability and performance measurements.
9. Native/device/offline/background execution implementation evidence.

These are now stable follow-ups `MAP-001` through `MAP-009` in the implementation
backlog. They are not silently treated as active implementation work.

The execution-ready master plan is [`docs/work/system-map-extension-master.yaml`](work/system-map-extension-master.yaml),
with its ordered queue in [`docs/work/system-map-extension-master-execution-inventory.yaml`](work/system-map-extension-master-execution-inventory.yaml).
Its detailed preflight review is [`system-map-extension-preflight.md`](system-map-extension-preflight.md).

## Recommended order from here

1. Build the module dependency and ownership graph (`MAP-001`).
2. Close endpoint classification and workflow-owner drift alongside the graph.
3. Map errors/recovery and integration/configuration boundaries (`MAP-002`,
   `MAP-003`) before adding more cross-module features.
4. Map data sensitivity/deletion and job replay (`MAP-004`, `MAP-005`).
5. Connect tests and runtime evidence into one navigable topology (`MAP-006`), then
   collect real performance/observability evidence (`MAP-008`).
6. Resolve deployment and native gaps only when the external environment or device
   implementation is available (`MAP-007`, `MAP-009`).

## Review conclusion

After 40 analysis/improvement rounds, the system map is mature as a repository
architecture and control map, but deliberately not “complete” in the production
operations sense. The correct next move is evidence closure in the nine dimensions
listed above, not another broad narrative pass. The coverage registry makes that
boundary visible and gives future feature work a deterministic starting point.

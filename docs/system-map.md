# TheMuffinMan System Map

Documentation class: derived_navigation. This file owns cross-system relationships and read order; canonical facts and statuses remain in the linked source registries.

Status: living navigation document. Last reviewed: 2026-07-22.

This document is the durable entry point for understanding how TheMuffinMan is organized. It intentionally points to canonical sources instead of copying their detailed rules, capability records, or implementation evidence.

## What this document owns

This map owns the relationships between the major systems:

- product direction and durable product memory
- business/domain meaning
- target capabilities and current implementation inventory
- application architecture and module boundaries
- implementation planning and verification
- documentation contracts and agent operating safety
- audits, runtime acceptance, and evidence

It does not own completion status, capability status, workflow rules, or generated audit output. Those remain in the sources listed below.

## Read order

For a broad system analysis:

1. `AGENTS.md`
2. `docs/codex-fast-path.md`
3. this document
4. `docs/product-vision.md` and `docs/product-memory.md`
5. `docs/business-logic.md` and `docs/domain-technical.md`
6. `docs/capability-inventory.yaml` and `docs/target-capability-catalog.yaml`
7. `docs/implementation-control.md`, `docs/control-surface-map.md`, and `docs/system-truth-registry.yaml`
8. the relevant relationship registry, `docs/work/*.yaml` plan, and runtime/evidence documents

For a focused change, use the relevant domain capsule and gateway instead of expanding every layer. Vision work starts with `docs/vision-architecture-patterns.md` and `docs/vision-context-gateway.md`.

## System relationship

```text
Product direction
  product-vision.md + product-memory.md
          ↓
Meaning and invariants
  business-logic.md + domain-technical.md + workflow-state-machines.yaml
          ↓
Target contract                         Current state
  target-capability-catalog.yaml  ↔  capability-inventory.yaml
          ↓                                  ↓
Implementation plans                  code + API + web/Vision
  docs/work/*.yaml                    tests + runtime evidence
          ↓                                  ↓
        verify-work.rb  ←  audits / diagnostics  →  audit-output/
                         ↓
                 verified plan evidence
```

The arrows are relationships, not automatic status propagation. A verified work plan does not automatically make every linked capability verified; a target capability does not prove implementation; and an audit report does not decide completion.

## Repository architecture

The repository currently contains one runnable modular monolith:

```text
apps/themuffinman/
├── Spring Boot backend
│   ├── domain packages under com.themuffinman.app
│   ├── REST API and Chat WebSocket (/ws/chat)
│   ├── PostgreSQL + Flyway migrations
│   └── typed operational configuration
├── Vue 3 / TypeScript / Vite frontend
│   ├── identity and Vision modules
│   └── shared authenticated app-shell for most product surfaces
└── local runtime helpers and optional object storage

docs/     product, domain, capability, control, plan, audit, and evidence contracts
scripts/  verifier, audits, generators, and database fixture helpers
services/ no implemented extracted service subtree at this baseline
```

The application is a modular monolith, not a set of independently deployable services. The `services/` directory must not be interpreted as evidence that service extraction already exists.

## Product module map

The machine-readable current inventory owns capability records. At the architectural level, the eight current modules are:

| Module | Responsibility | Main backend area | Main client boundary |
|---|---|---|---|
| Platform | identity, profiles, location, trust | `identity`, `location`, `security`, `trust` | identity + app-shell |
| Vision | adaptive command, discovery, guided execution | `vision`, `semantic`, `prompt`, `agent` | Vision module + shell bridge |
| Work | quests, applications, workers, reviews | `workmarket` | app-shell Work views |
| Chat | conversations, messages, realtime, attachments | `chat`, `storage` | app-shell Chat surface |
| Circles | circles, relations, membership, visibility | `social` | app-shell Circles/People |
| Business | profiles, offerings, availability, bookings | `business` | app-shell Business views |
| Sharing | things lending and voluntary rides | `things`, `rides` | app-shell Things/Rides views |
| Cross-module | shell, activity, search, notifications, handoff | `activity`, `search`, `notification`, `nativehandoff`, common | shared app-shell and APIs |

Detailed capabilities, statuses, gaps, and evidence belong only in `docs/capability-inventory.yaml`. Domain rules belong in `docs/domain-technical.md` and `docs/business-logic.md`.

## Client and authority model

```text
Web / Vision / future native clients
              ↓
       controller/API boundary
              ↓
     domain service and use case
              ↓
 repository + entity/state transition
              ↓
 backend-prepared DTO/read model/actions
```

Backend services own permissions, validation, workflow transitions, collection membership, target resolution, and allowed actions. Clients own presentation, local loading/dialog/focus state, route display state, and user interaction choreography. Vision may plan and guide, but execution remains confirmation-gated and delegated to domain services.

The frontend deliberately centralizes most authenticated surfaces in `frontend/src/modules/app-shell/`. `shellRouteRegistry.ts`, `shellDefinitions.ts`, and the router define canonical route ownership and Vision handoffs. The app shell is shared navigation/presentation infrastructure; it is not a separate product module.

## Control and evidence map

| Concern | Canonical source | Role |
|---|---|---|
| Product meaning | `docs/product-vision.md`, `docs/product-memory.md`, `docs/business-logic.md` | direction and user-facing rules |
| Domain invariants | `docs/domain-technical.md`, `docs/workflow-state-machines.yaml` | entities, permissions, transitions |
| Target scope | `docs/target-capability-catalog.yaml` | desired production-ready atomic capabilities |
| Current capability state | `docs/capability-inventory.yaml` | current statuses, gaps, evidence links |
| Implementation completion | `docs/work/*.yaml`, `scripts/verify-work.rb` | executable plans and verifier evidence |
| Agent safety | `docs/agent-operating-model.yaml` | deterministic fail-closed operating rules |
| Documentation consistency | `docs/docs-as-contract-slices.yaml`, `audit-docs-as-tests` | claim/evidence consistency checks |
| Diagnostic audits | `docs/audits.md`, `scripts/audits/*.rb` | optional risk and drift diagnostics |
| Runtime proof | `docs/runtime-acceptance-matrix.yaml`, `docs/regression-scenario-catalog.yaml`, `docs/runtime-evidence/` | browser/device acceptance evidence |
| Vision architecture | `docs/vision-architecture-patterns.md`, `docs/vision-context-gateway.md` | focused Vision map and constraints |
| Extension coverage | `docs/system-map-coverage-registry.yaml`, `docs/module-dependency-registry.yaml`, extension registries | completeness and cross-cutting relationship map |

The completed extension registries are indexed by the extension master:
`module-dependency-registry.yaml`, `error-recovery-registry.yaml`,
`integration-environment-registry.yaml`, `data-classification-registry.yaml`,
`job-replay-operations-registry.yaml`, `test-topology-registry.yaml`,
`release-operations-registry.yaml`, `performance-evidence-catalog.yaml`, and
the native-client contract sources. The consolidated status is in
`system-map-extension-final-baseline.md`.

The current System Map priority is runtime and truth closeout. ARCH-001 through
ARCH-005 are master-verified analysis/contracts; their remaining runtime and
enforcement gaps are consolidated in
`system-map-runtime-closeout-registry.yaml` and
`work/system-map-truth-runtime-closeout-master.yaml`.

The repair-first execution program is [`docs/work/repair-and-stabilize-master.yaml`](work/repair-and-stabilize-master.yaml), with its analysis in [`docs/repair-and-stabilize-analysis.md`](repair-and-stabilize-analysis.md) and preflight in [`docs/repair-and-stabilize-preflight.md`](repair-and-stabilize-preflight.md). It is the required queue for fixing existing capability correctness, reliability, observability, consent, and regression gaps before unrelated new feature work.

The completeness index is [`system-map-coverage-registry.yaml`](system-map-coverage-registry.yaml).
It is the canonical navigation aid for map coverage: `mapped` means the dimension
has an owning source and evidence path, `partial` means a meaningful source exists
but a relationship or proof gap remains, and `unknown` means no repository or
runtime evidence is currently available. It does not replace the registries above.

The narrower [control-surface-map.md](control-surface-map.md) remains the canonical map of implementation control specifically. This document is the cross-system index that points to it.

The current optimization baseline is [`system-map-optimization-baseline-2026-07-22.yaml`](system-map-optimization-baseline-2026-07-22.yaml). It records ranked relationship, ownership, documentation, and control findings without replacing the canonical sources or promoting runtime status.

The active optimization program is [`work/system-map-optimization-master.yaml`](work/system-map-optimization-master.yaml). Its preflight is [`system-map-optimization-preflight.md`](system-map-optimization-preflight.md), and its serial execution inventory is [`work/system-map-optimization-master-execution-inventory.yaml`](work/system-map-optimization-master-execution-inventory.yaml).

The selected cross-layer pilot is runtime truth synchronization: reconcile the runtime
matrix, capability evidence registry, closeout artifact, System Map runtime registry,
truth registry, and their automated audit. It is bounded to control/evidence behavior
and does not claim broader runtime capability completion.
The pilot is now measured and verified in the optimization child plan; its result is
recorded in `docs/system-map-optimization-preflight.md`.
The closeout artifact is `docs/system-map-optimization-closeout-2026-07-22.yaml`.
Optimization closeout remains subject to the final verifier gates and does not promote
any pending runtime, native, or production-operation boundary.

The next planned optimization program is [`work/system-map-next-optimization-master.yaml`](work/system-map-next-optimization-master.yaml), with preflight in [`system-map-next-optimization-preflight.md`](system-map-next-optimization-preflight.md). It covers dependency boundaries, endpoint review batches, canonical-source lint, change-impact closeout, evidence coverage, and configuration drift.

## Operating rule for future analysis rounds

Each round should:

1. use this map to locate the owning canonical source;
2. record detailed findings in a dated round document under `docs/` or `docs/work/`;
3. update the owning source when a fact, rule, status, or control contract changes;
4. add stable deferred work to the appropriate backlog when implementation is not in scope;
5. validate the affected control and evidence surfaces;
6. add the round document to the analysis register below.

Round documents are historical analysis snapshots. This map remains the stable index and should stay concise.

Current-state claims in this map are navigational only. Capability status belongs to
`docs/capability-inventory.yaml`, runtime status belongs to
`docs/runtime-acceptance-matrix.yaml`, truth ownership belongs to
`docs/system-truth-registry.yaml`, and implementation completion belongs to verifier
evidence in `docs/work/*.yaml`. Historical round documents may contain older counts
and outcomes; they must not be used as current status.

## Analysis register

The first full analysis program is now master-verified. Round 1 was the baseline prerequisite; Rounds 2–7 were executed serially through the master execution inventory.

The next deepening program is prepared in [`docs/work/system-map-deepening-master.yaml`](work/system-map-deepening-master.yaml). Its preflight is verified in [`docs/system-map-deepening-preflight.md`](system-map-deepening-preflight.md). It covers data ownership, API/client/evidence linkage, runtime operations, security/privacy/consent, control-graph integrity, and target-state architecture.

| Round | Document | Scope |
|---|---|---|
| 1 | `docs/system-analysis-round-1.md` | systems, control layers, audits, modules, and application architecture |
| Program | `docs/work/system-analysis-master.yaml` | sequenced master plan for rounds 2–7 |
| 2 | `docs/system-analysis-round-2-capability-reconciliation.md` | capability, plan, code, client, test, and runtime reconciliation |
| 3 | `docs/system-analysis-round-3-backend-domain.md` | backend/domain boundaries and dependencies |
| 4 | `docs/system-analysis-round-4-frontend-surfaces.md` | frontend shell, routes, API linkage, and surface ownership |
| 5 | `docs/system-analysis-round-5-vision-orchestration.md` | Vision semantic and execution orchestration |
| 6 | `docs/system-analysis-round-6-runtime-production-readiness.md` | runtime, security, configuration, storage, and production readiness |
| 7 | `docs/system-analysis-round-7-control-system.md` | documentation, audits, plan graph, verifier, and control quality |
| Deepening preflight | `docs/system-map-deepening-preflight.md` | post-program source, artifact, audit, and plan readiness; verified before Round 8 |
| 8 | `docs/system-map-deepening-round-8-data-ownership.md` | entity/table/repository/service ownership and cross-domain dependency graph |
| 9 | `docs/system-map-deepening-round-9-api-client-evidence.md` | backend endpoints, Web/Vision/native/admin consumers, contracts, and evidence linkage |
| 10 | `docs/system-map-deepening-round-10-runtime-operations.md` | runtime topology, configuration, jobs, providers, storage, observability, and recovery gaps |
| 11 | `docs/system-map-deepening-round-11-security-privacy-consent.md` | authentication, authorization, visibility, consent, retention, and abuse boundaries |
| 12 | `docs/system-map-deepening-round-12-control-graph.md` | canonical sources, generated artifacts, work-plan graph, verifier, and audit integrity |
| 13 | `docs/system-map-deepening-round-13-target-state-architecture.md` | target architecture, client parity, modular evolution, and extraction constraints |

The third analysis pass is master-verified in [`docs/work/system-map-third-pass-master.yaml`](work/system-map-third-pass-master.yaml). It moves from static ownership mapping to execution-flow, side-effect, read-model, failure/recovery, cross-client, and architecture-risk analysis. Its primary follow-up IDs are ARCH-001 through ARCH-005 in [`docs/implementation-backlog.md`](implementation-backlog.md).

ARCH-003 is now tracked in [`docs/work/arch-003-side-effect-delivery-master.yaml`](work/arch-003-side-effect-delivery-master.yaml), with the current contract in [`docs/side-effect-delivery-contract.yaml`](side-effect-delivery-contract.yaml) and the source-trace analysis in [`docs/arch-003-side-effect-delivery-analysis.md`](arch-003-side-effect-delivery-analysis.md). The shared domain event publisher defers publication until after commit when transaction synchronization is active; this is not an outbox or replay guarantee.

ARCH-004 is tracked in [`docs/work/arch-004-read-model-reliability-master.yaml`](work/arch-004-read-model-reliability-master.yaml), with the read-model evidence contract in [`docs/read-model-reliability-contract.yaml`](read-model-reliability-contract.yaml). Its runtime scenarios remain pending and do not imply measured performance.

ARCH-005 is tracked in [`docs/work/arch-005-visibility-consent-parity-master.yaml`](work/arch-005-visibility-consent-parity-master.yaml), with the privacy/client parity contract in [`docs/visibility-consent-parity-contract.yaml`](visibility-consent-parity-contract.yaml). Native and device behavior remain unproven.

ARCH-001 is tracked in [`docs/work/arch-001-endpoint-client-evidence-master.yaml`](work/arch-001-endpoint-client-evidence-master.yaml), with the canonical cross-reference in [`docs/endpoint-client-evidence-registry.yaml`](endpoint-client-evidence-registry.yaml). It links specialized endpoint, capability, client, permission, runtime, and native-handoff registries without replacing their authority.

The ARCH-001 endpoint review queue is tracked in [`docs/endpoint-reconciliation-review.yaml`](endpoint-reconciliation-review.yaml) and covers all 67 currently unclassified non-Web endpoints. Five are source-classified; 62 remain explicit owner review items.

| Third-pass master | `docs/work/system-map-third-pass-master.yaml` | verified program for Rounds 14–19 |
| 14 | `docs/system-map-third-pass-round-14-request-persistence-flows.md` | request-to-persistence flows and transaction boundaries |
| 15 | `docs/system-map-third-pass-round-15-events-side-effects.md` | events, side effects, realtime, and scheduled work |
| 16 | `docs/system-map-third-pass-round-16-read-model-performance.md` | read models, fetch strategy, and performance evidence gaps |
| 17 | `docs/system-map-third-pass-round-17-failure-recovery.md` | failure semantics, idempotency, recovery, and replay gaps |
| 18 | `docs/system-map-third-pass-round-18-cross-client-parity.md` | Web, Vision, admin, native contracts, and parity evidence |
| 19 | `docs/system-map-third-pass-round-19-architecture-risk-register.md` | architecture decisions, risks, and sequenced follow-ups |

The complete-system analysis program is master-verified in [`docs/work/system-map-complete-analysis-master.yaml`](work/system-map-complete-analysis-master.yaml). Its readiness and evidence rules are recorded in [`docs/system-map-complete-analysis-preflight.md`](system-map-complete-analysis-preflight.md). It covers canonical truth ownership, endpoint and client registry, data schema, workflows, frontend parity, evidence traceability, performance-evidence design, events and replay, security and operations, delivery and dependency provenance, drift controls, and final baseline consolidation. The resulting control follow-ups are DRIFT-001 through DRIFT-005 in [`docs/implementation-backlog.md`](implementation-backlog.md).

The operational expansion is master-verified in [`docs/work/system-map-operational-expansion-master.yaml`](work/system-map-operational-expansion-master.yaml). Its execution boundaries are recorded in [`docs/system-map-operational-expansion-preflight.md`](system-map-operational-expansion-preflight.md). It extends the current maps into endpoint-capability traceability, entity relations and retention, permissions and consent, runtime observability, release operations, and generated change-impact reporting.

| Operational-expansion master | `docs/work/system-map-operational-expansion-master.yaml` | verified program for Rounds 32–37 |
| 32 | `docs/system-map-operational-expansion-round-32-endpoint-capability-traceability.md` | endpoint, capability, consumer, test, and runtime-source traceability |
| 33 | `docs/system-map-operational-expansion-round-33-entity-relation-retention.md` | entity relationships, ownership, retention, and migration boundaries |
| 34 | `docs/system-map-operational-expansion-round-34-permission-visibility.md` | permissions, state, visibility, consent, and backend authority |
| 35 | `docs/system-map-operational-expansion-round-35-runtime-observability.md` | metric and trace requirements for runtime observability |
| 36 | `docs/system-map-operational-expansion-round-36-release-operations.md` | repository-visible delivery controls and external operations unknowns |
| 37 | `docs/system-map-operational-expansion-round-37-change-impact.md` | advisory generated changed-file relationship report |

| Complete-pass master | `docs/work/system-map-complete-analysis-master.yaml` | verified program for Rounds 20–31, 29, and 30 |
| 20 | `docs/system-map-complete-analysis-round-20-canonical-truth-graph.md` | canonical truth ownership and reference direction |
| 21 | `docs/system-map-complete-analysis-round-21-endpoint-client-registry.md` | endpoints, DTOs, client consumers, and evidence classes |
| 22 | `docs/system-map-complete-analysis-round-22-data-schema-ownership.md` | entities, migrations, repositories, and data ownership |
| 23 | `docs/system-map-complete-analysis-round-23-workflow-invariant-matrix.md` | workflows, invariants, permissions, and concurrency coverage |
| 24 | `docs/system-map-complete-analysis-round-24-frontend-surface-parity.md` | Web routes, actions, Vision, admin, and native handoff boundaries |
| 25 | `docs/system-map-complete-analysis-round-25-evidence-traceability.md` | test, runtime, and capability evidence distinctions |
| 26 | `docs/system-map-complete-analysis-round-26-performance-evidence-design.md` | measurement catalog for performance claims |
| 27 | `docs/system-map-complete-analysis-round-27-events-jobs-replay.md` | event, realtime, job, and replay semantics |
| 28 | `docs/system-map-complete-analysis-round-28-security-operations-recovery.md` | security, operational configuration, and recovery limits |
| 31 | `docs/system-map-complete-analysis-round-31-delivery-dependencies-release.md` | dependency, generation, delivery, and release provenance |
| 29 | `docs/system-map-complete-analysis-round-29-drift-control-design.md` | machine-checkable drift-control design |
| 30 | `docs/system-map-complete-analysis-round-30-complete-baseline.md` | consolidated baseline and sequenced next work |

Future rounds should extend this register rather than creating competing system-map documents.

The current completeness baseline and score are recorded in
[`system-map-completeness-review.md`](system-map-completeness-review.md). Its
follow-up IDs are `MAP-001` through `MAP-009` in the implementation backlog.
The execution-ready expansion program is [`work/system-map-extension-master.yaml`](work/system-map-extension-master.yaml),
with nine ordered child plans and an execution inventory.
Its consolidated result is [`system-map-extension-final-baseline.md`](system-map-extension-final-baseline.md).

## Known boundaries at last review

- The target catalog, current inventory, work-plan status, and runtime acceptance status are separate layers.
- Most work plans are historical or completed; active status must be read from YAML and verifier evidence, not filenames.
- Audit reports under `docs/audit-output/` are disposable snapshots.
- `SideQuest` remains in historical database/configuration names and compatibility aliases while the product/app identity is `TheMuffinMan`.
- Mobile and Watch are contract/future-client surfaces, not implemented native applications.

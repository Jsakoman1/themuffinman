# TheMuffinMan System Analysis — Round 1

Status: first deep baseline, captured 2026-07-22. This is an analysis document, not a replacement for any canonical product, domain, inventory, or implementation-control source.

## 1. Executive finding

The repository is one runnable product application, not a collection of independently deployable modules:

```text
TheMuffinMan repository
├── one Spring Boot backend: apps/themuffinman
│   ├── domain packages (identity, workmarket, business, chat, ...)
│   ├── PostgreSQL persistence + 76 Flyway migrations
│   ├── REST controllers and one Chat WebSocket endpoint
│   └── typed operational configuration
├── one Vue 3 / TypeScript frontend
│   ├── identity and Vision modules
│   └── most authenticated views centralized in app-shell
├── docs/ control and product knowledge system
├── scripts/ audits and verifier
└── services/ currently has no implemented service subtree
```

The product direction is broader than the original SideQuest/work-marketplace identity. The code has grown into a shared platform with eight capability modules, a unified authenticated shell, cross-module search/notifications/activity, and Vision as an adaptive command surface. The old naming remains in configuration, database names, environment variables, compatibility npm aliases, and some README examples; that is a historical compatibility layer, not evidence of a second running application.

The most important governance distinction is:

```text
product intent → domain meaning → target capability contract
       ↓                ↓                    ↓
current capability inventory ← code/API/web/Vision/tests/evidence
       ↓
work plans → verifier-generated evidence → verified implementation status
       ↑
optional audits and generated reports (diagnostics only)
```

No single report answers “is the product complete?”. The inventory answers current capability status; work plans answer implementation completion; runtime acceptance answers browser/device proof; and audits identify drift or risk.

## 2. Source-of-truth hierarchy

### 2.1 Ideation and product direction

The ideational layer is human-readable and intentionally not execution status:

- `docs/product-vision.md` defines the North Star, product feel, adaptive surfaces, Vision boundary, workspace language, and UI principles.
- `docs/product-memory.md` stores durable lessons and decisions learned during product evolution, especially backend authority, progressive clarification, and Vision/workspace separation.
- `docs/references/` contains inspiration and bounded references, not product requirements.

This layer answers “why and what should this feel like?”. It must not be used as proof that a capability exists.

### 2.2 Product/domain meaning

- `docs/business-logic.md` explains user-facing behavior and rules.
- `docs/domain-technical.md` describes entities, relations, validation, permissions, workflows, and invariants.
- `docs/workflow-state-machines.md` plus `docs/workflow-state-machines.yaml` describe state transitions and their owners.
- `docs/cross-domain-glossary.md`, `docs/location-services.md`, and Vision-specific docs explain shared terminology or bounded subsystems.

These are canonical for meaning and invariants, but not for active work completion. A logic change must update the affected living docs and agent artifacts together.

### 2.3 Target product contract

`docs/target-capability-catalog.yaml` is the production-ready target contract. It currently describes eight modules, domain objects, atomic capabilities, actors, access/privacy rules, outcomes, failure behavior, acceptance conditions, and required surfaces (`vision`, `web_ui`, `mobile`, `watch`, `api`). It explicitly does not copy current status; it links to current inventory IDs.

The target catalog is therefore a desired decomposition and coverage contract, not a backlog and not proof of delivery. `audit-target-capability-catalog` validates its structure and inventory links; `audit-target-capability-coverage` is diagnostic.

### 2.4 Current capability truth

`docs/capability-inventory.yaml` is the canonical current-state inventory. It connects capability intent, backend, user entry points, gaps, tests, plans, and runtime evidence. Its status vocabulary is:

- `planned`, `not_offered`, `partial`, `broken`, `implemented`, `verified`, `deprecated`.

At this baseline it contains 8 modules and 152 capabilities: 143 `implemented`, 5 `verified`, 2 `partial`, and 2 `planned`. This distribution is a key warning: “implemented” means the primary backend/web workflow exists; it does not mean every client, runtime flow, or production concern is verified.

### 2.5 Implementation control

The active implementation control surface is `docs/work/*.yaml`, governed by `docs/implementation-control.md` and executed by `scripts/verify-work.rb`.

Normal lifecycle:

```text
draft → active → verified
              ↘ deferred / blocked
```

For strict plans, every task declares exact required paths and leaf validations. The verifier records revision, timestamps, changed files, command exit codes, and output. A checkbox or manually written paragraph cannot create final `verified` state. Masters are verified only after their children; high-risk serial programs additionally use an execution inventory and one `work-start`/task verification at a time.

The active implementation queue is not the same as every file in `docs/work/`. Some YAML files are contracts, acceptance matrices, execution inventories, analyses, old plan-shaped artifacts, or generated reports. `docs/control-surface-map.md` correctly identifies only active work plans plus verifier evidence as completion control.

### 2.6 Documentation/control validation

`docs/docs-as-contract-slices.yaml`, `audit-docs-as-tests`, `audit-work-plan-recursion`, `audit-plan-coverage`, and `audit-inventory-freshness` form a documentation consistency layer. It detects unsupported or stale claims and graph/inventory drift, but it does not prove runtime behavior.

`docs/agent-operating-model.yaml` is an operational safety artifact for automation and agents. It is fail-closed by default and defines exact-target resolution, confirmation before destructive/external mutations, backend authority, ambiguity handling, and sandbox/production separation. Its sections under `docs/agent-operating-model/` add API and intent catalogs. `AgentOperatingModelValidationTest` is the code-level guard for this artifact.

### 2.7 Runtime/evidence layer

`docs/runtime-acceptance-matrix.yaml`, `docs/regression-scenario-catalog.yaml`, `docs/vision-capability-acceptance.yaml`, and `docs/runtime-evidence/*.json` connect capability IDs to browser/device actions and evidence. Runtime evidence is separate from source tests, type checks, and builds. The matrix has passed and pending scenarios; pending runtime proof must not be inferred from a verified work plan.

### 2.8 Optional diagnostics

`docs/audits.md` explicitly says audits are optional diagnostics. They write disposable JSON/Markdown under `docs/audit-output/`. A passing audit is evidence relevant to a task, never a completion signal and never a second source of truth.

## 3. Available audits and tools

### 3.1 Workflow/control tools

- `make work-create`, `make master-create`: create plan skeletons.
- `make work-start plan=... task=...`: snapshot a serial task before implementation.
- `make work-verify plan=... [task=...]`: the only final verifier path.
- `scripts/verify-work.rb`: validates plans, exact changed paths, leaf commands, serial ordering, master children, and evidence.
- `audit-work-plan-recursion`: prevents nested verification or recursive plans.
- `audit-plan-coverage`: checks active plan/master graph coverage and uniqueness.
- `audit-change-impact-preflight` and `score-changeset-risk`: estimate docs/tests/sibling-read-surface impact for changed files.

### 3.2 Backend audits (`make audit-backend`)

- API contract drift: backend DTO fields vs generated frontend contract/use.
- Read-surface inventory: read service boundaries and transaction/read-only signals.
- Repository fetch: relation-fetch and likely lazy-loading risk.
- Mapper usage: mapper callsites, caller classification, and risk flags.
- Mutation safety: mutation surfaces, risk factors, tests, and regression catalog signals.

### 3.3 Frontend audits (`make audit-frontend`)

- UI entrypoints: primary navigation, shell surfaces, routes, create handoffs, configured actions.
- Endpoint-callsite linker: backend endpoint ↔ frontend client ↔ reachable UI linkage.
- Route-surface inventory: route ownership, concrete surface, APIs, and composables.
- Stale-surface audit: detached/unused route, API, composable, style, or component candidates.
- Frontend state-logic duplication and generic duplicate-logic scans.
- Permission-rule duplication: backend action/presentation authority versus local frontend gates.

### 3.4 Documentation, tests, capability and runtime audits

- `audit-docs-as-tests`: scans documented factual statements and searches expected code/evidence.
- `audit-contract-test-gaps`: endpoint/contract/test/documentation gaps.
- `audit-test-fixture-duplication`: repeated test setup candidates.
- `audit-target-capability-catalog`: target structure and current-inventory links.
- `audit-target-capability-coverage`: generated target-to-current coverage/mapping quality report.
- `audit-inventory-freshness`: capability evidence paths and current source-domain freshness.
- `audit-runtime-acceptance`: passed versus pending runtime scenarios.
- `audit-native-client-handoff`: native/mobile/Watch contract readiness.
- `audit-ui-entrypoints`, `audit-main-surfaces-plan-preflight`, and `audit-todo` provide focused integrity checks.
- `generate-target-capability-slices`: planning input derived from target gaps; not status.

### 3.5 Build/runtime commands

The root Makefile delegates to the app Makefile. Backend: Maven test/package/run. Frontend: npm type-check/build/contract generation and validation. `make dev` owns a backend/frontend process tree, refuses occupied ports, and must be paired with `make dev-stop`; `make dev-doctor` inspects ports, Docker, and object-storage mode. Optional local S3-compatible object storage is MinIO via Docker Compose. DB fixtures are in `scripts/db/`, explicitly separate from production behavior.

## 4. Product modules and current boundaries

The canonical inventory has eight modules, while the original product README lists five planned product themes. The mapping is:

| Current module | Main responsibility | Backend package | Main web ownership | State at baseline |
|---|---|---|---|---|
| Platform | identity, profile, location, auth/session | `identity`, `location`, `security`, `trust` | `identity` plus app-shell views | implemented/partially runtime-proven |
| Vision | adaptive conversational/voice command, discovery, execution | `vision`, `semantic`, `prompt`, `agent` | `vision` plus shell bridge | extensive code/tests; execution is configuration-gated |
| Work | work marketplace: quests, applications, workers, reviews, news | `workmarket` | app-shell work views | first implemented product module; largely implemented |
| Chat | shared conversations, messages, realtime, attachments, moderation | `chat`, `storage` | app-shell `ChatSurfaceView` | implemented slices; pending runtime scenarios remain |
| Circles | circles, relations, membership, visibility eligibility | `social` | app-shell `CirclesView`, people surfaces | implemented |
| Business | profiles, offerings, availability, bookings, gallery | `business` | app-shell business views | implemented slices; runtime gaps remain |
| Sharing | things lending and voluntary rides | `things`, `rides` | app-shell things/rides views | implemented slices; residual acceptance gaps |
| Cross-module | shell, search, notifications, activity, handoff, traceability | `activity`, `notification`, `search`, `nativehandoff`, common primitives | app-shell and shared APIs | central integration layer |

The old five-theme model is still useful as product history: work marketplace, business/booking, things/lending, rides, and shared chat. Circles, platform, Vision, and cross-module capabilities are now explicit supporting modules in the machine-readable model.

`services/` is currently empty at the implemented subtree level. There is no extracted shared backend service deployment. “Shared services” in the docs means shared capabilities/packages inside the monolith.

## 5. Application and architecture

### 5.1 Backend

The backend is a modular monolith under `com.themuffinman.app`, organized primarily by domain, then by layer: controller, service, repository, model, DTO, mapper, config, and tests. The baseline contains approximately 38 controllers, 124 service classes, 49 repositories, 273 DTO classes, and 16 `*Mgr` mapping helpers. The largest domains by Java source count are Vision (195), Workmarket (139), Business (109), Chat (65), Identity (64), Social (44), and Location (34).

Controllers expose REST resources and delegate to services. Services own business rules, permissions, state transitions, and DTO/read-model assembly. Repositories own persistence. DTOs are the API boundary; mappers/assemblers translate domain state to viewer-scoped responses. `open-in-view=false` makes transaction/read-boundary discipline important and explains the repository-fetch/read-surface audits.

Cross-cutting backend capabilities include Spring Security/JWT, global structured error handling, typed config properties, scheduled retention/cleanup, WebSocket chat, object storage, location provider integration, OpenAI-backed agent/voice options, and Flyway schema evolution.

### 5.2 Persistence and operations

PostgreSQL is the configured database (`side_quest_db`, historical name), Hibernate runs with `ddl-auto=validate`, and Flyway owns schema changes. There are 76 migrations through V76. Local development uses `application-dev.properties` and optional `.env.backend.dev`/`.local`; production-oriented defaults disable seeded users and admin bootstrap unless explicitly enabled. Operational concerns are grouped in `@ConfigurationProperties` classes such as `SecurityProperties`, `RetentionProperties`, `ChatProperties`, `AgentProperties`, `VoiceProperties`, `BusinessProperties`, `LocationProviderProperties`, `ObjectStorageProperties`, and `AccountRecoveryProperties`.

Chat has both REST and `/ws/chat`; its WebSocket authentication uses a dedicated interceptor. Attachments can use local disk or S3-compatible storage, with MinIO as local infrastructure. Retention and rate limits are explicit config, not hidden in controllers.

### 5.3 Frontend

The frontend is Vue 3 + TypeScript + Vite + Axios + Vue Router. Identity and Vision have their own module directories. Most authenticated product views are intentionally centralized under `frontend/src/modules/app-shell/`: shared shell components, route registry, navigation model, detail/utility surfaces, dialogs, action handling, keyboard behavior, and app-shell views for all current product areas. The `business`, `chat`, `rides`, and `things` module directories contain little/no independent view implementation at this baseline; their user surfaces are app-shell-owned. This is a deliberate shared-shell architecture, but it concentrates surface complexity in `app-shell`.

The router has 45 routes according to the latest route-surface audit: 44 concrete surfaces and one redirect, with no placeholder module routes. Routes are native dynamic imports; router/auth/shell registry are the eager navigation core. `/` is the authenticated shell redirecting to `/home`; `/vision` is the adaptive surface; many domain routes are canonical and Vision bridge routes redirect into Vision with prompt/autorun query parameters.

The shell navigation policy promotes only surfaces with a stable backend read model, habitual value, lower cognitive load, and compact-nav fit. Vision is not a persistent sidebar dashboard; it is entered for guided, semantic, or cross-module work and returns to canonical domain routes for stable browsing/detail ownership.

Generated Vision contracts live under `frontend/src/contracts/generated/` and are checked during `npm run build`. Static web contract scripts validate route ordering, visible entry actions, Calendar labeling, and Chat/Circles recovery affordances.

### 5.4 Authority and client flow

The architectural rule repeated across docs and code is backend authority:

```text
request → controller → domain service → repository/model
                         ↓
                  viewer-scoped DTO/read model
                         ↓
             web shell / Vision / future native clients
```

The frontend may hold display, selected-row, dialog, URL, and loading state. It must not invent permissions, workflow transitions, collection membership, or shared persistence. Backend responses include allowed actions/presentation hints where relevant. Vision can plan and guide, but execution is gated and domain services remain authoritative.

## 6. Evidence snapshot and tensions

The latest committed audit summaries (generated 2026-07-20) show:

- Docs-as-tests: 292 statements, 0 review-needed, 0 high priority; advisory only.
- Endpoint linker: 214 backend endpoints, 194 frontend client methods, 140 linked, 74 unlinked. Unlinked does not automatically mean broken: admin, native, Vision, compatibility, or intentionally non-web endpoints can be legitimate, but it is a review queue.
- Route inventory: 45 routes, 44 concrete, 0 placeholders.
- Stale surface: 0 likely unused and 0 detached; one stylesheet (`styles/base.css`) is review-needed because it has no detected importer.
- Frontend duplication: workflow overlap for circle actions and error assignment across 35 files; permission duplication review has 26 backend sources and one local frontend gate category, plus action presentation overlap. These are diagnostic candidates, not proven defects.

The control model is mature, but the evidence layers are not equivalent:

1. Many work plans and masters are `verified` because their declared implementation tasks passed the verifier.
2. Capability inventory mostly says `implemented`, which is weaker than `verified` and retains gaps.
3. Runtime acceptance still includes pending browser/device scenarios, especially auth recovery, notification receive, chat lifecycle/attachments, Vision voice parity, mobile/Watch, and final capability closeout.
4. Existing reports are snapshots and may lag code. They must be regenerated before using them for a current decision.
5. `apps/themuffinman/README.md` still describes a much smaller/older work-marketplace-centered scope and old package paths. It is useful onboarding context, but docs/control sources now describe a broader current architecture.

## 7. Open questions for deeper rounds

These are analysis follow-ups, not silently converted into implementation work in round 1:

- Reconcile every capability status against fresh inventory and runtime audits, especially the 143 `implemented` records with explicit gaps.
- Classify the 74 endpoint linker misses into intentional non-web endpoints, admin surfaces, Vision/native contracts, and true drift.
- Trace the full master-plan graph and distinguish completed historical plans, active contracts, generated reports, and currently executable queue items.
- Audit domain-to-domain dependencies and database relations to reveal hidden coupling behind the modular-monolith package layout.
- Inspect Vision execution gating end-to-end: provider configuration, semantic routing, confirmation, adapter invocation, failure recovery, and cross-client parity.
- Measure frontend app-shell concentration and decide whether it is a healthy shared-shell boundary or a maintainability hotspot.
- Compare README, product docs, inventory, target catalog, and code for naming drift (`SideQuest`/`TheMuffinMan`) and define the canonical vocabulary.
- Verify actual local runtime topology in a controlled session (`make dev` → evidence → `make dev-stop`) rather than inferring it from source/config only.
- Review security/operational readiness: secrets, rate limits, retention, object storage, external providers, bootstrap, and production deployment topology.

## 8. Bottom line

TheMuffinMan is currently a well-instrumented modular monolith with a strong documentation/control discipline, not yet a set of extracted services or fully runtime-verified product modules. Its architecture has three intentional strengths: backend-owned domain authority, a shared app shell with canonical routes, and explicit machine-readable planning/evidence controls. Its main risks are evidence fragmentation across status layers, historical naming/docs drift, a large centralized frontend shell, and the gap between “implemented” capability records and pending browser/device production proof.

This document should be used as the map for round 2, while canonical facts continue to be updated in their owning YAML/Markdown files rather than copied here as a competing status system.

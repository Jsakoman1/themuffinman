# System Map Deepening — Round 9: API, Client, and Evidence Linkage

Date: 2026-07-22  
Status: **observed analysis**  
Scope: Spring controllers, frontend API/client surfaces, generated contracts, agent catalogs, capability inventory, and runtime evidence

## Executive finding

The backend exposes a broad HTTP surface of 241 explicitly annotated mapping
methods across the controller tree. The frontend is organized around an
authenticated app shell and module API files, while Vision and agent catalogs
provide additional orchestration and semantic access paths. The application has
good contract artifacts for agent/API metadata, but there is no single mechanical
registry proving that every backend route is consumed by Web, Vision, native,
admin, tests, and runtime evidence.

The strongest linkage is for agent-facing capabilities: the generated frontend
contract contains 163 endpoint IDs and 149 intent IDs, sourced from the referenced
agent YAML sections. The weaker linkage is ordinary frontend transport coverage:
many module API calls are locally typed or assembled from shared HTTP helpers,
without a route-to-client-to-test matrix.

## Surface inventory

### Backend ownership by controller surface

The largest controller surfaces are:

| Domain/surface | Mapping methods | Interpretation |
|---|---:|---|
| Chat conversation and messages | 33 | high-volume stateful API; includes product-context chat and attachments |
| Social circles | 25 | relationship, membership, visibility, and admin operations |
| Workmarket Quest | 16 | core Quest CRUD and lifecycle |
| Business bookings | 16 | customer and owner workflows in one controller |
| Quest applications | 14 | applicant, owner, worker, and admin actions |
| Things | 13 | listing and borrowing lifecycle |
| Rides | 11 | offer and participation lifecycle |
| Identity app-user surface | 10 | current user, admin, profile, and user lookup paths |
| Business profile/availability | 9 + 9 | profile and scheduling configuration |
| Other controllers | remaining | Vision, auth, activity, notification, location, search, trust, handoff, agent |

The count is source-derived from `@GetMapping`, `@PostMapping`, `@PutMapping`,
`@PatchMapping`, and `@DeleteMapping`; it excludes websocket message types and
does not claim every mapping is externally accessible under the same security
policy.

### Frontend client organization

The Web client uses a shared `src/api/httpClient.ts` transport and module-level
API files, including identity/auth, things, rides, app-shell navigation, and
Vision conversation/dashboard clients. Most user-facing surfaces are rendered
inside the app shell, with views for work, business, circles, chat, rides,
things, notifications, activity, and profile.

This gives the frontend a coherent navigation and presentation architecture, but
it also means “client ownership” is split between a module API file, an app-shell
view, shared composables, and sometimes Vision web-action handling. A route can
therefore be consumed indirectly through a shared surface rather than through a
same-named module file.

## Linkage classes

### Direct Web linkage

Direct linkage is strongest where a module has an API file and a corresponding
view, for example rides, things, identity/auth, Vision conversations, and shell
navigation. Workmarket, business, social, and chat are also represented in the
shell, but their transport calls are distributed across feature components and
shared composables rather than one obvious module client boundary.

### Agent/Vision linkage

The agent operating model defines endpoint and intent IDs in referenced YAML
sections. The frontend contract generator now loads those section files from the
root operating-model references and emits the generated TypeScript constants.
The generated artifact currently exposes 163 endpoint IDs and 149 intent IDs.

This is a valuable machine-readable linkage, but it is semantic linkage, not
proof that each route works in runtime. An intent can reference an endpoint with
backend implementation and policy documentation while still lacking a passing
runtime scenario or a Web consumer.

### Runtime and test linkage

Runtime acceptance is represented separately in `docs/runtime-acceptance-matrix.yaml`.
The capability inventory and agent catalog provide additional evidence paths.
These systems allow a capability to be traced to scenarios and source files, but
they do not currently form a complete reverse index from every controller method
to all clients and tests.

### Native linkage

The repository contains a native handoff domain and native handoff token API, but
the current source inventory does not establish a complete native client codebase
or native end-to-end evidence surface. Native should therefore be classified as
an integration boundary/target client unless separate client sources are found.

## Evidence strength model

| Evidence | What it proves | What it does not prove |
|---|---|---|
| Controller mapping | route exists in source | route authorization, client use, runtime health |
| Service/repository implementation | backend path has an implementation | correct client contract or full workflow behavior |
| Generated agent contract | catalog IDs match source YAML and generator output | semantic correctness or execution success |
| Frontend API call | some Web consumer exists | all states/errors/roles are supported |
| JUnit/integration test | selected behavior passes in test context | deployment and production topology |
| Runtime acceptance scenario | observed environment behavior | broad route coverage or production reliability |
| Capability inventory record | curated cross-layer trace | absence of stale or unlisted endpoints |

The key control rule is that route existence must not be promoted to “implemented
and verified” merely because the controller and generated catalog both exist.

## Gaps and mismatches found

1. **No complete endpoint consumer matrix.** There is no canonical table with one
   row per backend mapping and columns for Web client, Vision/agent intent, native,
   admin, unit/integration tests, runtime scenario, and evidence path.
2. **Controller-to-domain naming is not always one-to-one.** Dashboard routes are
   in workmarket controllers but serve Vision and workspace composition. Chat
   routes include Quest and circle context. Ownership must follow service and
   persistence responsibility, not URL prefix alone.
3. **Generated contract coverage is selective.** The contract generator covers
   DTOs, enums, agent IDs, and catalog material, but it is not an OpenAPI-like
   complete route/schema generator.
4. **Runtime evidence is capability-oriented.** A scenario may validate a
   workflow capability without enumerating every HTTP call made inside it.
5. **Indirect client consumers are hard to discover statically.** Shared shell
   composables and Vision web actions can hide the final transport call from a
   simple module-name search.
6. **Admin surfaces are mixed with product surfaces.** Admin controllers and
   admin agent intents have distinct authority requirements, but ordinary route
   inventories need an explicit role classification to prevent accidental
   equivalence.

## Architectural implications

- The API surface is a modular-monolith API, not a set of independently deployable
  module APIs.
- The app shell is a client integration layer that intentionally crosses domains;
  it should remain thin and consume backend-prepared DTOs.
- Vision is a second client/orchestration path over the same backend services,
  which is why its endpoint and intent catalogs are critical control artifacts.
- A future native client should consume the same backend contracts and service
  rules, but current evidence is insufficient to claim native parity.
- Contract generation should evolve toward a machine-readable endpoint registry,
  or an additional audit should mechanically compare controller mappings with the
  API catalog and capability inventory.

## Follow-up requirements

1. Build an endpoint registry keyed by HTTP method plus normalized path.
2. Add ownership, consumer, role, contract, test, runtime, and evidence columns.
3. Include websocket and async event surfaces as separate protocol classes.
4. Mark indirect consumers explicitly: shell, composable, Vision action, agent,
   admin, native handoff.
5. Keep “source exists”, “catalogued”, “client consumed”, and “runtime verified”
   as separate statuses.

## Source evidence

- `apps/themuffinman/src/main/java/com/themuffinman/app/**/controller/*.java`
- `apps/themuffinman/frontend/src/api/httpClient.ts`
- `apps/themuffinman/frontend/src/modules/**/api/*`
- `apps/themuffinman/frontend/src/modules/app-shell/**`
- `apps/themuffinman/frontend/scripts/generate-vision-contracts.mjs`
- `apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts`
- `docs/agent-operating-model.yaml`
- `docs/agent-operating-model/sections/api.yaml`
- `docs/agent-operating-model/sections/intents.yaml`
- `docs/capability-inventory.yaml`
- `docs/runtime-acceptance-matrix.yaml`

## Conclusion

Round 9 establishes that the system has strong semantic/API catalog support but
only partial mechanical client/evidence traceability. The next improvement to
the system map is an endpoint-level consumer and evidence registry, with protocol
and authority classification included from the beginning.

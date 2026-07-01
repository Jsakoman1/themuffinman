# Legacy Backend Surface Cleanup Plan

## Status

Complete.

## Parent

- Master plan: `.agents/legacy-frontend-decommission-master-plan.md`
- Depends on:
  - `.agents/legacy-frontend-inventory-and-gap-plan.md`
  - `.agents/vision-capability-parity-plan.md`
  - `.agents/legacy-module-code-prune-plan.md`

## Objective

Define how backend and contract cleanup should follow the frontend decommission so the repository stops carrying read models, endpoints, and docs that existed only to support removed legacy screens.

## Cleanup Principle

Do not delete backend surfaces just because the legacy route is gone.

Delete only after classifying each surface as one of:

1. still used by Vision
2. still used by admin enclave
3. still used by temporary bridge routes
4. truly legacy-screen-only and removable

## Main Cleanup Target Areas

### 1. Workmarket dashboard and read-model endpoints

Likely cleanup candidates:

- dashboard-oriented aggregated reads that existed mainly for `/work`
- frontend-shaped DTOs optimized for route-era tabbed pages

Risk:

- some quest/application/detail reads may still power bridge routes or future Vision read depth

Cleanup rule:

- split “dashboard-only” from “detail/shared” before removal

### 2. Admin data endpoints

Current status:

- still in use by the admin enclave

Cleanup rule:

- do not remove admin endpoints during the first user-facing legacy decommission wave
- instead, document which ones remain strategic operator surfaces and which are later admin-standardization debt

### 3. Social/profile/settings read surfaces

Current status:

- still needed by temporary `/users/:id`, `/settings`, and `/circles` bridges

Cleanup rule:

- keep until Vision owns equivalent social/profile/account depth
- after migration, remove page-specific DTO assembly that only existed for those views

### 4. Chat read/write surfaces

Current status:

- still needed by `/chat` bridge and `OPEN_CHAT` handoff

Cleanup rule:

- keep chat domain APIs while Vision-native continuation is incomplete
- only later remove any route-era-specific adapter code if it exists

### 5. Frontend contracts and generated assumptions

Cleanup candidates:

- contracts that only describe removed route-era blocks
- frontend API wrapper assumptions that no longer match the reduced surface set

Cleanup rule:

- update shared contracts when route families are deleted
- keep generated/admin/vision contracts aligned with the actual surviving surfaces

## Documentation Cleanup Requirements

When backend/frontend legacy support is removed, update:

- [docs/business-logic.md](/Users/jsakoman/Desktop/themuffinman/docs/business-logic.md)
- [docs/domain-technical.md](/Users/jsakoman/Desktop/themuffinman/docs/domain-technical.md)
- [docs/product-memory.md](/Users/jsakoman/Desktop/themuffinman/docs/product-memory.md)
- [docs/product-vision.md](/Users/jsakoman/Desktop/themuffinman/docs/product-vision.md)
- [docs/vision-architecture-patterns.md](/Users/jsakoman/Desktop/themuffinman/docs/vision-architecture-patterns.md)

Also update the decommission planning chain if a temporary bridge becomes officially removed or officially retained.

## Endpoint And DTO Audit Sequence

### Pass 1: Mark temporary survivors

Mark endpoints and DTOs still used by:

- admin enclave
- detail bridges
- profile/settings bridges
- chat bridge
- Vision follow-up reads

### Pass 2: Mark route-era-only reads

Identify endpoints and DTOs that only served:

- `/work` dashboard composition
- `/business`
- `/things`
- `/rides`
- topbar notification/read surfaces that disappear with the legacy shell

### Pass 3: Remove or downgrade

For route-era-only backend surfaces:

- remove endpoint/controller wiring if truly unused
- remove DTO assemblers and mapper code if they have no surviving consumer
- otherwise downgrade them from “active product surface” to “deferred admin/internal debt” explicitly in docs

## Validation Requirements

1. backend tests covering touched read services must pass
2. frontend type-check/build must pass after contract cleanup
3. docs must stop describing removed routes as active product surfaces
4. no removed endpoint should remain referenced by surviving frontend wrappers

## Completion Note

The backend cleanup boundary is now explicit: remove dashboard-era and dropped-module support aggressively, keep shared auth/admin/chat/detail capabilities until their surviving consumers disappear, and treat docs/contracts cleanup as part of the same decommission work rather than a later optional pass.

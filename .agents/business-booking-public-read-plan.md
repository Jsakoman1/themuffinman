---
machine_kind: plan
machine_status: complete
machine_title: Business Booking Public Read Plan
machine_goal: Deliver the public business read surface for mini-site profile, offering catalog, and availability preview using backend-prepared DTOs before reservation mutation workflows are introduced.
---

# Business Booking Public Read Plan

## Status

Complete.

## Goal

Deliver the public business read surface for mini-site profile, offering catalog, and availability preview using backend-prepared DTOs before reservation mutation workflows are introduced.

## Parent Master Plan

- Master plan: `.agents/business-booking-implementation-master-plan.md`

## Continuation Rule

- Once this phase reaches its closeout gates, execution should continue automatically into `.agents/business-booking-workflow-plan.md` without a routine stop.
- Do not pause after this phase only to ask whether to continue unless a real blocker or approval need appears.

## Scope

- Included: public business page DTO, active offering catalog reads, public availability preview reads, and read-model assembly separation for public viewers.
- Excluded: booking creation, owner booking management, booking events, gallery upload/storage infrastructure, and search/ranking across all businesses.

## Why This Phase Exists

- The booking domain needs a stable public read contract before reservation writes can target it.
- Public pages should be backend-shaped DTOs, not ad-hoc joins exposed through profile endpoints.
- This phase is where the module stops being only an owner editor and becomes an actual public-facing business surface.

## Phase Analysis

### Problem Being Solved

If public booking reads were deferred until after booking writes:

- booking endpoints would have no stable public-facing business context
- frontend and future `/vision` flows would guess how to combine profile, offerings, and availability
- later public page changes would risk breaking booking contracts

### Architectural Decisions

- Add a dedicated `BusinessPublicReadService`.
- Keep public page DTO separate from owner profile DTO.
- Keep public offerings read-only and filtered to active entries.
- Keep availability windows as derived read DTOs rather than exposing raw scheduling rows.
- Keep booking-facing status and action semantics backend-owned so future booking reads can reuse one DTO grammar across customer and owner surfaces.
- Return timezone context on public booking-facing DTOs so the backend remains authoritative for time interpretation.

### Main Risks

- Reusing owner DTOs and leaking management fields publicly.
- Embedding policy internals directly in the public page.
- Exposing too much schedule detail before booking policy is enforced.

### Mitigations

- Define dedicated public DTOs with only viewer-safe fields.
- Return user-facing booking hints, not internal policy rows.
- Keep directory/profile slug routes stable and add new public page endpoints explicitly.

## Proposed Backend Package Work

Services:

- `business/service/BusinessPublicReadService.java`
- optional assembler helpers:
  - `BusinessPublicPageAssembler.java`
  - `BusinessPublicOfferingAssembler.java`

DTOs:

- `BusinessPublicPageDTO`
- `BusinessPublicOfferingDTO`
- `BusinessAvailabilityWindowDTO`
- `BusinessDirectoryItemDTO` if current profile list DTO needs refinement

Repository additions:

- fetch-safe public profile/offering reads
- active offering lookup by profile

## API Surface For This Phase

- `GET /business/public/{slug}`
- `GET /business/public/{slug}/offerings`
- `GET /business/public/{slug}/availability`

Possible retained routes:

- existing `GET /business/profiles`
- existing `GET /business/profiles/{slug}`

Decision note:

- locked decision: keep `GET /business/profiles/{slug}` as the simpler legacy profile route and add `GET /business/public/{slug}` as the richer booking-facing contract.

## Implementation Slices

- [ ] Slice 1: define public DTO contract for business page, offering catalog, and availability windows.
- [ ] Slice 2: implement active offering public reads and public business page assembly.
- [ ] Slice 3: wire derived availability output into the public read path.
- [ ] Slice 4: decide whether legacy slug route stays simple or forwards to the richer public page assembler.
- [ ] Slice 5: cover public visibility and inactive profile/offering behavior with focused tests.

## Validation Plan

- Service tests for:
  - inactive profile hidden from public route
  - inactive offerings hidden from catalog
  - availability route rejects disabled or missing business appropriately
- Controller tests for public anonymous/authenticated access shape according to current auth rules

## Expected Docs and Artifacts

- `docs/business-logic.md`
- `docs/domain-technical.md`
- `docs/source-of-truth-inventory.md`
- frontend contract generation later when DTOs become part of the shared contract layer

## Closeout Gates

- Public business mini-site reads have a stable backend contract.
- Public reads do not reuse owner management DTOs.
- Future booking writes can depend on a stable public slug plus offering surface.

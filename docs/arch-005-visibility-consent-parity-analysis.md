# ARCH-005 Visibility, Consent, and Cross-Client Parity Analysis

Reviewed: ARCH-005 baseline pass.

## Current architecture

Identity, social, location, and module services collectively enforce visibility. The backend remains the authority; frontend action flags and Vision presentation fields are projections. Existing matrices cover profile, quests, bookings, chat, sharing, exact location, and native handoff.

## What is strong

- Exact location has explicit OFF/APPROXIMATE/EXACT policy states and fail-closed evidence.
- Circle membership is not treated as global consent.
- Chat and Things have targeted boundary tests and recovery scenarios.
- Vision and native handoff contracts keep domain permissions and transitions in backend services.
- Runtime evidence distinguishes web/browser proof from future device proof.

## Main gaps

| Gap | Current truth | Risk |
|---|---|---|
| Distributed policy | Identity/social/location consumers each own part of visibility | A new consumer can expose a field without updating every matrix |
| Field-level parity | Resource/action matrices are stronger than field-by-field output inventory | Web, Vision, and native may receive different privacy-safe projections |
| Consent lifecycle | Current policy inputs are documented, but revocation propagation and stale sessions need runtime proof | Previously visible data can remain cached or shown after revoke |
| Vision delegation | Backend delegation exists, but cross-module viewer context and denial parity need scenario evidence | Vision could explain or offer an action the canonical module rejects |
| Native/device proof | Contracts and handoff endpoints exist; no native source/device/offline/background proof exists | Contract readiness may be mistaken for client support |

## Decision

The next safe slice is a field/resource parity registry and runtime acceptance queue. It does not merge privacy policy into a new central service and does not claim native support. Each module retains authority while consumers must reference the same viewer-scoped projection and consent state.

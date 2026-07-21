# Implementation Backlog

## Current open plan

WORKSPACE-APP-SHELL-FIRST-LAYER [PLANNED]: Implement the first app-like workspace transition from
`docs/work/workspace-app-shell-first-layer.yaml`. Keep canonical routes and domain surfaces stable while adding a
backend-prepared module navigation tree with relevant nested destinations, counts, unread/attention markers,
responsive drawer behavior, and explicit backend/frontend ownership boundaries. Do not make Vision a persistent
sidebar dashboard.

## Deferred capability gaps

These gaps remain visible in `docs/capability-inventory.yaml`, but they do not have an open implementation plan until a
future slice is explicitly selected:

- Account recovery: delivery provider, distributed abuse controls, valid-token runtime proof, and later native clients.
- Profile visibility: circle-scoped consent, recipient selection, Vision handoff, and later native presentation.
- Search comparison: richer family-specific fields and broader acceptance beyond the verified bounded slice.
- Native clients: iPhone and Apple Watch clients, device permissions, offline/background behavior, and native runtime
  acceptance.
- Rides: future residual acceptance work only where the inventory still records a gap.

## Planning rule

The UI action-integrity closeout uses the canonical action matrix and runtime scenario catalog; it does not create screenshot or smoke-test artifacts.

Capability status and implementation-plan status are separate. Update the capability record in the canonical inventory
when product support changes; add a new `docs/work/*.yaml` plan only when a specific capability slice is opened. When a
plan is verified, run closeout cleanup and remove temporary validation outputs, screenshots, smoke traces, and
unreferenced plan artifacts.

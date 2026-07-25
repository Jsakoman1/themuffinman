# Implementation Backlog

The repository is in maintenance mode. Product capability status remains authoritative in
`docs/capability-inventory.yaml`; mapping and runtime truth remain in the canonical registries
listed in `docs/implementation-control.md`.

## Open follow-ups

- ENTITY-SURFACE-RESOURCE-CONTRACT-001: define a typed Business resource configuration contract
  before exposing a complete Web edit surface.
- RUNTIME-PENDING-TRIAGE-001: keep the 14 explicitly pending runtime boundaries classified in
  `docs/runtime-acceptance-matrix.yaml` and its runtime closeout registry.
- VISION-PROMPT-007: capture complete browser evidence for direct-message collection, confirmation,
  send, and authoritative Chat readback.
- VISION-PROMPT-010: retain the remaining real-provider and production-provider evidence boundary.
- OPT-BASELINE-002: continue endpoint-to-capability-to-client-to-test evidence reconciliation.
- OPT-BASELINE-003: maintain machine-checkable dependency direction rules and accepted exceptions.
- OPT-BASELINE-005: preserve explicit native/device and production-operation evidence boundaries.

## Mapping sources retained

- `docs/system-map.md` and the System Map registries.
- `docs/capability-inventory.yaml` and `docs/target-capability-catalog.yaml`.
- `docs/work/action-contract-matrix.yaml`.
- `docs/work/frontend-route-action-matrix.yaml`.
- `docs/work/frontend-surface-archetype-matrix.yaml`.
- `docs/work/home-query-scope-contract.yaml`.
- `docs/work/ui-action-integrity-runtime-scenarios.yaml`.
- `docs/runtime-acceptance-matrix.yaml` and runtime evidence registries.

## Operating rule

Do not create a new `docs/work/*.yaml` plan for routine maintenance. Add a plan only when a
specific, approved implementation slice needs serial execution and verifier evidence. Completed
plans and execution inventories are removed after their durable mappings, contracts, and runtime
evidence have been retained.

# Implementation Backlog

The repository is in maintenance mode. Product capability status remains authoritative in
`docs/capability-inventory.yaml`; mapping and runtime truth remain in the canonical registries
listed in `docs/implementation-control.md`.

## Open follow-ups

- BUSINESS-UX-HUMAN-FIRST-001: simplify the owner setup, service configuration,
  booking rules, hours, and customer booking journeys so ordinary businesses can
  publish and accept appointments without understanding internal booking-engine terms.
- SIDEJOBS-HUMAN-FIRST-001: execute the prepared `docs/work/sidejobs-human-first-master.yaml` to replace Work/Quest-facing language with SideJobs, publish backend-prepared human summaries, and simplify discovery, posting, requests, and owner decisions without renaming persisted Quest storage in the first migration.
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
- DEVTOOL-RUNTIME-SKILL-CREATION-001: in a separate explicitly authorized task, use
  the system skill-creator to package the verified runtime-evidence harness workflow;
  retain the repository work verifier as the sole completion authority. Read the
  readiness decision in `docs/runtime-evidence-skill-decision-2026-07-31.yaml` and
  the measured boundary in `docs/developer-system-operational-readiness-closeout-2026-07-31.yaml`.

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

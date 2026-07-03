---
machine_kind: plan
machine_status: complete
machine_title: Machine-Readable-First Policy Plan
machine_goal: Define the authoritative hierarchy for active state, generated control,
  and archive material so current behavior is always read from machine-readable sources
  first.
---

# Machine-Readable-First Policy Plan

## Status

Complete.

## Goal

Define the authoritative hierarchy for active state, generated control, and archive material so current behavior is always read from machine-readable sources first.

## Scope

- Included:
  - `docs/control-surface-map.md`
  - `docs/program-planning-model.md`
  - source-of-truth references that enumerate live truth, generated control, and archive surfaces
- Excluded:
  - workflow entrypoint wording
  - startup route guidance
  - feature behavior changes

## Implementation Slices

1. Make the live-truth / generated-control / historical-archive split explicit and concise.
2. Add a clear rule that durable operational state should live in machine-readable files first.
3. Clarify when a human-readable doc is authoritative versus merely explanatory.

## Validation Plan

- Manual docs consistency review
- Spot-check the control-surface map against active inventories

## Completion Evidence

- Status: complete
- Validation evidence:
  - `make audit-todo`
  - `make generate-agent-operating-model`
  - `./mvnw -Dtest=AgentOperatingModelValidationTest test`
- Doc delta summary:
  - `docs/control-surface-map.md`
  - `docs/program-planning-model.md`
  - `docs/source-of-truth-inventory.md`
  - `docs/agent-operating-model/sections/metadata.yaml`
  - `docs/agent-operating-model.yaml`

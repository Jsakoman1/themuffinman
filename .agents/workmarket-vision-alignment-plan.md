---
machine_kind: plan
machine_status: complete
machine_title: Workmarket Vision Alignment Plan
machine_goal: Shift the remaining quest-adjacent HTTP ownership from vision controllers onto workmarket entrypoints while keeping vision as the DTO and facade adapter layer.
---

# Workmarket Vision Alignment Plan

## Status

Complete.

## Goal

Shift the remaining quest-adjacent HTTP ownership from vision controllers onto workmarket entrypoints while keeping vision as the DTO and facade adapter layer.

## Scope

- Included: dashboard, quest news, and user review HTTP entrypoints; operating-model source references; workmarket capsule docs.
- Excluded: dashboard vision prompt internals, DTO package moves, and full deletion of vision facades.

## Slices

- [x] Move dashboard, quest news, and user review controllers under `workmarket.controller` without changing public routes.
- [x] Update source-of-truth and API ownership docs to point at the new workmarket controller classes.
- [x] Run targeted validation for the moved HTTP entrypoints before deciding whether the wider vision cleanup is ready.

## Validation

- `./mvnw -q -Dtest=ServiceLayeringConventionTest,AgentOperatingModelValidationTest test`
- `make generate-agent-operating-model`
- `make generate-agent-artifacts`

## Completion Evidence

- Status: complete
- Validation evidence:
  - `./mvnw -q -Dtest=ServiceLayeringConventionTest,AgentOperatingModelValidationTest test`
  - `make generate-agent-operating-model`
  - `make generate-agent-artifacts`
  - `make generate-frontend-contracts`
  - `make audit-generated-artifact-freshness`
- Doc delta summary: dashboard, quest news, and user review controller ownership now points at `workmarket.controller` in README, source-of-truth inventory, and operating-model API/source refs while routes remain unchanged.
- Remaining cleanup boundary: vision still owns DTO contracts, conversation surfaces, and facade adapters; the HTTP ownership migration for quest-adjacent workmarket surfaces is complete.

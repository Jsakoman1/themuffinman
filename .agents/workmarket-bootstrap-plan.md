---
machine_kind: plan
machine_status: complete
machine_title: Workmarket Bootstrap Plan
machine_goal: Establish the first real `com.themuffinman.app.workmarket` HTTP entrypoints
  and align the layering contract with the extracted workmarket service boundary.
---

# Workmarket Bootstrap Plan

## Status

Complete.

## Goal

Establish the first real `com.themuffinman.app.workmarket` HTTP entrypoints and align the layering contract with the extracted workmarket service boundary.

## Parent Master Plan

- Master plan: `.agents/workmarket-extraction-master-plan.md`

## Scope

- Included: quest and quest-application controller ownership, workmarket bootstrap documentation, and control-surface alignment for the first extracted entrypoints.
- Excluded: repository/model package moves, DTO package split, and `/vision` conversation orchestration.

## Implementation Slices

- [x] Slice 1: confirm the extracted workmarket service boundary and identify the first controller adapters that still live under `vision`.
- [x] Slice 2: move quest and quest-application HTTP entrypoints into `workmarket.controller` without changing API routes or DTO contracts.
- [x] Slice 3: align source-of-truth, API inventory, and workmarket bootstrap docs with the new controller ownership.
- [x] Slice 4: validate the layering contract and targeted backend tests for the moved entrypoints.

## Validation Plan

- Targeted checks: `./mvnw -q -Dtest=ServiceLayeringConventionTest test`
- Broader checks: targeted controller/service tests for quest and quest-application facades if compilation or ownership drift appears.
- Skipped checks or reasons: full backend suite deferred until the repository/model migration slice widens the change surface.

## Docs and Artifacts

- Expected docs: `docs/source-of-truth-inventory.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`
- Expected generated artifacts: source-of-truth audit and any endpoint/controller inventory that follows `docs/agent-operating-model/sections/`

## Closeout Gates

- Required closeout checks: workmarket owns the quest/application HTTP controllers, the plan and master plan match the real state, and generated operating-model artifacts are refreshed.
- Final response evidence: the first workmarket entrypoints are real backend surfaces rather than only service-layer ownership.

## Completion Evidence

- Status: complete
- Validation evidence: `./mvnw -q -Dtest=ServiceLayeringConventionTest,VisionQuestFacadeServiceTest,VisionQuestApplicationFacadeServiceTest test`, `make generate-agent-operating-model`, `make generate-agent-artifacts`, `./mvnw -q -Dtest=AgentOperatingModelValidationTest test`, `make control-refresh-full`
- Doc delta summary: quest and quest-application HTTP entrypoints now belong to `workmarket.controller`, and the operating-model inventories now recognize that ownership.
- Deferred work: DTO package split and repository/model moves stay with later child plans.

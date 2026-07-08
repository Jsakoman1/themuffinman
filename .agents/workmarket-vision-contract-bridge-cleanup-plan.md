---
machine_kind: plan
machine_status: complete
machine_title: Workmarket Vision Contract Bridge Cleanup Plan
machine_goal: Centralize the remaining workmarket-to-vision enum bridge so the DTO adapter boundary is explicit, narrow, and easier to audit.
---

# Workmarket Vision Contract Bridge Cleanup Plan

## Status

Complete.

## Goal

Centralize the remaining workmarket-to-vision enum bridge so the DTO adapter boundary is explicit, narrow, and easier to audit.

## Scope

- Included: quest, application, review, and news enum conversion helpers plus mapper call-site cleanup.
- Excluded: moving DTO packages out of `vision` and changing public API contracts.

## Slices

- [x] Introduce one explicit workmarket-to-vision contract bridge for the remaining enum conversions.
- [x] Replace scattered `valueOf(name())` conversions in workmarket mappers and facade adapters with the centralized bridge.
- [x] Add focused tests and update boundary docs to reflect the narrower adapter layer.

## Validation

- `./mvnw -q -Dtest=VisionQuestFacadeServiceTest,VisionQuestApplicationFacadeServiceTest,WorkmarketVisionContractBridgeTest test`
- `./mvnw -q -Dtest=AgentOperatingModelValidationTest test`
- `make generate-agent-artifacts`
- `make control-refresh-full`

## Completion Evidence

- Status: complete
- Validation evidence:
  - `./mvnw -q -Dtest=VisionQuestFacadeServiceTest,VisionQuestApplicationFacadeServiceTest,WorkmarketVisionContractBridgeTest test`
  - `./mvnw -q -Dtest=AgentOperatingModelValidationTest,VisionQuestFacadeServiceTest,VisionQuestApplicationFacadeServiceTest,WorkmarketVisionContractBridgeTest test`
  - `make generate-agent-operating-model generate-agent-artifacts`
  - `make generate-frontend-contracts`
  - `make control-refresh-full`
- Doc delta summary: the remaining quest, application, review, and news enum conversions between workmarket and the legacy vision DTO contract now flow through one explicit `WorkmarketVisionContractBridge` instead of being scattered across multiple mapper and facade call sites.
- Remaining boundary: request-to-domain DTO conversion still lives in the relevant workmarket use-case/update services by design because those paths translate incoming contract DTOs into domain state, not domain state back into the legacy DTO contract.

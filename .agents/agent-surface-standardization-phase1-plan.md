# Agent Surface Standardization Phase 1 Plan

## Workflow Frame

- Feature tier: tier4-agent-tooling-workflow
- Scope: establish shared surface policy boundaries and ship the first guarded admin execution capability
- Out of scope: generic admin execution across all entities, destructive automation, and broad Vision semantic refactors
- Manifest decision: required
- Manifest path: pending resolver

## Routing Snapshot

- Context commands: `make codex-context topic=agent-surfaces intent='standardize vision and admin playground execution boundaries'`
- Routing commands: `make audit-router`, `make audit-doc-sync-required-surfaces`, `make audit-manifest-decision`, `make resolve-manifest-path`
- Validation commands: targeted backend tests, `npm run type-check`, `npm run build`, `./mvnw test -Dtest=AgentOperatingModelValidationTest`
- Closeout commands: `make audit-todo`, `make audit-plan-completion`

## Implementation Slices

- [x] Slice 1: Add shared agent surface policy abstractions and apply them to Vision and Admin Playground boundaries.
- [x] Slice 2: Add a reusable admin execution planner for synthetic quest batch generation.
- [x] Slice 3: Add the admin execution endpoint/service plus the first frontend execution affordance.
- [x] Slice 4: Sync docs, contracts, agent-operating surfaces, and validation evidence.

## Validation Plan

- Targeted checks: admin agent service tests, admin execution tests, targeted vision tests if policy wiring changes them
- Broader checks: frontend type-check/build and agent-operating validation
- Skipped checks or reasons: pending

## Docs and Artifacts

- Expected docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/vision-architecture-patterns.md`
- Expected generated artifacts: `docs/agent-operating-model.yaml`, frontend generated contracts if DTO surface changes

## Closeout Gates

- Required closeout checks: manifest-backed validation and plan completion audit
- Final response evidence: changed policy boundary, first admin execution slice, validation, residual risk

## Open Questions

- Resolver outputs still needed: final manifest path
- Risks or approvals: none expected for workspace-only implementation

## Completion Evidence

- Status: complete for the Phase 1 guarded execution objective
- Changed files:
  - shared agent surface policy classes for admin and vision boundaries
  - admin prompt preparation, synthetic execution planner, target-user resolver, and execution service
  - admin execution request/response DTOs and `/admin/agent/execute` endpoint
  - admin playground response contract, admin UI execute preview/confirm affordance, and generated/frontend contract sync
  - business, domain, vision, and agent-operating docs plus regenerated machine-operating artifacts
- Validation evidence:
  - `cd apps/themuffinman && ./mvnw test -Dtest=AdminAgentPlaygroundServiceTest,AdminAgentCapabilityBoundaryTest,AdminAgentGoldenPromptMatrixTest,AdminSyntheticQuestExecutionPlannerTest,AdminAgentExecutionServiceTest,VisionConversationServiceTest` passed
  - `cd apps/themuffinman/frontend && npm run type-check` passed
  - `cd apps/themuffinman/frontend && npm run validate:contracts` passed
  - `cd apps/themuffinman/frontend && npm run build` passed
  - `cd apps/themuffinman && ./mvnw test -Dtest=AgentOperatingModelValidationTest` passed
- Doc delta summary:
  - documented the shared user-scoped versus admin-scoped surface policy split
  - documented the first guarded admin direct execution slice for synthetic quest batches
  - updated agent-operating API, intent, contract, permission, and workflow inventories
- Backlog update: none
- Residual risk: broader shared semantic planning between Vision and Admin Playground still belongs to later phases of the master plan

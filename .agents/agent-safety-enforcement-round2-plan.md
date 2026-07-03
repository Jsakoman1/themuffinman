---
machine_kind: plan
machine_status: unknown
machine_title: Agent Safety Enforcement Round 2 Plan
---

# Agent Safety Enforcement Round 2 Plan

Purpose: extend the control chain with additional enforcement layers that reduce manual review burden for future feature work.

## Scope

- [x] Add reusable temporary plan template and a documented naming rule.
- [x] Add a documented rule for when to create a temporary implementation plan.
- [x] Add read-model producer coverage enforcement beyond DTO field existence.
- [x] Add a capability boundary test suite for negative agent behavior.
- [x] Add a machine-readable feature completion manifest pattern and validate it.
- [x] Add dead-path tracking for automation-critical prompt coverage and stale machine references.
- [x] Add a frontend contract gate for admin-agent planner response shape.
- [x] Extend audit commands to include the new relevant gates.
- [x] Sync docs, policies, and checklists.
- [x] Run backend and frontend validation before closing the plan.

## Working Notes

- Prefer extending the existing YAML/schema/validator chain instead of introducing a second control system.
- Keep the feature manifest small and enforceable rather than narrative.
- Treat frontend contract checks as a narrow gate around planner/admin-agent response shape, not a general-purpose frontend schema system.

## Completion Check

- [x] Backend validation passes.
- [x] Frontend type-check passes.
- [x] Audit command covers the intended backend and frontend checks.
- [x] This plan is updated to reflect actual completion state.

## Outcome

- Added reusable `.agents` templates for plan-driven feature work and machine-readable completion manifests.
- Added manifest schema validation and current-feature manifest coverage.
- Extended the agent-operating validator with producer source checks, frontend contract checks, dead-path tracking, and manifest validation.
- Added a dedicated capability boundary test suite and kept the multilingual golden prompt matrix as executable coverage.
- Added a narrow frontend contract gate for the admin-agent planner response shape and included frontend type-check in `make audit-agent-safety`.
- Verified with `make audit-agent-safety`, full backend `./mvnw test`, and frontend `npm run build`.

## Completion Evidence

- Status: complete
- Changed files: Historical completed plan; see `.agents/feature-manifests/agent-safety-enforcement-round2-manifest.yaml` for the structured artifact list.
- Validation evidence: Historical completed plan; see `.agents/feature-manifests/agent-safety-enforcement-round2-manifest.yaml` for validation commands recorded during manifest migration.
- Doc delta summary: Historical completed plan; see `.agents/feature-manifests/agent-safety-enforcement-round2-manifest.yaml` for docs and generated-artifact coverage.
- Backlog update: Historical completed plan; see `.agents/feature-manifests/agent-safety-enforcement-round2-manifest.yaml` for backlog review state.
- Residual risk: none recorded in the historical manifest.

# Agent Safety Upgrade Plan

Purpose: implement the next automation-enforcement package with a temporary working document that tracks scope, status, and final validation.

## Scope

- [x] Add mutation risk classification to the machine-operating model.
- [x] Add intent-to-endpoint write safety coverage validation.
- [x] Add a multilingual golden prompt matrix skeleton with executable tests.
- [x] Add a local audit command for agent-safety checks.
- [x] Add documentation coverage manifest validation for automation-relevant source surfaces.
- [x] Sync human docs and policy docs for the new control layers.
- [x] Run validation and close the plan only after confirming every planned item shipped.

## Working Notes

- Prefer extending the existing operating-model validator instead of creating a parallel validator.
- Keep the new controls deterministic and source-of-truth driven.
- Treat the golden prompt matrix as both a regression test and a lightweight planning artifact.

## Completion Check

- [x] Backend tests pass.
- [x] Agent-operating validation covers the new sections.
- [x] Audit command runs the intended checks.
- [x] This file is updated to reflect actual completion state before closing the task.

## Outcome

- Implemented `intent_safety_catalog` in `docs/agent-operating-model.yaml` with risk groups plus explicit destructive, multi-actor, current-location, and exact-target-resolution requirements.
- Implemented `documentation_coverage` manifest in `docs/agent-operating-model.yaml` and validator coverage for auto-scanned controllers, mappers, and agent services.
- Extended `AgentOperatingModelValidationTest` to enforce the new machine contracts.
- Added executable multilingual golden prompt matrix coverage through `AdminAgentGoldenPromptMatrixTest` and `src/test/resources/agent/admin-agent-golden-prompt-matrix.yaml`.
- Added `make audit-agent-safety` at repo and app level.
- Verified with `make audit-agent-safety` and full `./mvnw test`.

## Completion Evidence

- Status: complete
- Changed files: Historical completed plan; see `.agents/feature-manifests/agent-safety-upgrade-manifest.yaml` for the structured artifact list.
- Validation evidence: Historical completed plan; see `.agents/feature-manifests/agent-safety-upgrade-manifest.yaml` for validation commands recorded during manifest migration.
- Doc delta summary: Historical completed plan; see `.agents/feature-manifests/agent-safety-upgrade-manifest.yaml` for docs and generated-artifact coverage.
- Backlog update: Historical completed plan; see `.agents/feature-manifests/agent-safety-upgrade-manifest.yaml` for backlog review state.
- Residual risk: none recorded in the historical manifest.

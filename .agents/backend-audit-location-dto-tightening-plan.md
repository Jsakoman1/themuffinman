# Backend Audit Location DTO Tightening Plan

Purpose: promote the location DTO contract surface into the strict `automation_relevant` subset after agent, chat, and identity DTO coverage.

## Scope

- [x] Create this plan before substantial edits because the change touches machine docs, source registration, generated artifacts, and validation.
- [x] Add a dedicated strict backend-audit rule for the location DTO surface.
- [x] Register location DTO files in source-of-truth and documentation coverage.
- [x] Regenerate artifacts and keep validation green.
- [x] Close this plan after docs and tests reflect the final state.

## Working Notes

- Keep the scope narrow: location DTOs only.
- This slice is still low-noise and directly useful for agent-safe resolution, debug, and settings contracts.

## Completion Check

- [x] Location DTOs are source-registered.
- [x] Location DTOs are documentation-covered.
- [x] Agent operating model validation passes.
- [x] Backend test suite passes.

## Completion Evidence

- Status: complete
- Changed files: Historical completed plan; see `.agents/feature-manifests/backend-audit-location-dto-tightening-manifest.yaml` for the structured artifact list.
- Validation evidence: Historical completed plan; see `.agents/feature-manifests/backend-audit-location-dto-tightening-manifest.yaml` for validation commands recorded during manifest migration.
- Doc delta summary: Historical completed plan; see `.agents/feature-manifests/backend-audit-location-dto-tightening-manifest.yaml` for docs and generated-artifact coverage.
- Backlog update: Historical completed plan; see `.agents/feature-manifests/backend-audit-location-dto-tightening-manifest.yaml` for backlog review state.
- Residual risk: none recorded in the historical manifest.

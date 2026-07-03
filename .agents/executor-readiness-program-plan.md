---
machine_kind: plan
machine_status: unknown
machine_title: Executor Readiness Program Plan
---

# Executor Readiness Program Plan

Purpose: implement the next large executor-readiness and feature-delivery control package in safe sequential phases.

## Phase Plan

### Phase 1: Process automation foundation

- [x] Persist workspace-edit preference and phased-work rule in repo instructions.
- [x] Add a bootstrap command that creates a temp plan and feature manifest from templates.
- [x] Add a feature close-out audit command that checks plan, manifest, and required validation commands.
- [x] Add a machine-readable risk-tier system that future phases can reuse.

### Phase 2: Agent execution-prep controls

- [x] Add dry-run executor-prep simulation contract and tests.
- [x] Add intent lineage tracking.
- [x] Add resolution confidence model.
- [x] Add composite workflow validation.
- [x] Add capability registry.
- [x] Add prompt drift detection.

### Phase 3: Backend enforcement expansion

- [x] Add backend contract snapshots.
- [x] Add service workflow inventory validation.
- [x] Add permission matrix artifact.
- [x] Add state-transition audit.
- [x] Add request validation completeness gate.

### Phase 4: Frontend enforcement expansion

- [x] Replace manual planner contracts with generated or machine-verified contract flow where practical.
- [x] Add automation-safe UI contract layering.
- [x] Add negative frontend workflow tests.
- [x] Add admin-agent regression tests.
- [x] Add feature-scoped frontend manifest expectations.

## Working Notes

- Complete one phase fully before starting the next phase.
- Run relevant gates after each phase.
- Keep the plan updated as an active control document, not a stale checklist.

## Completion Check

- [x] Phase 1 complete
- [x] Phase 2 complete
- [x] Phase 3 complete
- [x] Phase 4 complete
- [x] Backend validation green
- [x] Frontend validation green
- [x] This plan updated to reflect actual delivered scope

## Current Outcome

- Phase 1 is complete.
- `make bootstrap-feature-work` now creates a plan plus feature manifest from templates.
- `make feature-closeout-audit` now checks manifest and plan linkage plus risk-tier-driven required checks.
- Feature manifests now include `riskTier`, and validator rules enforce basic audit expectations for higher-risk work.
- Phase 2 is complete.
- Admin-agent dry-run simulation now exposes selected intent, endpoint plan, capability assessments, intent lineage, blocking reasons, and deterministic resolution confidence without mutating state.
- Capability registry, prompt drift fingerprints, and composite workflow validation are now enforced through the YAML contract plus `AgentOperatingModelValidationTest`.
- Validation is green for `make audit-agent-safety`, `npm run type-check`, and full backend `./mvnw test`.
- Phase 3 is complete.
- Backend contract snapshots now lock key planner, user, quest, application, and chat DTO shapes against silent drift.
- Service workflow inventory, permission matrix coverage, state-transition audit, and request-validation completeness are now enforced through the YAML contract plus `AgentOperatingModelValidationTest`.
- Validation remains green for `make audit-agent-safety`, `npm run type-check`, and full backend `./mvnw test`.
- Phase 4 is complete.
- Frontend planner and dry-run contracts now prefer generated aliases over hand-maintained duplicate DTO types.
- The admin-agent page now uses an automation-safe safety view model that separates informational planner output from execution-blocking ambiguity, confirmation, translation, multi-actor, current-location, and simulation-safety signals.
- Frontend regression coverage now validates explicit negative planner and dry-run scenarios plus one safe path through `npm run validate:admin-agent-ui`.
- Frontend contract generation, regression fixtures, and feature-scoped expectations are now enforced through the YAML contract plus `AgentOperatingModelValidationTest`.
- Final validation should stay green for `make audit-agent-safety`, `npm run type-check`, `npm run build`, and full backend `./mvnw test`.

## Completion Evidence

- Status: complete
- Changed files: Historical completed plan; see `.agents/feature-manifests/executor-readiness-program-manifest.yaml` for the structured artifact list.
- Validation evidence: Historical completed plan; see `.agents/feature-manifests/executor-readiness-program-manifest.yaml` for validation commands recorded during manifest migration.
- Doc delta summary: Historical completed plan; see `.agents/feature-manifests/executor-readiness-program-manifest.yaml` for docs and generated-artifact coverage.
- Backlog update: Historical completed plan; see `.agents/feature-manifests/executor-readiness-program-manifest.yaml` for backlog review state.
- Residual risk: none recorded in the historical manifest.

# Validation Memory Closeout Hardening Plan

## Goal

Standardize validation-memory closeout support by making drift checks part of the standard closeout loop and by generating a compact closeout command card from the canonical validation-memory JSON.

## Tasks

- [x] Add `audit-validation-memory-drift` to the standard closeout-sensitive audit flow without weakening existing checks.
- [x] Add a generated validation-memory closeout card report and expose it through Make and the audit registry.
- [x] Sync validation-memory docs and tooling references with the new closeout surfaces.
- [x] Run targeted validation for the new script, audit flow, and documentation surfaces.

## Completion Evidence

- Status: Complete
- Notes:
  - Added `validation-memory-closeout-card` generator, Make target, and audit-registry entry.
  - Added validation-memory drift rerun to root `audit-agent-safety` and to `scripts/feature-closeout-audit.sh`.
  - Synced validation-memory, fast-path, workflow, checklist, policy, agent-operating, and AGENTS references.
  - Validated with `make validation-memory-closeout-card`, `make audit-validation-memory-drift`, `make generate-audit-registry-artifacts`, `make audit-documentation`, `./mvnw test -Dtest=AgentOperatingModelValidationTest`, and `make feature-closeout-audit manifest=.agents/feature-manifests/vision-voice-surface-manifest.yaml`.

# CODEX Context Gateway Integration Plan

Purpose: integrate the gateway with existing local audits instead of duplicating their logic.

## Tasks

- [x] Reuse the existing Ruby tooling stack and `Makefile` conventions.
- [x] Register gateway targets in the local audit registry.
- [x] Wrap existing outputs for repo map, symbol index, targeted tests, validation matrix, dependency graph, usage graph, delta report, session handoff, hotspot ranking, and plan-code-map.
- [x] Default to reusing existing static generated outputs to avoid unnecessary churn.
- [x] Force refresh only for scoped/dynamic provider families where stale output would be misleading.

## Completion Evidence

- Status: complete
- Changed files: `scripts/audits/local_tooling_extended_tools.rb`, `scripts/local_tooling_common.rb`, `docs/tooling/codex-local-audits.yml`
- Validation evidence: `make generate-audit-registry-artifacts`, `make audit-summary-index`
- Residual risk: provider freshness depends on existing report quality for static wrapper packs.

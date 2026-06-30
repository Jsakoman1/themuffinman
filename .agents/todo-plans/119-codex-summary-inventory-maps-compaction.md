# Codex Summary Inventory Maps Compaction

Purpose: compact the biggest inventory and map-style summaries so the first read stays short and decision-first.

## Goal

Shorten the largest inventory, map, and graph summaries without losing the top sample entries or counts.

## Scope

- `docs/generated/local-tooling/api-contract-drift-summary.md`
- `docs/generated/local-tooling/domain-packs/workmarket-summary.md`
- `docs/generated/local-tooling/domain-packs/agent-summary.md`
- `docs/generated/local-tooling/plan-code-maps/codex-local-context-gateway-master-plan-summary.md`
- `docs/generated/local-tooling/plan-code-maps/86-codex-local-manifest-path-resolver-summary.md`
- `docs/generated/local-tooling/mapper-usage-audit-summary.md`
- `docs/generated/local-tooling/hotspots-summary.md`
- `docs/generated/local-tooling/backend-dependency-graph-summary.md`
- `docs/generated/local-tooling/frontend-usage-graph-summary.md`
- `docs/generated/local-tooling/read-surface-inventory-summary.md`
- `docs/generated/local-tooling/architecture-decision-index-summary.md`
- `docs/generated/local-tooling/dto-usage-packs/dashboardsectionsdto-summary.md`
- `docs/generated/local-tooling/manifest-path-resolution-summary.md`
- `docs/generated/local-tooling/symbol-test-links/questservice-summary.md`
- `docs/generated/local-tooling/test-gap-recommendations-summary.md`

## Checklist

- [x] Tighten the summary renderer for inventory and graph-like payloads.
- [x] Regenerate the affected summaries.
- [x] Compare output size and confirm the top evidence is still readable.

## Validation

- `ruby -c scripts/audits/local_tooling_extended_tools.rb`
- `make audit-api-contract-drift`
- `make domain-pack domain=workmarket`
- `make domain-pack domain=agent`
- `make plan-code-map plan=.agents/codex-local-context-gateway-master-plan.md`
- `make plan-code-map plan=.agents/todo-plans/86-codex-local-manifest-path-resolver.md`
- `make audit-mapper-usage`
- `make hotspots`
- `make backend-dependency-graph`
- `make frontend-usage-graph`
- `make read-surface-inventory`
- `make architecture-decision-index`
- `make dto-usage-pack dto=dashboardsectionsdto`
- `make resolve-manifest-path`
- `make link-symbol-to-tests symbol=QuestService`
- `make audit-test-gap-recommendations`

## Completion Evidence

- Status: complete
- Execution status: complete
- Persistent backlog item: none
- Primary source files: `scripts/audits/local_tooling_extended_tools.rb`, `scripts/audits/audit-api-contract-drift.rb`, `scripts/audits/audit-mapper-usage.rb`, `scripts/audits/generate-architecture-decision-index.rb`

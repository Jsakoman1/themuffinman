---
machine_kind: plan
machine_status: complete
machine_title: Codex Control Surface Hierarchy Plan
machine_goal: Standardize how the repo describes live truth, generated control, and
  historical archive material so Codex always knows which files are authoritative
  for current work.
---

# Codex Control Surface Hierarchy Plan

## Status

Complete.

## Goal

Standardize how the repo describes live truth, generated control, and historical archive material so Codex always knows which files are authoritative for current work.

## Scope

- Included:
  - `docs/control-surface-map.md`
  - `docs/generated/README.md`
  - `docs/source-of-truth-inventory.md`
  - `docs/documentation-sync-policy.md`
  - `AGENTS.md`
  - any generated docs that still blur live and archive semantics
- Excluded:
  - feature behavior changes
  - archive files that are intentionally historical and not meant to be normalized

## Implementation Slices

1. Tighten live-truth vs generated-control wording in the core docs.
2. Remove ambiguous phrases that treat summaries as if they were authoritative state.
3. Ensure active inventories and source maps point directly to the machine-readable source first.

## Validation

- `make audit-doc-canonical-phrases`
- `make audit-plan-completion plan=<plan-file>`

## Completion Evidence

- Status: complete
- Validation evidence:
  - `make audit-doc-canonical-phrases`
  - `make audit-summary-index`
  - `ruby scripts/generate-source-of-truth-audit.rb`
  - `make codex-context topic=codex-context-review intent='refresh local tooling compactification and hierarchy docs'`
  - `make audit-generated-artifact-freshness`
- Doc delta summary:
  - `docs/control-surface-map.md`
  - `docs/generated/README.md`
  - `docs/source-of-truth-inventory.md`
  - `docs/documentation-sync-policy.md`
  - `scripts/audits/local_tooling_extended_tools.rb`
  - `docs/generated/local-tooling/audit-summary-index.md`
  - `docs/generated/local-tooling/generated-artifact-freshness-summary.md`
  - `docs/generated/source-of-truth-audit.json`

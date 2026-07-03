---
machine_kind: plan
machine_status: complete
machine_title: Codex Local Tooling Compactification Plan
machine_goal: Make local-tooling outputs compact, reviewer-useful, and consistent
  with the new review/machine/explain naming model so Codex can consume them faster.
---

# Codex Local Tooling Compactification Plan

## Status

Complete.

## Goal

Make local-tooling outputs compact, reviewer-useful, and consistent with the new review/machine/explain naming model so Codex can consume them faster.

## Scope

- Included:
  - `scripts/audits/codex_local_context_gateway.rb`
  - `scripts/audits/local_tooling_extended_tools.rb`
  - `docs/tooling/codex-local-audits.yml`
  - `docs/codex-local-tooling-todo.md`
  - `docs/generated/local-tooling/*` summaries that act as active review outputs
- Excluded:
  - broad historical archives under `docs/generated/local-tooling/.history/`
  - unrelated generated artifacts outside the Codex local-tooling surface

## Implementation Slices

1. Keep `codex-context` outputs minimal and clearly named.
2. Reduce noisy or duplicate local-tooling summaries where a single compact index is enough.
3. Align helper registries and generated summaries with the same naming scheme.

## Validation

- `make generate-audit-registry-artifacts`
- `make codex-context topic=<topic> intent='<intent>'`
- `make audit-generated-artifact-freshness`

## Completion Evidence

- Status: complete
- Validation evidence:
  - `make audit-summary-index`
  - `make audit-doc-canonical-phrases`
  - `make audit-generated-artifact-freshness`
  - `make codex-context topic=codex-context-review intent='refresh local tooling compactification and hierarchy docs'`
- Doc delta summary:
  - `scripts/audits/local_tooling_extended_tools.rb`
  - `docs/tooling/codex-local-audits.md`
  - `docs/agent-operating-model.md`
  - `docs/generated/README.md`
  - `docs/generated/local-tooling/audit-summary-index.md`

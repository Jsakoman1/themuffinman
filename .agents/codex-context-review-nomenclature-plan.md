---
machine_kind: plan
machine_status: complete
machine_title: Codex Context Review Nomenclature Plan
machine_goal: Rename the `codex-context` human-facing output and its references to
  `latest.review.md`, while keeping `latest.machine.json`
---

# Codex Context Review Nomenclature Plan

## Status

Complete.

## Goal

Rename the `codex-context` human-facing output and its references to `latest.review.md`, while keeping `latest.machine.json`
and `latest.explain.md` as the complementary machine and explain surfaces.

## Scope

- Included:
  - `scripts/audits/codex_local_context_gateway.rb`
  - `scripts/audits/local_tooling_extended_tools.rb`
  - `docs/tooling/codex-local-audits.yml`
  - `docs/codex-local-tooling-todo.md`
  - docs, plans, manifests, and generated artifacts that reference the old codex-context human output name
- Excluded:
  - unrelated generated artifacts outside the codex-context naming surface
  - runtime behavior changes beyond output naming

## Slices

1. Update the codex-context generator and registry to write and advertise `latest.review.md`.
2. Bulk-rewrite tracked references to the legacy human-summary wording and output path.
3. Regenerate codex-context outputs and refresh affected generated docs.

## Validation

- `make audit-doc-canonical-phrases`
- `make audit-todo`
- `make audit-plan-completion plan=.agents/codex-context-review-nomenclature-plan.md`

## Completion Evidence

- Status: complete
- Validation evidence:
  - `make generate-audit-registry-artifacts`
  - `make codex-context-clean`
  - `make codex-context topic=codex-context-review intent='refresh codex context output naming'`
  - `make codex-context-explain`
  - `make audit-generated-artifact-freshness`
  - `make audit-doc-canonical-phrases`
  - `make audit-todo`
- Doc delta summary:
  - `scripts/audits/codex_local_context_gateway.rb`
  - `scripts/audits/local_tooling_extended_tools.rb`
  - `docs/tooling/codex-local-audits.yml`
  - `docs/codex-local-tooling-todo.md`
  - `.agents/codex-context-review-nomenclature-plan.md`
  - `.agents/codex-context-gateway-closeout-plan.md`
  - `.agents/codex-context-manifest-standardization-master-plan.md`
  - `.agents/feature-manifests/codex-context-optimization-manifest.yaml`
  - `.agents/validation-evidence/codex_context_optimization.yaml`
  - `.agents/todo-plans/120-codex-summary-session-context-compaction.md`

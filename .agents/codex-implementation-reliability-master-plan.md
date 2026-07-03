# Codex Implementation Reliability Master Plan

## Status

Complete.

## Goal

Clean up and standardize the Codex control surface so implementation work becomes faster, more precise, more accurate, and more reliable.

## Parent God Plan

- God Plan: TBD
- Machine-readable path: TBD

## Scope

- Included:
  - codex-context naming and output conventions
  - generated local-tooling review and machine artifact naming
  - source-of-truth hierarchy and archive boundaries
  - workflow docs, registry docs, and plan templates that steer Codex context selection
  - validation and freshness helpers that keep the control surface honest
- Excluded:
  - product feature behavior changes
  - frontend or backend runtime feature work unrelated to Codex control surfaces

## Child Plans

1. `.agents/codex-context-review-nomenclature-plan.md`
- Role: finish the codex-context review/machine/explain naming migration and remove stale legacy references.
- Status: complete

2. `.agents/codex-control-surface-hierarchy-plan.md`
- Role: standardize live truth, generated control, and archive boundaries across docs, registries, and generated summaries.
- Status: complete

3. `.agents/codex-local-tooling-compactification-plan.md`
- Role: reduce noisy generated outputs, keep only reviewer-useful summaries, and align local tooling outputs with the new naming model.
- Status: complete

4. `.agents/codex-validation-and-closeout-hardening-plan.md`
- Role: make validation, freshness, and closeout evidence more deterministic so Codex can trust the current state faster.
- Status: complete

## Pros

- Cuts context noise and stale naming drift.
- Makes the repo easier to scan mechanically and by eye.
- Improves reliability of future implementation sessions by reducing conflicting surfaces.

## Cons

- Requires broad doc and generated-artifact coordination.
- Some archive material may intentionally keep older names for traceability, so the plan needs a strict live-vs-archive boundary.
- Can trigger many freshness audits if the source hierarchy is inconsistent.

## Dependencies

- `docs/control-surface-map.md`
- `docs/documentation-sync-policy.md`
- `docs/codex-fast-path.md`
- `docs/feature-delivery-workflow.md`
- `docs/program-planning-model.md`
- `docs/generated/artifact-policy.yaml`
- `docs/tooling/codex-local-audits.yml`
- `scripts/audits/codex_local_context_gateway.rb`
- `scripts/audits/local_tooling_extended_tools.rb`

## Validation

- Targeted docs consistency review
- `make audit-doc-canonical-phrases`
- `make audit-generated-artifact-freshness`
- `make audit-todo`
- `make audit-plan-completion plan=<plan-file>`

## Completion Evidence

- Status: complete
- Child plan status: complete
- Validation evidence:
  - `make audit-doc-canonical-phrases`
  - `make audit-generated-artifact-freshness`
  - `make audit-summary-index`
  - `make validation-memory-closeout-card`
  - `make audit-todo`
  - `make audit-plan-completion plan=.agents/codex-control-surface-hierarchy-plan.md`
  - `make audit-plan-completion plan=.agents/codex-local-tooling-compactification-plan.md`
  - `make audit-plan-completion plan=.agents/codex-validation-and-closeout-hardening-plan.md`
- Doc delta summary:
  - `docs/control-surface-map.md`
  - `docs/generated/README.md`
  - `docs/source-of-truth-inventory.md`
  - `docs/documentation-sync-policy.md`
  - `docs/tooling/codex-local-audits.md`
  - `docs/agent-operating-model.md`
  - `docs/validation-memory.json`
  - `docs/validation-memory.md`
  - `scripts/audits/local_tooling_extended_tools.rb`
  - `scripts/audits/generate-validation-memory-closeout-card.rb`
  - `docs/generated/local-tooling/audit-summary-index.md`
  - `docs/generated/local-tooling/generated-artifact-freshness-summary.md`
  - `docs/generated/local-tooling/validation-memory-closeout-card-summary.md`
- Deferred work: pending

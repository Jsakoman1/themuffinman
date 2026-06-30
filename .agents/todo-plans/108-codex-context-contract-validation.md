# CODEX-CONTEXT-CONTRACT-VALIDATION Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `low`
Master order: 108 of 108

## Backlog Item

Register the new codex-context execution manifest in the workflow docs and freshness checks so the canonical machine-readable output stays visible and auditable.

## Source Findings

- [`docs/codex-fast-path.md`](/Users/jsakoman/Desktop/themuffinman/docs/codex-fast-path.md)
- [`docs/feature-delivery-workflow.md`](/Users/jsakoman/Desktop/themuffinman/docs/feature-delivery-workflow.md)
- [`docs/documentation-sync-policy.md`](/Users/jsakoman/Desktop/themuffinman/docs/documentation-sync-policy.md)
- [`scripts/audits/audit-generated-artifact-freshness.rb`](/Users/jsakoman/Desktop/themuffinman/scripts/audits/audit-generated-artifact-freshness.rb)

## Implementation Plan

- [x] Add workflow references for the execution manifest and its schema.
- [x] Extend the generated-artifact freshness audit to include the new execution manifest output.
- [x] Keep the docs compact and decision-first.
- [x] Re-run the relevant local validations and close the batch.

## Expected Validation

- `make audit-generated-artifact-freshness`
- `make audit-documentation`
- `make audit-doc-canonical-phrases`
- `make audit-plan-completion plan=.agents/todo-plans/108-codex-context-contract-validation.md`

## Completion Evidence

- Status: complete
- Backlog update: resolved.
- Residual risk: the new manifest must be discoverable in the same local-tooling path as the existing context artifacts.

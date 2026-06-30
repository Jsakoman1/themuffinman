# CODEX-CONTEXT-PLAN-AWARE-ROUTING Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `medium`
Master order: 102 of 105

## Backlog Item

Route plan and tooling work toward plan-code-map, documentation-sync surfaces, and generated-artifact freshness before falling back to broad repo inventory.

## Source Findings

- [`scripts/audits/codex_local_context_gateway.rb`](/Users/jsakoman/Desktop/themuffinman/scripts/audits/codex_local_context_gateway.rb)
- [`docs/codex-fast-path.md`](/Users/jsakoman/Desktop/themuffinman/docs/codex-fast-path.md)
- [`docs/documentation-sync-policy.md`](/Users/jsakoman/Desktop/themuffinman/docs/documentation-sync-policy.md)
- [`docs/feature-delivery-workflow.md`](/Users/jsakoman/Desktop/themuffinman/docs/feature-delivery-workflow.md)

## Implementation Plan

- [x] Detect workflow/tooling/plan topics and prefer plan-code-map and docs sync packs.
- [x] Prefer generated-artifact freshness and plan-code-map when the request is about workflow or automation.
- [x] Keep the routing deterministic and explainable in the human summary.
- [x] Leave product-oriented requests on the current compact chain.

## Expected Validation

- `make codex-context topic=<topic>`
- `make audit-doc-sync-required-surfaces files=<csv>`
- `make audit-generated-artifact-freshness`
- `make audit-plan-completion plan=.agents/todo-plans/102-codex-context-plan-aware-routing.md`

## Completion Evidence

- Status: complete
- Backlog update: resolved.
- Residual risk: routing must stay simple enough that it does not invent new workflow categories.

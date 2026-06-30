# CODEX-CONTEXT-EARLY-FILTERING Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `medium`
Master order: 101 of 105

## Backlog Item

Make the local context chain prefer the smallest high-signal reports first so Codex only widens to repo maps and symbol indexes when the compact audits do not answer the question.

## Source Findings

- [`scripts/audits/codex_local_context_gateway.rb`](/Users/jsakoman/Desktop/themuffinman/scripts/audits/codex_local_context_gateway.rb)
- [`docs/generated/local-tooling/diff-summary.md`](/Users/jsakoman/Desktop/themuffinman/docs/generated/local-tooling/diff-summary.md)
- [`docs/generated/local-tooling/audit-summary-index.md`](/Users/jsakoman/Desktop/themuffinman/docs/generated/local-tooling/audit-summary-index.md)
- [`docs/generated/local-tooling/targeted-tests-summary.md`](/Users/jsakoman/Desktop/themuffinman/docs/generated/local-tooling/targeted-tests-summary.md)

## Implementation Plan

- [x] Make diff-summary, audit-summary-index, and targeted-tests the default first-pass chain.
- [x] Delay repo-map and symbol-index until the compact chain fails to identify the needed files.
- [x] Keep the chain deterministic so repeated runs stay comparable.
- [x] Preserve concise output so the chain remains cheap to read.

## Expected Validation

- `make codex-context topic=<topic>`
- `make codex-context-explain`
- `make audit-plan-completion plan=.agents/todo-plans/101-codex-context-early-filtering.md`

## Completion Evidence

- Status: complete
- Backlog update: resolved.
- Residual risk: the compact chain must stay informative enough that widening is rare but available.

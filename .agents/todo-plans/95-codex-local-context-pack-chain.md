# CODEX-LOCAL-CONTEXT-CHAIN Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `high`
Master order: 95 of 99

## Backlog Item

Build a one-shot local context chain that leads with diff summary, audit summary index, the most relevant audit, targeted tests, and a concise evidence bundle so Codex can read less and decide faster.

## Source Findings

- [`.agents/codex-local-context-gateway-analysis-context.md`](/Users/jsakoman/Desktop/themuffinman/.agents/codex-local-context-gateway-analysis-context.md)
- [`docs/generated/local-tooling/codex-context/latest.machine.json`](/Users/jsakoman/Desktop/themuffinman/docs/generated/local-tooling/codex-context/latest.machine.json)
- [`docs/generated/local-tooling/audit-summary-index.md`](/Users/jsakoman/Desktop/themuffinman/docs/generated/local-tooling/audit-summary-index.md)
- [`docs/generated/local-tooling/diff-summary.md`](/Users/jsakoman/Desktop/themuffinman/docs/generated/local-tooling/diff-summary.md)
- [`docs/generated/local-tooling/targeted-tests-summary.md`](/Users/jsakoman/Desktop/themuffinman/docs/generated/local-tooling/targeted-tests-summary.md)

## Implementation Plan

- [x] Add a canonical read order to the gateway payload.
- [x] Include the audit summary index as an explicit context pack.
- [x] Derive the most relevant audit from the pack set instead of leaving it implicit.
- [x] Emit a concise evidence bundle with commands, generated artifacts, and next-step notes.
- [x] Keep the chain deterministic so repeated requests stay easy to compare.

## Expected Validation

- `make codex-context topic=<topic>`
- `make codex-context-explain`
- `make audit-plan-completion plan=.agents/todo-plans/95-codex-local-context-pack-chain.md`
- `make audit-todo`

## Completion Evidence

- Status: complete
- Backlog update: complete.
- Residual risk: the one-shot chain must stay concise even when the underlying audits produce large payloads.

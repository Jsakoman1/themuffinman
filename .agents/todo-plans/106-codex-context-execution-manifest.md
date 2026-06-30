# CODEX-CONTEXT-EXECUTION-MANIFEST Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `medium`
Master order: 106 of 108

## Backlog Item

Emit a canonical machine-readable execution manifest from `codex-context` so Codex can read one compact operational summary for plans, packs, evidence, and next actions without reconstructing the batch from multiple files.

## Source Findings

- [`scripts/audits/codex_local_context_gateway.rb`](/Users/jsakoman/Desktop/themuffinman/scripts/audits/codex_local_context_gateway.rb)
- [`docs/codex-fast-path.md`](/Users/jsakoman/Desktop/themuffinman/docs/codex-fast-path.md)
- [`docs/feature-delivery-workflow.md`](/Users/jsakoman/Desktop/themuffinman/docs/feature-delivery-workflow.md)
- [`docs/generated/local-tooling/codex-context/latest.machine.json`](/Users/jsakoman/Desktop/themuffinman/docs/generated/local-tooling/codex-context/latest.machine.json)

## Implementation Plan

- [x] Add a dedicated execution-manifest JSON output alongside the existing machine and human context payloads.
- [x] Include plan source, read order, evidence bundle, selected files, included packs, and next-action fields in the manifest.
- [x] Keep the manifest compact enough to serve as the first-read machine summary for the batch.
- [x] Preserve the existing context payloads unchanged for compatibility.

## Expected Validation

- `make codex-context topic=<topic>`
- `make codex-context topic=<topic> refresh=true`
- `make audit-plan-completion plan=.agents/todo-plans/106-codex-context-execution-manifest.md`

## Completion Evidence

- Status: complete
- Backlog update: resolved.
- Residual risk: the manifest must stay compact and deterministic so it does not become another verbose duplicate of `latest.machine.json`.

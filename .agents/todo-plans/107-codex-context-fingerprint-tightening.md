# CODEX-CONTEXT-FINGERPRINT-TIGHTENING Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `medium`
Master order: 107 of 108

## Backlog Item

Harden `codex-context` cache invalidation so gateway, tooling, plan, and selected-source drift all change the fingerprint and invalidate stale cache reuse.

## Source Findings

- [`scripts/audits/codex_local_context_gateway.rb`](/Users/jsakoman/Desktop/themuffinman/scripts/audits/codex_local_context_gateway.rb)
- [`scripts/local_tooling_common.rb`](/Users/jsakoman/Desktop/themuffinman/scripts/local_tooling_common.rb)
- [`scripts/audits/local_tooling_extended_tools.rb`](/Users/jsakoman/Desktop/themuffinman/scripts/audits/local_tooling_extended_tools.rb)
- [`docs/generated/local-tooling/codex-context/latest.machine.json`](/Users/jsakoman/Desktop/themuffinman/docs/generated/local-tooling/codex-context/latest.machine.json)

## Implementation Plan

- [x] Fold gateway and helper script fingerprints into the cache key.
- [x] Include the selected plan file hash and other plan-aware routing inputs in the invalidation surface.
- [x] Surface the fingerprint dimensions in the machine output so cache hits and misses stay explainable.
- [x] Keep the current file-source hashing behavior intact.

## Expected Validation

- `make codex-context topic=<topic>`
- `make codex-context topic=<topic> refresh=true`
- `make audit-generated-artifact-freshness`
- `make audit-plan-completion plan=.agents/todo-plans/107-codex-context-fingerprint-tightening.md`

## Completion Evidence

- Status: complete
- Backlog update: resolved.
- Residual risk: the fingerprint must stay strict enough to invalidate stale packs without causing unnecessary cache misses.

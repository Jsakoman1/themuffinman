# CODEX-CONTEXT-INVALIDATION-HARDENING Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `medium`
Master order: 105 of 105

## Backlog Item

Harden cache invalidation so context reuse only survives when docs, scripts, generated summaries, and plan files still match the cached fingerprint.

## Source Findings

- [`scripts/audits/local_tooling_extended_tools.rb`](/Users/jsakoman/Desktop/themuffinman/scripts/audits/local_tooling_extended_tools.rb)
- [`scripts/audits/codex_local_context_gateway.rb`](/Users/jsakoman/Desktop/themuffinman/scripts/audits/codex_local_context_gateway.rb)
- [`scripts/local_tooling_common.rb`](/Users/jsakoman/Desktop/themuffinman/scripts/local_tooling_common.rb)
- [`docs/generated/local-tooling/.cache/audit-inputs.json`](/Users/jsakoman/Desktop/themuffinman/docs/generated/local-tooling/.cache/audit-inputs.json)

## Implementation Plan

- [x] Include docs, scripts, generated summaries, and plan files in the invalidation fingerprint.
- [x] Recompute cache metadata when any tracked source hash changes.
- [x] Keep invalidation visible in the machine output and summary logs.
- [x] Avoid invalidating on cosmetic timestamp-only changes.

## Expected Validation

- `make codex-context topic=<topic>`
- `make codex-context topic=<topic> refresh=true`
- `make audit-generated-artifact-freshness`
- `make audit-plan-completion plan=.agents/todo-plans/105-codex-context-invalidation-hardening.md`

## Completion Evidence

- Status: complete
- Backlog update: resolved.
- Residual risk: invalidation must remain strict enough to avoid stale reuse but narrow enough to keep the cache useful.

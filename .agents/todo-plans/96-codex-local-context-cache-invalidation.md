# CODEX-LOCAL-CONTEXT-CACHE Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `medium`
Master order: 96 of 99

## Backlog Item

Add explicit cache reuse and invalidation for local context pack composition so unchanged requests stop recomputing the same summaries, hashes, and pack selections.

## Source Findings

- [`scripts/audits/codex_local_context_gateway.rb`](/Users/jsakoman/Desktop/themuffinman/scripts/audits/codex_local_context_gateway.rb)
- [`scripts/audits/local_tooling_extended_tools.rb`](/Users/jsakoman/Desktop/themuffinman/scripts/audits/local_tooling_extended_tools.rb)
- [`docs/generated/local-tooling/codex-context/latest.machine.json`](/Users/jsakoman/Desktop/themuffinman/docs/generated/local-tooling/codex-context/latest.machine.json)
- [`docs/generated/local-tooling/.cache/audit-inputs.json`](/Users/jsakoman/Desktop/themuffinman/docs/generated/local-tooling/.cache/audit-inputs.json)

## Implementation Plan

- [x] Add a request fingerprint and cache metadata for codex-context runs.
- [x] Reuse unchanged pack output when the selected files and source hashes are unchanged.
- [x] Invalidate the cache when the request shape or source hashes drift.
- [x] Keep the cache transparent in the machine output so reuse is auditable.

## Expected Validation

- `make codex-context topic=<topic>`
- `make codex-context topic=<topic> refresh=true`
- `make audit-generated-artifact-freshness`
- `make audit-plan-completion plan=.agents/todo-plans/96-codex-local-context-cache-invalidation.md`

## Completion Evidence

- Status: complete
- Backlog update: complete.
- Residual risk: cache reuse must never hide source drift in the selected files.

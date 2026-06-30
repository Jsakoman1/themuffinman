# CODEX-CONTEXT-CACHE-KEY Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `high`
Master order: 100 of 105

## Backlog Item

Add a real content-addressed cache key for the full `codex-context` payload so identical requests can reuse the composed local context without recomposing packs.

## Source Findings

- [`scripts/audits/codex_local_context_gateway.rb`](/Users/jsakoman/Desktop/themuffinman/scripts/audits/codex_local_context_gateway.rb)
- [`scripts/audits/local_tooling_extended_tools.rb`](/Users/jsakoman/Desktop/themuffinman/scripts/audits/local_tooling_extended_tools.rb)
- [`docs/generated/local-tooling/codex-context/latest.machine.json`](/Users/jsakoman/Desktop/themuffinman/docs/generated/local-tooling/codex-context/latest.machine.json)
- [`docs/generated/local-tooling/.cache/audit-inputs.json`](/Users/jsakoman/Desktop/themuffinman/docs/generated/local-tooling/.cache/audit-inputs.json)

## Implementation Plan

- [x] Define the cache key from topic, selected files, source hashes, enabled providers, mode, and budget.
- [x] Reuse the full composed payload when the cache key matches and the source fingerprint is unchanged.
- [x] Preserve explicit invalidation when any selected source file, generated artifact, or plan surface drifts.
- [x] Keep cache metadata visible in the machine output so reuse is auditable.

## Expected Validation

- `make codex-context topic=<topic>`
- `make codex-context topic=<topic> refresh=true`
- `make audit-generated-artifact-freshness`
- `make audit-plan-completion plan=.agents/todo-plans/100-codex-context-cache-key.md`

## Completion Evidence

- Status: complete
- Backlog update: resolved.
- Residual risk: cache reuse must never hide source drift in the selected files or plan surfaces.

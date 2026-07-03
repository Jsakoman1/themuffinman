---
machine_kind: plan
machine_status: unknown
machine_title: Validation Memory Canonicalization Plan
machine_goal: Add one stable validation-memory reference that captures exact validator
  expectations, canonical command strings, and closeout heuristics so future sessions
  can avoid rediscovering them through failing audits.
---

# Validation Memory Canonicalization Plan

Purpose: preserve durable validation, manifest, and closeout lessons from recent feature work in one canonical read-first document and wire that document into the main workflow entrypoints.

## Goal

Add one stable validation-memory reference that captures exact validator expectations, canonical command strings, and closeout heuristics so future sessions can avoid rediscovering them through failing audits.

## Scope

- `docs/validation-memory.md`
- `docs/validation-memory.json`
- `docs/codex-fast-path.md`
- `docs/feature-delivery-workflow.md`
- `docs/change-completion-checklist.md`
- `docs/tooling/codex-local-audits.md`
- `docs/product-memory.md`
- `docs/agent-operating-model.md`
- `docs/documentation-sync-policy.md`
- `AGENTS.md`
- `scripts/audits/codex_local_context_gateway.rb`

## Checklist

- [x] Add a canonical validation-memory document with command, manifest, and closeout rules.
- [x] Link the validation-memory document from the main workflow entrypoints.
- [x] Record the durable lesson in product and agent memory surfaces.
- [x] Run lightweight documentation and agent-safety validation.

## Validation

- `./mvnw test -Dtest=AgentOperatingModelValidationTest`
- `make audit-documentation`
- `make audit-todo`

## Completion Evidence

- Status: complete
- Execution status: complete
- Validation:
  - `./mvnw test -Dtest=AgentOperatingModelValidationTest`
  - `make audit-documentation`
  - `make audit-todo`
  - `ruby -c scripts/audits/codex_local_context_gateway.rb`
  - `make codex-context topic=validation-memory intent='manifest-backed closeout validation' files='docs/validation-memory.md,docs/validation-memory.json,scripts/audits/codex_local_context_gateway.rb' refresh=true`
- Notes:
  - Added `docs/validation-memory.md` as the canonical read-first memory for validator-facing command strings, manifest evidence shape, and common closeout failure patterns.
  - Added `docs/validation-memory.json` as the machine-readable companion cheat sheet for local tooling and context routing.
  - Wired the new validation memory into the compact workflow entrypoint, full workflow doc, completion checklist, documentation-sync policy, local audit guide, product memory, agent operating model, repository instructions, and the local context gateway.
  - Verified that `make codex-context` auto-includes a `validation-memory` pack and exposes it through the execution manifest when the request is manifest-backed or closeout-sensitive.
- Persistent backlog item: none

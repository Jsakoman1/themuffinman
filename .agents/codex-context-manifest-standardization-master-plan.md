---
machine_kind: master-plan
machine_status: unknown
machine_title: Codex Context Manifest Standardization Master Plan
machine_goal: Reduce future token spend by making `make codex-context` emit one compact
  execution manifest that can be read instead of reconstructing plan, audit, and evidence
  state from several files.
---

# Codex Context Manifest Standardization Master Plan

Purpose: turn the current codex-context output into a canonical machine-readable execution manifest, tighten cache fingerprints so script and plan drift invalidate reuse, and register the new manifest in the local freshness and workflow docs.

## Goal

Reduce future token spend by making `make codex-context` emit one compact execution manifest that can be read instead of reconstructing plan, audit, and evidence state from several files.

## Scope

- Add a canonical execution manifest output for `codex-context`.
- Expand cache fingerprinting to include gateway/tooling/version inputs that affect context assembly.
- Expose the manifest in the workflow docs and freshness checks.
- Keep the existing human and machine context payloads intact.

## Child Plans

- [x] `CODEX-CONTEXT-EXECUTION-MANIFEST` - `.agents/todo-plans/106-codex-context-execution-manifest.md`
- [x] `CODEX-CONTEXT-FINGERPRINT-TIGHTENING` - `.agents/todo-plans/107-codex-context-fingerprint-tightening.md`
- [x] `CODEX-CONTEXT-CONTRACT-VALIDATION` - `.agents/todo-plans/108-codex-context-contract-validation.md`

## Execution Order

1. Emit a canonical execution manifest from the gateway.
2. Include gateway/version/plan/tooling inputs in the cache fingerprint.
3. Register the new manifest in freshness, workflow, and closeout-adjacent docs.
4. Regenerate affected local-tooling artifacts and validate the batch.

## Closeout Rules

- Keep the implementation narrow and local-tooling-only.
- Preserve the current `latest.machine.json` and `latest.review.md` outputs.
- Record any deferred remainder in `docs/agent-improvement-backlog.md` before closing the master plan.

## Completion Evidence

- Status: complete
- Execution status: complete
- Persistent backlog item: none
- Primary source files: `scripts/audits/codex_local_context_gateway.rb`, `scripts/audits/audit-generated-artifact-freshness.rb`, `docs/codex-fast-path.md`, `docs/feature-delivery-workflow.md`, `docs/documentation-sync-policy.md`, `docs/change-completion-checklist.md`, `docs/agent-operating-model.md`

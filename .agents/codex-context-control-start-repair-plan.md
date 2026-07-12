---
machine_kind: plan
machine_status: complete
machine_title: Codex Context Control-Start Repair Plan
machine_goal: Restore the compact control-start snapshot required by the Codex context gateway without reintroducing removed legacy closeout tooling.
---

# Codex Context Control-Start Repair Plan

## Status

Complete.

## Workflow Frame

- Feature tier: tier4-agent-tooling-workflow
- Scope: restore the `control-start` Make target, compact generator, gateway registry entry, audit registry entry, and current tooling documentation.
- Out of scope: legacy God-plan, closeout-bundle, plan-completion, and control-refresh commands removed by the planning-model cleanup.
- Manifest decision: required
- Manifest path: `.agents/feature-manifests/codex-context-control-start-repair-manifest.yaml`
- Master plan: none; this is a bounded tooling repair discovered during Vision planning.
- Plan context: `codex-context` currently crashes before output assembly because the gateway still fetches the removed `control-start` output-registry key.

## Routing Snapshot

- Context command: unavailable before the repair because this plan fixes the context gateway itself.
- Routing commands: `make audit-router`, `make audit-doc-sync-required-surfaces`, `make audit-manifest-decision`, `make recommend-validation-preset`.
- Validation commands: Ruby syntax check, `make control-start`, `make codex-context`, `make generate-audit-registry-artifacts`, `make audit-agent-safety`.
- Doc sync commands: `make audit-documentation`, `make audit-doc-canonical-phrases`, `make audit-summary-index`.
- Generated artifact commands: `make control-start`, `make generate-audit-registry-artifacts`.

## Implementation Checkboxes

- [x] Add a compact, non-recursive `control-start` generator that writes the plan/control snapshots needed by the gateway.
- [x] Restore the Make target and context-gateway output registration so `codex-context` can load the snapshot.
- [x] Register the restored audit surface and update local-tooling documentation.
- [x] Record the unrelated missing `audit-plan-completion` reference in `implementation-batch` as deferred control-system work.
- [x] Run the routing, generator, gateway, documentation, and agent-safety validations.

## Validation Plan

- Targeted checks: `ruby -c scripts/audits/generate-control-start.rb`, `ruby -c scripts/audits/codex_local_context_gateway.rb`, `make control-start`, and the previously failing `make codex-context` invocation.
- Broader checks: `make generate-audit-registry-artifacts`, `make audit-documentation`, `make audit-doc-canonical-phrases`, `make audit-agent-safety`, `make audit-todo`, and `make audit-validation-memory-drift`.
- Skipped checks or reasons: frontend and domain test suites are not applicable to this Ruby/local-tooling-only repair.
- Validation preset: manifest-required.

## Docs and Artifacts

- Expected docs: `docs/codex-local-tooling-todo.md`, `docs/agent-improvement-backlog.md`, and the feature manifest.
- Expected generated artifacts: `docs/generated/local-tooling/control-start.json`, `docs/generated/local-tooling/control-start-summary.md`, `docs/tooling/codex-local-audits.yml`, and fresh Codex context output.
- Temporary work products: none.

## Closeout Gates

- Required closeout checks: all checkboxes complete, restored target produces valid JSON, `codex-context` completes, generated audit registry is refreshed, and agent-safety/doc audits pass.
- Final response evidence: root cause, changed files, successful re-run of the original command, and the deferred unrelated tooling issue.
- Backlog follow-up rule: keep any removed-target references that are outside this repair in `docs/agent-improvement-backlog.md` with a stable ID.

## Completion Evidence

- Status: complete
- Changed files: `Makefile`, `scripts/audits/generate-control-start.rb`, `scripts/audits/codex_local_context_gateway.rb`, `scripts/audits/local_tooling_extended_tools.rb`, local-tooling docs, manifest, and generated control outputs.
- Validation evidence: Ruby syntax checks passed; `make control-start` passed; the original `make codex-context ... refresh=true` command passed with `Provider failures: 0`; `make audit-agent-safety`, documentation audits, and scoped generated-artifact freshness passed.
- Doc delta summary: documented the restored control-start contract and recorded the unrelated broad-batch wrapper drift as a deferred tooling item.
- Backlog update: created `TOOLING-IMPLEMENTATION-BATCH-001`.
- Residual risk: `scripts/implementation-batch.sh` still references the removed `audit-plan-completion` target and is intentionally outside this bounded repair.

---
machine_kind: plan
machine_status: complete
machine_title: Implementation System Closeout Hygiene
machine_goal: Reduce closeout churn by making cleanup, plan completion, and master-plan sequencing deterministic before final closure.
---

# Implementation System Closeout Hygiene

## Status

Complete.

## Goal

Reduce closeout churn by making cleanup, plan completion, and master-plan sequencing deterministic before final closure.

## Scope

- Included: cleanup-first closeout behavior, archive-only history pruning, stricter plan-completion gating, and workflow/docs alignment.
- Included: deterministic batch sequencing so broad master-plan runs do not stop between safe slices.
- Excluded: product UI changes and feature behavior.

## Plan

1. Add an explicit cleanup step for generated history before closeout and broad control refreshes.
2. Tighten plan-completion and closeout wording so stale archive-only state and premature closure are harder to miss.
3. Update batch and closeout entrypoints so master-plan execution continues through the full slice sequence without per-slice prompting.
4. Refresh the relevant workflow docs and validation memory so the new behavior is discoverable.

## Validation

- Targeted checks: workflow-script syntax, closeout-related audits, and the smallest relevant repo test set.
- Broader checks: closeout audit paths and validation-memory or agent-model validation if any protected wording changes.

## Completion Evidence

- Status: complete
- Changed files: AGENTS.md, Makefile, docs/codex-fast-path.md, docs/feature-delivery-workflow.md, docs/change-completion-checklist.md, docs/documentation-sync-policy.md, docs/validation-memory.md, docs/validation-memory.json, docs/agent-operating-model.md, docs/agent-operating-model/sections/policies.yaml, scripts/implementation-batch.sh, scripts/feature-closeout-audit.sh, scripts/audits/audit-plan-completion.rb, scripts/audits/enforce-feature-closeout.rb, scripts/audits/local_tooling_extended_tools.rb, scripts/audits/cleanup-generated-history.rb
- Validation evidence: `make cleanup-generated-history`, `make generate-agent-operating-model`, `make generate-agent-artifacts`, `make audit-validation-memory-drift`, `./mvnw -q -Dtest=AgentOperatingModelValidationTest test`, `make control-start`
- Doc delta summary: closeout now prunes archive-only generated history before final review, rejects archive-only paths as live evidence, and requires stronger non-placeholder completion evidence before a completed plan can close.
- Deferred work: none

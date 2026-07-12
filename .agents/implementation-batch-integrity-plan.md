---
machine_kind: plan
machine_status: complete
machine_closeout_contract: 2
machine_baseline_ref: 0cf598d
machine_delivery_mode: implementation
machine_title: Implementation Batch Integrity Plan
machine_goal: Prevent master and child implementation plans from closing before their planned code and validation work are actually complete.
---

# Implementation Batch Integrity Plan

## Status

Complete.

## Workflow Frame

- Feature tier: tier4-agent-tooling-workflow
- Scope: Restore an explicit, evidence-backed closeout path for implementation batches, plan hierarchies, manifests, templates, and local tooling documentation.
- Out of scope: Product application behavior and unrelated historical plan migration.
- Manifest decision: required
- Manifest path: .agents/feature-manifests/implementation-batch-integrity-manifest.yaml
- Master plan: not applicable; this is one bounded control-system repair.
- Sequenced batch: audit contract, batch wiring, generators, documentation, validation.

## Implementation Checkboxes

- [x] Add a deterministic plan-completion audit that verifies final plan state, baseline-backed code evidence, and master-to-child completion.
- [x] Make `implementation-batch` preparation-only by default and permit closeout only through an explicit, validated path.
- [x] Align feature bootstrap, templates, manifest schema, and closeout audit with the new contract.
- [x] Update workflow documentation and generated agent-operating artifacts with the non-premature-closeout rule.
- [x] Validate both the rejected premature-closeout path and a completed control-system repair path.

## Validation Plan

- Targeted checks: Ruby/Zsh syntax, plan completion audit negative and positive cases, agent-operating validation test.
- Broader checks: generated agent artifacts, agent safety, validation-memory drift, TODO audit.
- Skipped checks or reasons: application backend/frontend suites are not affected by this tooling-only change.

## Docs and Artifacts

- Expected docs: docs/codex-fast-path.md; docs/feature-delivery-workflow.md; docs/documentation-sync-policy.md; docs/change-completion-checklist.md; docs/program-planning-model.md; docs/agent-operating-model.md; docs/agent-operating-model.yaml; docs/domain-technical.md.
- Expected generated artifacts: generated agent-operating model and local tooling index outputs required by validation.
- Temporary work products: none.

## Closeout Gates

- Required closeout checks: `make audit-plan-completion`; `make feature-closeout-audit`; `make generate-agent-operating-model`; `make generate-agent-artifacts`; `make audit-agent-safety`.
- Final response evidence: changed behavior; rejected premature closeout; passed final completion audit; remaining limitations.
- Backlog follow-up rule: resolve `TOOLING-IMPLEMENTATION-BATCH-001` in the same change and record any new deferred control work with an ID.

## Completion Evidence

- Status: complete
- Baseline ref: `0cf598d`
- Implemented code paths: `Makefile`, `scripts/audits/audit-plan-completion.rb`, `scripts/bootstrap-feature-work.sh`, `scripts/feature-closeout-audit.sh`, and `scripts/implementation-batch.sh`
- Changed files: plan and manifest templates, completion-manifest schema, closeout tooling, Java validator coverage, workflow docs, agent-operating model, validation memory, and regenerated control artifacts.
- Validation evidence: Ruby and Zsh syntax checks; `AgentOperatingModelValidationTest`; `QuestWorkflowScenarioTest`; preparation-only implementation batch; rejected active-plan and draft-master audits; `make generate-agent-operating-model`; `make generate-agent-artifacts`; `make audit-agent-safety`; and validation-memory drift audit all passed.
- Doc delta summary: centralized the no-premature-closeout contract across the planning model, fast path, completion checklist, documentation-sync policy, agent-operating model, technical documentation, and validation memory.
- Backlog update: resolved `TOOLING-IMPLEMENTATION-BATCH-001` because the wrapper no longer calls a removed target during preparation and now has an explicit audited closeout path.
- Residual risk: baseline proof is repository-wide and depends on plan authors accurately declaring their implemented paths; the audit rejects undeclared or unchanged paths but cannot infer semantic intent from a diff alone.

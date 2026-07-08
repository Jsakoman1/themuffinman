---
machine_kind: plan
machine_status: complete
machine_title: Control System Hardening Plan
machine_goal: Reduce planning and generated-artifact noise while preserving the current safety guarantees around implementation tracking, validation evidence, and closeout correctness.
---

# Feature Implementation Plan

## Status

Complete.

## Workflow Frame

- Feature tier: high-risk internal workflow and control-system hardening
- Scope: reduce control-surface bloat, improve signal-to-noise, and tighten evidence quality without losing auditability
- Out of scope: product feature work and domain-module rewrites
- Manifest decision: no feature manifest by default unless a narrower child slice requires one
- Manifest path: TBD only if a slice later requires it
- Master plan: `.agents/platform-quality-hardening-master-plan.md`

## Routing Snapshot

- Context commands:
  - `make control-start`
  - `make codex-context topic=control-system intent='review control-system noise and safety'`
- Routing commands:
  - `make implementation-batch topic=control-system`
- Validation commands:
  - `./mvnw -q -Dtest=AgentOperatingModelValidationTest,WorkflowStateMachineCatalogTest test`
  - `make audit-generated-artifact-freshness`
- Closeout commands:
  - `make control-start`
  - `ruby scripts/audits/audit-plan-completion.rb plan=.agents/control-system-hardening-plan.md`
- Doc sync commands:
  - `make generate-agent-operating-model`
  - `make generate-agent-artifacts`
- Generated artifact commands:
  - `make audit-generated-artifact-freshness`
  - `make temp-work-product-closeout plan=.agents/control-system-hardening-plan.md`

## Implementation Slices

- [x] Slice 1: classify `docs/generated/local-tooling/` outputs into durable, ephemeral, and history-only groups and stop treating all of them as equal operational signal
- [x] Slice 2: reduce `.agents/` plan sprawl by tightening when a broad master plan needs many child plans versus when one narrower durable plan is enough
- [x] Slice 3: harden plan-completion evidence so generated status cannot drift ahead of real implementation again
- [x] Slice 4: reduce history and generated-artifact commit noise, especially under `docs/generated/local-tooling/.history/`
- [x] Slice 5: simplify operator entrypoints so everyday work uses fewer control commands for the same safe outcome

## Validation Plan

- Targeted checks:
  - `./mvnw -q -Dtest=AgentOperatingModelValidationTest,WorkflowStateMachineCatalogTest test`
  - `make audit-generated-artifact-freshness`
- Broader checks:
  - `./mvnw test`
  - `make control-start`
- Skipped checks or reasons: TBD
- Validation preset: documentation/control-system

## Docs and Artifacts

- Expected docs:
  - `AGENTS.md`
  - `docs/agent-operating-model.md`
  - `docs/agent-operating-model.yaml`
  - `docs/feature-delivery-workflow.md`
  - `docs/change-completion-checklist.md`
  - `docs/documentation-sync-policy.md`
- Expected generated artifacts:
  - `docs/generated/agent-endpoint-inventory.json`
  - `docs/generated/automation-read-model-inventory.json`
  - `docs/generated/backend-audit-inventory.json`
  - `docs/generated/source-of-truth-audit.json`
  - selected `docs/generated/local-tooling/*` outputs only when still operationally required
- Temporary work products: any temporary inventories must name this plan and be closed out before completion

## Closeout Gates

- Required closeout checks:
  - control docs and YAML remain aligned
  - generated-artifact freshness returns `stale: 0`
  - no new plan-completion false positive path remains
- Final response evidence: include which artifacts stayed durable, which became ephemeral, and what operator noise was removed
- Backlog follow-up rule: any deferred control tightening must be recorded in `docs/agent-improvement-backlog.md`
- Do not mark the plan complete until the scope is actually implemented, the required validation has passed or been explicitly skipped with a reason, and the completion evidence matches reality.

## Open Questions

- Resolver outputs still needed: exactly which generated local-tooling surfaces must remain committed versus generated-on-demand
- Risks or approvals: low technical risk, medium process risk if signal-bearing files are removed too aggressively

## Completion Evidence

- Status: complete
- Changed files: `AGENTS.md`, `docs/control-surface-map.md`, `docs/tooling/codex-local-audits.md`, `docs/feature-delivery-workflow.md`, `docs/agent-operating-model.md`, `docs/documentation-sync-policy.md`, `docs/change-completion-checklist.md`, `docs/program-planning-model.md`, `docs/agent-operating-model/sections/policies.yaml`, `.agents/templates/master-plan.template.md`, `.agents/templates/feature-implementation-plan.template.md`, `scripts/local_tooling_common.rb`, `scripts/audits/local_tooling_extended_tools.rb`, `scripts/audits/audit-plan-completion.rb`
- Validation evidence: `ruby -c scripts/local_tooling_common.rb`, `ruby -c scripts/audits/local_tooling_extended_tools.rb`, `make generate-agent-operating-model`, `make generate-audit-registry-artifacts`, `make control-start`, `./mvnw -Dtest=AgentOperatingModelValidationTest,WorkflowStateMachineCatalogTest test`, `make audit-generated-artifact-freshness`, `ruby scripts/audits/audit-plan-completion.rb plan=.agents/control-system-hardening-plan.md`
- Doc delta summary: classified operator-core versus focused-review versus archive-only control surfaces, documented archive-only `.history` and `.cache` handling, reduced history growth through local-tooling retention pruning, tightened when Master Plans are warranted, and made plan-completion audits distinguish incomplete active work from invalid completed closeout state.
- Backlog update: none
- Residual risk: workmarket and vision hardening child plans remain open under the parent master plan.

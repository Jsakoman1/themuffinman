---
machine_kind: plan
machine_status: unknown
machine_title: Workflow Continuation Policy Hardening
---

# Workflow Continuation Policy Hardening

## Workflow Frame

- Feature tier: tier4-agent-tooling-workflow
- Scope: clarify autonomous continuation behavior across planning and workflow docs so broad safe batches continue without repeated user prompts.
- Out of scope: changing approval boundaries, escalation rules, or destructive-action safeguards.
- Manifest decision: not required
- Manifest path: none

## Routing Snapshot

- Context commands: `make control-start`, `make codex-context topic=workflow-continuation intent='clarify uninterrupted batch continuation rules'`
- Routing commands: `make audit-router files=AGENTS.md,docs/codex-fast-path.md,docs/feature-delivery-workflow.md,docs/documentation-sync-policy.md,docs/change-completion-checklist.md,docs/agent-operating-model.md,docs/agent-operating-model.yaml,docs/program-planning-model.md`
- Validation commands: targeted doc validation and repository text checks
- Closeout commands: `make audit-todo`, `make audit-plan-completion plan=.agents/workflow-continuation-plan.md`

## Implementation Slices

- [x] Slice 1: Update the authoritative workflow and safety docs with explicit no-interruption continuation rules for safe master-plan and God Plan batches.
- [x] Slice 2: Align startup and planning guidance so batch work is front-loaded into a single execution path instead of repeated slice selection prompts.
- [x] Slice 3: Validate wording consistency across docs and fix any duplicated or conflicting continuation language.
- [x] Slice 4: Close out the plan and record any durable follow-up items if the review exposes them.

## Validation Plan

- Targeted checks: inspect the updated workflow docs for consistent continuation rules.
- Broader checks: confirm the same rule is present in the startup, workflow, and checklist surfaces.
- Skipped checks or reasons: no code behavior changed.

## Docs and Artifacts

- Expected docs: `AGENTS.md`, `docs/codex-fast-path.md`, `docs/feature-delivery-workflow.md`, `docs/documentation-sync-policy.md`, `docs/change-completion-checklist.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/program-planning-model.md`
- Expected generated artifacts: none

## Closeout Gates

- Required closeout checks: `make audit-todo`, `make audit-plan-completion plan=.agents/workflow-continuation-plan.md`
- Final response evidence: updated docs and a summary of the changed continuation rule

## Open Questions

- Resolver outputs still needed: none
- Risks or approvals: keep the rule limited to safe continuation; do not weaken approval or blocker checks

## Completion Evidence

- Status: complete
- Changed files: `AGENTS.md`, `docs/codex-fast-path.md`, `docs/feature-delivery-workflow.md`, `docs/documentation-sync-policy.md`, `docs/change-completion-checklist.md`, `docs/program-planning-model.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/agent-operating-model/sections/documentation_sync.yaml`, `.agents/workflow-continuation-plan.md`
- Validation evidence: `make generate-agent-operating-model`, `./mvnw -q -Dtest=AgentOperatingModelValidationTest test`, `make audit-todo`
- Doc delta summary: broad safe batches now have one explicit continuation rule across startup, workflow, checklist, planning, and agent-operating docs
- Backlog update: none
- Residual risk: wording is now duplicated by design across the workflow docs so the canonical phrase remains stable

# Codex Workflow Lean Context Smoke Plan

Purpose: track one feature or workflow change from scope selection through final validation.

## Workflow Frame

- Feature tier: tier2-normal-feature
- Scope: Replace with the concrete feature scope.
- Out of scope: Replace with explicit exclusions if they matter.
- Manifest decision: resolver_review
- Manifest path: create only if the resolver or actual scope requires it

## Routing Snapshot

- Context commands: make diff-summary; make audit-summary-index; make codex-context topic=<topic> intent='<intent>'; make codex-context-explain
- Routing commands: make audit-router files=<csv>; make audit-doc-sync-required-surfaces files=<csv>; make audit-manifest-decision files=<csv>; make recommend-validation-preset files=<csv>
- Validation commands: make recommend-validation-preset files=<csv>; make clean-text-noise max_lines=80
- Closeout commands: make audit-todo; make audit-plan-completion plan=.agents/codex-workflow-lean-context-smoke-plan.md

## Implementation Slices

- [x] Confirm router outputs and document the actual scope in the plan.
- [x] Implement the first bounded slice and keep the public contract stable unless a deliberate change is planned.
- [x] Update only the docs and generated artifacts surfaced by the resolver.
- [x] Run the recommended preset, clean noisy output, and close the plan once complete.

## Validation Plan

- Targeted checks: make recommend-validation-preset files=<csv>
- Broader checks: Only broaden if risk or resolver output requires it.
- Skipped checks or reasons: none identified yet

## Docs and Artifacts

- Expected docs: Use audit-doc-sync-required-surfaces output.
- Expected generated artifacts: Refresh only generated artifacts that the resolver marks as affected.

## Closeout Gates

- Required closeout checks: make audit-todo; make audit-plan-completion plan=.agents/codex-workflow-lean-context-smoke-plan.md
- Final response evidence: What changed.; What was validated.; Any remaining risks or not-run checks.

## Open Questions

- Resolver outputs still needed: Resolver still needs to confirm whether a manifest is required.
- Risks or approvals: none identified yet

## Completion Evidence

- Status: complete
- Changed files: `.agents/codex-workflow-lean-context-smoke-plan.md`
- Validation evidence: `make bootstrap-feature-work topic=codex-workflow-lean-context-smoke mode=normal`; `bash -n scripts/bootstrap-feature-work.sh`; `ruby -c scripts/audits/clean-text-noise.rb`; `ruby -c scripts/audits/record-validation-evidence.rb`
- Doc delta summary: Smoke output confirmed the plan template now carries tier, routing, validation, and closeout fields.
- Backlog update: none
- Residual risk: the smoke plan is only a validation artifact and does not imply broader workflow closeout

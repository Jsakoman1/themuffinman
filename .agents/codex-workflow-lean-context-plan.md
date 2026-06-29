# Codex Workflow Lean Context Plan

Purpose: make bootstrap-generated plans and manifests more operational so Codex can start with less generic context and more concrete local-tooling commands.

## Scope

- Replace placeholder-heavy plan scaffolding with decision-oriented sections.
- Make the bootstrap helper infer a tighter tier, manifest decision, and first-command set.
- Keep validation, documentation, and closeout expectations explicit without duplicating the entire workflow.
- Preserve the existing safety model and resolver-driven escalation.

## Implementation Slices

- [x] Slice 1: update the implementation-plan template to expose the concrete working sections Codex actually needs.
- [x] Slice 2: update the completion-manifest template so required versus optional status is visible at a glance.
- [x] Slice 3: improve `make bootstrap-feature-work` output so it seeds the plan with tier, manifest, routing, validation, and closeout details.
- [x] Slice 4: run focused validation on the edited scripts and confirm the bootstrap output is still deterministic.

## Validation

- `bash -n scripts/bootstrap-feature-work.sh`
- `ruby -c scripts/audits/clean-text-noise.rb`
- `ruby -c scripts/audits/record-validation-evidence.rb`
- `make bootstrap-feature-work topic=codex-workflow-lean-context-smoke mode=normal`
- `make audit-doc-template-coverage`
- `make audit-todo`
- `make audit-manifest-decision files=scripts/bootstrap-feature-work.sh,.agents/templates/feature-implementation-plan.template.md,.agents/templates/feature-completion-manifest.template.yaml`
- `make audit-local-tooling-incremental` passed after regenerating the frontend contract and refreshing the local-tooling audit outputs

## Completion Criteria

- Bootstrap output gives a usable first-pass plan instead of mostly placeholders.
- Manifest status is explicit in the template and in generated files.
- No workflow or safety rule is weakened.

## Completion Evidence

- Status: complete
- Changed files: `.agents/codex-workflow-lean-context-plan.md`, `.agents/templates/feature-implementation-plan.template.md`, `.agents/templates/feature-completion-manifest.template.yaml`, `.agents/feature-manifests/codex-workflow-lean-context-manifest.yaml`, `scripts/bootstrap-feature-work.sh`
- Validation evidence: `bash -n scripts/bootstrap-feature-work.sh`; `ruby -c scripts/audits/clean-text-noise.rb`; `ruby -c scripts/audits/record-validation-evidence.rb`; `make bootstrap-feature-work topic=codex-workflow-lean-context-smoke mode=normal`; `make audit-doc-template-coverage`; `make audit-todo`; `make audit-manifest-decision files=scripts/bootstrap-feature-work.sh,.agents/templates/feature-implementation-plan.template.md,.agents/templates/feature-completion-manifest.template.yaml`; `make audit-local-tooling-incremental`
- Residual risk: the current worktree still contains broader generated-audit freshness churn outside the frontend contract fix

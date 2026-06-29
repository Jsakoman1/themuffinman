# AGENT-GENERATED-ARTIFACT-POLICY-TIGHTENING Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `medium`
Master order: 38 of 82

## Backlog Item

Define which generated reports are source-of-truth artifacts, which are disposable local context, and which should not be committed by default.

Source notes:
  Purpose: reduce large generated diffs and make future Codex sessions read only the generated artifacts that actually matter.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make audit-documentation`
- [x] `make audit-agent-safety`
- [x] `make audit-local-tooling-incremental`

## Completion Evidence

- Status: complete
- Changed files:
  - `docs/generated/artifact-policy.yaml`
  - `docs/generated/README.md`
  - `docs/agent-improvement-backlog.md`
  - `docs/domain-technical.md`
  - `.agents/todo-master-plan.md`
  - `.agents/todo-plans/38-agent-generated-artifact-policy-tightening.md`
- Validation evidence:
  - `ruby -ryaml -e 'policy = YAML.load_file("docs/generated/artifact-policy.yaml"); abort "missing policy" unless policy.dig("generated_artifact_policy", "source_of_truth", "paths"); puts "artifact policy ok"'`
  - `make audit-documentation`
  - `make audit-agent-safety`
  - `make audit-local-tooling-incremental`
  - `ruby scripts/todo-audit.rb`
- Backlog update: removed `AGENT-GENERATED-ARTIFACT-POLICY-TIGHTENING` from `docs/agent-improvement-backlog.md`.
- Residual risk: the policy is explicit and machine-readable but not yet a hard commit gate; later generated commit-scope tooling can enforce it.

# CODEX-LOCAL-MANIFEST-PATH-RESOLVER Plan

Source: `docs/codex-local-tooling-todo.md`
Group: `local-tooling`
Risk: `medium`
Master order: 86 of 92

## Backlog Item

Infer the most likely feature manifest path for the current changeset so closeout-oriented tools can emit concrete manifest paths instead of placeholders.

Source notes:
  Proposed entrypoints:
  - `ruby scripts/audits/resolve-manifest-path.rb [files...]`
  - `make resolve-manifest-path files=<csv>`
  Proposed outputs:
  - `docs/generated/local-tooling/manifest-path-resolution.json`
  - `docs/generated/local-tooling/manifest-path-resolution-summary.md`
  Notes:
  - Infer the most likely feature manifest path for the current changeset from changed files, topic names, active plans, and existing manifest metadata.
  - Feed `CODEX-LOCAL-CHANGESET-PLAYBOOK`, `CODEX-LOCAL-VALIDATION-PRESET-RECOMMENDER`, and closeout commands so they can emit concrete manifest paths instead of placeholders when the mapping is deterministic.
  - Primary value is lower prompt overhead and fewer manual substitutions during routine closeout.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Integrate manifest resolution into changeset playbook and validation preset outputs.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make resolve-manifest-path`
- [x] `make changeset-playbook`
- [x] `make recommend-validation-preset`
- [x] `make generate-audit-registry-artifacts`
- [x] `make audit-plan-completion plan=.agents/todo-plans/86-codex-local-manifest-path-resolver.md`

## Completion Evidence

- Status: complete
- Changed files:
  - `scripts/audits/resolve-manifest-path.rb`
  - `scripts/audits/local_tooling_extended_tools.rb`
  - `Makefile`
  - `docs/codex-local-tooling-todo.md`
  - `docs/domain-technical.md`
  - `docs/generated/local-tooling/manifest-path-resolution.json`
  - `docs/generated/local-tooling/manifest-path-resolution-summary.md`
  - `docs/generated/local-tooling/changeset-playbook.json`
  - `docs/generated/local-tooling/changeset-playbook-summary.md`
  - `docs/generated/local-tooling/validation-preset.json`
  - `docs/generated/local-tooling/validation-preset-summary.md`
- Validation evidence:
  - `ruby -c scripts/audits/local_tooling_extended_tools.rb` passed.
  - `ruby -c scripts/audits/resolve-manifest-path.rb` passed.
  - `make resolve-manifest-path` passed and wrote manifest resolution JSON and summary.
  - `make changeset-playbook` passed with manifest-resolution integration.
  - `make recommend-validation-preset` passed with manifest-resolution integration.
  - `make generate-audit-registry-artifacts` passed with 53 registered audits.
  - `make audit-local-tooling-incremental` passed and included `make resolve-manifest-path`.
  - `ruby scripts/todo-audit.rb` passed.
  - `make audit-plan-completion plan=.agents/todo-plans/86-codex-local-manifest-path-resolver.md` passed.
- Backlog update: `CODEX-LOCAL-MANIFEST-PATH-RESOLVER` is now marked complete in `docs/codex-local-tooling-todo.md` and listed in the available audits section.
- Residual risk: resolver intentionally returns `review` when multiple manifest candidates score similarly; it prefers no automatic substitution over a wrong closeout target.

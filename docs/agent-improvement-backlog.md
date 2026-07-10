# Agent Improvement Backlog

This file is the persistent forward-looking backlog for agent-safety, backend audit tightening, control-flow hardening, and documentation automation improvements.

It is not the execution source of truth for a single change. Active implementation details should still live in temporary plans under `.agents/`.

Use this file to preserve what remains worth tightening across sessions.
Completed plans and master plans should stay out of this file; keep their history in plan-completion or retrospective artifacts instead.

## Current State

- `executor_critical` backend audit tier is fail-hard.
- Strict `automation_relevant` DTO subsets now include:
- admin-agent DTO contracts
- chat DTO contracts
- identity DTO contracts
- location DTO contracts
- social request/relation DTO contracts
- social overview/member DTO contracts
- social search/contact DTO contracts
- social admin circle DTO contracts
- workmarket dashboard DTO contracts
- workmarket quest-detail DTO contracts
- workmarket application-detail DTO contracts
- workmarket list/search/options DTO contracts
- workmarket news read-model DTO contracts
- common action/navigation DTO contracts
- Backend audit inventory now assigns every backend file a `domainId` and `ownerId`.
- Source-of-truth audit output now includes ownership-aware candidate entries plus domain and owner summaries for executor-critical files, strict DTO slices, and tracked tests.
- Admin-agent frontend workflow helpers now validate generated intent, endpoint, and safety-flag ids before the UI treats simulation output as contract-shaped.
- Application use-case contract tests now cover applicant-side apply, update, and withdraw workflows with target resolution, validation, persistence, event, and fail-closed checks.
- Workflow state-machine catalog validation now cross-checks documented transition intent ids against the agent operating model and requires real transition intents to be known mutating intents.
- Change impact preflight now reports deterministic changeset-scope guardrails for mixed product domains, runtime-plus-tooling changes, broad generated-report churn, and unexpected generated report files.
- Generated artifact policy is now explicit in `docs/generated/artifact-policy.yaml`, separating source-of-truth artifacts, tracked review context, disposable local context, and do-not-commit-by-default paths.
- Context-first session starts are standardized around diff summary, audit summary index, topic context pack, and only then repo map, symbol index, or broad repository exploration.
- Broad implementation work now has explicit operating-model checkpoints for plan, first backend slice, first frontend slice, docs/artifacts sync, validation, and commit boundary.
- Documentation ownership is now machine-readable in the agent operating model, mapping backend audit domains and change categories to required living docs, generated artifacts, and validation checks.
- Lightweight validation evidence records are now schema-backed under `.agents/validation-evidence/` and capture command results, generated artifact refreshes, skipped checks, and skipped-check reasons.
- Persistent backlog IDs now have traceability reporting in `scripts/todo-audit.rb` across plans, feature manifests, docs, code surfaces, and inline backlog references.
- Cross-domain terminology is now centralized in `docs/cross-domain-glossary.md`.
- Documentation templates by change type now live under `.agents/templates/docs/` and are registered in `documentation_ownership.documentation_templates`.
- Logic-drift closeout now requires a doc delta summary with behavior changed, docs updated, and intentionally unchanged related surfaces.
- Documentation staleness scoring now has a report-first local audit at `make audit-doc-staleness-scoring`.
- Compact implementation examples now live in `docs/example-scenario-library.md`.
- Self-test tiers by risk and change profile now live in `policies.self_test_matrix`.
- Validation evidence quality now has a strict local audit at `make audit-validation-evidence-quality`.
- Feature closeout is now hard-fail enforced by `make feature-closeout-audit manifest=<manifest-file>` for completed manifests, including structured validation evidence, plan completion, profile-specific required commands, and duplicate artifact bucket checks.
- Feature manifest decisions now distinguish required, optional, and skipped-with-reason cases in the completion checklist and documentation sync policy.
- Validation evidence records now require `ranAt` on commands and generated-artifact entries, and `make audit-validation-evidence-quality` rejects vague generated-artifact evidence or skipped entries without concrete reasons.
- Documentation sync policy wording now keeps protected canonical phrases in complete authoritative rules instead of duplicate or fragmentary bullets.
- Plan completion is now machine-checkable through `make audit-plan-completion plan=<plan-file>`, and feature closeout fails completed manifests whose referenced plan lacks completion evidence.
- Closeout now prunes archive-only generated history through `make cleanup-generated-history`, rejects archive-only paths as live evidence, and requires non-placeholder changed-files, validation-evidence, and doc-delta plan completion fields before a completed plan can close cleanly.
- Closeout reports can now be generated with `make closeout-report manifest=<manifest-file>` from manifest evidence, changed files, docs delta, generated artifacts, backlog delta, validation commands, and residual risks.
- Critical regression scenarios are cataloged by domain in `docs/regression-scenario-catalog.yaml` and summarized in `docs/regression-scenario-catalog.md`.
- Selected living-document sections are promoted into contract-like slices in `docs/docs-as-contract-slices.yaml` and summarized in `docs/docs-as-contract-slices.md`.
- Architecture drift now has a report-first audit at `make audit-architecture-drift`.
- Large-change retrospectives can be generated with `make post-merge-retrospective topic=<short-topic>` and are kept as disposable local context.
- Current report-only remainder inside `automation_relevant`, including the broad service catch-all, stays intentionally broad inventory coverage; future tightening should promote only small rule-scoped read-model or service slices after they prove low-noise.

## Open Items

## Operating Rule

- Prefer small, low-noise strict slices over whole-tier tightening.
- When one slice is promoted, update this backlog to remove or narrow the remaining work.
- Do not treat this file as proof that a change was implemented; it is a continuity aid for future sessions.
- Record new deferred implementation or control-system work in the appropriate persistent backlog with a stable ID before closing the change that discovered it.
- Keep only open items here. When an item is implemented, remove it from the open backlog in the same change and clear matching inline `TODO(<ID>):` or `FIXME(<ID>):` references.
- When a backlog item is implemented, remove it from the open backlog and clear matching inline `TODO(<ID>):` or `FIXME(<ID>):` references in the same change.

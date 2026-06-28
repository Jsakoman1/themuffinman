# Agent Improvement Backlog

This file is the persistent forward-looking backlog for agent-safety, backend audit tightening, control-flow hardening, and documentation automation improvements.

It is not the execution source of truth for a single change. Active implementation details should still live in temporary plans under `.agents/`.

Use this file to preserve what remains worth tightening across sessions.

## Current State

- `executor_critical` backend audit tier is fail-hard.
- Strict `automation_relevant` DTO subsets now include:
- admin-agent DTO contracts
- chat DTO contracts
- identity DTO contracts
- location DTO contracts
- Backend audit inventory now assigns every backend file a `domainId` and `ownerId`.
- Current report-only remainder inside `automation_relevant` is still intentionally broad and should be tightened in small slices.

## Open Items

- [ ] AGENT-SOCIAL-DTO-REQUEST-RELATION: Tighten `social/dto` request and relation DTOs as the next small strict automation-relevant slice.
- [ ] AGENT-SOCIAL-DTO-OVERVIEW-MEMBER: Tighten `social/dto` overview and member DTOs after the request and relation slice is stable.
- [ ] AGENT-SOCIAL-DTO-SEARCH-CONTACT: Tighten `social/dto` search and contact-list DTOs after the overview and member slice is stable.
- [ ] AGENT-SOCIAL-DTO-ADMIN-CIRCLE: Tighten admin circle DTOs as the last social DTO slice instead of promoting the whole package at once.
- [ ] AGENT-WORKMARKET-DASHBOARD-DTO: Tighten workmarket dashboard DTOs as the first selected workmarket read-contract slice.
- [ ] AGENT-WORKMARKET-QUEST-DETAIL-DTO: Tighten workmarket quest-detail DTOs after the dashboard slice is stable.
- [ ] AGENT-WORKMARKET-APPLICATION-DETAIL-DTO: Tighten workmarket application-detail DTOs after the quest-detail slice is stable.
- [ ] AGENT-WORKMARKET-LIST-SEARCH-OPTIONS-DTO: Tighten workmarket list, search, and option DTOs after the detail slices are stable.
- [ ] AGENT-AUTOMATION-RELEVANT-SERVICE-COVERAGE: Keep broader `automation_relevant` service coverage report-first until DTO contract drift is much lower.
- [ ] AGENT-RULE-SCOPED-READ-MODEL-SLICES: Add more rule-scoped strict slices for high-value automation read models before considering wider tier promotion.
- [ ] AGENT-OWNERSHIP-AWARE-SOURCE-REPORTING: Add ownership-aware reporting to source-of-truth audit output, not only backend inventory output.
- [ ] AGENT-WORKFLOW-AWARE-FRONTEND-HELPERS: Tighten workflow-aware frontend helper coverage as a separate control-system slice.
- [ ] AGENT-USE-CASE-CONTRACT-HARNESS: Standardize use-case workflow contract harness coverage across more mutation surfaces.
- [ ] AGENT-DOC-TO-RUNTIME-SEMANTIC-CHECKS: Add stronger semantic checks between documented rules and runtime behavior.
- [ ] AGENT-BROADER-PLANNER-DTO-REGISTRATION: Extend source-of-truth registration audits to broader planner-visible DTO surfaces.
- [ ] AGENT-CHANGESET-SCOPE-GUARDRAILS: Add deterministic guardrails that warn when a requested change mixes unrelated feature work, generated reports, and infrastructure/tooling changes in one commit-sized scope.
  Purpose: keep future Codex work cheaper to review, easier to validate, and less likely to preserve accidental unrelated edits.
- [ ] AGENT-GENERATED-ARTIFACT-POLICY-TIGHTENING: Define which generated reports are source-of-truth artifacts, which are disposable local context, and which should not be committed by default.
  Purpose: reduce large generated diffs and make future Codex sessions read only the generated artifacts that actually matter.
- [ ] AGENT-CONTEXT-FIRST-WORKFLOW: Standardize a session-start workflow where Codex reads a compact codebase capsule, diff summary, audit summary index, and topic context pack before broad repository exploration.
  Purpose: reduce token usage by making compact local summaries the default entrypoint for implementation work.
- [ ] AGENT-IMPLEMENTATION-SLICE-CHECKPOINTS: Extend the operating model with explicit small-slice checkpoints for broad changes: plan, first backend slice, first frontend slice, docs/artifacts sync, validation, and commit boundary.
  Purpose: avoid oversized implementation turns and make partial progress safer to resume.
- [ ] AGENT-DOCS-OWNERSHIP-MATRIX: Add a machine-readable mapping from domains and change categories to required living docs, generated artifacts, and validation tests.
  Purpose: preserve documentation logic without making Codex repeatedly infer which docs are in scope.
- [ ] AGENT-VALIDATION-EVIDENCE-MANIFEST: Add a lightweight per-change validation evidence record that captures commands run, generated reports refreshed, skipped checks, and reasons.
  Purpose: make closeout cheaper to audit and reduce repeated user questions about what was or was not validated.
- [ ] AGENT-BACKLOG-TO-CODE-TRACEABILITY: Add audits that link persistent backlog IDs to inline TODO/FIXME references, plans, feature manifests, docs, and code surfaces.
  Purpose: prevent forgotten backlog work and make future cleanup sessions cheaper.
- [ ] AGENT-CROSS-DOMAIN-CONCEPT-GLOSSARY: Maintain a compact glossary for reused concepts such as users, circles, bookings, visibility, consent, messaging, quests, applications, and reviews.
  Purpose: reduce terminology drift and keep future module implementations aligned without rereading all living docs.
- [ ] AGENT-DOC-TEMPLATE-BY-CHANGE-TYPE: Define short documentation templates for new workflow, new endpoint, new DTO contract, new module, new permission rule, and schema migration changes.
  Purpose: make docs updates faster and less inconsistent while keeping living docs concise.
- [ ] AGENT-DOC-DELTA-SUMMARY-REQUIRED: Add a closeout rule that every logic change records a short doc delta summary: what behavior changed, which docs were updated, and what intentionally did not change.
  Purpose: reduce drift between code, docs, and final Codex explanations.
- [ ] AGENT-DOC-STALENESS-SCORING: Add report-first scoring for stale docs based on code changes, endpoint inventory, DTO inventory, and workflow state changes since each doc section was last touched.
  Purpose: direct Codex to the highest-risk documentation gaps instead of rereading all docs.
- [ ] AGENT-EXAMPLE-SCENARIO-LIBRARY: Maintain compact examples for common implementation patterns such as adding an endpoint, changing a workflow transition, adding a DTO, adding a migration, and updating docs.
  Purpose: give Codex canonical patterns to follow instead of inferring style from many files.
- [ ] AGENT-SELF-TEST-MATRIX-BY-RISK: Extend the operating model with explicit self-test tiers: syntax-only, targeted unit, domain scenario, contract/type-check, generated-artifact validation, and full validation.
  Purpose: make validation cheaper for low-risk changes while still fail-hard for high-risk ones.
- [ ] AGENT-TEST-EVIDENCE-QUALITY-GATES: Add validation evidence quality checks that reject vague closeouts such as "tests not run" without a reason, or "build passed" without command and scope.
  Purpose: preserve reliable implementation process across sessions.
- [ ] AGENT-REGRESSION-SCENARIO-CATALOG: Maintain a catalog of critical regression scenarios per domain and map them to test classes and commands.
  Purpose: help Codex choose targeted tests without scanning the whole test tree.
- [ ] AGENT-DOCS-AS-CONTRACT-SLICES: Promote selected business/domain documentation sections into contract-like slices that must have corresponding runtime tests or audit checks.
  Purpose: make important documented behavior executable instead of purely narrative.
- [ ] AGENT-ARCHITECTURE-DRIFT-REVIEW: Add a recurring audit that flags services, controllers, Vue views, and docs sections that exceed size or responsibility thresholds.
  Purpose: catch architectural decay before it makes future Codex changes expensive.
- [ ] AGENT-POST-MERGE-LEARNING-LOOP: After large changes, generate a short retrospective artifact with failure points, missing tools, docs gaps, and reusable patterns.
  Purpose: turn every expensive implementation into a cheaper future implementation.
- [ ] AGENT-FEATURE-CLOSEOUT-HARD-ENFORCEMENT: Convert the current feature closeout flow from advisory checklist output into a hard-fail enforcement layer for plan-driven and high-risk changes.
  Problem:
  - `scripts/feature-closeout-audit.sh` currently prints required checks and checklist status but does not fail when checklist fields remain false.
  - Feature manifests can claim `backendTestsPassed`, `frontendValidationPassed`, `docsSynced`, or `agentModelSynced` without structured evidence.
  - Plan-driven workflow is documented, but high-risk changes can still bypass manifest completion unless the agent voluntarily uses it.
  Implementation phases:
  - Phase 1: extend `docs/feature-completion-manifest.schema.json` with `validationEvidence`, `docDelta`, `generatedArtifacts`, `planCompletion`, and `closeoutDecision` sections.
  - Phase 2: update `.agents/templates/feature-completion-manifest.template.yaml` so new manifests start with explicit evidence placeholders and skipped-check reason fields.
  - Phase 3: make `scripts/feature-closeout-audit.sh` fail if `status: complete` is missing, required checklist fields are false, required profile commands lack evidence, or skipped checks have no reason.
  - Phase 4: add profile-specific enforcement rules: `backend-logic` requires backend test evidence and domain docs review; `agent-contract` requires generated model/artifact evidence plus `make audit-agent-safety`; `frontend-contract` requires type-check/build evidence; `workflow-expansion` requires scenario or use-case contract test evidence.
  - Phase 5: add a validation test that loads all `.agents/feature-manifests/*.yaml`, validates schema, verifies referenced artifact paths exist, and verifies completed manifests have evidence for every declared required check.
  Acceptance criteria:
  - A manifest with `status: complete` and `backendTestsPassed: false` fails closeout.
  - A manifest with `backendTestsPassed: true` but no command evidence fails closeout.
  - A high-risk or executor-critical manifest without `make audit-agent-safety` evidence fails closeout.
  - A frontend-contract manifest without both `npm run type-check` and `npm run build` evidence fails closeout.
  - A workflow-expansion manifest without scenario-style or use-case contract test evidence fails closeout.
  - A manifest that lists the same file in multiple artifact buckets fails closeout.
  - The closeout script exits non-zero on all enforcement failures instead of only printing warnings.
  Files likely affected:
  - `docs/feature-completion-manifest.schema.json`
  - `.agents/templates/feature-completion-manifest.template.yaml`
  - `scripts/feature-closeout-audit.sh`
  - `apps/themuffinman/src/test/java/com/themuffinman/app/docs/AgentOperatingModelValidationTest.java`
  - `docs/change-completion-checklist.md`
  - `docs/documentation-sync-policy.md`
- [ ] AGENT-MANDATORY-MANIFEST-DECISION-GATE: Add an explicit rule that every non-trivial change must record either a feature manifest or a documented reason why the manifest workflow was not used.
  Problem:
  - The current checklist says the feature manifest is optional for plan-driven workflow, but it does not force a decision record for multi-layer changes.
  - This leaves room for broad changes to finish with only conversational closeout.
  Proposed rule:
  - Cosmetic and single-file contract-neutral refactors may skip manifests.
  - Multi-file, multi-layer, high-risk, executor-critical, workflow-expansion, agent-contract, or generated-artifact changes must use a manifest.
  - If a change is borderline, the agent must record a one-line manifest decision in the temporary plan or final closeout.
  Acceptance criteria:
  - `docs/change-completion-checklist.md` distinguishes `manifest required`, `manifest optional`, and `manifest skipped with reason`.
  - `docs/documentation-sync-policy.md` explains the same decision rule without weaker wording.
  - `make bootstrap-feature-work` remains the preferred way to create required manifests.
- [ ] AGENT-VALIDATION-EVIDENCE-SCHEMA: Standardize evidence records for validation commands and generated artifacts.
  Proposed evidence fields:
  - `command`: exact command that was run.
  - `scope`: backend, frontend, docs, generated-artifact, local-tooling, or smoke.
  - `result`: passed, failed, skipped, or not_applicable.
  - `ranAt`: ISO timestamp or `unknown` for historical migration entries.
  - `summary`: short result summary.
  - `outputPath`: optional generated report or diagnostic summary path.
  - `skippedReason`: required when `result` is skipped or not_applicable.
  Acceptance criteria:
  - Closeout rejects skipped checks without `skippedReason`.
  - Closeout rejects required checks with `result: failed`.
  - Closeout accepts optional checks only when they are not required by risk tier or profile.
- [ ] AGENT-DOC-SYNC-POLICY-CLEANUP: Remove duplicated and fragmentary documentation-sync wording while preserving protected canonical phrases exactly.
  Problem:
  - `docs/documentation-sync-policy.md` contains repeated protected wording and fragment-only bullets.
  - The current meaning is strong, but repeated near-duplicates make future edits riskier.
  Acceptance criteria:
  - Protected phrases still satisfy `AgentOperatingModelValidationTest`.
  - Duplicate bullets are consolidated into one authoritative rule per concept.
  - `docs/change-completion-checklist.md`, `docs/documentation-sync-policy.md`, and `AGENTS.md` no longer have conflicting strength for the same requirement.
- [ ] AGENT-PLAN-COMPLETION-ENFORCEMENT: Add machine-checkable completion markers for temporary plans and master plans.
  Problem:
  - Plans are useful working memory, but completion is currently human-discipline only.
  - Master plans can claim completion without checking child plan state or validation evidence.
  Proposed approach:
  - Add a required `Completion Evidence` section to plan templates.
  - Add explicit child-plan status rows to master plans.
  - Extend closeout audit to verify the manifest plan file exists and contains completion evidence when manifest status is complete.
  Acceptance criteria:
  - Completed manifests fail if their referenced plan is still mostly unchecked or lacks completion evidence.
  - Master plan closeout fails if any listed child plan is incomplete without a reason.
- [ ] AGENT-CLOSEOUT-REPORT-GENERATION: Generate a compact final closeout report from manifest evidence, changed files, docs delta, backlog delta, generated artifacts, and validation results.
  Purpose:
  - Make final user summaries factual and cheap.
  - Reduce repeated manual inspection before commit/push.
  Proposed output:
  - `docs/generated/local-tooling/closeout-reports/<feature-id>.json`
  - `docs/generated/local-tooling/closeout-reports/<feature-id>-summary.md`
  Acceptance criteria:
  - Report lists changed code/docs/tests/generated artifacts.
  - Report lists validation commands with pass/fail/skipped status.
  - Report lists created/resolved backlog IDs.
  - Report lists known residual risks.

## Operating Rule

- Prefer small, low-noise strict slices over whole-tier tightening.
- When one slice is promoted, update this backlog to remove or narrow the remaining work.
- Do not treat this file as proof that a change was implemented; it is a continuity aid for future sessions.
- Record new deferred implementation or control-system work in the appropriate persistent backlog with a stable ID before closing the change that discovered it.
- Keep only open items here. When an item is implemented, remove it from the open backlog in the same change and clear matching inline `TODO(<ID>):` or `FIXME(<ID>):` references.
- When a backlog item is implemented, remove it from the open backlog and clear matching inline `TODO(<ID>):` or `FIXME(<ID>):` references in the same change.

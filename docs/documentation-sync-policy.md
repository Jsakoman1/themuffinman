# Documentation Sync Policy

This file defines how logical code changes must propagate into living documentation and agent-safety artifacts.

## Goal

- Prevent silent drift between backend logic and documentation.
- Prevent future chatbot or voice-agent behavior from relying on stale assumptions.
- Make logical changes fail review when affected documentation is not updated.

## Rule

- A logic change is not complete when only code and tests are updated.
- If a change affects business meaning, permissions, validations, workflows, state transitions, automation assumptions, contact eligibility, visibility, or endpoint contracts, all affected documentation files must be updated in the same change.
- If a change introduces or expands entity capabilities, generation inputs, workflow branches, or edge cases that matter for admin or sandbox data generation, review and extend affected admin or sandbox generation flows in the same change.
- Treat admin-generation or sandbox-generation coverage for entities and workflows as part of the same maintenance surface as backend logic and agent-safety rules.
- For multi-file, multi-layer, or high-risk logical changes, create a temporary implementation plan in `.agents/` before substantial edits and close it only after validation is green.
- Prefer bootstrapping that plan and its matching feature manifest through `make bootstrap-feature-work` when the change is large enough to justify the plan-driven workflow.
- A feature manifest is required for multi-file, multi-layer, high-risk, executor-critical, workflow-expansion, agent-contract, frontend-contract, backend-logic, generated-artifact, or master-plan-driven changes.
- A feature manifest is optional only for cosmetic and single-file contract-neutral refactors that do not alter behavior, contracts, generated artifacts, validation scope, or documentation meaning.
- If a non-trivial change does not use a manifest, the temporary plan or final closeout must record a one-line skipped-with-reason decision explaining why the required category does not apply.
- For broad, long-running, or high-complexity work, prefer a master plan that coordinates a group of narrower `.agents/*-plan.md` files in explicit sequence instead of treating the entire task as one flat plan.
- Use the master-plan pattern when it safely reduces unnecessary human interaction, increases automation, or makes a larger batch auditable through one final closeout pass.
- For broad, long-running, high-complexity, multi-layer, high-risk, or master-plan-driven changes, use explicit implementation checkpoints for plan, first backend slice, first frontend slice, docs/artifacts sync, validation, and commit boundary.
- Treat feature manifests as profile-driven control artifacts, not free-form notes.
- Completed feature manifests must pass `make feature-closeout-audit manifest=<manifest-file>`, including structured validation evidence for every required profile check, completed plan evidence, and a ready closeout decision.
- Temporary plans, master plans, and completed manifest-backed changes must pass `make audit-plan-completion plan=<plan-file> [manifest=<manifest-file>]` when plan completion evidence is part of the closeout boundary.
- Manifest-backed closeout summaries can be generated with `make closeout-report manifest=<manifest-file>` so final review can use structured evidence instead of rereading the whole change.
- Treat `docs/implementation-backlog.md` as the persistent source of truth for open implementation follow-ups, and `docs/agent-improvement-backlog.md` as the persistent source of truth for open agent/control-system follow-ups.
- Record new deferred implementation or control-system work in the appropriate persistent backlog with a stable ID before closing the change that discovered it.
- When a backlog item is implemented, remove it from the open backlog and clear matching inline `TODO(<ID>):` or `FIXME(<ID>):` references in the same change.
- Keep open backlog IDs traceable outside the backlog file through at least one plan, feature manifest, doc, code surface, or inline `TODO(<ID>):` or `FIXME(<ID>):` reference.
- Keep feature-manifest artifact lists precise: runtime code belongs in `codePaths`, tests and test resources belong in `testPaths`, and the same path must not be listed in multiple artifact groups.
- Keep validation evidence records under `.agents/validation-evidence/` aligned with `docs/validation-evidence.schema.json` when a change needs command, generated-artifact, or skipped-check evidence.
- Validation evidence must name exact commands, scopes, `ranAt`, generated-artifact actions, and concrete reasons for skipped checks, and pass `make audit-validation-evidence-quality` instead of relying on vague closeout text.
- Keep `docs/regression-scenario-catalog.yaml` aligned with critical domain workflows, permission rules, validation rules, and automation-safe behaviors so targeted regression commands stay discoverable.
- Keep `docs/docs-as-contract-slices.yaml` aligned with living-document sections whose claims are backed by runtime tests or audit checks.
- Use the matching `.agents/templates/docs/` documentation template for new workflows, endpoints, DTO contracts, modules, permission rules, and schema migrations.
- Update `docs/feature-delivery-workflow.md` when the implementation workflow, planning workflow, context gateway workflow, evidence capture path, manifest workflow, or closeout command flow changes.
- For logic-drift changes, record a doc delta summary that names behavior changed, docs updated, and related surfaces intentionally left unchanged.
- Purely cosmetic edits are excluded.

## Change Classification

- `small-change` is for cosmetic edits or contract-neutral refactors that do not change workflow meaning, endpoint contracts, or automation expectations.
- `major-change` is for backend logic, agent contract, workflow, or planner-surface changes that touch multiple maintenance surfaces.
- `cosmetic` means wording, formatting, or non-semantic cleanup only.
- `contract-neutral-refactor` means code movement or simplification with preserved runtime contract.
- `logic-drift` means behavior, validation, workflow, or automation meaning changed and docs must move with it.

## Feature Manifest Profiles

- `backend-logic` changes must include backend validation plus `docs/domain-technical.md`.
- `agent-contract` changes must include `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, the generator step, and `make audit-agent-safety`.
- `frontend-contract` changes must include generated frontend contracts, `npm run validate:contracts`, `npm run type-check`, and `npm run build`.
- `workflow-expansion` changes must include generated agent artifacts and at least one scenario-style test.
- Use `make bootstrap-feature-work topic=<topic> [risk=<tier>] [mode=<mode>] [impact=<impact>] [profiles=<csv>]` so the plan and manifest start with the correct control profile.
- Every newly introduced workflow or self-service action must include an explicit feature-introduction review for agent-operating docs, machine-readable intents and endpoints, generated contract or inventory artifacts, and any synthetic admin-generation or sandbox-generation flow coverage that depends on the changed behavior.

## Authoring Discipline

- When `docs/agent-operating-model.yaml` uses `documentation_sync.rules[*].must_contain_all`, treat those phrases as canonical wording, not as paraphrase prompts.
- For protected documentation-sync phrases, copy the exact canonical sentence verbatim into every required file.
- Do not paraphrase, shorten, reorder, or partially restate protected canonical wording.
- When the machine-operating model is edited through `docs/agent-operating-model/sections/*.yaml`, regenerate `docs/agent-operating-model.yaml` and the generated inventory artifacts before final validation.
- Copy protected phrases directly between YAML and target docs when the rule is meant to enforce the same meaning in multiple places.
- Validation should ignore case, punctuation, markdown markers, and whitespace differences, but it should not ignore wording drift that changes or weakens meaning.
- If a phrase keeps drifting, reduce synonyms and near-duplicates instead of adding more variants just to satisfy the test.
- Run `AgentOperatingModelValidationTest` after editing protected docs, YAML rules, generation-flow docs, or agent-safety instructions.
- Run `make audit-todo` when backlog entries or inline `TODO/FIXME` references were added, resolved, or moved.
- Regenerate `docs/generated/agent-endpoint-inventory.json` and `docs/generated/automation-read-model-inventory.json` when controller mappings or automation DTO fields change.
- Regenerate `docs/generated/source-of-truth-audit.json` when tracked controllers, services, mappers, or workflow tests change.
- Keep source-of-truth audit ownership reporting aligned with backend domain ownership so source registration, documentation gaps, and workflow gaps can be routed to the right product owner.
- Keep documentation ownership mapping aligned with backend audit domains and change categories so required living docs, generated artifacts, and validation checks stay machine-readable.
- Regenerate `docs/generated/backend-audit-inventory.json` when backend package coverage, classification rules, or strict audit tiers change.
- When business rules, domain models, permissions, validations, workflows, endpoint contracts, or automation assumptions change, update all affected living docs in the same change.
- No logic-only change is complete until the affected docs, agent artifacts, and validation tests are updated together.

Use `docs/change-completion-checklist.md` as the fast pre-finish review layer, but keep this policy and `AGENTS.md` as the stronger source rules.

## Mandatory Files

For logical product changes, review and update as needed:
- `docs/business-logic.md`
- `docs/domain-technical.md`
- `docs/agent-operating-model.md`
- `docs/agent-operating-model.yaml`
- `docs/implementation-backlog.md`
- `docs/agent-improvement-backlog.md` when finishing or reprioritizing long-running agent/control-system tightening work
- `docs/documentation-sync-policy.md`
- `docs/change-completion-checklist.md`
- `docs/feature-completion-manifest.schema.json`
- `docs/validation-evidence.schema.json`
- `docs/regression-scenario-catalog.md`
- `docs/regression-scenario-catalog.yaml`
- `docs/docs-as-contract-slices.md`
- `docs/docs-as-contract-slices.yaml`
- `AGENTS.md`

## Mandatory Tests

For logical product changes, keep passing:
- `apps/themuffinman/src/test/java/com/themuffinman/app/docs/AgentOperatingModelValidationTest.java`
- domain tests that cover the changed behavior, such as service tests for the affected module

## Change Matrix

### Business or permission logic

Update:
- `docs/business-logic.md`
- `docs/domain-technical.md`
- `AGENTS.md` only if the repository-wide maintenance rule itself changes

Test:
- add or extend domain tests around the changed service behavior

### Agent or automation safety logic

Update:
- `docs/agent-operating-model.md`
- `docs/agent-operating-model.yaml`
- `docs/business-logic.md`
- `docs/domain-technical.md`

Test:
- `AgentOperatingModelValidationTest`

Rule:
- extend affected admin-generation, batch-generation, or sandbox-generation workflows whenever a new feature changes what safe generated entities or transitions should contain
- extend read-before-write resolution flows whenever new natural-language management commands need exact target lookup, deterministic selection, or destructive confirmation
- keep executor-critical read DTO inventory aligned with required resolution fields, deterministic selection fields, producer references, and verifier tests
- keep mutating intent risk groups, exact-target resolution links, multilingual prompt matrix coverage, and documentation coverage manifests aligned with the code surface
- keep capability registries, intent lineage mappings, dry-run simulation contracts, and prompt drift fingerprints aligned with the actual planner surface
- keep backend contract snapshots, service workflow inventory, permission matrix rules, state-transition audit, and request-validation gates aligned with the actual backend mutation surface
- keep generated frontend contracts, automation-safe UI safety layers, frontend regression scenarios, and frontend feature expectations aligned with the actual planner and simulation surface
- keep generated workflow-aware frontend helpers, intent ids, endpoint ids, unresolved-input ids, and safety-flag ids aligned with the actual operating model
- keep frontend planner-response contract gates, dead-path checks, and feature completion manifests aligned with the actual implementation state
- keep persistent backlog IDs, inline `TODO/FIXME` references, and close-out manifest backlog links aligned with the actual implementation state
- keep feature risk tiers, bootstrap workflow, and close-out audit workflow aligned with the actual delivery process
- keep backend audit tier classification aligned with the actual repo structure so full backend inventory stays complete even while only one tier is fail-hard today
- keep backend audit domain ownership aligned with the actual repo structure so review can route drift to the right product surface and owner
- keep source-of-truth audit ownership summaries aligned with backend domain ownership so source registration gaps can be routed to the right product surface and owner
- tighten broader backend audit in small rule-scoped slices, starting with high-value low-noise planner/admin-agent DTO contracts before wider automation-relevant DTO or service coverage
- tighten broader backend audit in small rule-scoped slices, with chat DTO contracts now added as the second strict automation-relevant subset
- tighten broader backend audit in small rule-scoped slices, with identity DTO contracts now added as the third strict automation-relevant subset
- tighten broader backend audit in small rule-scoped slices, with location DTO contracts now added as the fourth strict automation-relevant subset
- tighten broader backend audit in small rule-scoped slices, with social request/relation DTO contracts now added as the fifth strict automation-relevant subset
- tighten broader backend audit in small rule-scoped slices, with social overview/member DTO contracts now added as the sixth strict automation-relevant subset
- tighten broader backend audit in small rule-scoped slices, with social search/contact DTO contracts now added as the seventh strict automation-relevant subset
- tighten broader backend audit in small rule-scoped slices, with social admin circle DTO contracts now added as the eighth strict automation-relevant subset
- tighten broader backend audit in small rule-scoped slices, with workmarket dashboard DTO contracts now added as the ninth strict automation-relevant subset
- tighten broader backend audit in small rule-scoped slices, with workmarket quest-detail DTO contracts now added as the tenth strict automation-relevant subset
- tighten broader backend audit in small rule-scoped slices, with workmarket application-detail DTO contracts now added as the eleventh strict automation-relevant subset
- tighten broader backend audit in small rule-scoped slices, with workmarket list/search/options DTO contracts now added as the twelfth strict automation-relevant subset
- tighten broader backend audit in small rule-scoped slices, with workmarket news read-model DTO contracts now added as the thirteenth strict automation-relevant subset
- keep the broad automation-relevant service catch-all report-first until specific service slices have stable ownership, source registration, documentation coverage, and low-noise validation evidence

### Sandbox or synthetic-data logic

Update:
- `docs/business-logic.md`
- `docs/domain-technical.md`
- `docs/agent-operating-model.md`
- `docs/agent-operating-model.yaml`

Test:
- `AgentOperatingModelValidationTest`

Rule:
- sandbox behavior must stay explicitly separated from production-like user flows
- synthetic admin-generation flows must be kept current with newly introduced feature logic, validations, and edge cases

### Social or chat eligibility logic

Update:
- chat sections in `docs/business-logic.md`
- chat sections in `docs/domain-technical.md`
- `docs/agent-operating-model.md` and YAML when the rule affects future automation safety

Test:
- chat service tests
- `AgentOperatingModelValidationTest` when machine-operational rules or doc-sync rules change

### Schema or migration logic

Update:
- technical docs and source-of-truth inventory when migration lineage changes

Test:
- migration-related startup or integration verification

## Current Protected Rules

- Pending circle requests do not create chat eligibility.
- Chat workspace only includes current accepted circle contacts.
- Existing conversation history does not preserve chat access after the accepted relation is lost.

# Codex Tiered Workflow Plan

Purpose: implement a leaner, tiered Codex orchestration layer that keeps high-risk safety guarantees while reducing default startup context and manifest overhead for small and normal feature work.

## Scope

- [x] Add a compact fast-path workflow entrypoint for most feature work.
- [x] Introduce explicit tiered workflow guidance across AGENTS, workflow docs, checklist, and operating-model docs.
- [x] Make manifest usage conditional and tier-driven instead of effectively mandatory for most backend changes.
- [x] Improve bootstrap output so plans and optional manifests start with useful, deterministic guidance.
- [x] Keep machine-operational docs, templates, generated artifacts, and validation tests aligned.

## Child Phases

- [x] Phase 1: inspect manifest-decision, bootstrap, and operating-model policy sources that drive workflow enforcement.
- [x] Phase 2: implement the compact fast path, tier definitions, and startup routing updates in human-facing docs.
- [x] Phase 3: implement conditional manifest and tier-aware bootstrap/template changes in scripts and templates.
- [x] Phase 4: update machine-operational docs, generated YAML/artifacts, and validation expectations.
- [x] Phase 5: run targeted validation, closeout audits, and finalize documentation sync evidence.

## Working Notes

- Prefer repo-native updates over adding a new framework.
- Keep `docs/codex-fast-path.md` short and decision-oriented.
- Avoid duplicating long inventories across docs; link deeper references instead.
- Preserve full closeout rigor for high-risk and workflow/tooling changes.

## Deferred Follow-ups

- `WORKMARKET-QUEST-DEFAULT-PRICE-SUGGESTION-BACKEND`: remaining dashboard and detail application price suggestion cleanup.
- `WORKMARKET-QUEST-APPLICATION-DRAFT-VALIDATION-CENTRALIZATION`: remaining frontend draft-submit guard mirroring backend validation.
- `WORKMARKET-QUEST-DETAIL-PRESENTATION-GATING`: remaining quest detail visibility and dialog-gating cleanup in frontend composables.

## Completion Check

- [x] Fast-path entrypoint exists and routes small and normal work away from unnecessary full workflow reading.
- [x] Manifest policy is conditional and tier-driven across docs, scripts, templates, and operating-model rules.
- [x] Bootstrap output includes tier, manifest decision, recommended commands, expected docs, validation, and closeout.
- [x] Agent-operating model and validation tests pass after workflow updates.
- [x] Final closeout reflects actual validation and residual risk.

## Completion Evidence

- Status: complete
- Changed files: AGENTS startup routing, `docs/codex-fast-path.md`, tiered workflow docs, manifest policy docs, bootstrap/template tooling, manifest decision audit logic, and regenerated agent artifacts.
- Validation evidence: `ruby scripts/generate-agent-operating-model.rb`, `make generate-agent-artifacts`, `make audit-manifest-decision`, `./apps/themuffinman/mvnw -f apps/themuffinman/pom.xml -Dtest=AgentOperatingModelValidationTest test`, `make audit-agent-safety`, bootstrap smoke runs for Tier 2 and Tier 3, and final closeout audits.
- Doc delta summary: Feature implementation now starts from a compact fast path, escalates into the full workflow by tier, and only requires manifests when the tier or resolver says so.
- Backlog update: Added `AGENT-AUTOFILL-MANIFEST-WRITE-EPERM` to `docs/agent-improvement-backlog.md` for the direct autofill helper write failure observed during closeout.
- Residual risk: Manifest decision remains partly file-shape-driven, so truly high-risk single-surface business logic still depends on user-selected risk or resolver escalation rather than static path heuristics alone.

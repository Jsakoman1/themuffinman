# Architecture Decision Index

- Generated at: `2026-06-29T12:47:18Z`
- Decisions: `10`

## `backend-centric-domain-logic`

- Title: Keep business rules in backend services
- Decision: Permissions, validations, workflow rules, state transitions, and automation assumptions belong in backend services so web and future mobile clients share the same behavior.
- Applies to: `backend`, `frontend`, `automation`
- Sources: `AGENTS.md`, `docs/domain-technical.md`, `docs/change-completion-checklist.md`

## `thin-controllers`

- Title: Keep controllers transport-only
- Decision: Controllers should stay thin and delegate behavior, DTO assembly, and permission decisions to service or mapper layers.
- Applies to: `backend`
- Sources: `AGENTS.md`, `docs/domain-technical.md`

## `docs-sync-required`

- Title: Logic changes move with living docs
- Decision: A logic change is not complete when only code and tests are updated; affected docs, agent artifacts, and validation tests move in the same change.
- Applies to: `docs`, `backend`, `frontend`, `automation`
- Sources: `AGENTS.md`, `docs/documentation-sync-policy.md`, `docs/change-completion-checklist.md`

## `generated-artifacts-authoritative`

- Title: Generated source-of-truth artifacts must stay fresh
- Decision: Generated endpoint, read-model, backend-audit, source-of-truth, and frontend contract artifacts are authoritative review inputs when their source surfaces change.
- Applies to: `generated-artifacts`, `automation`, `frontend`, `backend`
- Sources: `docs/generated/artifact-policy.yaml`, `docs/domain-technical.md`, `docs/change-completion-checklist.md`

## `flyway-forward-only`

- Title: Schema changes use new Flyway migrations
- Decision: Schema changes add a new Flyway migration instead of editing old migrations.
- Applies to: `database`, `backend`
- Sources: `AGENTS.md`, `docs/change-completion-checklist.md`

## `sandbox-production-separation`

- Title: Sandbox behavior stays separate
- Decision: Sandbox and synthetic admin-generation behavior must stay explicitly separated from production-like user flows and semantics.
- Applies to: `sandbox`, `automation`, `docs`
- Sources: `AGENTS.md`, `docs/documentation-sync-policy.md`, `docs/agent-operating-model.md`

## `no-commit-without-explicit-request`

- Title: No commit or push by default
- Decision: Do not commit or push changes unless the user explicitly requests it.
- Applies to: `workflow`, `git`
- Sources: `AGENTS.md`

## `plan-backed-high-risk-work`

- Title: High-risk work needs plan-backed closeout
- Decision: Multi-file, multi-layer, high-risk, master-plan-driven, or manifest-backed changes need explicit plan evidence, validation evidence, backlog hygiene, and closeout checks.
- Applies to: `workflow`, `docs`, `automation`
- Sources: `docs/documentation-sync-policy.md`, `docs/change-completion-checklist.md`, `docs/feature-completion-manifest.schema.json`

## `typed-operational-config`

- Title: Centralize operational config
- Decision: Operational backend settings such as TTLs, cleanup jobs, limits, polling intervals, and timeouts belong in typed central configuration objects.
- Applies to: `backend`, `config`
- Sources: `AGENTS.md`

## `frontend-shared-shell`

- Title: Standardize authenticated frontend surfaces
- Decision: Authenticated pages should use shared app shell, shared components, shared tokens, and minimal UI-side product logic.
- Applies to: `frontend`
- Sources: `AGENTS.md`, `apps/themuffinman/frontend/README.md`

# Codebase Capsule

- Generated at: `2026-06-29T12:47:35Z`
- Purpose: Very small read-first context for a new Codex session before broad repository discovery.

## Repo Layout

- apps/themuffinman: current Spring Boot app and Vue frontend
- apps/themuffinman/src/main/java/com/themuffinman/app: backend domains and layers
- apps/themuffinman/src/main/resources/db/migration: Flyway migrations
- apps/themuffinman/frontend: frontend app
- services: planned shared backend capabilities
- docs: living documentation and agent operating artifacts
- .agents: temporary plans, manifests, and validation evidence

## Active Conventions

- Keep business rules, permissions, validations, workflows, and state transitions in backend services.
- Keep controllers thin and frontend logic minimal.
- Use new Flyway migrations for schema changes; do not edit old migrations.
- Update living docs and generated agent artifacts with logic or contract changes.
- Use `.agents/*-plan.md` for multi-file, multi-layer, high-risk, or broad autonomous work.
- Do not commit or push unless the user explicitly asks.

## Open Backlog IDs

- None

## Open Master Plan Items

- None

## Preferred First Commands

- ruby scripts/todo-audit.rb
- make diff-summary
- make audit-summary-index
- make context-pack topic=<topic>
- make audit-router files=<csv>
- make changeset-risk

## Read Next

- AGENTS.md
- docs/codex-local-tooling-todo.md
- docs/domain-technical.md
- docs/business-logic.md
- docs/generated/local-tooling/audit-summary-index.md

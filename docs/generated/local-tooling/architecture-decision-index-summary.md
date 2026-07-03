# Architecture Decision Index

- Generated at: `2026-07-03T09:17:30Z`
- Decisions: `10`

## `backend-centric-domain-logic`

- Keep business rules in backend services
- Applies to: `backend`, `frontend`, `automation`

## `thin-controllers`

- Keep controllers transport-only
- Applies to: `backend`

## `docs-sync-required`

- Logic changes move with living docs
- Applies to: `docs`, `backend`, `frontend`, `automation`

## `generated-artifacts-authoritative`

- Generated source-of-truth artifacts must stay fresh
- Applies to: `generated-artifacts`, `automation`, `frontend`, `backend`

## `flyway-forward-only`

- Schema changes use new Flyway migrations
- Applies to: `database`, `backend`

## `sandbox-production-separation`

- Sandbox behavior stays separate
- Applies to: `sandbox`, `automation`, `docs`

- ... 4 more decisions
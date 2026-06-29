# Workmarket Frontend Capsule

## Responsibility

Owns workmarket routes, dashboard screens, quest/application detail surfaces, admin workmarket pages, API clients, and thin state adapters over backend-prepared DTOs.

## Main Entry Points

- Routes/pages: `pages/`, `views/`
- Shared components: `components/shared/`
- Dashboard components and composables: `components/dashboard/`, `composables/dashboard/`
- API gateway and clients: `api/workmarketApi.ts`, `api/clients/`
- Contract aliases: `api/contracts.ts`

## Validation

- `npm run type-check`
- `npm run build`

## Living Docs

- `docs/business-logic.md`
- `docs/domain-technical.md`
- `docs/generated/README.md`

## Forbidden Shortcuts

- Do not re-derive quest/application permissions when backend presentation flags or sections exist.
- Do not bypass module API clients by calling raw endpoints from views.
- Do not add large workflow logic directly to Vue route files.

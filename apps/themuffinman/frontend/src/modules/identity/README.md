# Identity Frontend Capsule

## Responsibility

Owns login and registration screens plus thin API wrappers for authentication.

## Main Entry Points

- Views: `views/LoginView.vue`, `views/RegisterView.vue`
- API: `api/authApi.ts`
- Auth state bridge: `auth.ts`

## Validation

- `npm run type-check`
- `npm run build`

## Living Docs

- `docs/business-logic.md`
- `docs/domain-technical.md`

## Forbidden Shortcuts

- Do not duplicate backend credential rules beyond basic form ergonomics.
- Do not store sensitive credential material in route state or local generated artifacts.

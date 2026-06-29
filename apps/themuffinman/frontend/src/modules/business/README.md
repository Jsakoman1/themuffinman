# Business Frontend Capsule

## Responsibility

Owns the business profile workspace and public business directory UI.

## Main Entry Points

- View: `views/BusinessHubView.vue`
- API: `api/businessApi.ts`
- Styles: `src/styles/business.css`

## Validation

- `npm run type-check`
- `npm run build`

## Living Docs

- `docs/business-logic.md`
- `docs/domain-technical.md`

## Forbidden Shortcuts

- Do not invent client-only profile ownership rules.
- Do not duplicate slug validation beyond basic form feedback.
- Do not add appointment booking UI until backend workflow exists.

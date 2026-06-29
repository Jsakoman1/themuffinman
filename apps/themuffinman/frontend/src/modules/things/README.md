# Things Frontend Capsule

## Responsibility

Owns the thing sharing directory, owner listing form, and borrower request interactions.

## Main Entry Points

- View: `views/ThingSharingView.vue`
- API: `api/thingsApi.ts`
- Styles: `src/styles/things.css`

## Validation

- `npm run type-check`
- `npm run build`

## Living Docs

- `docs/business-logic.md`
- `docs/domain-technical.md`

## Forbidden Shortcuts

- Do not enforce ownership or pending-request uniqueness only in the client.
- Do not add borrower approval workflows until backend state exists.
- Do not mix thing sharing copy or behavior with commercial rentals.

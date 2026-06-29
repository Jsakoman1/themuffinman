# Rides Frontend Capsule

## Responsibility

Owns voluntary ride sharing offer creation and active ride directory UI.

## Main Entry Points

- View: `views/RideSharingView.vue`
- API: `api/ridesApi.ts`
- Styles: `src/styles/rides.css`

## Validation

- `npm run type-check`
- `npm run build`

## Living Docs

- `docs/business-logic.md`
- `docs/domain-technical.md`

## Forbidden Shortcuts

- Do not present ride sharing as paid booking or transport commerce.
- Do not implement circle visibility only in frontend filtering.
- Do not add passenger reservation state before backend workflow exists.

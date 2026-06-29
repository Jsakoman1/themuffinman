# Social Frontend Capsule

## Responsibility

Owns circles, profile, user settings, and social admin route surfaces.

## Main Entry Points

- Views/pages: `views/`, `pages/`
- Components: `components/circles/`, `components/profile/`
- Composables: `composables/`
- Shared helpers: `shared/`

## Validation

- `npm run type-check`
- `npm run build`

## Living Docs

- `docs/business-logic.md`
- `docs/domain-technical.md`

## Forbidden Shortcuts

- Do not infer relationship authority only from displayed labels.
- Do not duplicate backend circle request state transitions in view files.
- Do not introduce one-off dialog patterns when shared UI components fit.

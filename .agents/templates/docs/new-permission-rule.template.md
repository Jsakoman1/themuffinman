# New Permission Rule Documentation Template

## Rule Summary

- Name the protected action or read surface.
- State the allowed and denied actor groups.

## Enforcement Point

- Service:
- Controller:
- Repository or query boundary:
- Frontend surface:

## Visibility And Consent

- Document ownership, circle relation, admin, consent, and exact-location visibility implications.

## Failure Behavior

- Document forbidden, not-found, validation, and redaction outcomes.
- Note audit or admin visibility behavior where relevant.

## Documentation Delta

- Update `docs/business-logic.md` when users can observe the rule.
- Update `docs/domain-technical.md` with the authoritative permission invariant.
- Update `docs/agent-operating-model.md` and permission matrices when automation can invoke or explain the rule.

## Validation Evidence

- Record positive and negative service/controller tests plus any agent-safety or frontend checks.

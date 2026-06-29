# New Endpoint Documentation Template

## Endpoint Contract

- Method:
- Path:
- Controller:
- Request DTO:
- Response DTO:

## Behavior Summary

- Describe the user-visible action or read surface.
- Document pagination, sorting, filtering, and idempotency assumptions where relevant.

## Permissions And Visibility

- Document authentication requirements.
- Document ownership, admin, circle, visibility, and consent checks enforced by the backend.

## Validation And Errors

- List required fields, constraints, 4xx cases, and important 5xx prevention checks.

## Documentation Delta

- Update `docs/domain-technical.md` with the endpoint contract and service boundary.
- Update `docs/business-logic.md` when the endpoint changes user-facing behavior.
- Update `docs/agent-operating-model.md` and generated endpoint inventories when agents can call or reason about the endpoint.

## Validation Evidence

- Record exact controller/service tests, contract generation checks, and frontend type/build checks.

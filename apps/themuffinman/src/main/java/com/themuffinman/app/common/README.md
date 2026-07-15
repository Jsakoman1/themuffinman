# Common Backend Capsule

## Responsibility

Owns reusable backend helpers that are intentionally domain-neutral: pagination, validation, normalization, time helpers, errors, DTO primitives, and shared service utilities.

## Main Entry Points

- Errors: `errors/ServiceErrors.java`
- Pagination: `pagination/`
- Validation and normalization: `validation/`, `normalization/`
- Time and date helpers: `time/`
- Shared DTOs and contracts: `dto/`, `contract/`
- Domain events: `event/DomainEventPublisher.java` and `event/SpringDomainEventPublisher.java`

## Tests

- Add focused unit tests near the affected shared behavior.
- Run affected domain tests when common behavior changes downstream contracts.

## Living Docs

- `docs/domain-technical.md`
- `docs/implementation-control.md`

## Forbidden Shortcuts

- Do not put domain-specific business rules in `common`.
- Do not change shared validation semantics without checking all callers.
- Do not add broad abstractions before at least two domains need the same behavior.
- Do not put event handling side effects in `common`; handlers belong to the owning domain.

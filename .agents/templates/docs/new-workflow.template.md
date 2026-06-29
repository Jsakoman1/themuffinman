# New Workflow Documentation Template

## Behavior Summary

- Describe the workflow in user-facing terms.
- List the actors that can start, advance, cancel, or observe the workflow.

## State Model

- List each state and the allowed transitions.
- Note terminal states and retry paths.

## Permissions And Visibility

- Document who can read each workflow surface.
- Document who can perform each transition and which backend service enforces it.

## Validation And Edge Cases

- List required fields, invalid transitions, ownership checks, and concurrency assumptions.
- Include failure responses or user-visible outcomes where relevant.

## Documentation Delta

- Update `docs/business-logic.md` for product behavior.
- Update `docs/domain-technical.md` for entities, states, permissions, and invariants.
- Update `docs/agent-operating-model.md` or generated artifacts when automation can invoke or inspect the workflow.

## Validation Evidence

- Record exact backend tests, generated-artifact refreshes, and frontend checks that cover the workflow.

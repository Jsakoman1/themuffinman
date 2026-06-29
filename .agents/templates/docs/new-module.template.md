# New Module Documentation Template

## Module Purpose

- Describe the module outcome and the user groups it serves.
- State how it relates to users, circles, scheduling, bookings, visibility, consent, and messaging.

## Core Entities

- List entities, ownership rules, lifecycle states, and cross-module references.
- Note which concepts are shared with existing modules.

## Workflows

- Describe create, read, update, delete, approval, booking, messaging, and admin-generation flows as applicable.

## Permissions And Visibility

- Document default visibility, consent requirements, admin powers, and production versus sandbox behavior.

## Documentation Delta

- Update `docs/business-logic.md` for product behavior.
- Update `docs/domain-technical.md` for entities, relations, validations, workflows, and invariants.
- Update `docs/agent-operating-model.md`, generated artifacts, and admin/sandbox generation coverage if automation can operate on the module.

## Validation Evidence

- Record domain tests, migration checks, generated-artifact refreshes, frontend type-check, and build status.

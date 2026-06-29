# Regression Scenario Catalog

This catalog maps critical product and agent-safety regression scenarios to the focused tests and commands that protect them.

The machine-readable source is `docs/regression-scenario-catalog.yaml`. Keep this summary aligned with that file.

## Usage

- Start from this catalog when selecting targeted validation for a domain change.
- Prefer the listed command before widening to `./mvnw test`.
- Add or update a scenario when a domain workflow, permission rule, validation rule, or automation-safe behavior gains a new critical edge case.

## Current Scenarios

- `agent-exact-target-fail-closed`: admin-agent mutating prompts require exact target resolution and confirmation.
- `workmarket-quest-lifecycle`: quest creation, mutation, execution, and term-change workflow contracts.
- `workmarket-application-lifecycle`: application apply, edit, withdraw, approve, decline, and admin mutation workflow contracts.
- `workmarket-dashboard-read-model`: backend-prepared dashboard sections, actions, groups, and notification destinations.
- `social-chat-relation-access`: circle relation state and chat eligibility boundaries.
- `location-exact-visibility`: exact-location visibility scopes and lookup-disabled behavior.
- `identity-admin-account-safety`: account, role, duplicate email, profile sanitization, and admin detail safety.
- `business-profile-public-directory`: business profile slugs, active directory entries, and inactive profile hiding.
- `things-borrow-request-workflow`: listing ownership, borrow request boundaries, duplicate pending requests, and viewer read state.
- `rides-circle-scoped-offers`: ride offer scheduling and circle-scoped visibility validation.
- `common-cross-module-primitives`: reusable actor, ownership, scheduling, visibility, rich-text, and structured error primitives.

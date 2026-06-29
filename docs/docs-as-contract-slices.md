# Docs As Contract Slices

This document summarizes selected living-document sections that are treated as contract-like slices.

The machine-readable source is `docs/docs-as-contract-slices.yaml`. Each slice names the protected documentation headings, the behavior claim, the test files, and the focused validation command.

## Current Slices

- `identity-account-admin-safety`: account management and admin-detail fail-closed rules.
- `location-exact-visibility-policy`: exact-location visibility and lookup-disabled rules.
- `social-chat-access-boundary`: accepted-relation and chat eligibility boundaries.
- `workmarket-quest-workflow-contract`: quest lifecycle, execution, and term negotiation workflow rules.
- `workmarket-application-workflow-contract`: application lifecycle and admin correction rules.
- `sandbox-boundary-contract`: sandbox-generation separation from production-like user actions.
- `shared-validation-rich-text-contract`: shared validation, structured errors, and rich-text sanitization.

## Maintenance Rule

When a protected documentation section changes, update the matching slice or its tests in the same change. If a section describes new behavior that is not covered by an existing test or audit, either add that verification or add a stable backlog item before closeout.

# Round 34: Permission, Visibility, and Consent Matrix

Status: source-trace matrix. Reviewed: 2026-07-22.

## Conclusion

Permissions, state transitions, visibility, consent, and recovery are backend
service decisions. The matrix provides a feature-navigation index across the most
important authority families; it is not an executable authorization policy and must
not be copied into frontend eligibility logic.

## Matrix interpretation

Every row identifies the controlling domain, main viewer roles, relevant object
state/visibility input, and recoverable failure boundary. Exact service logic and
tests remain canonical. Admin authority is explicit and does not imply normal-user
visibility or mutation access.

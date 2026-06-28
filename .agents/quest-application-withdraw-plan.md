## Goal

Allow a user to withdraw their own quest application from the quest detail flow while the application is still pending.

## Steps

1. Update quest detail backend assembly so `myApplication` includes applicant allowed actions and presentation flags.
2. Add backend regression coverage for pending and non-pending quest detail application actions.
3. Add quest detail frontend withdraw action and confirmation flow.
4. Update living docs for the pending-application withdrawal rule.
5. Run focused backend tests and frontend type/build verification.

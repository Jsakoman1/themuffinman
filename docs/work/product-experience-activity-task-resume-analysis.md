# Activity timeline and task resume analysis

Use a viewer-scoped projection over domain events rather than duplicating each module's history. Resume state must be bounded, expiring, dismissible, and free of secrets/raw sensitive form values. A resume card may reopen a route, but it must not mutate workflow state or bypass backend permissions.

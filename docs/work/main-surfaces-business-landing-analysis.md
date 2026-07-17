# Business landing child analysis

The current owner landing requests dashboard, profile, bookings, and calendar independently and uses `safeRequest`, which intentionally hides request failures. That is acceptable for best-effort secondary sections but not for the primary landing state. The implementation must distinguish setup-required, forbidden, unavailable, and empty states and should prefer one backend-prepared landing DTO if the existing composition cannot express those states deterministically.

No public business discovery data may be used to fake owner setup state. Owner permissions and booking availability remain backend-owned.

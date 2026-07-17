# Multi-business and favorites analysis

## Findings

- Existing business endpoints use `/me` and implicitly assume one owner profile in several read/write paths.
- Offerings, availability, bookings, public slugs, and galleries must become business-scoped before a user can safely own more than one business.
- Favorites should reference a public business identity, not copy mutable business data into the user record.

## Contract proposal

- Introduce explicit business owner membership/role and active business selection in backend-prepared account context.
- Scope all owner endpoints by `businessId`; reject cross-business ids even when the user owns another business.
- Add idempotent favorite/unfavorite commands and a public-safe favorites read model.
- Favorite booking handoff returns only backend-valid offerings and availability; booking creation revalidates all constraints.

## Risks and mitigations

- Migrating `/me` contracts can cause silent writes to the wrong business: maintain compatibility adapters and require an explicit active business after migration.
- Favorite visibility can leak private business existence: only public/visible businesses may be returned.
- Booking shortcuts can bypass consent or availability: use the existing booking service as the sole transition authority.

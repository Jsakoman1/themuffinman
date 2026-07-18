# Continuity and performance baseline

## Evidence collected

- `make audit-frontend` passed before this slice: 43 routes and 42 concrete surfaces are connected. The audit reports no detached route surface. It does report the previously introduced preview primitives as not yet adopted by a route, so this slice does not make them an alternate detail implementation.
- `make audit-docs` passed. The route/query policy in `linear-inspired-interaction-contract-audit.md` remains the governing contract.
- `WorkDiscoveryView` already aborts replaced requests and uses a 250 ms query debounce. It calls the backend-owned `/quests/search` or preset endpoint with `q`, `sort`, `page`, `size`, and `scheduledOnly`; no client ranking or visibility decision exists.
- The collection had no URL restoration for its supported backend filters, no keyed local display state, and no explicit scroll restoration. Vue Router had no scroll behavior configured.

## Decision

The first supported continuity slice is limited to Work discovery:

- Canonical URL query state is limited to the existing backend inputs `q`, `sort`, and `scheduled`. It intentionally excludes display density, selected item, scroll position, drafts, permissions, and object data.
- Local display density, selected row id, and scroll position are stored only in `sessionStorage`, keyed by authenticated viewer id plus route path and the normalized backend query. A changed viewer or backend query therefore cannot reuse another context's state.
- A selected id is discarded after a successful response when that object is no longer in the backend result. No cached result is reused as authority after a mutation or visibility change.
- The implementation preserves the existing abort/debounce behavior. It adds no prefetch, optimistic mutation, virtualization, or infinite scroll because this baseline contains no measured need or stable contract for them.
- Router saved-position restoration is used for browser back/forward. The collection additionally restores its own local position after its current backend response renders, preventing a blank or stale collection from being treated as a completed return.

## Runtime evidence boundary

The existing Web smoke script is the browser evidence mechanism. Its trace must cover cold loading, filtered collection-to-detail return, unavailable selected object discard, failed refresh/retry, and a permission/visibility changed response before this plan can be verified.

# Durable Application-News Outbox

This slice implements durability only for Workmarket application-news delivery.

The application mutation and outbox row are written in the same transaction. The
outbox stores a stable UUID plus event type, application ID, and actor ID; it does
not store detached JPA entities or unrestricted event payloads. A scheduled
dispatcher reloads the application through the existing handler, claims work with a
persisted 60-second lease, reclaims expired processing rows, marks successful delivery
as `DELIVERED`, and records bounded retry metadata on failure. Delivered rows are
removed by the typed notification-retention schedule. The existing news delivery key
prevents duplicate news rows if a delivery is replayed, while the admin-only replay
endpoint records an operator reference before making a row immediately retryable.

This is not yet a universal outbox, process-crash runtime proof, alerting integration,
operator runbook, cross-process production proof, or production-scale guarantee. Those
remain explicit follow-up gaps. The lease and retention behavior are covered by unit
tests; browser runtime evidence covers the protected replay path and delivered readback.

Runtime evidence: `docs/runtime-evidence/browser-runtime-workmarket-news-outbox-2026-07-22.json`.
Operational evidence: `docs/runtime-evidence/browser-runtime-workmarket-news-outbox-operations-2026-07-22.json`.

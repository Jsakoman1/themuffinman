# Browser Runtime Evidence Pass

Reviewed: browser-runtime-evidence-pass baseline.

## Scope

This pass collects fresh local browser evidence for high-risk System Map scenarios. It is distinct from backend unit tests and does not promote a scenario without an actual browser trace.

## Execution order

1. Inspect and start the workspace-owned local stack with `make dev-doctor` and `make dev`.
2. Capture mutation reliability and read-back traces.
3. Capture visibility/consent and WebSocket reconnect traces.
4. Capture representative read-model latency/query evidence where instrumentation is available.
5. Stop the owned stack and close out only artifacts that were actually captured.

## Unsupported in this pass

Database failure injection, provider timeout injection, process-crash replay, native/device execution, offline/background behavior, and production-scale measurements remain pending unless their required environment and evidence are explicitly available.

## Fresh artifact

The local stack baseline trace is recorded at `docs/runtime-evidence/browser-runtime-baseline-2026-07-22.json`. It proves login and route/shell rendering only; it does not close the higher-risk mutation, visibility, realtime, or performance scenarios.

A fresh Chat reconnect trace is recorded at `docs/runtime-evidence/browser-runtime-chat-reconnect-2026-07-22.json`. It proves browser-observed disconnect and reconnect state transitions, but not message delivery during loss or multi-instance recovery.

Fresh profile visibility evidence is recorded at `docs/runtime-evidence/browser-runtime-profile-visibility-2026-07-22.json`. It proves a browser mutation persisted through reload and that the original value was restored. It does not prove cross-viewer denial, consent revocation, or Vision/native parity.

The read-model timing trace is recorded at `docs/runtime-evidence/browser-runtime-read-models-2026-07-22.json`. It records browser-observed API resource timings for `/work` and `/chat`; it does not prove SQL query count, row volume, database/provider timing separation, or production-scale performance.

The controlled visibility trace at `docs/runtime-evidence/browser-runtime-visibility-cross-viewer-2026-07-22.json` proves a Web owner can transition description visibility to `PRIVATE`, a second Web viewer receives the profile without the description, and the owner can restore `PUBLIC`. Vision parity, native/device revoke, and cache invalidation remain pending.

The message-level Chat recovery trace at `docs/runtime-evidence/browser-runtime-chat-message-reconnect-2026-07-22.json` proves an online message, a transport disconnect, a message committed while the owner was offline, reconnect, and authoritative HTTP sync containing both messages. It does not prove process-crash replay, multi-instance delivery, or durable event replay.

The Web location consent trace at `docs/runtime-evidence/browser-runtime-location-consent-revocation-2026-07-22.json` proves exact location fields are visible to a second authenticated viewer while the owner scope is `EVERYONE`, and that a fresh read after changing the owner setting to `OFF`/`NOBODY` returns no coordinates or exact address fields. Cached-view invalidation, Vision parity, WebSocket reconnect, and native/device revoke remain pending.

The ride join trace at `docs/runtime-evidence/browser-runtime-duplicate-ride-join-idempotency-2026-07-22.json` proves two authenticated join requests with the same idempotency key return one joined membership (`joinedSeats=1`), with authoritative read-back confirming the same state. The ride fixture was left, cancelled, and removed through normal API operations.

The stale quest update probe at `docs/runtime-evidence/browser-runtime-stale-quest-update-browser-finding-2026-07-22.json` is a genuine Playwright Chromium trace and found a concrete gap: after a first writer updated the quest, a stale writer also received HTTP 200 and overwrote the newer title. The scenario remains pending; it requires a resource-version or equivalent stale-state conflict before it can pass. The API-only reproduction is retained at `docs/runtime-evidence/browser-runtime-stale-quest-update-finding-2026-07-22.json`.

Closeout status for this pass: 41 runtime scenarios are passed and 40 remain `pending_runtime`. The pending set explicitly includes unsupported database/provider failure injection, process-crash replay, native/device execution, and unmeasured SQL/scale evidence. The work-plan child validations are being verified independently; capability completion is not inferred from this pass.

The original mutation probe found a concrete browser integration gap: the same mutation failed when `X-Request-Id` and `X-Correlation-Id` were sent from the Web origin. The CORS configuration and the profile update client path were corrected. Regression evidence is recorded at `docs/runtime-evidence/browser-runtime-mutation-trace-headers-2026-07-22.json`; the original finding remains at `docs/runtime-evidence/browser-runtime-mutation-cors-finding-2026-07-22.json`. This closes header propagation only, not idempotency replay, retry equivalence, or failure recovery.

The first duplicate booking probe reached the real booking endpoint but was blocked by seeded data; that finding remains at `docs/runtime-evidence/browser-runtime-duplicate-booking-blocker-2026-07-22.json`. A second controlled probe created an availability fixture, submitted the same booking twice with the same idempotency key, and read back exactly one authoritative booking row. The fixture was cleaned through normal API operations. Evidence is recorded at `docs/runtime-evidence/browser-runtime-duplicate-booking-idempotency-2026-07-22.json`. Timeout recovery, crash replay, and provider-failure behavior remain pending.

The first work application side-effect probe committed application row `270` but produced no corresponding news row; that pre-fix finding remains at `docs/runtime-evidence/browser-runtime-work-application-news-gap-2026-07-22.json`. The handler was hardened to reload its entity graph and use `REQUIRES_NEW` after the outer commit. A fresh browser/database trace for application `274` then observed exactly one persisted `APPLICATION_CREATED` row (`docs/runtime-evidence/browser-runtime-work-application-news-after-commit-2026-07-22.json`). This closes one work-application after-commit path only; failure injection, durable replay, and duplicate consumer delivery remain pending.

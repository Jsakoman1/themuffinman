# Implementation Backlog

## Current open plan

REPAIR-AND-STABILIZE [ACTIVE-PLANNED]: Execute `docs/work/repair-and-stabilize-master.yaml` before unrelated new features; baseline snapshot is `docs/repair-and-stabilize-baseline-2026-07-22.yaml`.
REPAIR-AND-STABILIZE-CLOSEOUT [2026-07-22]: All six repair child plans are verified. Feature intake remains maintenance-only while durable crash replay, provider/database failure injection, Vision/cache/reconnect consent parity, native/device/offline behavior, and production-scale/query instrumentation remain open. Closeout: `docs/repair-and-stabilize-closeout-2026-07-22.yaml`.
- MAINT-DURABLE-NEWS-OUTBOX [VERIFIED-SLICE 2026-07-22]: Workmarket application-news mutations now enqueue durable delivery intent with bounded retry and consumer dedupe; browser/runtime evidence proves one delivered owner news item within 300 ms. Process-crash runtime proof, cross-process lease, retention, and broader consumers remain open. Plan: `docs/work/maintenance-durable-application-news-outbox.yaml`.
- MAINT-OUTBOX-OPERATIONS [VERIFIED-SLICE 2026-07-22]: Workmarket news outbox now has persisted processing leases, expiry-based reclaim, typed notification retention cleanup, failure codes, replay references, and an admin-only replay endpoint. Browser evidence proves the admin replay path and delivered readback. Failure alerting, operator runbook, crash proof, and broader consumers remain open. Plan: `docs/work/maintenance-outbox-operations.yaml`.
- UI-ACTION-NOTIFICATION-TARGET-001 [RESOLVED 2026-07-22]: Local authenticated runtime showed stale `/news/me` quest/application references whose canonical detail routes returned 404. Backend now resolves target availability and downgrades unavailable references to `QUEST_LIST`; frontend follows backend `destinationType` and routes application notifications to application detail. Regression and runtime evidence: `docs/runtime-evidence/browser-runtime-ui-action-integrity-2026-07-22.json`.
- RUNTIME-AUTH-ROUTE-CONTEXT-001 [RESOLVED 2026-07-22]: Authenticated Playwright route pass found incorrect Settings shell context for `/notifications` and `/activity`; route metadata and shell ownership now use dedicated surfaces. Fresh registration, route, and API-error coverage passed. Evidence: `docs/runtime-evidence/browser-runtime-authenticated-route-matrix-2026-07-22.json`.
- RUNTIME-ACTIVITY-NOTIFICATION-TARGET-001 [RESOLVED 2026-07-22]: Activity previously derived dead quest routes from notification type. It now follows backend destination metadata and suppresses unavailable canonical targets to `/notifications`; authenticated Activity recovery and dismissal remain proven. Evidence: `docs/runtime-evidence/browser-runtime-activity-recovery-2026-07-22.json`.
- RUNTIME-READ-BUSINESS-001 [RESOLVED 2026-07-22]: Public Business detail and booking-form runtime load succeeded for the representative fixture; opt-in dev observability now records server-side query count and request duration. Evidence: `docs/runtime-evidence/browser-runtime-read-business-booking-2026-07-22.json`.
- RUNTIME-READ-CHAT-001 [RESOLVED 2026-07-22]: Authenticated Chat pagination, older-page readback, sync, refresh-hint, and prior reconnect evidence now include dev-only server query-count/request-duration measurements. Evidence: `docs/runtime-evidence/browser-runtime-read-chat-sync-2026-07-22.json`.
- RUNTIME-READ-WORK-001 [RESOLVED 2026-07-22]: Work preset search ignored query/page/size because `QuestSearchRequestDTO` lacked bindable setters. Setters and a regression test were added; authenticated search, pagination, empty state, query count, and duration now pass. Evidence: `docs/runtime-evidence/browser-runtime-read-work-discovery-2026-07-22.json`.
- RUNTIME-SEARCH-ORCHESTRATION-001 [RESOLVED 2026-07-22]: Fresh Work-to-Vision command-center handoff is now combined with the existing authenticated search-v1 query/version/filter/compare trace. Vision-specific pagination/empty coverage remains separately pending. Evidence: `docs/runtime-evidence/browser-runtime-search-orchestration-2026-07-22.json`.
- RUNTIME-CHAT-GROUP-001 [RESOLVED 2026-07-22]: Existing authenticated multi-user traces now cover both circle-access denial and eligible group creation branches for Chat group eligibility. Evidence: `docs/runtime-evidence/browser-runtime-chat-group-2026-07-22.json`.
- RUNTIME-PENDING-TRIAGE-001 [OPEN 2026-07-22]: Current pending runtime matrix pass is classified in `browser-runtime-pending-scenario-triage-2026-07-22.yaml` and batch evidence `browser-runtime-pending-batch-03-2026-07-22.yaml`; the audit is now 54 passed / 27 pending. Native/device, deterministic failure-injection, multi-user/storage, Vision parity, and row-specific server-observability evidence boundaries remain explicit and must be closed before the corresponding rows can pass.
- RUNTIME-VISION-RECOVERY-001 [RESOLVED 2026-07-22]: Vision discovery responses carried `recoveryAction`, but the Web renderer exposed no no-match recovery control. The renderer now clears/focuses the prompt and a valid retry was proven in the authenticated browser pass. Evidence: `docs/runtime-evidence/browser-runtime-vision-read-discovery-2026-07-22.json`.
- RUNTIME-VISION-RETURN-001 [RESOLVED 2026-07-22]: Vision quest discovery detail links previously dropped return context. They now carry `/vision` return context and the Work detail surface exposes `Back to Vision`; authenticated open/return was proven. Evidence: `docs/runtime-evidence/browser-runtime-vision-detail-return-2026-07-22.json`.
- RUNTIME-VISION-CREATE-002 [RESOLVED 2026-07-22]: The execution-enabled authenticated runtime trace now proves Vision review, confirmation, quest creation, authoritative OPEN readback, and owner cleanup. Provider-failure/retry branches remain pending under their dedicated scenarios. Evidence: `docs/runtime-evidence/browser-runtime-vision-create-quest-execution-2026-07-22.json`.
- RUNTIME-LOCATION-VISIBILITY-002 [RESOLVED 2026-07-22]: Fresh local owner/viewer runtime trace exercised public, circle-member, and private exact-location boundaries; private readback redacted street and coordinates to approximate area. Evidence: `docs/runtime-evidence/browser-runtime-location-visibility-boundaries-2026-07-22.json`.
- RUNTIME-READ-SOCIAL-002 [RESOLVED 2026-07-22]: Fresh authenticated profile reads covered public, circle-member, and private description visibility with server query count, duration, row volume, relation, and redaction evidence. Evidence: `docs/runtime-evidence/browser-runtime-read-social-visibility-2026-07-22.json`.
- RUNTIME-VISION-SEARCH-002 [RESOLVED 2026-07-22]: Combined authenticated Vision browser evidence with a fresh search-v1 server trace covering result, empty recovery, pagination, filter schema, comparison, and detail return. Evidence: `docs/runtime-evidence/browser-runtime-vision-search-contract-2026-07-22.json`.

WORKSPACE-APP-SHELL-FIRST-LAYER [PLANNED]: Implement the first app-like workspace transition from
`docs/work/workspace-app-shell-first-layer.yaml`. Keep canonical routes and domain surfaces stable while adding a
backend-prepared module navigation tree with relevant nested destinations, counts, unread/attention markers,
responsive drawer behavior, and explicit backend/frontend ownership boundaries. Do not make Vision a persistent
sidebar dashboard.

## Deferred capability gaps

These gaps remain visible in `docs/capability-inventory.yaml`, but they do not have an open implementation plan until a
future slice is explicitly selected:

- Account recovery: delivery provider, distributed abuse controls, valid-token runtime proof, and later native clients.
- Profile visibility: circle-scoped consent, recipient selection, Vision handoff, and later native presentation.
- Search comparison: richer family-specific fields and broader acceptance beyond the verified bounded slice.
- Native clients: iPhone and Apple Watch clients, device permissions, offline/background behavior, and native runtime
  acceptance.
- Rides: future residual acceptance work only where the inventory still records a gap.

## Planning rule

The current System Map closeout queue is `docs/work/system-map-truth-runtime-closeout-master.yaml`; it consolidates runtime, enforcement, and external-evidence gaps without changing capability status.

## Architecture analysis follow-ups

These stable IDs were opened by the third system-map analysis pass. They remain
deferred analysis/implementation candidates and do not imply an active delivery
plan until explicitly selected:

- ARCH-001: Endpoint-to-client-to-evidence canonical registry and endpoint-level reconciliation queue are implemented and master-verified. All 67 unclassified non-Web endpoints now have an explicit owner, proposed class, reason, and review state; 62 remain review-required and no runtime completion is claimed.
- ARCH-002: Transport context, operation policy primitives, booking/ride reference adoption, and evidence matrix are implemented and master-verified; remaining Work/Things/Social/Chat/Vision adoption, durable replay storage, side-effect propagation, and runtime failure-injection evidence remain open.
- ARCH-002-CORS: Resolved the browser mutation trace-header CORS gap by allowing and exposing request/correlation/idempotency/operation headers in the shared security CORS configuration and adopting mutation context headers for the profile update path; retain the original finding artifact as regression evidence.
- ARCH-003: Shared after-commit event boundary and side-effect delivery contract are implemented and master-verified. Workmarket application news now carries an event identity and unique delivery key, preventing duplicate consumer inserts; business consumer breadth, durable outbox/replay, direct publisher migration, scheduled run ledger, and runtime crash/replay evidence remain open.
- ARCH-004: Read-model reliability and runtime evidence contract are implemented and master-verified. A fresh Work browser measurement now covers request latency, response bytes, result size, and viewer role; query count, SQL timing, database row volume, and broader module measurements remain pending.
- ARCH-005: Visibility, consent, and cross-client parity contract are implemented and master-verified. Web exact-location grant/revoke and fresh-read redaction are proven; cached-view, Vision parity, reconnect, and native/device/offline evidence remain open.

## System-map drift-control follow-ups

These IDs were opened by the complete-system analysis program. They are deferred
implementation slices; their design is in `docs/system-drift-control-registry.yaml`.

## Completed system-map completeness mapping slices

These IDs were opened by the completeness baseline review and completed by the
verified extension master. They describe evidence/mapping work, not product
capability completion.

- MAP-001 through MAP-009: completed in `docs/work/system-map-extension-master.yaml`; residual runtime, enforcement, and external-evidence gaps remain explicit in `docs/system-map-coverage-registry.yaml` and the ARCH backlog.


The UI action-integrity closeout uses the canonical action matrix and runtime scenario catalog; it does not create screenshot or smoke-test artifacts.

Capability status and implementation-plan status are separate. Update the capability record in the canonical inventory
when product support changes; add a new `docs/work/*.yaml` plan only when a specific capability slice is opened. When a
plan is verified, run closeout cleanup and remove temporary validation outputs, screenshots, smoke traces, and
unreferenced plan artifacts.
- RUNTIME-STALE-QUEST-UPDATE [RESOLVED 2026-07-22]: Added the quest resource-version contract and JPA version guard. Playwright Chromium evidence proves HTTP 409 `STALE_RESOURCE` and unchanged authoritative state after a stale writer attempt: `docs/runtime-evidence/browser-runtime-stale-quest-update-repaired-2026-07-22.json`.
- RUNTIME-READ-VISION-002 [OPEN 2026-07-22]: Authenticated Vision context and conversation readback now have aggregate query/duration evidence, but separate provider-versus-context-assembly timing and provider outcome correlation remain unexposed. Evidence: `docs/runtime-evidence/browser-runtime-read-vision-context-2026-07-22.json`.
- RUNTIME-WORK-WORKERS-002 [RESOLVED 2026-07-22]: Fresh multi-user worker trace proves approve, replacement, stale retry conflict (`WORKER_ASSIGNMENT_STALE`), release, and disposable fixture cleanup. Evidence: `docs/runtime-evidence/browser-runtime-worker-replacement-stale-2026-07-22.json`.
- RUNTIME-CHAT-ATTACHMENTS-002 [RESOLVED 2026-07-22]: Fresh authenticated Chat trace proves local upload, preview/access, object read, unavailable response, cancel/repeat-cancel, message send, sync, and readback attachment propagation. Evidence: `docs/runtime-evidence/browser-runtime-chat-attachment-lifecycle-2026-07-22.json`.
- RUNTIME-CHAT-VIEW-ATTACHMENT-002 [RESOLVED 2026-07-22]: Fresh authorized Chat view trace proves object open, access refresh, expired-upload redaction, unavailable key, and restored fixture state. Evidence: `docs/runtime-evidence/browser-runtime-chat-view-attachment-2026-07-22.json`.
- RUNTIME-NOTIFICATION-RECEIVE-002 [RESOLVED 2026-07-22]: Fresh authenticated event trace proves notification delivery, unread/read transition, attention-center visibility, reconnect, retry, and disposable event fixture cleanup. Evidence: `docs/runtime-evidence/browser-runtime-notification-receive-2026-07-22.json`.
- RUNTIME-CHAT-LEAVE-002 [RESOLVED 2026-07-22]: Fresh three-user group trace proves owner leave, replacement-owner transfer, stale repeat rejection, refreshed membership, and minimum-membership recovery. Evidence: `docs/runtime-evidence/browser-runtime-chat-leave-owner-transfer-2026-07-22.json`.
- RUNTIME-VISION-CONFIRM-002 [RESOLVED 2026-07-22]: Combined authenticated Vision traces prove review, confirm, cancel, and retry state transitions without claiming provider-failure recovery. Evidence: `docs/runtime-evidence/browser-runtime-vision-confirm-cancel-retry-2026-07-22.json`.
- RUNTIME-CAPABILITY-CLOSEOUT-002 [RESOLVED 2026-07-22]: Runtime capability closeout now reconciles 65 passed evidence-backed rows, 16 explicitly classified pending boundaries, and zero unclassified pending rows. Evidence: `docs/runtime-evidence/runtime-capability-closeout-2026-07-22.yaml`.

# Round 27: Events, Jobs, Realtime, and Replay

Status: source-trace analysis. Reviewed: 2026-07-22.

## Conclusion

The modular monolith uses several local side-effect mechanisms rather than a
unified durable event-delivery platform: Spring application events, direct service
coordination, in-process chat WebSockets, and scheduled cleanup/compaction jobs.
HTTP/database state remains the recovery authority for chat; WebSocket delivery is
best-effort within the running process and clients resynchronize through read APIs.

## Delivery classes

| Class | Current mechanism | Durability/recovery boundary |
|---|---|---|
| Workmarket news | Spring domain event and listener | listener semantics need explicit after-commit/replay classification |
| Booking events | booking events and listener | business audit records support domain history; no universal outbox established |
| Password recovery | event publication | provider delivery outcome requires its own evidence |
| Chat realtime | process-local session and typing maps, WebSocket handler | reconnect/sync API is recovery path; process-local state is not cluster proof |
| Cleanup/compaction | scheduled retention/token/vision jobs | configuration-driven execution; operator replay ledger is not established |

## Risks retained

- No repository-wide outbox or durable replay contract was found in this source
  pass.
- Synchronous application listeners may couple mutation latency and side-effect
  failure behavior unless their transaction phase is explicit.
- Process-local WebSocket session and typing state does not establish multi-instance
  delivery, ordering, or replay semantics.
- Scheduled work needs observable start/result/failure/retry evidence before an
  operational reliability claim.

## Boundary

This maps source mechanisms only. It does not prove event timing, provider delivery,
cluster behavior, or job execution in a deployed environment.

# System Map Deepening — Round 10: Runtime Operations and Observability

Date: 2026-07-22  
Status: **observed analysis**  
Scope: local runtime, configuration, scheduled jobs, storage/providers, WebSocket, health evidence, runtime acceptance, and operational gaps

## Executive finding

The repository has a credible local-development operating model and several
explicit operational controls: typed configuration properties, Flyway-managed
schema, scheduled retention jobs, WebSocket chat, local/object-storage modes,
provider fallbacks, and browser runtime evidence. It does not yet contain enough
deployment-topology, metrics, alerting, backup, or recovery evidence to claim
production operations readiness.

The runtime architecture is therefore **local-runtime mature, production-
operations partially evidenced**.

## Runtime topology observed

```text
Browser Web client
      |
      | HTTP + WebSocket
      v
Spring Boot application
  |       |        |         |
Postgres Flyway  object     external providers
                 storage    (location, OpenAI/voice)
```

The local stack is started through the repository Makefiles. PostgreSQL is
configured as the application database. Object storage can use local disk or an
S3-compatible provider; local development can start a MinIO-compatible compose
stack when configured for S3. The evidence confirms local browser execution and
object-storage flows, but does not establish a production network, process,
container, load-balancer, or database topology.

## Operational configuration

Operational settings are centralized in typed classes under `config/`, including
account recovery, agent, business, chat, location, native handoff, object
storage, retention, security, Vision, and voice properties. Environment mapping
is kept in `application.properties`, consistent with the repository operating
rules.

Important controls include:

- Vision conversation TTL: 14 days by default;
- notification retention: 7 days with a scheduled cleanup;
- chat image retention: 30 days;
- chat message retention: 180 days;
- revoked auth-token cleanup schedule;
- chat presence and typing timeouts;
- per-minute limits for chat open/send/read/delivery/reactions;
- attachment byte-size and MIME-type allowlists;
- object-storage presigned URL TTL and local proxy path;
- configurable location, agent, voice, and synthesis providers.

These are real runtime controls, not only documentation claims. They remain
environment-dependent and need deployment-level configuration evidence before
being treated as operational guarantees.

## Scheduled and lifecycle operations

Scheduling is enabled in the Spring Boot application. Observed scheduled jobs
include:

| Operation | Source | Purpose |
|---|---|---|
| Vision memory compaction | `VisionMemoryCompactionService` | compact persisted Vision learning/memory data |
| Chat retention cleanup | `ChatRetentionService` | expire chat messages/images according to retention settings |
| Auth-token cleanup | `AuthSessionService` | remove expired/revoked authentication material |
| Notification cleanup | retention configuration and notification services | retain inbox data only for configured period |

The code shows scheduling and retention policy, but there is no complete evidence
registry for job duration, last-run status, failure alerting, retry policy,
distributed-lock behavior, or operator replay. In a multi-instance deployment,
those questions are material.

## Provider and failure boundaries

The application explicitly models unavailable providers and retryable outcomes.
Vision preserves conversations and communicates provider unavailability rather
than silently inventing a local result. Location and voice providers are
configurable. Storage supports local and S3-style modes with metadata and
presigned access.

The current failure model is application-level and user-facing:

- typed failure/blocking reasons;
- retryable flags on several DTOs and execution results;
- provider-unavailable messaging;
- local fallback/route behavior in selected development-only Vision proofs;
- authorization failures for protected storage and chat operations.

What is not evidenced is infrastructure-level resilience: circuit breakers,
queue-based retry, dead-letter handling, provider latency metrics, connection
pool exhaustion behavior, or operator escalation.

## Observability assessment

Logging configuration exists and Hibernate SQL logging is controlled, but the
repository scan did not find a complete metrics/trace/alerting surface such as a
documented Prometheus registry, actuator dashboard, distributed tracing contract,
or alert thresholds. Runtime evidence is primarily browser traces and JSON
scenario artifacts.

This is sufficient for feature acceptance and regression evidence. It is not
sufficient for answering operational questions such as:

- Which endpoint or job is degrading?
- How often do provider calls fail or time out?
- How many scheduled cleanups succeeded?
- Are WebSocket reconnects increasing?
- Is storage capacity or database connection capacity near a limit?
- Can an operator correlate one user workflow across HTTP, WebSocket, provider,
  and background-job logs?

## Runtime evidence strengths

The repository contains substantial browser evidence for chat reconnect and
WebSocket connection, notification delivery, attachments, location permission
and visibility boundaries, Vision flows, business gallery storage, search,
authentication, things lifecycle, worker assignment, and app-shell behavior.
The runtime acceptance matrix separates `passed` from `pending_runtime`, which
is the correct evidence boundary.

The acceptance audit currently reports 30 passed and 24 pending scenarios. The
pending set includes important Vision execution/recovery, chat edge cases,
notifications, cross-client/device handoff, authentication recovery, and final
closeout scenarios. These are explicit gaps, not failures of the source model.

## Production-readiness gaps

1. **Deployment topology.** No canonical production diagram, process model,
   ingress/TLS boundary, scaling model, or environment promotion evidence.
2. **Database operations.** Flyway migrations are present, but backup/restore,
   migration rollback, lock contention, replication, and connection-pool
   operating limits are not evidenced.
3. **Job operations.** Scheduled cleanup exists, but job observability,
   idempotency under multiple instances, failure notification, and replay are not
   centrally documented.
4. **Provider operations.** Provider selection and unavailable states exist, but
   timeout budgets, quotas, cost controls, retry/backoff, and incident behavior
   are not mapped.
5. **Metrics and tracing.** No complete endpoint, provider, WebSocket, storage,
   or job metric taxonomy is present in the inspected operational surface.
6. **Secrets and configuration delivery.** Environment variable mapping exists,
   but deployment secret rotation and configuration validation evidence is absent.
7. **Recovery evidence.** Browser retry behavior is present in many flows; system
   recovery after process, database, provider, or storage failure is not proven.

## Operational dependency graph

```text
scheduled jobs ──> Postgres records / retention deletes
HTTP services ───> Postgres + optional object storage + providers
WebSocket chat ──> authenticated session + chat state + reconnect client
Vision ──────────> persisted conversation + semantic/voice providers + product services
browser evidence ─> local runtime only unless deployment context is recorded
```

The most important operational coupling is that Vision, chat attachments, and
location/voice providers cross both persistence and external-service boundaries.
They need end-to-end timeout and failure evidence in future production work.

## Recommendations

1. Define a runtime topology document with local, staging, and production views.
2. Add an operational metric taxonomy for HTTP, database, jobs, WebSocket,
   providers, storage, and authorization failures.
3. Add job execution evidence: start/end, outcome, affected rows, failure reason,
   and safe replay semantics.
4. Add provider contract tests for timeout, unavailable, quota, and malformed
   response cases.
5. Add backup/restore and migration rehearsal evidence before production claims.
6. Keep pending runtime scenarios visible and prioritize Vision execution,
   provider failure, cross-client handoff, and notification recovery.

## Source evidence

- `Makefile`
- `apps/themuffinman/Makefile`
- `apps/themuffinman/scripts/dev-stack.sh`
- `apps/themuffinman/docker-compose.object-storage.dev.yaml`
- `apps/themuffinman/src/main/resources/application.properties`
- `apps/themuffinman/src/main/java/com/themuffinman/app/config/**`
- `apps/themuffinman/src/main/java/com/themuffinman/app/**/service/*Retention*`
- `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionMemoryCompactionService.java`
- `docs/runtime-acceptance-matrix.yaml`
- `docs/runtime-evidence/*`
- `scripts/audits/audit-runtime-acceptance.rb`

## Conclusion

Round 10 confirms a well-defined local operational surface with explicit
retention, limits, provider, storage, and retry controls. The system map must
keep production topology and observability marked as evidence gaps until actual
deployment and operational artifacts exist.

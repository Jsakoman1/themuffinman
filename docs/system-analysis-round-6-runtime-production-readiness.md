# System Analysis Round 6 — Runtime, Security, and Production Readiness

Status: completed analysis snapshot, 2026-07-22. This round assesses runtime topology, environment/configuration boundaries, security, storage, retention, rate limits, external providers, and acceptance evidence. It does not claim a full production deployment audit.

## 1. Executive finding

The local runtime model is explicit and safety-conscious, but the repository is still a development-oriented single-application runtime rather than a fully evidenced production topology.

```text
PostgreSQL
   ↑
Spring Boot backend :8080
   ├── REST API
   ├── Chat WebSocket /ws/chat
   ├── scheduled cleanup/retention
   ├── local/S3 object storage
   ├── optional Geoapify location provider
   └── optional OpenAI semantic/voice providers
        ↑
Vue/Vite frontend :5173
```

At analysis time `make dev-doctor` reported:

- development profile selected;
- local object storage enabled with provider `local`;
- Docker and Docker Compose unavailable;
- ports 8080 and 5173 free;
- no MinIO requirement because local-disk storage is selected.

This is a healthy local preflight result, not proof that the application was run end-to-end in this round.

## 2. Runtime topology

The root Makefile delegates to `apps/themuffinman`. `make dev` owns a workspace-recorded backend/frontend process tree and refuses to reuse occupied ports. `make dev-stop` is the required cleanup path. `make dev-doctor` checks ports, Docker, and object-storage mode before launch.

The application expects:

- Java 21;
- PostgreSQL on localhost:5432;
- database `side_quest_db` with user `jsakoman` in local defaults;
- backend on 8080;
- Vite frontend on 5173;
- optional object storage on local disk or S3-compatible service;
- optional external providers for OpenAI and Geoapify.

Flyway is enabled, Hibernate uses `ddl-auto=validate`, and `open-in-view=false`. This makes schema migration and transaction boundaries explicit at startup/runtime.

The local runtime has two storage modes:

1. `local`: no container required; object bytes are stored under a configured local base path and proxied through the backend boundary.
2. `s3`: local development can start MinIO through Docker Compose; production expects configured S3-compatible credentials, bucket, endpoint/region, and public/presigned URL settings.

## 3. Configuration architecture

Operational properties are grouped in typed `@ConfigurationProperties` classes rather than scattered values. Major groups include:

- Security/JWT/CORS;
- account recovery;
- bootstrap and seed users;
- location provider;
- agent/OpenAI;
- voice;
- Vision;
- Business booking/lists/dashboard;
- retention;
- Chat presence/messages/attachments/typing/moderation/support;
- object storage;
- native handoff.

This is a strong operational pattern because defaults and environment mappings are discoverable and testable. `application.properties` is production-oriented; `application-dev.properties` intentionally turns on local seed users, local credentials, Vision execution, and the deterministic local emergency agent path.

The most important configuration boundary is:

```text
production application.properties
  → required secrets / external providers / no local fixture defaults

development profile
  → deterministic local accounts / fallback JWT / local emergency parser / enabled local execution
```

Development configuration is safe only if profile separation is reliable. Production must never inherit `application-dev.properties` or leave bootstrap/seed/local-emergency settings enabled.

## 4. Security model

The backend is stateless JWT-based Spring Security:

- registration, login, password recovery, and password reset are public endpoints;
- authenticated mutations and reads require the JWT filter;
- admin routes are role protected;
- passwords use BCrypt;
- CORS allowed origins are typed/configured;
- CSRF is disabled because the API is stateless and token-based;
- WebSocket HTTP registration is permissive at the route matcher but a dedicated `ChatWebSocketAuthInterceptor` authenticates the chat handshake.

Logout is stronger than a purely client-side token discard: `AuthSessionService` stores a hash of the presented token until expiry, `JwtAuthFilter` checks revocation, and scheduled cleanup removes expired revocations. This preserves a stateless signed-token architecture while making current-token logout effective.

Important production requirements remain external/configuration concerns:

- long random JWT secret;
- exact production CORS origins;
- secure admin bootstrap credentials used only once;
- password-recovery delivery provider;
- distributed abuse/rate-limit coordination if multiple instances run;
- secret storage and rotation;
- TLS/secure WebSocket termination;
- database backups and migration rollback/recovery policy.

## 5. Rate limits and abuse controls

The system has typed Chat rate and size controls for:

- online/presence heartbeats;
- conversation opening;
- message send/delivery/read;
- typing updates;
- reactions and moderation removal;
- attachment/image size and MIME types;
- page/conversation limits.

Account recovery has a configured request window (`5` per `900` seconds by default). Vision has prompt/turn limits. Agent synthetic quest batch size is capped by typed config.

The key limitation is deployment scope: these controls appear process/configuration-local. The inventory explicitly retains “distributed abuse coordination” as an account-recovery gap. Production horizontal scaling should define shared rate-limit state, abuse telemetry, and ban/escalation behavior rather than assuming local counters are sufficient.

## 6. Retention and data lifecycle

Scheduled cleanup is enabled through `@EnableScheduling` and typed retention properties:

- notifications: 7 days by default, cleanup at 03:30;
- Chat images: 30 days, with expired-image placeholder;
- Chat messages: 180 days, cleanup at 03:45;
- revoked auth tokens: cleanup at 03:15;
- Vision conversations: 14-day TTL and memory compaction at 04:20 by default.

This is a good start because retention is centralized and explicit. Production readiness still requires verifying:

- cleanup execution and failure visibility;
- idempotency and batching for large tables;
- timezone/cron semantics;
- deletion/redaction of related storage objects;
- Vision memory/preferences/feedback deletion and provider payload minimization;
- legal/privacy policy alignment;
- backup retention versus application retention.

Retention properties express intended behavior; they do not by themselves prove that all related records and external blobs are removed.

## 7. External providers and graceful degradation

### OpenAI agent and voice

The agent provider defaults to `mock` and the API key is optional in base configuration. Voice uses OpenAI transcription/synthesis defaults but exposes enabled/configured states and hard payload/recording limits. Vision can operate through deterministic local emergency behavior in development when no provider key exists.

Production risks are provider availability, latency, cost, model drift, payload privacy, retries, timeout behavior, and observability. The runtime contract intentionally keeps semantic provider failure visible and recoverable rather than inventing client-side meaning.

### Location provider

Location provider defaults to `none`; Geoapify is configured explicitly. The backend distinguishes provider unavailable, no result, and resolved states. This is safer than assuming a provider exists, but production needs API-key lifecycle, quota/rate handling, provider outage monitoring, and privacy/retention guarantees for location data.

### Object storage

Chat and gallery storage support local and S3-compatible modes. Attachment access uses backend authorization and expiring/presigned URLs; clients do not construct storage keys. Production needs bucket policy, encryption, lifecycle cleanup, upload size/content validation, orphan-object cleanup, and recovery from provider outages.

## 8. Runtime evidence status

The runtime matrix currently records 30 passed and 24 pending scenarios. Pending scope is concentrated in:

- authenticated route and account recovery proof;
- Vision confirmation, execution, discovery/detail/create/read discovery, and voice parity;
- Work worker management;
- Chat realtime, attachments, group eligibility, leave, and expired attachment recovery;
- notification receive/reconnect;
- search orchestration;
- location visibility/provider permission behavior;
- mobile/Watch contracts and cross-client consumption;
- final capability closeout aggregation.

The repository contains 38 runtime-evidence files at this snapshot, but evidence presence is not equivalent to complete coverage. Existing traces are scenario-specific and can be stale relative to current code; a future runtime slice must produce its own trace under the work-plan rules.

## 9. Production readiness strengths

1. Typed central operational configuration.
2. Explicit dev/prod profile separation.
3. Workspace-owned process lifecycle and port preflight.
4. Flyway schema ownership and Hibernate validation.
5. Stateless JWT with revocation support.
6. Backend-owned WebSocket authentication and attachment authorization.
7. Explicit retention, rate, size, and TTL settings.
8. Provider-unavailable/recoverable states in location, Vision, Chat, and storage flows.
9. Native handoff contracts define expiry, device binding, and safe boundaries.

## 10. Production readiness risks

### 10.1 No deployment topology

The repository describes local startup but does not define production deployment, ingress/TLS, process supervision, scaling, health checks, metrics, logging aggregation, backups, migration rollout, or disaster recovery. This is outside the current app code but essential for production readiness.

### 10.2 Runtime evidence gap

24 pending runtime scenarios mean static implementation confidence exceeds user-flow proof. High-risk auth, Vision mutation, Chat recovery, notification delivery, and location privacy scenarios should be prioritized.

### 10.3 External dependency operations

OpenAI, Geoapify, and object storage have configuration and graceful-degradation boundaries, but provider budgets, retry/timeout/observability, quota alarms, and incident runbooks are not fully represented in the repository.

### 10.4 Distributed state assumptions

JWT revocation, rate limits, WebSocket presence, scheduled jobs, local object storage, and cleanup coordination need explicit behavior in a multi-instance deployment. Local correctness does not guarantee cluster correctness.

### 10.5 Secrets and bootstrap

Development has explicit fallback credentials; production configuration correctly requires explicit values. The operational risk is procedural: secure secret injection, rotation, bootstrap disablement, and environment validation must be enforced outside source defaults.

## 11. Recommended readiness gates

- Add production topology documentation and health/observability contracts before claiming deployment readiness.
- Run the highest-risk pending browser traces through `make dev` and close with `make dev-stop`.
- Define provider timeout, retry, quota, cost, and failure telemetry for OpenAI, Geoapify, storage, and email recovery.
- Define multi-instance semantics for rate limits, token revocation, scheduled cleanup, WebSocket presence, and object storage.
- Add automated startup checks that reject production profiles with dev seed, local emergency, weak JWT, or bootstrap settings.
- Verify retention against database rows, Vision memory, and external object blobs.
- Maintain separate production and local fixture documentation; never route synthetic generation through normal production-like user actions.

## 12. Round 6 conclusion

The application has a credible local runtime and strong configuration/security foundations, but production readiness is not only a code/config question. The remaining gap is operational proof: deployment topology, distributed behavior, provider operations, retention verification, and pending browser/device acceptance. The system is ready for disciplined runtime hardening, not for an unqualified production-complete claim.

No production code or operational defaults were changed in this analysis round.

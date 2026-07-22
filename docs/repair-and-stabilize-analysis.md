# Repair and Stabilize Program Analysis

Reviewed: 2026-07-22

The executable baseline snapshot is [`docs/repair-and-stabilize-baseline-2026-07-22.yaml`](repair-and-stabilize-baseline-2026-07-22.yaml).

## Purpose

This analysis converts the current System Map findings into a repair-first execution program. The program is intended to make existing capabilities reliable, observable, and regression-safe before new product features are started.

The program does not treat a verified plan as proof that the product capability is complete. Runtime acceptance, capability status, and implementation-plan verification remain separate authorities.

## Current baseline

The System Map is strong enough to guide repair work. It has explicit module boundaries, backend authority rules, client boundaries, truth registries, runtime evidence, work-plan verification, and persistent backlog IDs.

The current system is not yet stable enough for unrestricted feature expansion:

- 41 runtime scenarios are passed and 40 remain `pending_runtime`.
- 62 non-Web endpoints remain in the endpoint reconciliation review queue.
- Work quest updates have a proven stale-write finding: a stale writer receives HTTP 200 and silently overwrites a newer value.
- Mutation idempotency is proven for selected booking and ride-join paths, but adoption is incomplete across modules.
- Work application after-commit news delivery has one fresh proof, but durable outbox, replay, duplicate-consumer, crash, and failure evidence remain open.
- Read-model browser timing exists, but SQL query count, row volume, database/provider timing separation, and scale evidence do not.
- Web location revoke/read-back is proven for one fresh-read path; cache invalidation, Vision parity, WebSocket revoke behavior, and native/device parity remain open.
- Native/device, offline/background, provider-failure, database-failure, and process-crash evidence is explicitly unavailable and must remain pending.

## Repair priority

### P0 — correctness and data-loss risk

1. `RUNTIME-STALE-QUEST-UPDATE`: add a resource-version or equivalent stale-state guard to quest updates.
2. Complete mutation operation-policy adoption for state-changing paths where retries or concurrent writes can duplicate or overwrite user intent.
3. Define and implement durable side-effect delivery for side effects that cannot be lost after a successful domain commit.
4. Preserve authoritative read-back semantics for uncertain transport outcomes.

### P1 — reliability and operability

1. Add query-count, query-timing, row-volume, and request-correlation evidence for priority read models.
2. Audit sibling read surfaces whenever mapper, fetch, or authorization behavior changes.
3. Complete consent and visibility parity across fresh reads, cached views, Vision, reconnect, and supported Web clients.
4. Reconcile endpoint ownership and capability links for the 62 remaining non-Web endpoints.

### P2 — release confidence and maintenance quality

1. Convert high-risk runtime scenarios into repeatable regression scenarios.
2. Reconcile implementation backlog, capability inventory, runtime matrix, and System Map after every repair slice.
3. Establish a stabilization closeout report that distinguishes fixed, verified, pending, unknown, and externally unavailable behavior.
4. Keep native/device and production-operations work explicitly outside the Web repair gate until their environments exist.

## Plan dependency analysis

The existing ARCH plans are useful inputs but cannot independently serve as the repair program:

- ARCH-002 defines mutation vocabulary and partial adoption. The repair program must turn the stale-update finding and remaining adoption gaps into implementation slices.
- ARCH-003 defines after-commit semantics and the outbox/replay gap. The repair program must decide which side effects require durability and implement the smallest safe slice.
- ARCH-004 defines evidence requirements. The repair program must add instrumentation before performance optimization claims are made.
- ARCH-005 defines parity rules. The repair program must close Web fresh-read gaps while keeping native/device claims pending.
- The Browser Runtime Evidence Pass supplies fresh proof and concrete findings, but it does not by itself fix defects.

The new master plan therefore sequences repair work as:

`baseline and triage → mutation correctness → side-effect durability → read-model observability → consent/client parity → regression and release gate`

## Entry gate

Before implementation starts:

- current System Map and runtime registry are read;
- every repair item has a stable ID, owner source, and evidence target;
- no pending or unavailable scenario is promoted by inference;
- changes are isolated into one child plan at a time;
- the local runtime and required test commands are available for the selected slice.

## Exit gate for the stabilization program

The program may be considered stable for new feature intake only when:

- all P0 items are implemented or explicitly blocked by an external dependency;
- stale updates produce a typed conflict or have an explicit, documented last-write-wins policy;
- retryable mutations have operation policy, authoritative read-back, and runtime proof;
- required side effects have a durable failure/replay strategy or an explicit non-durable classification;
- priority read models have fresh empirical evidence or are explicitly excluded from performance claims;
- supported Web visibility/consent paths have fresh-read and redaction evidence;
- the regression catalog covers each repaired defect;
- all relevant tests, audits, work-plan verifiers, and documentation checks pass;
- native/device, provider-failure, database-failure, crash-replay, and production-scale gaps remain explicitly labeled rather than silently treated as complete.

## Decision

The System Map is ready to be used as the repair system of record. New feature work should be paused except for maintenance required by this master plan. The map is detailed enough to route changes and assess impact, while the repair plan and runtime matrix provide the stricter evidence gates needed to decide whether an existing capability is actually healthy.

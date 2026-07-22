# Repair and Stabilize Master Plan Preflight

Reviewed: 2026-07-22
Master: `docs/work/repair-and-stabilize-master.yaml`

## Review result

The master plan is structurally ready for goal pursuing after the following corrections:

- baseline triage now owns capability inventory, endpoint reconciliation, and runtime closeout sources;
- P0 findings have direct stable-ID coverage in the relevant child plans;
- side-effect work is bounded by an explicit classification step before any outbox implementation;
- read-model work requires instrumentation and evidence before optimization;
- consent work is limited to supported Web/Vision surfaces and cannot promote native/device behavior;
- the final gate requires regression scenarios, registry reconciliation, and explicit pending/unknown states.

## Dependency review

The sequence is intentionally linear for safe execution and reviewability:

`baseline → mutation correctness → side-effect durability → read-model observability → Web consent parity → regression gate`

Mutation correctness is first because stale writes and retry semantics can invalidate later read and side-effect conclusions. Side-effect work follows because durable delivery needs stable operation identity and commit semantics. Read-model and consent work then consume the repaired mutation and evidence contracts. The final gate is the only point where feature intake may be reconsidered.

The sequence is conservative rather than a claim that every technical dependency is hard. A future execution pass may split read-model and consent slices when their required evidence and changed surfaces are independently isolated.

## Scope review

Each child plan has one primary outcome and one serial task. This is deliberate: a repair slice must be independently verifiable and must not silently mix a correctness fix, a performance rewrite, and a client redesign.

The plan does not authorize:

- native or device implementation;
- provider/database failure injection without the required environment;
- process-crash testing without an operator-controlled runtime;
- production-scale performance claims;
- broad feature expansion;
- a generic outbox implementation before event identity, payload, consumer idempotency, retention, and ownership are mapped.

## Evidence review

Every implementation child requires source changes, tests, living-document updates, and leaf audits. Runtime scenarios remain governed by `docs/runtime-acceptance-matrix.yaml`; a green work-plan verifier cannot promote a pending capability. The final gate must rerun the matrix and preserve evidence artifacts for both passed and pending scenarios.

## Goal-pursuing readiness decision

Ready to start with child 01 baseline triage. Do not start child 02 until the baseline task has reconciled the current backlog, capability inventory, endpoint queue, runtime matrix, and exact repair evidence owners.

The baseline snapshot is now materialized at `docs/repair-and-stabilize-baseline-2026-07-22.yaml`; it is the input artifact for child 01 verification and must be refreshed if the runtime matrix or priority queue changes before implementation begins.

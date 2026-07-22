# Runtime Pending Web and Operations Preflight

Status: deep-dive complete, ready for goal pursuing after the plan set is committed or
explicitly baselined. The plan baseline is `4bcd499`.

This program covers 11 of the 16 pending runtime scenarios. It explicitly excludes
the five native/device/offline rows and must not reduce their pending count.

## In-scope groups

- Vision runtime: execution, provider-failure correlation, and context/provider timing.
- Reliability: timeout/readback, transaction boundaries, side-effect failures,
  duplicate delivery, and process replay.
- Web–Vision parity: visibility fields, allowed actions, redaction, and recovery.

## Excluded rows

`runtime-mobile-presentation`, `runtime-watch-presentation`,
`runtime-cross-client-contract`, `runtime-visibility-native-handoff`, and
`runtime-visibility-device-revoke` remain pending until native/device evidence exists.

## Entry requirements

```text
make audit-runtime-acceptance
make audit-capability-evidence
make audit-truth-registry
make audit-docs
make audit-plan-coverage
git diff --check
```

The expected starting state is 65 passed and 16 pending, with exactly 11 rows in
scope and five rows excluded by native/device boundary.

## Deep-dive topology result

The plan set is structurally consistent:

- 11 in-scope scenario IDs map to exactly 3 evidence groups;
- 5 native/device scenario IDs are excluded explicitly;
- 4 child plans cover the 3 evidence groups plus closeout;
- the execution inventory contains 10 strictly ordered tasks;
- every inventory item maps to one child plan and one task;
- no task may start before all earlier inventory items are verifier-verified.

The scenario ownership is:

| Group | Rows | Required evidence boundary |
|---|---:|---|
| Vision runtime | 3 | browser/runtime trace, provider outcome, retry, final state, separated timing |
| Reliability runtime | 7 | browser, database/outbox, correlation, and operator process trace where required |
| Web–Vision parity | 1 | same-policy Web/Vision viewer, field, action, redaction, and recovery trace |

The first execution command remains:

```text
make work-start plan=docs/work/runtime-pending-web-operations-01-vision-runtime.yaml task=analyze-vision-rows
```

## Readiness controls

Before starting the first task, the plan files themselves must be committed or
explicitly recorded as the pre-existing baseline. The goal must then verify that the
runtime matrix still contains exactly 65 passed, 16 pending, 11 in scope, and 5
native/device exclusions. This prevents plan-authoring changes from being confused
with runtime evidence changes.

Implementation tasks must create bounded local/dev controls before runtime capture.
The controls must be opt-in, deterministic, correlation-aware, unable to run in a
production profile, and documented with cleanup/reset behavior. Runtime capture tasks
must not change a row to passed merely because a route or controller was reached.

The process-crash row is in scope but has a distinct operator evidence boundary. If
the local environment cannot safely provide controlled termination and restart, the
closeout must record a structured blocker rather than silently leaving the row
unclassified.

## Evidence rules

Browser evidence alone cannot prove provider failure, database/outbox state,
process-crash replay, or native behavior. Failure injection must be deterministic
and local-only. Every promoted row requires fresh evidence, correlation identifiers,
recovery observation, and authoritative final-state readback where applicable.

The five native/device rows are not failures of this plan. They remain an explicitly
deferred queue for a later native/device master plan.

The first task is:

```text
make work-start plan=docs/work/runtime-pending-web-operations-01-vision-runtime.yaml task=analyze-vision-rows
```

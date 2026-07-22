# Round 30: Complete System Baseline Consolidation

Status: consolidated source-trace baseline. Reviewed: 2026-07-22.

## Baseline conclusion

TheMuffinMan is a Spring Boot and Vue modular monolith with eight architectural
modules and one shared app-shell. Backend services remain the authority for
permissions, transitions, validation, visibility, and backend-prepared read models.
The system should remain a modular monolith while relationship traceability,
side-effect reliability, empirical performance evidence, and cross-client proof are
hardened. The analysis found no evidence-based reason to extract a service now.

## System truth model

The new registries form a relationship layer over existing canonical sources:

```text
product/domain/target/current-state sources
             ↓
 truth, interface, data, workflow, client, evidence registries
             ↓
 performance, side-effect, operations, delivery, drift controls
             ↓
 strict work plans and fresh runtime evidence
```

The relationship layer does not own product rules, capability status, plan status,
or runtime acceptance. It makes their ownership, evidence class, and change impact
explicit.

## Confidence and boundaries

| Area | Confidence | Boundary |
|---|---|---|
| module/code/DTO/repository/config structure | high | traced from workspace source |
| workflow and permission ownership | medium-high | declared booking owner drift remains; runtime cases vary |
| endpoint/client relationship | medium | static linker has 140 linked of 214 discovered endpoints; remaining rows need classification |
| Web/Vision parity | medium | source and contract evidence exists; capability-by-capability runtime proof remains incomplete |
| native/mobile/Watch | low | handoff contract exists; no native source or device trace |
| query/performance | low | static risk identified; no measurement traces |
| events/jobs/replay | medium | mechanisms traced; durable delivery/replay and deployed behavior unproven |
| security/operations/delivery | medium-low | configuration and safeguards traced; external deployment, backup, CI, release proof unknown |

## Primary next actions

1. Select DRIFT-001 as the next control implementation slice; it makes registry
   path/ownership/evidence metadata enforceable.
2. Execute ARCH-002 and ARCH-003 where cross-module idempotency and durable
   side-effects are required by product risk.
3. Open a dedicated performance runtime plan from the performance catalog before
   making scale, query, cache, or extraction decisions.
4. Close runtime and native/device evidence gaps through explicit strict plans,
   never by static inference.

## Consolidation rule

Future feature work starts from `docs/system-map.md`, then opens only the relevant
canonical source and relationship registries. Every nontrivial change must either
refresh affected registry rows, record a stable deferred ID, or state with evidence
why no relationship update is necessary.

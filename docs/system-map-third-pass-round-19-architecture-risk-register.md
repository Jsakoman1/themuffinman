# System Map Third Pass — Round 19: Architecture Risk and Decision Register

Date: 2026-07-22  
Status: **observed analysis**

## Executive conclusion

The three analysis programs now describe the system from static structure,
ownership/control, execution flow, and evolution perspectives. The central
architecture decision is to keep the current modular monolith as the system of
record while hardening contracts, side effects, observability, and client parity.
Premature service extraction would hide unresolved coupling rather than solve it.

## Risk register

| ID | Risk | Impact | Evidence | Priority | Next action |
|---|---|---|---|---|---|
| ARCH-001 | No complete endpoint-to-client-to-evidence registry | route/client/runtime drift and false completeness | Rounds 9, 12, 18 | High | build canonical registry |
| ARCH-002 | Idempotency and request correlation are not uniform across modules | duplicate writes and weak retry diagnostics | Rounds 14, 17 | High | define shared mutation contract |
| ARCH-003 | In-process events and direct notifications lack universal after-commit/replay semantics | inconsistent side effects or transaction coupling | Rounds 15, 17 | High | classify workflows and choose outbox/after-commit strategy |
| ARCH-004 | Realtime sessions and rate-limit caches are process-local | multi-instance delivery and abuse-control drift | Rounds 10, 15, 17 | High | define shared runtime topology |
| ARCH-005 | Query performance is statically flagged but not empirically measured | latency/memory regressions in high-centrality reads | Round 16 | High | add query/latency evidence scenarios |
| ARCH-006 | Native/mobile/Watch have contracts but no implementation/device proof | target capability claims exceed current client reality | Rounds 13, 18 | Medium | implement one device slice and trace it |
| ARCH-007 | Visibility/consent policy is distributed across identity, social, and consumers | privacy regression when one consumer changes | Round 11 | High | maintain field/resource parity matrix |
| ARCH-008 | Scheduled cleanup and compaction lack operator replay ledger | retention/compliance uncertainty after job failure | Rounds 10, 15, 17 | Medium | record job outcome and replay semantics |
| ARCH-009 | Vision orchestration has very high domain fan-in | contract drift and failure propagation | Rounds 8, 14, 17 | Medium | preserve adapters and measure turn boundaries |
| ARCH-010 | Historical/current/target plan and capability layers can be confused | incorrect status promotion | Round 12 | Medium | keep verifier and canonical inventories separate |

## Architecture decisions

### Decision D-001 — Keep the modular monolith for the next evolution stage

The current Spring Boot domain packages, services, repositories, DTOs, events,
and Flyway schema are a productive integration boundary. Keep them co-located
while ownership, consent, event, performance, and client evidence are hardened.

### Decision D-002 — Keep Vision as an orchestration/client layer

Vision may resolve, preview, review, and execute through typed adapters, but
Workmarket, Business, Things, Rides, Circles, Chat, and Identity remain owners of
their business truth. This preserves direct Web/native reuse and backend policy.

### Decision D-003 — Treat Web and Vision as equal production clients

Capability completion requires discoverable direct Web behavior and corresponding
Vision behavior where targeted, with separate runtime evidence. One client cannot
substitute for the other.

### Decision D-004 — Extract services only after evidence-backed boundaries exist

A candidate domain must have explicit aggregate/table ownership, stable API/event
contracts, no hidden repository access, clear consent/authorization, independent
operations, and migration/recovery evidence before network extraction.

### Decision D-005 — Keep target state separate from current status

The target capability catalog, current capability inventory, runtime matrix, work
plan status, and diagnostic audits remain separate sources with separate meanings.

## Priority sequence

1. ARCH-001 and ARCH-005: make endpoint/evidence and empirical read performance
   visible.
2. ARCH-002 and ARCH-003: standardize mutation retry and side-effect semantics.
3. ARCH-004 and ARCH-008: establish production runtime/job operations.
4. ARCH-007: consolidate privacy/consent evidence before expanding clients.
5. ARCH-006: implement native/device proof only after shared contracts stabilize.
6. Reassess ARCH-009 and extraction candidates after the evidence pass.

## Backlog linkage

Stable follow-up IDs are recorded in `docs/implementation-backlog.md`:

- ARCH-001 through ARCH-005 are the primary next architecture/control slices.
- ARCH-006 through ARCH-010 remain deferred risks or decision constraints.

No item is marked implemented or verified by this analysis. The next change must
create its own YAML work plan and produce implementation/runtime evidence.

ARCH-002 analysis and implementation planning now live in
`docs/arch-002-mutation-reliability-analysis.md`,
`docs/mutation-reliability-contract.yaml`, and
`docs/work/arch-002-mutation-reliability-master.yaml`.

## Source evidence

- `docs/system-map.md`
- `docs/system-analysis-round-1.md` through `docs/system-analysis-round-7.md`
- `docs/system-map-deepening-preflight.md`
- `docs/system-map-deepening-round-8-data-ownership.md` through
  `docs/system-map-deepening-round-13-target-state-architecture.md`
- `docs/system-map-third-pass-round-14-request-persistence-flows.md`
- `docs/system-map-third-pass-round-15-events-side-effects.md`
- `docs/system-map-third-pass-round-16-read-model-performance.md`
- `docs/system-map-third-pass-round-17-failure-recovery.md`
- `docs/system-map-third-pass-round-18-cross-client-parity.md`
- `docs/implementation-backlog.md`

## Conclusion

The system map is now strong enough to guide implementation sequencing. The next
work should target the highest-impact evidence/control gaps first, then use those
results to decide whether any domain is genuinely ready for service extraction.

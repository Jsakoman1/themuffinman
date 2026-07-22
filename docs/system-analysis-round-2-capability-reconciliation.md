# System Analysis Round 2 — Capability, Plan, and Evidence Reconciliation

Status: completed analysis snapshot, 2026-07-22. Sources were read from the current working tree after starting `docs/work/system-analysis-round-2.yaml`. This document records reconciliation findings; `docs/capability-inventory.yaml` remains the current capability source of truth.

## 1. Objective and method

Round 1 established that the repository has separate target, current-state, implementation, and runtime layers. Round 2 tested whether those layers agree.

For each target/current relationship, the analysis compared:

```text
target catalog
  ↔ current inventory ID and status
  ↔ backend/API evidence
  ↔ Web/Vision surface evidence
  ↔ tests and verified work plans
  ↔ runtime acceptance evidence
  ↔ declared gaps
```

The target coverage generator and inventory freshness audit were used as diagnostics. They do not modify capability status.

## 2. Reconciled totals

| Layer | Result | Meaning |
|---|---:|---|
| Current capability inventory | 152 records | current product decomposition |
| Current modules | 8 | platform, vision, work, chat, circles, business, sharing, cross-module |
| Current statuses | 143 implemented, 5 verified, 2 partial, 2 planned | status semantics are intentionally conservative about runtime proof |
| Target catalog | 123 atomic capabilities | desired production-ready scope |
| Target-to-current mapping | 123/123 exact | no unmapped or broad mapping at this snapshot |
| Target Web UI coverage | 123/123 | target report detects a Web surface for every target capability |
| Target Vision coverage | 121/123 | two planned native handoff capabilities are Vision-missing in the target coverage diagnostic |
| Target report not-ready | 3 | two mobile/watch handoffs plus one Vision-surface deficiency in the diagnostic output |
| Inventory records with declared gaps | 140/152 | gaps are primarily runtime, mobile/watch, recovery, or production-depth gaps |
| Runtime acceptance | 30 passed, 24 pending | browser/device proof is materially incomplete even where static implementation exists |

The headline is not “there are only three gaps”. The target report measures target-surface/mapping readiness, while the current inventory records broader capability gaps. The two reports answer different questions.

## 3. Module reconciliation

| Module | Records | Status mix | Interpretation |
|---|---:|---|---|
| Platform | 17 | 16 implemented, 1 partial | Account recovery is the only open status; most remaining gaps concern runtime, mobile, consent, or provider lifecycle. |
| Vision | 14 | 13 implemented, 1 verified | Static contracts and tests are extensive; execution rollout, edge cases, voice/device proof, and adapter-wide recovery remain. |
| Work | 32 | 30 implemented, 2 verified | The first product module has broad Web/backend coverage; worker assignment and lifecycle acceptance still need runtime and mobile proof. |
| Chat | 16 | all implemented | Realtime, attachments, membership, and message behavior exist, but browser reconnect, attachment, mobile/watch, and concurrent-change proof remains. |
| Circles | 14 | all implemented | Core relationship/membership behavior exists; privacy explanation and complete cross-module acceptance remain. |
| Business | 22 | all implemented | Profiles, offerings, booking, calendar, and gallery are present; most gaps are browser acceptance and production-depth behavior. |
| Sharing | 20 | all implemented | Things and rides are implemented in Web/backend slices; lending and circle-scoped ride lifecycle evidence is incomplete. |
| Cross-module | 17 | 12 implemented, 1 partial, 2 planned, 2 verified | The biggest structural gap is native/mobile/watch and production-scale cross-module infrastructure, not Web route existence. |

The inventory's module grouping is now broadly coherent, but the historical `capability-inventory-audit.yaml` records a previous grouping defect: an older snapshot placed some `business.*` and `sharing.*` records under Circles. Current inventory counts and current audit output should be treated as newer than that historical finding.

## 4. What “implemented” means in practice

The inventory semantics are internally consistent:

- `implemented` means the primary backend/Web workflow exists.
- `verified` means the capability additionally has current acceptance evidence.
- `partial` means a meaningful slice exists but an important intended workflow/client remains.
- `planned` means the intended capability has not started as a product slice.

This explains why a capability can have a backend endpoint, a Web route, tests, and a declared mobile/runtime gap while remaining `implemented`. Promoting all such records to `verified` would erase the distinction between source-level implementation and user-facing runtime proof.

The current status distribution therefore should not be read as 143 complete capabilities. It is better read as:

```text
143 primary backend/Web workflows exist
5 currently meet the stronger verified evidence threshold
4 records remain explicitly open or planned
140 records retain one or more residual gaps
```

The apparent tension between “140 declared gaps” and “only 4 open statuses” is intentional: most gaps do not prevent the primary Web workflow from being implemented.

## 5. Gap taxonomy

The inventory gaps cluster into the following categories:

### 5.1 Runtime acceptance gap

This is the largest category. Many Work, Chat, Business, Circles, Things, Rides, Profile, Notification, Search, and Vision records explicitly need browser-level acceptance. Static tests and builds do not substitute for a browser trace.

Pending runtime scenarios currently include authentication route coverage, activity recovery, Vision confirm/execution/search/detail/create/read-discovery flows, worker management, Chat realtime/attachments/group/leave/attachment-view, notification receive, search orchestration, auth recovery, location visibility, voice parity, cross-client contracts, and final capability closeout.

### 5.2 Native/mobile/Watch gap

The target catalog requests mobile or Watch surfaces for many capabilities, but the repository has contracts and handoff preparation rather than implemented native clients. This is expected scope, not accidental missing Web UI. It is concentrated in Platform, Vision, Chat, Work, Cross-module handoff, and shared shell capabilities.

### 5.3 Production-depth gap

Some capabilities have the basic workflow but still need provider integrations, distributed abuse controls, production-scale search/indexing, notification workers, retry/dead-letter observability, richer booking/business ranking, or complete attachment/storage behavior.

### 5.4 Edge-case and recovery gap

Examples include ambiguous multi-turn Vision corrections, stale/inaccessible discovery results, worker conflict/dispute flows, concurrent Chat membership changes, upload retry, expired media, location provider recovery, and explicit visibility/consent explanations.

### 5.5 Surface or UX-depth gap

Some records call out richer comparison, dedicated detail routes, inline editing, image/location/schedule UX, clearer privacy explanation, public directory filtering, or more complete action discoverability. These are not necessarily backend capability failures.

## 6. Target catalog findings

The target catalog is structurally healthy at this snapshot:

- 8 modules are declared.
- 123 target capabilities are declared.
- Every target capability maps to exactly one current inventory ID.
- No target capability is unmapped.
- No mapping is classified as broad or decomposition-required.
- Web UI coverage is detected for all 123 target capabilities.

This is strong traceability, but it is not proof of complete production readiness. The coverage script detects evidence references and surface signals; it does not execute the user flow. The current report flags two Vision-missing target records (`cross_module.handoff.mobile` and `cross_module.handoff.watch`) because their current inventory records are planned and native handoff is not a Vision surface in the current implementation.

The target catalog is therefore functioning primarily as a decomposition and traceability contract. It is not yet a complete “done” gate because mobile/watch runtime and production acceptance are intentionally outside current Web implementation.

## 7. Plan and evidence reconciliation

The implementation plan layer is much more complete than the runtime layer:

- many historical child/master plans are `verified`;
- the current inventory has `open_plans: []`, so there is no active capability implementation queue in the inventory;
- the implementation backlog still records the planned workspace-shell follow-up;
- runtime acceptance has 24 pending scenarios;
- generated audit reports are snapshots and cannot be used as current status without regeneration.

The important conclusion is that “no open inventory plans” means no currently opened capability implementation slice, not “no remaining work”. Residual gaps are explicitly retained in capability records and backlog/deferred scope.

The previous capability inventory audit already captured this semantic rule as `AUDIT-002` and `AUDIT-007`. Those findings remain valid and should be treated as control rules, not defects to remove.

## 8. Evidence quality findings

Inventory freshness passed and checked 138 evidence paths. The audit snapshot reports:

- 72 current capabilities with evidence-bearing records in the fresh scan;
- 19 backend DTOs without generated frontend contracts, mostly likely admin/internal or non-Web DTOs;
- 10 read-like service methods without explicit read-only transaction boundaries;
- 74 backend endpoints without a detected frontend callsite;
- one remaining frontend local permission/action duplication candidate.

These are diagnostic review queues. They do not automatically indicate defects:

- endpoint misses can be admin, native, Vision, backend-only, or intentionally not Web-exposed;
- missing generated contracts can be legitimate for non-client DTOs;
- read transaction findings require context because a service may delegate to a transactional boundary;
- one local frontend gate must be checked against backend-provided presentation/action metadata before cleanup.

## 9. Decisions and non-decisions

### Decisions supported by the evidence

1. Keep capability status semantics unchanged.
2. Keep target catalog and current inventory separate.
3. Treat runtime acceptance as the main next evidence bottleneck.
4. Treat native/mobile/Watch scope as an explicit future-client boundary, not as missing Web implementation.
5. Use atomic target/current capability IDs for future planning; aggregate IDs remain roll-up context.
6. Prioritize reconciliation of the 24 pending runtime scenarios before promoting large groups of `implemented` records to `verified`.

### Decisions not justified by this round

- Do not rewrite all `implemented` statuses as `partial` merely because mobile or runtime gaps exist.
- Do not close or delete declared gaps because target coverage reports Web evidence.
- Do not open implementation plans for every gap automatically; some require native-client or external-provider decisions.
- Do not classify the 74 unlinked endpoints as dead code without endpoint-by-endpoint ownership analysis.
- Do not normalize historical audit snapshots in place; preserve them as historical evidence and refresh through the owning audit workflow.

## 10. Recommended next slices

The master plan's Round 3 should now inspect backend/domain architecture, with special attention to:

- cross-domain repository/service dependencies;
- aggregate versus atomic capability boundaries;
- DTO/read-model assembly and transaction ownership;
- workflow state transitions and event projections;
- persistence coupling revealed by 76 migrations;
- the ten read-like methods without explicit read-only boundaries.

Runtime work should be selected separately from analysis. The clearest evidence queue is the pending runtime matrix, beginning with high-risk auth, Vision execution, Chat recovery/attachments, worker assignment, notifications, and location visibility.

## 11. Round 2 conclusion

The current system has strong capability traceability but incomplete evidence closure. Target-to-current mapping is exact and Web surface detection is complete, while runtime and native-client proof are the limiting factors. The implementation/control model is not contradicted by the inventory; rather, the reconciliation confirms why the model distinguishes product intent, implementation, verified work, and runtime acceptance.

No canonical capability status was changed in this round because the evidence supports the existing semantics and exposes no unambiguous stale status that can be safely corrected without a dedicated inventory maintenance slice.

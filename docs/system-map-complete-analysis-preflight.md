# Complete System Analysis Preflight

Status: prepared for goal pursuing. Reviewed: 2026-07-22.

## Purpose

This preflight makes the Round 20–30 program executable as a system-truth program,
not a sequence of isolated narrative reports. The program must produce traceable,
machine-readable relationship registries alongside concise human explanations.

## Readiness conclusion

The program is ready to start serially with Round 20. The master has one ordered
execution inventory, one atomic task per child plan, and no recursive verifier
commands. Its baseline is the current `HEAD` revision; work-start fingerprints
remain the authoritative protection for each future analysis artifact in a dirty
working tree.

The existing system already has strong canonical sources for product meaning,
domain rules, target scope, current inventory, plans, and runtime acceptance.
It does not yet have one cross-cutting registry that joins those sources to code,
clients, tests, and evidence. Rounds 20–29 fill that relationship gap; Round 30
consolidates only stable conclusions into the living map.

## Current analysis scale

Static source inventory at preflight contains 39 controller files, 49 repository
files, 90 model files, 270 DTO files, 280 service files, 16 mapper files, and 16
configuration files. The Web client is concentrated in the shared app shell with
separate Vision and identity areas. These counts describe inspection scope, not
feature completeness or runtime behavior.

## Baseline diagnostics to classify, not assume

The current frontend diagnostic sees 214 backend endpoints, 194 frontend client
methods, and 140 linked endpoint call sites. The remaining 74 endpoints are not
automatically missing Web support: some can be admin-only, internal, native-handoff,
or deliberately server-only. Round 21 must classify every relevant endpoint rather
than treating this count as a defect total.

The repository-fetch diagnostic flags 14 high-risk and 24 medium-risk methods. It
is a static review signal, not a measured performance regression. Round 26 must
turn the relevant risk rows into explicit measurement candidates with thresholds,
data shapes, and a required evidence type.

Runtime acceptance currently records 30 passed and 24 pending scenarios. Round 25
must preserve this distinction: source and test coverage may strengthen confidence,
but they cannot turn a pending runtime scenario into runtime proof. The frontend
diagnostics also retain a small number of stale-surface and duplicated-ui-logic
review candidates; Round 24 classifies those candidates by route ownership and
actual behavior.

## Program-wide rules

Every finding must identify:

1. its source links and evidence class;
2. whether it describes current implementation, target intent, or an unknown;
3. an owner and last-reviewed round in the related registry;
4. a disposition: canonical-source update, registry row, stable backlog ID, or
   documented no-change decision;
5. any boundary that prevents a stronger conclusion.

Static inspection may establish source structure and likely risks. It cannot prove
runtime latency, browser behavior, device behavior, delivery order, or operational
recovery without current evidence. Those claims remain explicitly unproven until a
dedicated runtime or measurement plan creates evidence.

## Round dependency and output map

| Round | Depends on | Primary machine-readable output | Decision gate |
|---|---|---|---|
| 20 | existing canonical docs | `docs/system-truth-registry.yaml` | establishes ownership and reference vocabulary |
| 21 | 20 | `docs/interface-evidence-registry.yaml` | separates endpoint existence from consumer and runtime proof |
| 22 | 20 | `docs/data-ownership-registry.yaml` | establishes data owners before workflow conclusions |
| 23 | 20, 22 | `docs/workflow-invariant-coverage.yaml` | reconciles declared states with service enforcement |
| 24 | 20, 21 | `docs/client-surface-registry.yaml` | distinguishes route presence from usable client parity |
| 25 | 21, 23, 24 | `docs/capability-evidence-registry.yaml` | distinguishes source/test/runtime evidence classes |
| 26 | 22, 25 | `docs/performance-evidence-catalog.yaml` | defines measurement work; does not claim measurements |
| 27 | 21, 22, 23, 25 | `docs/side-effect-delivery-registry.yaml` | classifies delivery, retry, ordering, and replay semantics |
| 28 | 20, 22, 23, 27 | `docs/security-operations-recovery-registry.yaml` | joins logical security with operational recovery boundaries |
| 31 | 20, 21, 22, 28 | `docs/delivery-dependency-registry.yaml` | maps supply chain, generation, delivery, and release evidence |
| 29 | 20–28, 31 | `docs/system-drift-control-registry.yaml` | specifies automated checks and update triggers |
| 30 | 29 | `docs/system-map.md` | consolidates stable findings and sequenced next work |

The execution inventory remains serial even where dependencies would permit parallel
reading. This preserves one coherent vocabulary and lets each later registry reuse
the identifiers established earlier.

## Plan improvements made by this preflight

- Added a shared evidence taxonomy and required finding-disposition rule to the master.
- Added explicit dependency and output fields to every inventory item.
- Added one machine-readable registry output to Rounds 20–29.
- Made Round 30 require an update to the living system map, preventing an isolated
  final report from becoming a competing source.
- Reframed Round 26 as a performance-evidence design round. Actual query, latency,
  memory, or scale measurement requires a separate runtime execution plan and fresh
  traces.
- Added Round 31 before drift-control design so generated artifacts, dependencies,
  release inputs, environment provenance, and external providers are part of the
  change-impact model rather than an untracked operational assumption.

## Known constraints and deferred execution decisions

- Native/mobile/Watch source and device evidence is absent. Rounds 21, 24, and 25
  must record contract readiness separately from client implementation and device proof.
- Production topology, backup restore, provider outage, and scale behavior cannot be
  verified solely from this workspace. Round 28 records available configuration and
  recovery design, then creates bounded runtime or operator follow-ups as needed.
- Existing audits are diagnostics. They support findings but cannot promote a
  capability or architecture claim to verified status without plan-verifier evidence.
- New registries are relationship indexes. They must point to canonical product,
  domain, inventory, and evidence sources rather than duplicating their statuses.

## Start protocol

1. Read the master, this preflight, and Round 20 before mutating any artifact.
2. Run `make work-start plan=docs/work/system-map-complete-analysis-round-20.yaml task=establish-canonical-truth-graph`.
3. Create the Round 20 report and `docs/system-truth-registry.yaml` from traced
   canonical sources.
4. Verify only that task with the work-plan verifier.
5. Continue to the next inventory item only after the prior one is verified.

No round may silently change capability completion status, target scope, or runtime
acceptance state. If analysis reveals a real correction, update its owner in the
same round and retain an evidence link explaining the correction.

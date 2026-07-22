# System Analysis Round 7 — Documentation, Audit, and Control System

Status: completed analysis snapshot, 2026-07-22. This round examines the YAML/Markdown hierarchy, work-plan graph, verifier evidence lifecycle, audit quality, backlog linkage, and automation safety.

## 1. Executive finding

The repository has a mature control vocabulary and a real executable verifier, but `docs/work/` is a heterogeneous registry rather than a directory containing only active implementation plans. The system is safe when operators follow the declared source-of-truth rules; it becomes confusing when filenames/statuses are interpreted without `kind`, ownership, evidence, and canonical-source context.

```text
canonical meaning / target scope / current inventory
                 ↓
         work plan or contract
                 ↓
       leaf validation commands
                 ↓
          verify-work.rb
                 ↓
      task/master evidence + status

optional audits → disposable diagnostics → review queue
runtime traces  → acceptance evidence      → capability evidence
```

The central design is correct: Markdown explains, YAML models active state/contracts, and verifier-generated evidence decides implementation completion. The largest control risk is inventory/plan graph discoverability, not the verifier algorithm itself.

## 2. Work directory composition

At this snapshot `docs/work/` contains approximately 140 YAML files and 332 task records. The files are intentionally mixed:

| Kind | Approximate role |
|---|---|
| `work` | implementation or documentation child plans |
| `master` | parent programs |
| `execution_inventory` | serial queue and child-task mapping |
| `contract_matrix`, `query_scope_contract`, `runtime_scenario_catalog` | machine-readable acceptance/control contracts |
| `capability_execution_board`, `capability_gap_analysis` | capability planning/diagnostic artifacts |
| `target_capability_coverage_report` | generated diagnostic report |
| `plan`, `domain_contract_draft`, `product_backend_device_contract_bundle` | historical/planning/contract artifacts |

The status distribution from the broad directory scan was approximately 122 `verified`, 5 `active`, 2 `completed`, 2 `done`, 2 `resolved_for_implementation`, 7 without a top-level status, and one current serial task in progress during this round. This distribution is not itself a defect because not every YAML file is a work plan. It does prove that `status` is only meaningful together with `kind` and the owning workflow.

The current system-analysis master is the one active program intentionally in progress. Its six child plans are verifier-controlled in one shared execution inventory; only one can be started at a time.

## 3. Implementation control model

`docs/implementation-control.md` defines the active workflow:

1. create a plan from the template;
2. record a Git baseline;
3. declare exact tasks, paths, and leaf validations;
4. implement/analyze the task;
5. run `make work-verify plan=...`;
6. let the verifier record evidence and state;
7. close temporary artifacts while retaining canonical evidence.

Strict plans require:

- exact `required_paths` changed from the baseline;
- validation commands that do not recursively invoke the verifier;
- visual/runtime evidence paths when the task declares those evidence classes;
- a passing verifier result for every task.

Strict serial programs add:

- shared execution inventory;
- exact plan/task mapping;
- one `work-start` at a time;
- start fingerprints;
- ordered verification;
- inventory status/evidence updates.

The system-analysis master demonstrates this pattern correctly. Round 1 is a completed prerequisite; rounds 2–7 are child plans; the inventory maps one task per round in strict order.

## 4. What the verifier actually guarantees

The verifier guarantees a narrow but valuable claim:

> The declared task paths changed from the declared baseline, the declared leaf validation commands passed, and the plan evidence records that result.

For child plans it also guarantees required-path evidence and task completion. For masters it guarantees all listed children are verified and, for strict masters, all inventory items are verified.

It does not guarantee:

- that the product target is fully implemented;
- that every capability gap is closed;
- that browser/device runtime proof exists unless the task explicitly required it;
- that audit output is current unless the audit was rerun;
- that a plan's objective was correctly scoped;
- that a generated report is canonical;
- that a verified historical plan still represents the entire current product.

This narrowness is a strength. It prevents “verified” from becoming an untestable general quality claim.

## 5. Audit system analysis

`docs/audits.md` correctly classifies audits as optional diagnostics. The available groups cover:

- backend API drift, read surfaces, repository fetch, mapper usage, mutation safety;
- frontend entrypoints, endpoint linkage, routes, stale surfaces, duplication, permission duplication;
- docs-as-tests and work-plan recursion;
- contract-test and fixture duplication;
- target catalog, target coverage, inventory freshness;
- runtime acceptance and native handoff;
- change impact and risk scoring.

The audit system has two important properties:

1. It produces useful concrete queues rather than binary “all good” claims.
2. It relies on static heuristics and therefore requires semantic interpretation.

Examples found in prior rounds:

- endpoint linker reports 74 unlinked endpoints, including legitimate admin/native/backend-only routes;
- generic route audit reports `/vision` without a surface even though dedicated Vision routing exists;
- stale-surface audit flags `ActivityRail.vue` and `styles/base.css` for review;
- permission duplication finds a small local gate alongside backend action sources;
- inventory freshness identifies DTO/read-boundary and endpoint-linkage review queues.

The correct response is classification and ownership metadata, not deleting every flagged item or weakening the audit until it is quiet.

## 6. Documentation system layers

The documentation hierarchy now has a stable entry point in `docs/system-map.md`:

- product intent: `product-vision.md`, `product-memory.md`;
- user/domain meaning: `business-logic.md`, `domain-technical.md`, workflow state machines;
- target scope: `target-capability-catalog.yaml`;
- current status: `capability-inventory.yaml`;
- implementation control: `implementation-control.md`, `control-surface-map.md`, `docs/work/*.yaml`;
- agent safety: `agent-operating-model.yaml` and API/intent sections;
- runtime proof: runtime matrix, regression catalog, runtime evidence;
- focused gateways: Vision context, location, native handoff, cross-domain acceptance;
- historical analysis: `system-analysis-round-*.md`.

This separation avoids the common failure where a Markdown summary becomes the only status source. `system-map.md` is an index/relationship map, not a duplicate inventory or completion ledger.

## 7. Agent safety and synthetic-data boundary

`docs/agent-operating-model.yaml` has a clear fail-closed default and explicit rules for exact target resolution, confirmation before destructive/external actions, backend authority, ambiguity handling, and sandbox/production separation.

The API and intent catalogs in `docs/agent-operating-model/sections/` are important machine-readable sources. Round 6 exposed a control integration bug: the frontend generated contract builder read only the root operating-model file even though the root points to section catalogs. This produced empty generated agent endpoint IDs and caused a safety fixture validator to reject valid `create_quest` references.

The safe fix was to make the generator resolve and load the declared API/intents section files, regenerate the contract, and rerun the safety audit. After the fix:

- 63 backend safety tests passed;
- frontend type-check passed;
- 3 admin-agent UI safety scenarios passed;
- frontend build and generated-contract freshness passed.

This finding is significant because it demonstrates that a control artifact can be logically correct while an automation consumer silently reads an incomplete source. Section references need integration tests, not only YAML validity tests.

## 8. Backlog and follow-up linkage

`docs/implementation-backlog.md` is the persistent human-readable queue for deferred product/control work. Machine-readable active status lives elsewhere: `docs/work/*.yaml`, inventory, execution boards, and runtime matrices.

The current system follows the intended rule that newly discovered deferred work should receive a stable backlog ID before closeout. The analysis rounds themselves did not open production implementation tasks for every finding; they record recommendations and preserve scope boundaries. That is correct because analysis findings range from runtime evidence needs to external-provider decisions and future native-client work.

The main future control improvement is to make the relationship between:

```text
capability gap → backlog item → selected work plan → verifier evidence → inventory update
```

more mechanically inspectable. Today these links exist in many records but are not one universal graph query.

## 9. Control-system strengths

1. One stated active implementation control surface.
2. Verifier-generated evidence rather than checkbox completion.
3. Strict exact-path and serial execution support.
4. Explicit separation between target catalog, current inventory, and runtime acceptance.
5. Audit reports intentionally disposable and diagnostic.
6. Machine-readable agent safety rules with validation tests.
7. Backlog IDs and TODO/FIXME linkage rules.
8. Dedicated system map and round register preventing analysis documents from becoming competing status sources.

## 10. Control-system risks

### 10.1 Heterogeneous `docs/work/`

The directory contains plans, contracts, reports, boards, inventories, and historical artifacts. This is workable but cognitively expensive. Every consumer must filter by `kind`, status semantics, and ownership.

### 10.2 Historical status snapshots

Many plans are verified historical records. They prove their own scope and time, not current product completeness. Reports and audit snapshots must retain generation timestamps and source references.

### 10.3 Section-source integration drift

The generator bug shows that root YAML references can be structurally valid while consumers ignore them. All generators should load referenced sources through one tested helper or have integration tests that assert non-empty expected catalogs.

### 10.4 Diagnostic false positives

Static audits need semantic ownership metadata for admin/native/Vision/backend-only surfaces. Without it, teams may either overreact to noise or learn to ignore useful findings.

### 10.5 Plan coverage ambiguity

`audit-plan-coverage` currently validates capability inventory `open_plans`, not every active analysis/control plan in `docs/work/`. The implementation-control rules additionally require active master references and unique graph paths, but not every general audit exposes that distinction.

## 11. Recommended control improvements

- Add a single plan-kind/status inventory report that classifies every `docs/work/*.yaml` file without becoming a second completion source.
- Add generator integration tests for every referenced section catalog, including non-empty intent/endpoint ID generation.
- Add explicit endpoint ownership metadata (`web`, `admin`, `native`, `Vision`, `backend-only`, `legacy`) to reduce linker ambiguity.
- Add an audit freshness timestamp/source revision to generated diagnostic summaries and report when they are stale relative to changed source domains.
- Add a machine-readable capability-gap-to-backlog/plan relationship check.
- Keep system-analysis master as the serial queue for this program and do not create parallel analysis checklists.
- Preserve the distinction between analysis findings, backlog decisions, and implementation status.

## 12. Round 7 conclusion

The control system is substantially stronger than a conventional Markdown checklist: it has canonical layers, executable plans, strict evidence, serial queues, diagnostics, and agent safety validation. Its main remaining weakness is discoverability across a large heterogeneous YAML graph and the possibility that consumers read only a root file while canonical data lives in referenced sections. Round 6 exposed and repaired one such integration defect. Future control work should improve graph introspection and source-reference integration without turning generated diagnostics into competing status systems.

No product domain behavior was changed in this round. The frontend contract generator integration fix discovered during Round 6 is the exception: it corrected a tooling/automation source-loading defect and was validated by the agent safety audit.

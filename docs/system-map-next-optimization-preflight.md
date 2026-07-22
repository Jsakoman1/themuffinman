# Next System Map Optimization Preflight

Status: execution complete, master verified. The previous optimization program and
this plan set are committed at `acc9581`; the plan baseline remains `62cdb7f`
because that is the repository state against which the plan was authored.

This document is a preflight record, not execution evidence. It does not start the
master plan, change any inventory status, or promote any pending runtime scenario.

## Deep-dive topology result

The master plan is structurally sound for strict serial execution:

- 7 child plans are referenced by the master;
- the execution inventory contains 16 ordered items;
- every child task has exactly one `inventory_item` mapping;
- every inventory item maps back to the correct child plan and task;
- the order is intentionally serial, so later controls cannot hide an earlier
  boundary or evidence failure;
- all plans use `strict_verification: true` and declare `required_paths`.

The first execution command must therefore be:

```text
make work-start plan=docs/work/system-map-next-optimization-01-dependency-boundaries.yaml task=analyze-boundaries
```

Only the first inventory item may start. Each later item must wait until the prior
item has verifier-recorded evidence.

## Deep-dive control findings

### Dependency boundaries

The scope is correctly limited to repository-visible module directions and explicit
exceptions. The implementation audit must report both sides of a forbidden edge and
the rule/exception identifier; a plain file-count or grep result is insufficient.
It must not claim runtime deployment architecture or native-client behavior.

### Endpoint review batches

The four review batches are a good risk boundary for the 62 non-Web owner-review
rows. Each batch must preserve owner, consumer, backend authority, client evidence,
test evidence, runtime class, and unresolved-gap fields. Static ownership completion
may reduce the review queue, but it must never change `pending` runtime status by
itself. The shared reconciliation registry is intentionally updated serially.

### Canonical-source lint

The lint should inspect a small, declared set of current-claim classes and canonical
links. Historical plans and dated reports must be excluded or explicitly marked
historical, otherwise the lint will manufacture false drift. Its seeded stale-claim
test must be isolated and removed or restored before closeout. Failure messages must
name the stale claim, canonical owner, and remediation path.

### Change-impact closeout

The impact report is an advisory review input. It may identify affected layers,
registries, tests, and documentation, but it cannot become the authority for plan
status or verification. The pilot must show a generated report referenced in one
closeout artifact and a human/machine disposition for each material impact.

### Capability evidence coverage

Coverage must distinguish source, endpoint, client, unit/integration test, runtime,
native/device, provider, and production-operation evidence. Missing evidence is a
reported gap, not a failed runtime claim and not an automatic promotion of a pending
scenario. The report should link each gap to a stable capability or backlog ID.

### Configuration and environment drift

The audit is limited to repository-visible typed configuration, properties, local
defaults, environment names, and secret classification. Secret values must never be
printed. Deployment state, provider state, and production rollout status remain
explicitly unknown unless separately evidenced.

## Draft-phase prerequisites

Four required paths are intentionally absent at this preflight stage because their
implementation tasks create them:

- `scripts/audits/audit-module-dependency-direction.rb`;
- `scripts/audits/audit-canonical-source-integrity.rb`;
- `scripts/audits/generate-capability-evidence-coverage.rb`;
- `scripts/audits/audit-configuration-environment-drift.rb`.

Their corresponding `Makefile` targets are also future deliverables. This is not a
draft failure. Before each implementation task can be verified, the script, target,
focused tests/fixtures where applicable, and remediation documentation must exist;
the task's strict `required_paths` and validation command enforce that boundary.

## Readiness decision

Decision: **completed and master-verified**. All 16 inventory items across 7 child
plans passed strict verification and the final closeout gates passed. Runtime remains
65 passed / 16 pending; the optimization controls did not promote pending evidence.

Main risks to monitor during execution:

1. endpoint classifications can become overly broad; retain per-row evidence and
   unresolved gaps;
2. canonical lint can confuse historical narrative with current truth; keep the
   claim-class allowlist narrow;
3. generated impact and coverage reports can become a second source of truth; keep
   canonical ownership in the registries;
4. config audits can accidentally expose secrets or imply deployment proof; redact
   values and classify external state as unknown;
5. new audit scripts can pass only their happy path; require deterministic negative
   fixtures or seeded failure checks before marking their implementation tasks done.

## Change-impact pilot evidence

The generated `docs/audit-output/system-map-change-impact.json` was consumed as an
advisory review input during this goal run. It identified 21 changed files and 3
registry review groups. The consuming work plan retained verifier status as the
authority and required disposition rather than automatic promotion. This proves the
closeout linkage pattern without making the generated report a second truth source.

## Program order

1. dependency boundaries;
2. endpoint review batches;
3. canonical-source lint;
4. change-impact closeout integration;
5. capability/test/runtime evidence coverage;
6. configuration/environment drift;
7. final closeout.

## Why the clean baseline is mandatory

The previous System Map optimization program created and verified many documentation,
registry, audit, and plan changes. They must be committed before this program starts so
strict verifier start fingerprints can distinguish new work from prior work.

## Current boundaries

- 62 non-Web endpoints remain owner-review items.
- Native/device/offline runtime is not proven.
- Provider failure injection, process-crash replay, and production operations remain
  externally or operationally pending.
- Static reports remain advisory unless a work plan explicitly consumes their evidence.

## Success model

The program is successful when every new audit is actionable, every endpoint batch has
explicit evidence boundaries, current claims have one owner, change-impact is part of
closeout, capability evidence gaps are navigable, and configuration drift is detected
without pretending to validate external deployment state.

## Entry commands

```text
make audit-runtime-acceptance
make audit-capability-evidence
make audit-truth-registry
make audit-docs
make audit-plan-coverage
git status --short --branch
git diff --check
```

After the entry commands pass, start only the first task. Do not set the master or
inventory to complete during preflight; verifier-driven status changes belong to the
goal-pursuing execution run.

The instructions above describe the original entry gate. The completed execution
state is recorded in the verified master plan and execution inventory.

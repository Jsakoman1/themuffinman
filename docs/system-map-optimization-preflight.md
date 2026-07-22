# System Map Optimization Preflight

Status: ready for goal pursuing. Reviewed: 2026-07-22. Baseline: `62cdb7f`.

## Objective

Use the System Map as an operational change-impact index to improve the implementation,
documentation, and control layers. The pass is an optimization program, not a new
feature program and not a reason to collapse distinct truth, capability, runtime, or
verification states.

## Readiness assessment

The source map is usable for this program. The current runtime truth counts are
reconciled at 65 passed and 16 pending, and the runtime audit now checks the matrix,
closeout artifact, and System Map closeout registry against one another.

The program is intentionally `draft` until its first child task is started through the
execution inventory. No child task is verified, and no future child may be started
before its predecessor is independently verified.

The baseline ranking is now explicit: P0 truth synchronization, P1 endpoint/evidence
linkage, P1 dependency enforcement, P1 documentation ownership, and P2 external/native
boundaries. The stable IDs are `OPT-BASELINE-001` through `OPT-BASELINE-005` in the
implementation backlog and baseline YAML.

## Program shape

| Order | Child | Outcome |
|---|---|---|
| 1 | baseline | ranked findings with canonical owners and measurable success conditions |
| 2 | implementation | clearer module, endpoint, client, test, configuration, and change-impact relationships |
| 3 | documentation | canonical-source ownership, duplicate-claim policy, and improved navigation |
| 4 | control | stronger drift checks and a smaller reliable verification path |
| 5 | cross-layer pilot | one real repair proves the optimized operating model |
| 6 | closeout | reconciled registries, backlog, evidence, and next queue |

## Required controls

- Use `docs/work/system-map-optimization-master-execution-inventory.yaml` as the sole
  serial queue.
- Start one child task with `make work-start plan=<child> task=<task-id>`.
- Change only the declared task scope unless an impact note expands it.
- Verify the task with the verifier and its declared required paths before starting the
  next task.
- Keep capability status, implementation status, runtime evidence, and work-plan
  verification separate.
- Preserve historical analysis and runtime evidence; archive only through an explicit
  decision.
- Add every stable deferred finding to `docs/implementation-backlog.md` with a stable ID.

## Success measures

The pass is successful when:

1. every selected finding has one owner, one canonical source, one affected surface,
   and one validation path;
2. selected current-state claims have one canonical owner and no unresolved
   contradictory duplicate;
3. selected high-value cross-file relationships have automated drift checks;
4. the pilot records a concrete reduction in lookup steps, duplicated edits, or
   unverifiable claims;
5. all remaining native, external-operations, failure-injection, and runtime limits
   remain explicitly pending rather than being promoted by static analysis.

## Known boundaries entering the pass

- 62 non-Web endpoints remain owner-review items.
- Native/device/offline runtime is not repository-proven.
- Process-crash replay, deterministic provider/database failure injection, and some
  cross-client consent parity remain pending.
- Production deployment, backup/restore, rollback, and incident evidence remain
  externally unknown.

These boundaries are inputs to optimization, not defects to hide or silently close.

## Entry validation

```text
make audit-runtime-acceptance
make audit-capability-evidence
make audit-truth-registry
make audit-docs
make audit-plan-coverage
git diff --check
```

The next action is to start the first task in
`docs/work/system-map-optimization-01-baseline.yaml` through the master execution
inventory.

## Pilot measurement

The runtime truth synchronization pilot completed its implementation validation:

- Before: multiple runtime summaries could disagree (`42/39`, `54/27`, `64/17`,
  and `65/16` were found across current and historical surfaces), and the audit did
  not compare every active summary registry.
- After: the current matrix, closeout artifact, System Map runtime registry, capability
  evidence registry, and authority metadata reconcile at `65 passed / 16 pending`;
  `audit-runtime-acceptance` rejects mismatches.
- Control friction found: two child validation commands needed repository-root-safe
  Maven paths, and one task path list needed concrete files instead of directories.
- Remaining boundary: historical work-plan outputs intentionally retain old results and
  are not rewritten as current status.

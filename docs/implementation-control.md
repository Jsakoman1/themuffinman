# Implementation Control

Optimization baseline: `docs/system-map-optimization-baseline-2026-07-22.yaml`.
Documentation class: execution_control. This file owns the implementation workflow; active task state is verifier-generated in YAML plans and inventories.
Control optimization review: `docs/system-map-optimization-control-review-2026-07-22.yaml`.
Optimization closeout: `docs/system-map-optimization-closeout-2026-07-22.yaml`.

This is the single active workflow for implementation work.

Every new master and non-trivial work plan must apply
`docs/plan-scope-control-standard.yaml`. Before goal pursuing, the plan must state
which verified plans and surfaces are baseline-only, what residual scope remains,
which concrete changes trigger a retest, and how duplicate verification will be
avoided. A broad feature or module name is not sufficient residual scope.

At closeout, report only new repairs and new evidence. Preserve prior verified
evidence and existing pending/external boundaries instead of counting them again.

Every non-trivial change has one YAML work plan under `docs/work/`. It contains the change identity, Git baseline,
ordered tasks, expected paths, validation commands, and verifier-generated evidence.

Markdown explains the workflow. It is not completion state. Generated reports and old plan formats are not active
control state.

## Closeout cleanup

After a plan or master plan reaches `verified`, remove temporary validation outputs, screenshots, smoke traces, and
unreferenced analysis or plan files created only for that work. Keep canonical product/domain/control documents,
current runtime JSON evidence referenced by the capability inventory, and any completed plan still used as an explicit
dependency or historical source. The closeout must leave only the current `active`/`planned` work queue plus retained
evidence; it must not leave duplicate generated reports or stale plan references.

## States

`draft -> active -> verified` is the normal path. A plan may be `deferred` when its remaining work requires an
external runtime, device, or implementation workspace that is not available; the plan must record why and when to
resume. A task may be `blocked`; a blocked or pending task prevents the work plan from becoming verified.

Only `ruby scripts/verify-work.rb plan=<path>` may create final `verified` state. A checkbox or manually written
evidence paragraph is not proof.

`draft` is preparation state and is intentionally excluded from the active inventory
queue until `make work-start` promotes a selected child/task to `active`. A strict
serial program must keep one global execution inventory and one in-progress item.

## Workflow

1. Create a work plan from `docs/work-plan.template.yaml`.
2. Record the Git baseline before implementation.
3. Implement each task and keep its expected file list accurate.
4. Run `make work-verify plan=<path>`.
5. The verifier executes every task command and records the exit code, timestamp, revision, changed files, and output.
6. Verification succeeds only when every task passes and every implementation task is linked to a changed file.
7. New plans use `strict_verification: true`: every task lists exact `required_paths`, all of them must change, visual tasks attach changed screenshot evidence, and runtime tasks attach changed runtime artifacts. A build or static audit alone is never browser evidence.

Validation commands are leaf validations only. A task must never invoke `make work-verify` or
`scripts/verify-work.rb`; the verifier rejects those commands and propagates a guard variable to prevent indirect
nested verification. Run `make audit-work-plan-recursion` when reviewing or generating future plans.

Master plans use `kind: master`, the template `docs/master-plan.template.yaml`, and a `children` list of work-plan paths.
They contain no duplicate task state and are verified only when every child is independently verified.

## High-risk serial programs

For a broad, high-risk program with visual, API, and behavioral scope, create a
separate `execution_inventory` YAML next to the master. It lists every atomic
delivery item in execution order and maps it to exactly one child-plan task.
Set `serial_task_execution: true` and the inventory path on every child. The
workflow is mandatory: `make work-start ... task=<id>` snapshots the required
paths, then code changes are made, then `ruby scripts/verify-work.rb ...
task=<id>` verifies that one item. The verifier rejects bulk closeout, a second
in-progress item, out-of-order starts, and paths that did not change after the
explicit start snapshot. A strict master cannot verify until every inventory item
is verifier-marked `verified`.

For browser runtime tasks, pass `WEB_RUNTIME_EVIDENCE_PATH=<path>` to the smoke
harness. It writes a timestamped JSON outcome with the tested base URL, observed
API responses, and any failure message. This trace is an artifact of the current
run, not a claim based on a pre-existing HAR or another developer's process.

For local browser evidence, use `make dev` and close the owned stack with `make dev-stop`.
The dev launcher records its workspace-owned process tree, removes it on normal
exit, and refuses to silently attach to occupied ports. Diagnose an unexpected
listener with `make dev-doctor`; do not terminate a process merely because it owns
a standard development port.

This is the required control pattern when partial delivery would make a plan look
complete while named routes, contracts, or runtime behavior remain absent.

## Minimal commands

```text
make work-create id=my-change title="Short title"
make work-verify plan=docs/work/my-change.yaml
make master-create id=my-program title="Program title"
make work-verify plan=docs/work/my-program.yaml
```

Use only the current work-plan and verifier workflow for new implementation work.

## System Map optimization entrypoint

For a normal non-trivial repair or feature, the smallest reliable control path is:

```text
make system-map-impact
make work-start plan=<child-plan> task=<task-id>   # serial plans only
make work-verify plan=<child-plan> task=<task-id>
make audit-truth-registry
make audit-docs
```

Add `make audit-runtime-acceptance` and
`make audit-capability-evidence` when runtime or capability evidence is affected.
The impact report is advisory; the work plan and verifier remain the completion path.

## Autonomous batch continuation

When the user authorizes autonomous continuation, the active work plan is a batch rather than a single-slice
request. Load the safe task queue up front, execute tasks in dependency order, and continue automatically after each
successful slice. Do not end the turn merely because one task or module is complete. End only after the batch reaches
its planned closeout, a real blocker requires a product or external decision, or required validation has failed and
safe recovery is exhausted. Record newly discovered follow-up work in the persistent backlog before continuing.

The `make audit-plan-coverage` check also verifies that every active inventory plan is listed by the active master, every master child points back to that master, and active plan paths are unique and present.

The target product capability contract lives in `docs/target-capability-catalog.yaml`. It describes complete
production-ready user capability scope and required client surfaces. It does not replace
`docs/capability-inventory.yaml`, which remains the current implementation and evidence inventory. Run
`make audit-target-capability-catalog` to validate target capability structure, module/object indexing, current
inventory links, and coverage of documented inventory modules.

Run `make audit-truth-registry` after changing `docs/system-truth-registry.yaml` or a path it owns. It checks required
ownership/evidence metadata, duplicate IDs, approved evidence and confidence values, and referenced canonical or
derived paths. It verifies registry integrity only; it never promotes capability, runtime, or work-plan status.

The canonical-source lint, when enabled by the System Map optimization plan, checks only its declared current-claim
classes. Historical analysis and disposable audit output are excluded. A lint finding must identify the claim path,
canonical owner, and remediation; the lint is a consistency control and does not become a second status authority.

Run `make audit-interface-evidence` after changing controllers, frontend API calls, or the interface-evidence registry.
It assigns every statically discovered endpoint an explicit evidence class and retains non-Web endpoints as a review
queue. A static Web link is not a usability or runtime claim.

Run `make audit-data-workflow-impact` after migration, entity/repository, ownership-registry, or workflow-coverage
changes. It checks source links and required metadata only; it does not infer a live schema or runtime workflow proof.

Run `make audit-capability-evidence` after changing runtime acceptance or capability evidence registries. It checks
that passed runtime scenarios reference current artifacts and preserves the rule that evidence alone does not set a
capability's inventory status.

Run `make audit-delivery-provenance` after changing build sources, dependency manifests, generated-contract paths, or
delivery provenance. It validates repository-visible paths and requires external CI/release behavior to stay explicitly
unknown until independent evidence is available.

Run `make system-map-impact` before a non-trivial feature closeout or with explicit changed paths to generate an
advisory relationship report. It recommends registries, canonical docs, and runtime sources for review; it never sets
capability status, runtime proof, permissions, or work-plan completion.

For the System Map optimization program, the impact report is an entrypoint diagnostic:
the selected task must still name exact changed paths, canonical owners, and leaf
validations in its work plan. The report does not replace the execution inventory or
the verifier.

For non-trivial closeouts that consume the report, the work plan must record the
report path, generation time, and a disposition for each material recommendation:
`reviewed`, `deferred_with_backlog_id`, or `not_applicable_with_reason`. The report
is advisory evidence only and cannot set task, capability, runtime, or release status.

Vision surface coverage in the target report requires explicit `current.vision` evidence, a Vision-prefixed capability ID, or an unambiguous Vision path in current web/backend evidence; generic backend implementation alone is not treated as Vision support.

Web UI coverage is also more than a route or component reference. A capability is web-ready only when the user can discover its entry point, complete its direct interaction flow, see backend-owned validation and permissions, and recover from failure. Web and Vision are equal production clients; “Vision available” does not compensate for an incomplete web flow, and “web route exists” does not compensate for an incomplete Vision flow.

Run `make audit-target-capability-coverage` for a generated YAML report comparing
target capabilities with current inventory status and detected web/Vision evidence.
The report also marks `mapping_quality` as `exact`, `broad`, or `unmapped`.
`broad` means the target action is linked to a broader current record and still
needs independent implementation/evidence decomposition. The report is
diagnostic output only; it does not become a second status source of truth.

For the runtime-pending Vision slice, `app.agent.runtime-failure-mode=provider_failure`
is a development-only deterministic provider control. It must remain `none` outside
local/dev evidence capture, must never be used as production behavior, and must be
reset after the trace. A valid provider-failure trace records the control state,
correlation ID, unavailable outcome, retry, and authoritative final state.

Run `make generate-target-capability-slices` to produce the next implementation
queue from the target catalog. The output is planning input only. A selected slice
must still become its own `docs/work/*.yaml` plan and reach `verified` through the
normal work-plan verifier before it is considered complete.

## Safety rules

- Validation evidence must be generated by the verifier.
- Failed validation leaves work active or blocked.
- No completion status is inferred from checkboxes alone.
- Verification never commits or pushes.

## Runtime evidence

Source tests, type checks, and production builds do not count as browser or device runtime evidence. For strict plans, a task with `manual_runtime_command` cannot verify without its declared changed `runtime_evidence_paths`; a visual implementation task cannot verify without its declared changed `visual_evidence_paths`. Open runtime gaps belong to the owning capability and strict work plan. Runtime evidence is intentionally ephemeral: a future runtime slice must declare its own changed evidence paths and may only close after an actual browser or device trace.

The static Web contract preflight can be run with `npm --prefix apps/themuffinman/frontend run validate:web-surface`.
It checks canonical route presence and ordering, visible entry actions, Calendar modes/timezone labeling, and
Chat/Circles recovery affordances; it supplements but does not replace browser traces.

Run `ruby scripts/audits/audit-ui-entrypoints.rb` (also included in `make audit-frontend`) to verify that primary
navigation, shell surfaces, authenticated routes, create handoffs, and configured surface actions remain connected.

Run `make audit-main-surfaces-plan` before starting the Home, Business, Work,
and shared-surface modernization batch. It checks the six-child plan graph,
backend/frontend ownership boundaries recorded in the child objectives, exact
verifier-safe paths, and non-recursive leaf validations.

The active 30-capability pursuit is governed by `docs/work/capability-30-execution-board.yaml`. It is the frozen ordered list of partial capability IDs, owner plans, required source files, close criteria, and explicit product deferrals. Do not create an unlisted continuation slice; update the board first when scope genuinely changes.

The current cross-layer control optimization program is governed by
`docs/work/system-map-optimization-master.yaml`. It is a serial maintenance and
control program, not a product capability board. Its execution inventory is the
sole queue for its child tasks and must be advanced in order.

The runtime-pending Vision control uses `app.agent.runtime-failure-mode=provider_failure`
only for local evidence capture. It must be reset to `none` before the owned stack is
reused for ordinary scenarios; production profiles must never activate the control.

The same development-only property may use `side_effect_failure` to force the Workmarket
application-news consumer into its existing failed-outbox path. This is a bounded local
control for observing domain commit versus delivery failure; it is not a production
feature and must be reset to `none` after capture.

The reliability slice also requires this outbox control to remain local-only and
covered by the backend test suite before any runtime evidence is promoted.

The System Map hardening runtime boundary index is
`docs/system-map-runtime-boundary-registry.yaml`; it classifies durability and
replay requirements without prescribing a universal outbox for every module.

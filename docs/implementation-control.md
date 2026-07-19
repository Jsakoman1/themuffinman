# Implementation Control

This is the single active workflow for implementation work.

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

Vision surface coverage in the target report requires explicit `current.vision` evidence, a Vision-prefixed capability ID, or an unambiguous Vision path in current web/backend evidence; generic backend implementation alone is not treated as Vision support.

Web UI coverage is also more than a route or component reference. A capability is web-ready only when the user can discover its entry point, complete its direct interaction flow, see backend-owned validation and permissions, and recover from failure. Web and Vision are equal production clients; “Vision available” does not compensate for an incomplete web flow, and “web route exists” does not compensate for an incomplete Vision flow.

Run `make audit-target-capability-coverage` for a generated YAML report comparing
target capabilities with current inventory status and detected web/Vision evidence.
The report also marks `mapping_quality` as `exact`, `broad`, or `unmapped`.
`broad` means the target action is linked to a broader current record and still
needs independent implementation/evidence decomposition. The report is
diagnostic output only; it does not become a second status source of truth.

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

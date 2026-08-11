# Project read model

`Dora::ProjectReadModel` is Dora's transport-neutral, read-only aggregate query
boundary. It validates one declared project adapter, reads only declared project
and work-plan roots, and returns a compact projection for an external bridge.
Before opening a known project artifact or caller-selected work plan, it resolves
the path canonically and rejects a symlink that leaves the configured project root.

Its summary contains project identity, doctor health, an explicit integrity
projection, active and latest verified delivery state, a deterministic current goal,
a next task when resolvable, open decisions, and relative citations. It does not
require project memory to be valid before reporting independent health, plan,
inventory, or verified-delivery data.

Bridge health is stricter than the local doctor's pass/fail exit status. A local
doctor `advisory` becomes a sanitized Bridge integrity signal and changes the summary
state to `WARNING`; `health.doctor_healthy` preserves the local pass/fail fact while
`health.healthy` and `health.status` describe the safe external context. Integrity
signals classify `warning`, `conflict`, `stale`, `ambiguous`, or `invalid` state and
cite only project-relative references. The model never repairs or suppresses the
underlying artifact.

Project memory is schema-validated navigation, not a completion claim. Its
`current_work` must exactly match declared execution state: one `in_progress` or
`blocked` inventory item, or the uniquely ordered next `pending` item. When no
such item remains it must use the explicit terminal representation
`{ state: none }`. Missing, stale, contradictory, or ambiguous navigation is an
explicit `project_memory` `INVALID` inconsistency; the model never picks a task
from raw history merely to fill a current-work field.

Strict master closeout runs this same validation before it records the master as
verified. A successful closeout then marks its execution inventory `verified`;
the task-level evidence and prior verified-delivery record remain unchanged.
`current_goal` reuses the existing delivery and next-task resolution: it is `active`
for one in-progress inventory, `planned` or `blocked` for the existing next-task
result, `none` when no declared candidate exists, and `ambiguous` when more than one
active inventory exists. It never chooses a goal from historical evidence.

Latest verified delivery is the uniquely latest inventory item whose inventory state
is `verified`, whose referenced work plan is `verified`, and whose task has passing
Dora evidence. The timestamp is that item's valid `verified_at`. Active, superseded,
and control-reconciled inventories cannot win merely because they contain a newer
timestamp. Equal latest timestamps yield an explicit ambiguous delivery result rather
than an arbitrary selection; no qualifying item is represented by an absent latest
delivery field.

The model exposes only safe plan/task fields and evidence summaries. It never
returns raw verifier output, validation command text, changed source paths,
absolute paths, environment values, or evidence references outside the declared
runtime-evidence root. It does not select projects; an authorization layer must
allow-list an adapter before constructing the model.

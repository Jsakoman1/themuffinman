# Project read model

`Dora::ProjectReadModel` is Dora's transport-neutral, read-only aggregate query
boundary. It validates one declared project adapter, reads only declared project
and work-plan roots, and returns a compact projection for an external bridge.
Before opening a known project artifact or caller-selected work plan, it resolves
the path canonically and rejects a symlink that leaves the configured project root.

Its summary contains project identity, doctor health, explicit inconsistencies,
active and latest verified delivery state, a next task when deterministically
resolvable, open decisions, and relative citations. It does not require project
memory to be valid before reporting independent health, plan, inventory, or
verified-delivery data.

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
Current work is otherwise an inventory with an `in_progress` item. More than one
such inventory is reported as an ambiguity. Latest verified work is the inventory
item with the latest valid `verified_at` timestamp; inventories with no verified
items, including historical superseded inventories, are ignored.

The model exposes only safe plan/task fields and evidence summaries. It never
returns raw verifier output, validation command text, changed source paths,
absolute paths, environment values, or evidence references outside the declared
runtime-evidence root. It does not select projects; an authorization layer must
allow-list an adapter before constructing the model.

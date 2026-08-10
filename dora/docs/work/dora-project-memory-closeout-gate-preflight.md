# Project-memory closeout gate preflight

## Discovery

- `docs/project-memory.yaml` is currently invalid: its `current_work` value is a prose string and therefore is not the mapping required by `Dora::ProjectMemory`. The last verified V2.2 closeout recorded that file as changed, but the completion update used a narrative terminal sentence instead of the domain's explicit idle representation, `{ state: none }`.
- `ProjectMemory` already defines the valid terminal state and rejects a task-less `verified` state. Its validation was not invoked from `WorkExecution#verify_master!`, so a delivery could report verified without validating the final project navigation.
- `ProjectReadModel` reports schema-invalid memory as an explicit `INVALID` inconsistency, but it previously did not compare otherwise schema-valid `current_work` navigation to declared execution inventories. It could therefore expose a valid-looking stale or contradictory pointer.
- The V2.1 and V2.2 execution inventories have only verified items but retain `state: active`. Their item evidence is authoritative and remains untouched; the inventory lifecycle label must be reconciled separately to a terminal value.

## Prevention design

Use `ProjectMemory` as the single validation boundary. It will derive exactly one expected navigation target from declared Dora execution inventory items: active or blocked work takes precedence, otherwise the uniquely ordered pending item is planned, and no unresolved item requires `{ state: none }`. Multiple equally authoritative candidates are an explicit failure, never a historical-artifact selection.

`WorkExecution#verify_master!` will load and validate project memory against the declared inventory before it can set master status to `verified`, then reconcile that inventory's lifecycle label to `verified` after all its items have verified. `ProjectReadModel` will run the same validator after collecting inventories and surface navigation drift as an explicit `project_memory` inconsistency while retaining independent delivery evidence.

The final closeout will repair this project's memory to `{ state: none }` and reconcile only the V2.1/V2.2 inventory state labels. It will not edit V2.2 work plans, handoffs, task evidence, timestamps, or result records.

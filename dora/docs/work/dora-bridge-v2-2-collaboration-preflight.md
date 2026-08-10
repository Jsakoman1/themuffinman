# Dora Bridge V2.2 collaboration feedback preflight

## Discovered authority

- V2.1's private `Dora::Handoff` event log is the authoritative mutable handoff state. Its immutable created event, claim, delivery link, block, completion, and follow-up lineage already satisfy the required auditability boundary.
- Dora work plans, serial inventories, `ProjectReadModel`, and `HandoffCLI#complete!` are authoritative for delivery and passing verifier evidence. V2.2 must reference them and must not copy work/task progress into a parallel status system.
- Existing bridge reads already expose object-shaped V2 handoff results. Extending `get_handoff` and `get_handoff_status` is smaller and safer than adding a new MCP tool; `get_current_delivery` remains the project-level Dora delivery projection.

## V2.2 design decision

Add only three validated append-only lifecycle actions for a claimed handoff:

1. `feedback` records one bounded semantic phase (`DISCOVERY`, `PLANNING`, `IMPLEMENTING`, or `VERIFYING`), a milestone, optional significant progress/finding, explicit verification state, and compact deviations or residual risks.
2. `block_owner_decision` records a terminal `BLOCKED` state with the question, why it is required, known realistic options, an optional recommendation, blocked work, and work that may continue.
3. `complete` retains its existing delivery-and-passing-evidence gate and adds a compact validated completion result. The read model shows the referenced Dora master/work plan and verified task alongside work performed, interpretations, acceptance results, deviations, residual risks, and follow-up need.

The current lifecycle status and current Dora verification evidence override any earlier handoff snapshot text. `COMPLETED` is therefore always read beside `VERIFIED` evidence references; it never substitutes for them.

## Explicit boundaries

- V2.2 has no terminal transcript stream: feedback is flat, bounded, high-signal data and has no raw command-output field.
- ChatGPT gains no write beyond existing handoff creation and no ability to start, steer, or control Codex. The local Codex CLI remains the only feedback/block/completion writer.
- V1 stays read-only and V2.1 creation, idempotency, project authorization, and object-shaped read compatibility remain regression requirements.
- Automatic remote Codex start, steering, or control remains an explicitly separate V3 security program.

## Preflight result

The declared master, serial execution inventory, and every child work plan pass Dora's work-plan validation. The first implementation slice is limited to the private handoff store and local CLI; the bridge read projection cannot start until that bounded lifecycle contract is verifier-recorded. No owner/product decision is currently missing: the handoff requires a safe evolutionary projection and explicitly excludes V3 control.

# IDC owner-started orchestration atomic hardening review

## Baseline

`dora-idc-operational-v0-master.yaml` is verified. It already supplies the local
IDC contract/renderer, sanitized Dora envelope, and fixed read-only Bridge envelope.
`chatgpt-intent-plan-alignment-master.yaml` is verified and supplies only a precedent
for transient, sanitized Bridge evaluation. Neither baseline gives Bridge execution
authority or creates local IDC authorization.

## Atomic-task review

| Inventory item | One observable outcome | Exact changed surface | Leaf proof | Authority boundary |
| --- | --- | --- | --- | --- |
| `idc-orchestration-preflight` | Record the future authority model and harden later slices. | Two Markdown records only. | Atomic-plan contract test and Doctor. | No runtime behavior or canonical mutation. |
| `idc-orchestration-triage` | Classify one structured request without execution. | Triage schema, pure evaluator, unit test, DomainLibrary invariant. | Triage and IDC v0 contract tests. | No source discovery, process, Bridge, or write. |
| `idc-orchestration-local-render` | Locally invoke the existing renderer after revalidated authorization. | Dora command entry, one wrapper, focused tests. | Wrapper, integration, renderer, and command-envelope tests. | Fixed Ruby entrypoint only; no shell/Codex/Git/network/Dora write. |
| `idc-orchestration-bridge-readback` | Return one sanitized triage readback and explain the local handoff. | Bridge server/test, two operating docs, triage test extension. | Triage, Bridge, wrapper, and Doctor tests. | Bridge cannot invoke the wrapper or accept paths/commands. |

## Dependency and overlap review

- The inventory is contiguous and each item maps to one task with its direct
  predecessor dependency.
- Only `test/idc_triage_test.rb` appears in two slices. Slice 02 introduces the
  pure evaluator test; slice 04 deliberately extends that same test for the Bridge
  adapter. This is a serial, behaviorally connected overlap, not a second feature.
- Slice 03 is the only slice allowed to introduce a process boundary. It delegates
  to the verified renderer rather than duplicating it.
- Slice 04 is deliberately last so a Bridge readback cannot be mistaken for the
  mechanism that grants or executes local rendering.

## Eligibility and stop conditions

- No implementation slice is eligible without the prior slice's recorded verifier
  evidence.
- Stop if a required behavior needs retained authorization, arbitrary command/path
  input, automatic source selection, Bridge execution, remote invocation, Dora write,
  consumer-project access, or a second renderer.
- Stop if the local wrapper cannot demonstrate direct fixed-entrypoint invocation
  without shell interpolation and bounded output containment.
- A failed leaf validation may be repaired only within that slice's declared target
  paths and authority boundary; otherwise it requires a new owner decision.

## Closeout standard

Only new triage, wrapper, and Bridge-readback evidence counts for this master.
IDC v0's earlier evidence is baseline only. Master closeout requires all four child
plans verified, a verified inventory, comparison of new evidence, and no widened
authority.

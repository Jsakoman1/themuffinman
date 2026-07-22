# Round 20: Canonical System Truth Graph

Status: source-trace analysis. Reviewed: 2026-07-22.

## Conclusion

The repository has authoritative documents for the major fact classes, but their
relationships have mostly been navigational rather than machine-readable. The new
`system-truth-registry.yaml` is an index of ownership and evidence direction. It
does not duplicate capability status, domain rules, or verification evidence.

## Truth layers

| Fact class | Canonical owner | What must not replace it |
|---|---|---|
| Product direction | `product-vision.md`, `product-memory.md` | backlog items or UI implementation details |
| User-facing rules | `business-logic.md` | controller behavior inferred from one endpoint |
| Domain invariants and transitions | `domain-technical.md`, `workflow-state-machines.yaml` | client-side eligibility logic |
| Target scope | `target-capability-catalog.yaml` | current inventory or plan status |
| Current capability state | `capability-inventory.yaml` | a route, test, or audit report alone |
| Implementation completion | `docs/work/*.yaml` plus verifier evidence | checkboxes, prose, or audit output |
| Runtime proof | acceptance matrix, scenario catalog, current runtime artifacts | source inspection or builds |
| Agent safety | `agent-operating-model.yaml` and its validated sections | a prompt or sandbox fixture |
| Generated TypeScript contract | backend DTO source plus generator script | manually edited generated output |

## Reference direction

The canonical source may link outward to code, tests, evidence, plans, or a
cross-cutting registry. A registry may link to a canonical source, but it may not
become an independent status authority. A plan may produce evidence, but it may not
silently promote target or capability status.

## Update triggers

- Product meaning changes update business logic first when users observe the change.
- Entity, permission, validation, workflow, or endpoint changes update domain and
  affected registry rows in the same work plan.
- Capability support changes update the current inventory; target changes update
  the target catalog independently.
- Runtime claims change only after fresh runtime evidence is recorded.
- A generated contract source change reruns the generator and checks the generated
  artifact; the artifact itself is not manually authoritative.

## Boundaries

This round traced repository documentation and control sources. It did not claim
that every reference inside those documents remains source-current; that is the
purpose of later interface, data, workflow, evidence, and drift rounds.

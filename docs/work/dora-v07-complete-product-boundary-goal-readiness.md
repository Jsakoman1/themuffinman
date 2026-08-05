# Dora v0.7 goal-pursuit readiness review

Reviewed on 2026-08-05 against baseline `55b4fc4`.

## Plan graph result

The v0.7 master has one hardening child, four delivery children, and a single
thirteen-item strict serial inventory. Every inventory item maps once to one child
task, has one observable outcome, exact required paths, a leaf validation command,
an evidence boundary, and the immediately preceding serial dependency.

The generic atomic hardening, serial integrity, work-plan schema, and recursion
audits pass for the draft graph. This is structural readiness, not implementation
evidence. The first task must create and run the v0.7 semantic hardening audit.

The generated System Map impact report maps this planning-only diff to
`docs/system-truth-registry.yaml`, `docs/system-drift-control-registry.yaml`, and
`docs/system-map.md`. They are unchanged ownership context; a later extraction that
changes one of them must refresh the report and record a closeout disposition.

## Scope decision

The catalogue task is deliberately first after hardening. It must classify every
local audit, including those that do not currently import Dora. A reusable mechanism
is extracted or delegated only when it can run with declared inputs and no
MuffinMan business meaning. Product workflows, permissions, entities, registries,
business/domain documents, and runtime evidence remain product-retained.

## Start gate

The plan is ready to become a goal. The only permitted first action is
`harden-v07-product-boundary-plan`. Tasks 1–11 are local repository work. Task 12
publishes Dora v0.7 and task 13 changes the MuffinMan pin; both remain separate
explicit-approval gates.

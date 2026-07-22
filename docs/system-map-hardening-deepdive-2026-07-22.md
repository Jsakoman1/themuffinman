# System Map Hardening Deep-Dive

## Decision

The hardening master plan is suitable for goal pursuing after the controls below
were added. Its purpose is to improve the System Map as an operating instrument,
not to create another broad descriptive document and not to claim that mapped
capabilities are implemented or runtime-verified.

## Review result

Initial plan quality was strong in scope but incomplete in execution control. The
main weaknesses were:

1. The seven child plans had no explicit dependency order.
2. Existing ARCH-001 through ARCH-005, drift plans, and runtime registries could
   be duplicated instead of reconciled.
3. Planned modules and current implementation were named together without a
   machine-readable state rule.
4. A module could appear mapped without proving data ownership, workflow authority,
   client edges, operational boundaries, and evidence edges together.
5. Side-effect mapping could accidentally turn into a premature universal-outbox
   implementation plan.
6. The closeout did not explicitly distinguish mapping completeness from product or
   runtime completeness.

All six weaknesses are now represented as plan controls.

## Execution order

The sequence is intentionally serial:

1. module topology and denominator
2. endpoint/capability/client/test/runtime evidence
3. dependency direction and data ownership
4. workflows, states, permissions, and invariants
5. side-effects, jobs, providers, storage, retries, and replay
6. Web–Vision/native/external client boundaries
7. reconciliation, drift-control, and closeout

This prevents later relationship maps from inventing owners or using an incomplete
module list.

## Existing-source reuse

The hardening pass must reconcile, not replace, the existing canonical surfaces:

- endpoint and capability evidence registries
- runtime acceptance matrix and runtime evidence artifacts
- module dependency and truth registries
- workflow/state and permission/visibility matrices
- side-effect and security/operations registries
- native client and provider/configuration contracts
- System Map coverage, backlog, and change-impact records

If an existing source owns a status, the new registry may index it but may not create
a competing status authority.

## Complete module coverage model

Each current or planned module is evaluated across these dimensions:

`topology, data ownership, dependency direction, schema/migrations, endpoint,
capability, client surface, workflow/state, permission/visibility, tests, runtime
evidence, side-effects, scheduled jobs, provider/storage boundary,
configuration/environment, change impact, drift control`

Current implementation and planned/deferred boundaries are separate states. Native
handoff readiness is not device implementation proof. Source or static evidence is
not runtime evidence.

## Risk controls

- No universal outbox is prescribed by this plan. Each side-effect is classified
  first by durability requirement and recovery contract.
- No module is marked complete when an important edge is unknown; the gap gets an
  owner, required evidence, next action, and review date.
- Dependency exceptions require an owner and review/expiry information.
- Cross-module changes must record downstream data, permissions, clients,
  workflows, side-effects, and evidence impact.
- Generated reports remain diagnostic and cannot become canonical truth by accident.

## Goal-pursuing readiness

The plan is ready when the preflight, source-reuse rules, serial dependencies, and
closeout gates are present. Goal pursuing may begin with child 01. No product code
change is implied by this preparation pass.

## Expected closeout

The final result should answer, for every module:

- What does it own?
- Which services and endpoints implement it?
- Which clients expose it?
- Which workflows and permissions govern it?
- Which data and external systems does it touch?
- What is durable versus process-local?
- What tests and runtime evidence exist?
- What remains unknown, deferred, or risky?

The closeout must preserve those unknowns rather than hiding them behind a higher
System Map coverage score.

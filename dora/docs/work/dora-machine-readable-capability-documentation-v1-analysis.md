# Machine-readable capability and documentation control analysis

## Observed baseline

Dora already has project-owned control primitives: `documentation_evidence`,
`system_map`, `workspace_inventory`, change routing, product/domain knowledge,
ProjectDoctor and project initialization. TheMuffinMan additionally has a rich
machine-readable product capability inventory.

Those primitives are insufficient as a shared standard today:

- `ControlContracts` checks only each control's kind and one non-empty top-level
  collection. It does not validate capability status, references, system-map
  edge endpoints, or documentation-evidence references.
- Dora's self system map has only core/read-model/Bridge topology. It omits the
  bundled IDC component and release-pinned consumer boundary.
- Dora's current IDC source, schemas, tests and work plans prove v0 behavior,
  but there is no single current IDC capability document or component README.
- Dora's verified IDC triage already distinguishes bounded delivery from broad
  research and greenfield discovery, and fails closed without current owner
  authorization. The rule is visible in the accelerated route, but is not yet a
  single machine-readable default-routing policy that future plans can cite.
- DoomsDayStorage and TheMuffinMan declare the existing controls, but their
  documentation-evidence and system-map records are thin and neither has a
  shared Dora capability-inventory contract.
- `ProjectInitializer` creates generic control files, but no capability
  inventory or component/documentation template. New projects therefore begin
  valid but do not begin with a usable machine-readable capability baseline.

## Authority model

The new inventory must not become a second delivery authority.

| Surface | Owns | Must not own |
| --- | --- | --- |
| Capability inventory | Current declared capability posture, links, gaps and evidence references | Decisions, task lifecycle, evidence truth or verified delivery state |
| System map | Declared topology and dependencies | Product status or permission truth |
| Documentation evidence | Claim-to-document traceability | Capability status or implementation proof |
| Dora DecisionLog/work plans/evidence | Decisions, delivery lifecycle and verification | Narrative capability summaries |
| AI-system component registry | Cross-component architecture and planned-component posture | Dora or consumer project facts, decisions, plans or evidence |

`verified` in a capability inventory is only permitted when it cites existing
project-owned verification evidence. The inventory may link it; it may never
create, overwrite or reinterpret it.

## Recommended minimal standard

1. Add a portable capability-inventory schema and a strict semantic validator.
2. Strengthen existing system-map and documentation-evidence validation rather
   than creating a parallel graph or documentation database.
3. Make `capability_inventory` an optional compatible project control for legacy
   projects, but scaffold it for every new project and require it for a project
   that explicitly adopts the standard.
4. Add a deterministic Doctor report for an adopted documentation-control set;
   legacy projects remain valid until their owner-approved adoption slice.
5. Self-adopt in Dora, including IDC as a Dora-owned component; publish a
   release-gated adoption contract; then let each owner-approved consumer and
   the private `ai-system` architecture root run its own project-local plan.
6. Add one machine-readable request-routing policy that maps bounded delivery,
   wide research, greenfield discovery, Master Plan preparation and goal
   pursuing to the verified existing Dora/IDC routes. It must be advisory at
   routing time: it cannot read sources, render IDC, promote conclusions or
   start implementation without the existing owner gates.

## Rollout boundary

The Dora execution inventory must not directly execute a task in `ai-system`,
`DoomsDayStorage` or `TheMuffinMan`. A Dora work item can only make Dora-owned
changes, and an external project needs its own baseline, worktree check,
owner-approved plan, evidence and rollback. The master therefore proves the
portable standard, Dora/IDC self-adoption and an adoption guide. It names the
three external follow-ons as explicit gates but does not pretend they are Dora
tasks or silently mark them adopted.

## Automatic default, not automatic execution

When Codex receives a request, the durable policy should require it to classify
the request before planning: a bounded delivery request stays on the existing
Dora route; broad research and greenfield discovery are an IDC candidate. This
classification is only a recommendation. The existing `IdcTriage` contract
continues to require a current-request explicit owner authorization before the
fixed local renderer can run. A Master Plan request always receives preflight,
child-plan review, atomic hardening and a serial inventory. Goal pursuing still
requires explicit owner authorization after the prepared plan is read back.

## Explicit exclusions

This program does not create an LLM documentation generator, vector index,
automatic source discovery, generic knowledge base, PC/CPPE runtime, Bridge
write path, consumer product implementation, or a second status/evidence store.

## Owner gates before implementation

- Confirm the proposed status vocabulary and the exact rule for `verified`.
- Confirm that legacy projects receive an explicit adoption warning/report rather
  than a breaking Doctor failure until they adopt the new control.
- Confirm the proposed v1.12.0 release version before the release task starts.
- Confirm the policy vocabulary before the request-routing slice starts; it must
  preserve current IDC triage and owner gates rather than imply standing or
  autonomous execution authority.
- Confirm each project-local adoption plan separately after Dora's portable
  contract is released.
- Confirm that PC and CPPE remain `planned` architecture entries only; this
  program must not authorize their implementation.

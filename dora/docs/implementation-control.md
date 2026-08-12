# Dora implementation control

This document owns Dora's plan-scope control policy.

- Dora work begins only after explicit owner approval and uses one bounded work item at a time.
- A strict master uses a serial execution inventory; each queued task has one observable outcome, exact required paths, a leaf validation command, and an evidence boundary.
- Existing verified behavior is baseline evidence, not new completion. New work must declare residual scope and concrete retest triggers.
- A task is verified only through the declared Dora work-verification workflow with passing recorded evidence.
- Advisory research, Bridge projections, handoffs, and derived context cannot create owner decisions, plans, task state, evidence, or verified status.
- Consumer repositories and private local workspaces remain outside Dora source-repository changes unless an owner explicitly approves a separate bounded task.
- Before planning, follow `docs/agent-request-routing-policy.yaml`: bounded delivery
  uses Dora directly; wide research and greenfield discovery use only owner-gated
  IDC triage. Every Master Plan receives preflight, child-plan review, atomic
  hardening, serial inventory and owner readback. Goal pursuing still requires
  explicit owner authorization after that readback.

The detailed required plan fields and review questions are defined in
`docs/plan-scope-control-standard.yaml`.

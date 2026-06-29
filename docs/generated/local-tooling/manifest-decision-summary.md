# Manifest Decision

- Changed files: `7`
- Decision: `required`
- Manifest required: `true`
- Skip allowed: `false`

- `invoice_critical_change` files=3 reason=Invoice-critical behavior needs explicit scope, validation, and residual-risk evidence.
- `agent_contract_change` files=2 reason=Agent-facing contracts and operating-model surfaces require manifest tracking.
- `workflow_expansion_change` files=4 reason=Workflow or scenario surfaces require scenario evidence and docs synchronization.
- `agent_tooling_change` files=6 reason=Agent, tooling, startup-routing, or closeout workflow changes need explicit evidence.

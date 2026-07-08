# Manifest Decision

- Changed files: `75`
- Decision: `required`
- Manifest required: `true`
- Skip allowed: `false`

- `multi_surface_change` files=75 reason=Changes touching three or more meaningful surfaces need manifest-backed closeout traceability.
- `invoice_critical_change` files=8 reason=Invoice-critical behavior needs explicit scope, validation, and residual-risk evidence.
- `agent_contract_change` files=9 reason=Agent-facing contracts and operating-model surfaces require manifest tracking.
- `workflow_expansion_change` files=50 reason=Workflow or scenario surfaces require scenario evidence and docs synchronization.
- `schema_or_generated_artifact_change` files=36 reason=Schema and generated-artifact changes require explicit generated-artifact evidence.
- `agent_tooling_change` files=7 reason=Agent, tooling, startup-routing, or closeout workflow changes need explicit evidence.

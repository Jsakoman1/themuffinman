# Manifest Decision

- Changed files: `303`
- Decision: `required`
- Manifest required: `true`
- Skip allowed: `false`

- `multi_surface_change` files=303 reason=Changes touching three or more meaningful surfaces need manifest-backed closeout traceability.
- `invoice_critical_change` files=12 reason=Invoice-critical behavior needs explicit scope, validation, and residual-risk evidence.
- `agent_contract_change` files=12 reason=Agent-facing contracts and operating-model surfaces require manifest tracking.
- `workflow_expansion_change` files=138 reason=Workflow or scenario surfaces require scenario evidence and docs synchronization.
- `frontend_contract_change` files=5 reason=Frontend contract or API surfaces require frontend validation evidence.
- `schema_or_generated_artifact_change` files=114 reason=Schema and generated-artifact changes require explicit generated-artifact evidence.
- `agent_tooling_change` files=13 reason=Agent, tooling, startup-routing, or closeout workflow changes need explicit evidence.
- `mixed_product_domains` files=303 reason=Multiple product domains in one changeset require explicit scope and residual-risk review.

# Manifest Decision

- Changed files: `373`
- Decision: `required`
- Manifest required: `true`
- Skip allowed: `false`

- `multi_file_multi_layer_change` files=373 reason=Multiple files across multiple change categories need a manifest for closeout traceability.
- `high_risk_or_executor_critical_surface` files=326 reason=Backend logic, automation safety, or validation tooling changes need explicit evidence.
- `agent_contract_change` files=22 reason=Agent-facing contracts and operating-model surfaces require manifest tracking.
- `workflow_expansion_change` files=98 reason=Workflow or scenario surfaces require scenario evidence and docs synchronization.
- `frontend_contract_change` files=11 reason=Frontend contract or API surfaces require frontend validation evidence.
- `schema_or_generated_artifact_change` files=186 reason=Schema and generated-artifact changes require explicit generated-artifact evidence.
- `mixed_product_domains` files=373 reason=Multiple product domains in one changeset require explicit scope and residual-risk review.

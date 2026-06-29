# Changeset Risk

- Changed files: `413`
- Score: `104`
- Risk tier: `high`

- `dto_or_model_contract_change` +18: DTO/model changes can affect API, frontend, persistence, or automation contracts. (`3`)
- `migration_or_schema_change` +18: Schema changes require migration and documentation review. (`3`)
- `service_workflow_or_permission_logic` +18: Service changes mention workflow, permission, validation, state, or visibility logic. (`13`)
- `frontend_contract_or_api_change` +12: Frontend API/contract surfaces can drift from backend contracts. (`2`)
- `agent_or_docs_contract_change` +12: Agent operating docs and sync policies affect automation safety. (`18`)
- `generated_artifact_churn` +10: Large generated artifact churn needs commit-scope review. (`199`)
- `mixed_product_domains` +10: Multiple product domains in one changeset increase review scope. (`10`)
- `tooling_or_infrastructure_change` +6: Tooling and infrastructure changes can affect validation behavior. (`44`)

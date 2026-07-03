# Change Impact Preflight

- Decision: `review`
- Why: guardrail warnings=5 files=114
- Next action: `./mvnw`, `build`, `generate-agent-artifacts`
- Evidence: docs=5, tests=36, generated=5

## Scope Guardrails

- `mixed_product_domains`: `warn`
- `runtime_plus_tooling_or_infrastructure`: `warn`
- `large_generated_report_churn`: `warn`

- `.agents/feature-manifests/agent-control-phase-two-manifest.yaml` `other` `unknown`
- `.agents/feature-manifests/agent-operating-refactor-manifest.yaml` `other` `unknown`
- `.agents/feature-manifests/agent-safety-enforcement-round2-manifest.yaml` `other` `unknown`
- `.agents/feature-manifests/agent-safety-upgrade-manifest.yaml` `other` `unknown`
- `.agents/feature-manifests/backend-audit-domain-tagging-manifest.yaml` `other` `unknown`
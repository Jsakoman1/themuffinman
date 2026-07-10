# Doc Sync Required Surfaces


## Details

- Changed File Count: 88
- Original File Count: 315
- Filtered File Count: 227
- Excluded File Count: 227
- Required docs: docs/agent-operating-model.md | docs/agent-operating-model.yaml | docs/business-logic.md | docs/codex-local-tooling-todo.md | docs/domain-technical.md
- Required docs more: 1
- Required generated artifacts: docs/generated/local-tooling/codex-context/latest.machine.json | docs/generated/local-tooling/codex-context/latest.review.md | docs/generated/local-tooling/manifest-path-resolution.json | docs/tooling/codex-local-audits.yml
- Required validation commands: make audit-api-contract-drift | make audit-async-mutation-flow | make audit-doc-canonical-phrases | make audit-documentation
- Required validation commands more: 10
- Recommended audits: make audit-read-surface-inventory | make audit-repository-fetch | make audit-mapper-usage | make audit-api-contract-drift | make audit-endpoint-callsite-linker
- Recommended audits more: 11
- Files: file: .agents/feature-manifests/chat-prod-ready-manifest.yaml | category: other | domain: shared | likely_docs: docs/feature-delivery-workflow.md | generated_artifacts: docs/generated/local-tooling/manifest-path-resolution.json | residual_risk: Agent-facing logic changes may also require manifest and closeout updates. | file: .agents/templates/feature-implementation-plan.template.md | category: other | domain: shared | likely_docs: docs/feature-delivery-workflow.md | generated_artifacts: docs/generated/local-tooling/manifest-path-resolution.json | residual_risk: Agent-facing logic changes may also require manifest and closeout updates.
- Files more: 86

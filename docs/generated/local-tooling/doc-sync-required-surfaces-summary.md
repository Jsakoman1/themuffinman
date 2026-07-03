# Doc Sync Required Surfaces


## Details

- Changed File Count: 25
- Original File Count: 235
- Filtered File Count: 210
- Excluded File Count: 210
- Required docs: docs/agent-operating-model.md | docs/agent-operating-model.yaml | docs/codex-local-tooling-todo.md | docs/domain-technical.md | docs/feature-delivery-workflow.md
- Required generated artifacts: docs/generated/local-tooling/codex-context/latest.review.md | docs/generated/local-tooling/codex-context/latest.machine.json | docs/generated/local-tooling/manifest-path-resolution.json | docs/tooling/codex-local-audits.yml
- Required validation commands: make audit-doc-canonical-phrases | make audit-documentation | make audit-summary-index
- Required validation commands more: 2
- Recommended audits: make audit-doc-sync-preflight | make audit-doc-sync-required-surfaces | make audit-documentation | make audit-doc-canonical-phrases | make audit-test-gap-recommendations
- Recommended audits more: 1
- Files: file: docs/agent-operating-model.md | category: docs | domain: agent | likely_docs: docs/agent-operating-model.md, docs/agent-operating-model.yaml, docs/domain-technical.md | generated_artifacts: docs/generated/local-tooling/manifest-path-resolution.json | validation_commands: ./mvnw test, make audit-documentation, make audit-doc-canonical-phrases | residual_risk: Agent-facing logic changes may also require manifest and closeout updates. | file: docs/change-completion-checklist.md | category: docs | domain: shared | likely_docs: docs/agent-operating-model.yaml | generated_artifacts: docs/generated/local-tooling/manifest-path-resolution.json | validation_commands: ./mvnw test, make audit-documentation, make audit-doc-canonical-phrases | residual_risk: Agent-facing logic changes may also require manifest and closeout updates.
- Files more: 23

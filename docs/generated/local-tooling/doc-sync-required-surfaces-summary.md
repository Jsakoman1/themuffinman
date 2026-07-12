# Doc Sync Required Surfaces


## Details

- Changed File Count: 8
- Original File Count: 8
- Filtered File Count: 0
- Excluded File Count: 0
- Required docs: docs/agent-operating-model.md | docs/agent-operating-model.yaml | docs/codex-local-tooling-todo.md | docs/domain-technical.md | docs/feature-delivery-workflow.md
- Required generated artifacts: docs/generated/local-tooling/codex-context/latest.machine.json | docs/generated/local-tooling/codex-context/latest.review.md | docs/generated/local-tooling/manifest-path-resolution.json | docs/tooling/codex-local-audits.yml
- Required validation commands: make audit-doc-canonical-phrases | make audit-documentation | make audit-summary-index
- Required validation commands more: 2
- Recommended audits: make audit-doc-sync-preflight | make audit-doc-sync-required-surfaces | make audit-documentation | make audit-doc-canonical-phrases | make audit-test-gap-recommendations
- Recommended audits more: 1
- Files: file: Makefile | category: other | domain: shared | generated_artifacts: docs/tooling/codex-local-audits.yml | residual_risk: No likely docs were resolved for this path., Script or Makefile changes can invalidate generated audit registry outputs. | file: scripts/audits/audit-plan-completion.rb | category: script | domain: shared | likely_docs: docs/codex-local-tooling-todo.md, docs/feature-delivery-workflow.md | generated_artifacts: docs/generated/local-tooling/codex-context/latest.machine.json, docs/generated/local-tooling/codex-context/latest.review.md, docs/tooling/codex-local-audits.yml | validation_commands: ruby -c <script>, make audit-summary-index | residual_risk: Script or Makefile changes can invalidate generated audit registry outputs.
- Files more: 6

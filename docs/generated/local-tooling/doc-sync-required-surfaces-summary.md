# Doc Sync Required Surfaces

- Generated At: `2026-06-29T15:49:29Z`
- Original File Count: `63`
- Filtered File Count: `42`
- Excluded File Count: `42`
## `excluded_files_sample`

- `{:path: "docs/generated/local-tooling/.cache/audit-inputs.json", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/local-tooling/audit-deltas/diff-summary-summary.md", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/local-tooling/audit-deltas/diff-summary.json", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/local-tooling/audit-registry-artifacts-summary.md", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/local-tooling/audit-registry-artifacts.json", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/local-tooling/audit-summary-index.json", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/local-tooling/audit-summary-index.md", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/local-tooling/hotspots-summary.md", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/local-tooling/hotspots.json", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/local-tooling/targeted-tests-summary.md", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/local-tooling/targeted-tests.json", :excluded: true, :reasons: ["generated"]}`
- `{:path: ".agents/codex-context-gateway-closeout-plan.md", :excluded: true, :reasons: ["agent_transient"]}`
- `{:path: ".agents/codex-context-gateway-core-plan.md", :excluded: true, :reasons: ["agent_transient"]}`
- `{:path: ".agents/codex-context-gateway-integration-plan.md", :excluded: true, :reasons: ["agent_transient"]}`
- `{:path: ".agents/codex-context-optimization-closeout-plan.md", :excluded: true, :reasons: ["agent_transient"]}`
- `{:path: ".agents/codex-context-optimization-core-plan.md", :excluded: true, :reasons: ["agent_transient"]}`
- `{:path: ".agents/codex-context-optimization-master-plan.md", :excluded: true, :reasons: ["agent_transient"]}`
- `{:path: ".agents/codex-local-context-gateway-master-plan.md", :excluded: true, :reasons: ["agent_transient"]}`
- `{:path: ".agents/validation-evidence/codex_context_optimization.yaml", :excluded: true, :reasons: ["agent_transient"]}`
- `{:path: "docs/generated/local-tooling/.history/audit-deltas/diff-summary/2026-06-29T15-07-09Z.json", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/local-tooling/.history/audit-deltas/diff-summary/2026-06-29T15-10-38Z.json", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/local-tooling/.history/audit-registry-artifacts/2026-06-29T15-07-36Z.json", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/local-tooling/.history/audit-registry-artifacts/2026-06-29T15-47-37Z.json", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/local-tooling/.history/audit-registry-artifacts/2026-06-29T15-49-24Z.json", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/local-tooling/.history/audit-summary-index/2026-06-29T15-07-36Z.json", :excluded: true, :reasons: ["generated"]}`

- Changed File Count: `21`
## `files`

- `{:file: ".gitignore", :category: "other", :domain: "shared", :likely_docs: [], :generated_artifacts: [], :validation_commands: [], :residual_risk: ["No likely docs were resolved for this path."]}`
- `{:file: "AGENTS.md", :category: "other", :domain: "shared", :likely_docs: [], :generated_artifacts: [], :validation_commands: [], :residual_risk: ["No likely docs were resolved for this path."]}`
- `{:file: "Makefile", :category: "other", :domain: "shared", :likely_docs: [], :generated_artifacts: ["docs/tooling/codex-local-audits.yml"], :validation_commands: [], :residual_risk: ["No likely docs were resolved for this path.", "Script or Makefile changes can invalidate generated audit registry outputs."]}`
- `{:file: "docs/agent-operating-model.md", :category: "docs", :domain: "agent", :likely_docs: ["docs/agent-operating-model.md", "docs/agent-operating-model.yaml", "docs/domain-technical.md"], :generated_artifacts: ["docs/generated/local-tooling/manifest-path-resolution.json"], :validation_commands: ["./mvnw test", "make audit-documentation", "make audit-doc-canonical-phrases"], :residual_risk: ["Agent-facing logic changes may also require manifest and closeout updates."]}`
- `{:file: "docs/change-completion-checklist.md", :category: "docs", :domain: "shared", :likely_docs: ["docs/agent-operating-model.yaml"], :generated_artifacts: ["docs/generated/local-tooling/manifest-path-resolution.json"], :validation_commands: ["./mvnw test", "make audit-documentation", "make audit-doc-canonical-phrases"], :residual_risk: ["Agent-facing logic changes may also require manifest and closeout updates."]}`
- `{:file: "docs/codex-local-tooling-todo.md", :category: "docs", :domain: "shared", :likely_docs: ["docs/feature-delivery-workflow.md", "docs/agent-operating-model.yaml"], :generated_artifacts: ["docs/generated/local-tooling/manifest-path-resolution.json"], :validation_commands: ["./mvnw test", "make audit-documentation", "make audit-doc-canonical-phrases"], :residual_risk: ["Agent-facing logic changes may also require manifest and closeout updates."]}`
- `{:file: "docs/documentation-sync-policy.md", :category: "docs", :domain: "shared", :likely_docs: ["docs/agent-operating-model.yaml"], :generated_artifacts: ["docs/generated/local-tooling/manifest-path-resolution.json"], :validation_commands: ["./mvnw test", "make audit-documentation", "make audit-doc-canonical-phrases"], :residual_risk: ["Agent-facing logic changes may also require manifest and closeout updates."]}`
- `{:file: "docs/tooling/codex-local-audits.yml", :category: "docs", :domain: "shared", :likely_docs: ["docs/feature-delivery-workflow.md", "docs/agent-operating-model.yaml"], :generated_artifacts: ["docs/generated/local-tooling/manifest-path-resolution.json"], :validation_commands: ["./mvnw test", "make audit-documentation", "make audit-doc-canonical-phrases"], :residual_risk: ["Agent-facing logic changes may also require manifest and closeout updates."]}`
- `{:file: "docs/validation-evidence.schema.json", :category: "docs", :domain: "shared", :likely_docs: ["docs/agent-operating-model.yaml"], :generated_artifacts: ["docs/generated/local-tooling/manifest-path-resolution.json"], :validation_commands: ["./mvnw test", "make audit-documentation", "make audit-doc-canonical-phrases"], :residual_risk: ["Agent-facing logic changes may also require manifest and closeout updates."]}`
- `{:file: "scripts/audits/local_tooling_extended_tools.rb", :category: "script", :domain: "shared", :likely_docs: ["docs/codex-local-tooling-todo.md", "docs/feature-delivery-workflow.md"], :generated_artifacts: ["docs/generated/local-tooling/codex-context/latest.machine.json", "docs/generated/local-tooling/codex-context/latest.human.md", "docs/tooling/codex-local-audits.yml"], :validation_commands: ["ruby -c <script>", "make audit-summary-index"], :residual_risk: ["Script or Makefile changes can invalidate generated audit registry outputs."]}`
- `{:file: "scripts/audits/record-validation-evidence.rb", :category: "script", :domain: "shared", :likely_docs: ["docs/codex-local-tooling-todo.md", "docs/feature-delivery-workflow.md"], :generated_artifacts: ["docs/generated/local-tooling/codex-context/latest.machine.json", "docs/generated/local-tooling/codex-context/latest.human.md", "docs/tooling/codex-local-audits.yml"], :validation_commands: ["ruby -c <script>", "make audit-summary-index"], :residual_risk: ["Script or Makefile changes can invalidate generated audit registry outputs."]}`
- `{:file: "scripts/local_tooling_common.rb", :category: "script", :domain: "shared", :likely_docs: ["docs/codex-local-tooling-todo.md", "docs/feature-delivery-workflow.md"], :generated_artifacts: ["docs/generated/local-tooling/codex-context/latest.machine.json", "docs/generated/local-tooling/codex-context/latest.human.md", "docs/tooling/codex-local-audits.yml"], :validation_commands: ["ruby -c <script>", "make audit-summary-index"], :residual_risk: ["Script or Makefile changes can invalidate generated audit registry outputs."]}`
- `{:file: ".agents/codex-local-context-gateway-analysis-context.md", :category: "other", :domain: "shared", :likely_docs: ["docs/feature-delivery-workflow.md"], :generated_artifacts: ["docs/generated/local-tooling/manifest-path-resolution.json"], :validation_commands: [], :residual_risk: ["Agent-facing logic changes may also require manifest and closeout updates."]}`
- `{:file: ".agents/feature-manifests/codex-context-optimization-manifest.yaml", :category: "other", :domain: "shared", :likely_docs: ["docs/feature-delivery-workflow.md"], :generated_artifacts: ["docs/generated/local-tooling/codex-context/latest.machine.json", "docs/generated/local-tooling/codex-context/latest.human.md", "docs/generated/local-tooling/manifest-path-resolution.json"], :validation_commands: [], :residual_risk: ["Agent-facing logic changes may also require manifest and closeout updates."]}`
- `{:file: "docs/feature-delivery-workflow.md", :category: "docs", :domain: "shared", :likely_docs: ["docs/agent-operating-model.yaml"], :generated_artifacts: ["docs/generated/local-tooling/manifest-path-resolution.json"], :validation_commands: ["./mvnw test", "make audit-documentation", "make audit-doc-canonical-phrases"], :residual_risk: ["Agent-facing logic changes may also require manifest and closeout updates."]}`
- `{:file: "scripts/audits/CodexJavaAstContext.java", :category: "script", :domain: "shared", :likely_docs: ["docs/codex-local-tooling-todo.md", "docs/feature-delivery-workflow.md"], :generated_artifacts: ["docs/generated/local-tooling/codex-context/latest.machine.json", "docs/generated/local-tooling/codex-context/latest.human.md", "docs/tooling/codex-local-audits.yml"], :validation_commands: ["ruby -c <script>", "make audit-summary-index"], :residual_risk: ["Script or Makefile changes can invalidate generated audit registry outputs."]}`
- `{:file: "scripts/audits/audit-doc-sync-required-surfaces.rb", :category: "script", :domain: "shared", :likely_docs: ["docs/codex-local-tooling-todo.md", "docs/feature-delivery-workflow.md"], :generated_artifacts: ["docs/generated/local-tooling/codex-context/latest.machine.json", "docs/generated/local-tooling/codex-context/latest.human.md", "docs/tooling/codex-local-audits.yml"], :validation_commands: ["ruby -c <script>", "make audit-summary-index"], :residual_risk: ["Script or Makefile changes can invalidate generated audit registry outputs."]}`
- `{:file: "scripts/audits/autofill-feature-closeout.rb", :category: "script", :domain: "shared", :likely_docs: ["docs/codex-local-tooling-todo.md", "docs/feature-delivery-workflow.md"], :generated_artifacts: ["docs/generated/local-tooling/codex-context/latest.machine.json", "docs/generated/local-tooling/codex-context/latest.human.md", "docs/tooling/codex-local-audits.yml"], :validation_commands: ["ruby -c <script>", "make audit-summary-index"], :residual_risk: ["Script or Makefile changes can invalidate generated audit registry outputs."]}`
- `{:file: "scripts/audits/codex-context.rb", :category: "script", :domain: "shared", :likely_docs: ["docs/codex-local-tooling-todo.md", "docs/feature-delivery-workflow.md"], :generated_artifacts: ["docs/generated/local-tooling/codex-context/latest.machine.json", "docs/generated/local-tooling/codex-context/latest.human.md", "docs/tooling/codex-local-audits.yml"], :validation_commands: ["ruby -c <script>", "make audit-summary-index"], :residual_risk: ["Script or Makefile changes can invalidate generated audit registry outputs."]}`
- `{:file: "scripts/audits/codex_ast_context.mjs", :category: "script", :domain: "shared", :likely_docs: ["docs/codex-local-tooling-todo.md", "docs/feature-delivery-workflow.md"], :generated_artifacts: ["docs/generated/local-tooling/codex-context/latest.machine.json", "docs/generated/local-tooling/codex-context/latest.human.md", "docs/tooling/codex-local-audits.yml"], :validation_commands: ["ruby -c <script>", "make audit-summary-index"], :residual_risk: ["Script or Makefile changes can invalidate generated audit registry outputs."]}`
- `{:file: "scripts/audits/codex_local_context_gateway.rb", :category: "script", :domain: "shared", :likely_docs: ["docs/codex-local-tooling-todo.md", "docs/feature-delivery-workflow.md"], :generated_artifacts: ["docs/generated/local-tooling/codex-context/latest.machine.json", "docs/generated/local-tooling/codex-context/latest.human.md", "docs/tooling/codex-local-audits.yml"], :validation_commands: ["ruby -c <script>", "make audit-summary-index"], :residual_risk: ["Script or Makefile changes can invalidate generated audit registry outputs."]}`

## `required_docs`

- `docs/agent-operating-model.md`
- `docs/agent-operating-model.yaml`
- `docs/codex-local-tooling-todo.md`
- `docs/domain-technical.md`
- `docs/feature-delivery-workflow.md`

## `required_generated_artifacts`

- `docs/generated/local-tooling/codex-context/latest.human.md`
- `docs/generated/local-tooling/codex-context/latest.machine.json`
- `docs/generated/local-tooling/manifest-path-resolution.json`
- `docs/tooling/codex-local-audits.yml`

## `required_validation_commands`

- `./mvnw test`
- `make audit-doc-canonical-phrases`
- `make audit-documentation`
- `make audit-summary-index`
- `ruby -c <script>`

## `recommended_audits`

- `make audit-doc-sync-preflight`
- `make audit-doc-sync-required-surfaces`
- `make audit-documentation`
- `make audit-doc-canonical-phrases`
- `make audit-test-gap-recommendations`
- `make audit-summary-index`

- `manifest_decision`: `8` entries
- `manifest_resolution`: `7` entries
- `validation_preset`: `9` entries
## `residual_risk`

- `Agent-facing logic changes may also require manifest and closeout updates.`
- `No likely docs were resolved for this path.`
- `Script or Makefile changes can invalidate generated audit registry outputs.`


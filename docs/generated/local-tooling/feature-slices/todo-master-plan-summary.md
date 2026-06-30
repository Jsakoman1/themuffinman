# Feature Slices todo-master-plan

- Generated At: `2026-06-30T11:20:21Z`
- Topic: `todo-master-plan`
## `domains`

- `agent`
- `shared`

## `categories`

- `docs`
- `script`

- Original File Count: `25`
- Filtered File Count: `0`
## `files_considered`

- `docs/agent-operating-model.md`
- `docs/change-completion-checklist.md`
- `docs/codex-fast-path.md`
- `docs/documentation-sync-policy.md`
- `docs/feature-delivery-workflow.md`
- `scripts/audits/CodexJavaAstContext.java`
- `scripts/audits/audit-api-contract-drift.rb`
- `scripts/audits/audit-change-impact-preflight.rb`
- `... 17 more`

## `slices`

- `{:id: "docs-and-artifacts", :purpose: "Update living docs and generated artifacts that move with the implementation.", :files: ["docs/agent-operating-model.md", "docs/change-completion-checklist.md", "docs/codex-fast-path.md", "docs/documentation-sync-policy.md", "docs/feature-delivery-workflow.md", "docs/codex-context-execution-manifest.schema.json", "docs/agent-operating-model.yaml", "docs/domain-technical.md"], :validation: ["make audit-documentation", "make audit-generated-artifact-freshness"]}`
- `{:id: "final-validation", :purpose: "Run focused and broad validation after implementation slices are complete.", :files: [], :validation: ["./mvnw test", "make audit-documentation", "make audit-doc-canonical-phrases", "ruby -c <script>", "make audit-summary-index"]}`

## `read_next`

- `Run `make context-pack topic=todo-master-plan` before editing if more file context is needed.`
- `Run `make audit-router files=<csv>` after the first implementation slice.`
- `Keep slices sequential; avoid mixing backend, frontend, generated artifacts, and final validation in one edit pass unless the change is tiny.`


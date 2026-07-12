# Feature Slices implementation-closeout-operability

- Generated At: `2026-07-12T09:17:24Z`
- Topic: `implementation-closeout-operability`
## `domains`

- `agent`
- `shared`

## `categories`

- `docs`
- `other`
- `script`

- Original File Count: `8`
- Filtered File Count: `0`
## `files_considered`

- `Makefile`
- `scripts/audits/audit-plan-completion.rb`
- `scripts/audits/test-audit-plan-completion.rb`
- `scripts/feature-closeout-audit.sh`
- `scripts/implementation-batch.sh`
- `.agents/templates/master-plan.template.md`
- `docs/codex-fast-path.md`
- `docs/agent-operating-model/sections/policies.yaml`

## `slices`

- `{:id: "docs-and-artifacts", :purpose: "Update living docs and generated artifacts that move with the implementation.", :files: ["docs/codex-fast-path.md", "docs/agent-operating-model/sections/policies.yaml", "docs/agent-operating-model.md", "docs/agent-operating-model.yaml", "docs/domain-technical.md"], :validation: ["make audit-documentation", "make audit-generated-artifact-freshness"]}`
- `{:id: "final-validation", :purpose: "Run focused and broad validation after implementation slices are complete.", :files: [], :validation: ["ruby -c <script>", "make audit-summary-index", "./mvnw test", "make audit-documentation", "make audit-doc-canonical-phrases"]}`

## `read_next`

- `Run `make context-pack topic=implementation-closeout-operability` before editing if more file context is needed.`
- `Run `make audit-router files=<csv>` after the first implementation slice.`
- `Keep slices sequential; avoid mixing backend, frontend, generated artifacts, and final validation in one edit pass unless the change is tiny.`


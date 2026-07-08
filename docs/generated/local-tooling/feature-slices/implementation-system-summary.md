# Feature Slices implementation-system

- Generated At: `2026-07-07T16:32:21Z`
- Topic: `implementation-system`
## `domains`

- `agent`
- `shared`

## `categories`

- `docs`
- `other`
- `script`

- Original File Count: `18`
- Filtered File Count: `0`
## `files_considered`

- `.agents/implementation-system-improvement-master-plan-next.md`
- `.agents/templates/feature-implementation-plan.template.md`
- `.agents/templates/god-plan.template.yaml`
- `.agents/templates/master-plan.template.md`
- `AGENTS.md`
- `apps/themuffinman/src/test/java/com/themuffinman/app/docs/AgentOperatingModelValidationTest.java`
- `docs/agent-operating-model.md`
- `docs/agent-operating-model.yaml`
- `... 10 more`

## `slices`

- `{:id: "docs-and-artifacts", :purpose: "Update living docs and generated artifacts that move with the implementation.", :files: ["docs/agent-operating-model.md", "docs/agent-operating-model.yaml", "docs/agent-operating-model/sections/documentation_sync.yaml", "docs/agent-operating-model/sections/policies.yaml", "docs/change-completion-checklist.md", "docs/codex-fast-path.md", "docs/documentation-sync-policy.md", "docs/feature-delivery-workflow.md", "docs/program-planning-model.md", "docs/domain-technical.md"], :validation: ["make audit-documentation", "make audit-generated-artifact-freshness"]}`
- `{:id: "final-validation", :purpose: "Run focused and broad validation after implementation slices are complete.", :files: [], :validation: ["./mvnw test", "make audit-documentation", "make audit-doc-canonical-phrases", "ruby -c <script>", "make audit-summary-index"]}`

## `read_next`

- `Run `make context-pack topic=implementation-system` before editing if more file context is needed.`
- `Run `make audit-router files=<csv>` after the first implementation slice.`
- `Keep slices sequential; avoid mixing backend, frontend, generated artifacts, and final validation in one edit pass unless the change is tiny.`


# Targeted Tests

- Generated At: `2026-06-29T15:10:33Z`
- Original File Count: `8`
- Filtered File Count: `0`
- Excluded File Count: `0`
## `files_considered`

- `.gitignore`
- `Makefile`
- `docs/codex-local-tooling-todo.md`
- `docs/tooling/codex-local-audits.yml`
- `scripts/audits/local_tooling_extended_tools.rb`
- `scripts/local_tooling_common.rb`
- `scripts/audits/codex-context.rb`
- `scripts/audits/codex_local_context_gateway.rb`

## `domains`

- `shared`

## `categories`

- `docs`
- `other`
- `script`

## `direct_tests`


## `recommended_commands`

- `{:command: "make audit-documentation", :reason: "Docs, plans, or agent artifacts changed.", :confidence: "high", :covers: ["docs/codex-local-tooling-todo.md", "docs/tooling/codex-local-audits.yml"], :uncovered: []}`
- `{:command: "make audit-doc-canonical-phrases", :reason: "Protected documentation wording may be affected by docs or agent-safety edits.", :confidence: "medium", :covers: ["docs/codex-local-tooling-todo.md", "docs/tooling/codex-local-audits.yml"], :uncovered: ["Does not validate Java-side agent operating model tests."]}`
- `{:command: "make audit-generated-artifact-freshness", :reason: "Generated artifacts, generation scripts, or Make targets changed.", :confidence: "high", :covers: ["Makefile", "scripts/audits/local_tooling_extended_tools.rb", "scripts/local_tooling_common.rb", "scripts/audits/codex-context.rb", "scripts/audits/codex_local_context_gateway.rb"], :uncovered: []}`
- `{:command: "make audit-generated-commit-scope", :reason: "Classifies changed generated artifacts before closeout.", :confidence: "medium", :covers: [], :uncovered: ["Advisory only; reviewer still chooses which generated files belong in the changeset."]}`

## `residual_risk`


## `notes`

- `This is a targeted recommendation report, not a replacement for full validation.`
- `Use full `cd apps/themuffinman && ./mvnw test` for high-risk backend behavior, schema, or broad cross-domain changes.`


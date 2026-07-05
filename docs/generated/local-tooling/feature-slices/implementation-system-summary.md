# Feature Slices implementation-system

- Generated At: `2026-07-05T09:22:55Z`
- Topic: `implementation-system`
## `domains`

- `shared`

## `categories`

- `other`
- `script`

- Original File Count: `5`
- Filtered File Count: `0`
## `files_considered`

- `Makefile`
- `scripts/audits/audit-generated-artifact-freshness.rb`
- `scripts/implementation-batch.sh`
- `scripts/audits/local_tooling_extended_tools.rb`
- `scripts/audits/audit-plan-completion.rb`

## `slices`

- `{:id: "docs-and-artifacts", :purpose: "Update living docs and generated artifacts that move with the implementation.", :files: [], :validation: ["make audit-documentation", "make audit-generated-artifact-freshness"]}`
- `{:id: "final-validation", :purpose: "Run focused and broad validation after implementation slices are complete.", :files: [], :validation: ["ruby -c <script>", "make audit-summary-index"]}`

## `read_next`

- `Run `make context-pack topic=implementation-system` before editing if more file context is needed.`
- `Run `make audit-router files=<csv>` after the first implementation slice.`
- `Keep slices sequential; avoid mixing backend, frontend, generated artifacts, and final validation in one edit pass unless the change is tiny.`


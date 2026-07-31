# IntelliJ MCP fast path

Use this alongside [Codex Fast Path](codex-fast-path.md). IntelliJ MCP provides
semantic discovery and diagnostics; repository tools provide deterministic validation
and the work verifier remains the only completion authority.

1. Start with `make context-search q="phrase"` using `symbol`, `callsite`, or
   `canonical` mode when the task is specific.
2. For a resolved callable, use IDEA call hierarchy instead of a broad usage search.
3. Use IDEA file problems, lint, and rename only with an exact project/file/symbol
   target. Bound every result.
4. If IDEA cannot resolve a symbol or has no active run configuration/debug session,
   use the stated local fallback immediately; do not repeatedly probe the IDE.
5. Use `make change-validation paths="..."` to choose candidate leaf commands.
6. Run the task's declared leaf validation through `make work-verify`; an IDEA build,
   inspection, or debugger result does not verify the task.

For tooling-only maintenance, `ruby scripts/tool-self-test.rb --tooling-only` checks
the repository mechanics without hiding the separately labeled product-contract stages
that run in the full self-test.

## Shared run configurations

The repository provides five shared shell runs under `.run/`: backend start, backend
tests, and frontend dev, type-check, and build. Use them for local execution after
IntelliJ discovers them; their XML is checked by `make audit-intellij-mcp-routing`.
The files prove configuration shape only. Record an independent IDEA run-configuration
observation when an IDE execution or discovery claim matters.

The complete machine-readable routing and fallback contract is
[`intellij-mcp-tool-routing.yaml`](intellij-mcp-tool-routing.yaml).

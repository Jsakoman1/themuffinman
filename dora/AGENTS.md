# Dora project entrypoint

Read these sources before changing Dora itself:

1. `.dora/agent-project-profile.yaml` — authority limits and project commands.
2. `docs/product-brief.yaml` — Dora product intent and explicit open decisions.
3. `docs/domain-library.yaml` — Dora domain vocabulary, invariants, and acceptance intent.

Use one bounded work item at a time. Record passing leaf validation before treating work as verified. Dora Bridge V1 is read-only: do not add bridge writes, source retrieval, shell execution, or direct Codex invocation without a separately approved program.

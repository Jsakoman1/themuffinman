# Dora Bridge V2.1 collaboration handoff preflight

## Discovery findings

- Dora’s declared project root is `dora/`. The earlier V2 work records are under the parent checkout’s `docs/work/`, so `bin/dora work-verify .dora/project.yaml` cannot resolve them. They remain historical/provisional records and are not used as V2.1 verifier authority.
- Dora’s work verifier initially compared Git-worktree-relative paths with plan paths relative to the declared project root. In a nested project this made valid project-relative changes invisible to strict verification. V2.1 first repairs that normalization and adds an isolated nested-project regression test.
- V2 already has the reusable concepts needed for delivery linkage (`master_plan`, `work_plan`, task evidence), append-only lifecycle events, idempotency, and immutable `supersedes` / `follows_up` references. V2.1 must extend those records instead of introducing a second plan, task, or evidence store.
- `list_handoffs` currently returns an array as MCP `structuredContent`. MCP structured content must be object-shaped when an output schema is supplied; this explains the observed ChatGPT structured-content validation failure. `get_next_handoff` can likewise return `null`, which is not a stable structured object.
- The existing fields describe a goal and constraints but do not distinguish locked owner decisions from unresolved decisions or give Codex a deterministic stop instruction. The smallest useful addition is one bounded immutable `brief` object, not additional MCP tools.

## V2.1 decision

Keep the V2 tool set and owner-local execution gate. Add a required `brief` to newly created implementation handoffs with:

- request mode and expected result;
- locked decisions, non-goals, relevant context, stop conditions, and required verification;
- explicit unresolved owner decisions.

`owner_decision_required` handoffs are blocked by the local claim boundary with a deterministic sanitized reason. They cannot become active Codex implementation work. Corrections use the existing immutable `follows_up` / `supersedes` references, now validated against an existing handoff in the same project.

V2 handoff read tools expose object-shaped `structuredContent` and an output schema. `list_handoffs` returns `{handoffs: [...]}` and `get_next_handoff` returns `{handoff: record-or-null}`. V1 tool definitions and response contracts are unchanged.

## Security boundary

The brief accepts bounded text only; none of its fields are a path, command, executable instruction, project selector, or lifecycle authority. The registry remains the sole project allow-list, state remains owner-local, and no MCP tool starts, steers, or controls Codex.

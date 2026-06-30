# CODEX-JAVA-AST-CONTEXT-TIGHTENING Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `medium`
Master order: 98 of 99

## Backlog Item

Tighten Java context extraction so the local AST pack returns clearer spans, better symbols, and fewer heuristic misses on changed Java files.

## Source Findings

- [`scripts/audits/CodexJavaAstContext.java`](/Users/jsakoman/Desktop/themuffinman/scripts/audits/CodexJavaAstContext.java)
- [`scripts/audits/codex_ast_context.mjs`](/Users/jsakoman/Desktop/themuffinman/scripts/audits/codex_ast_context.mjs)
- [`scripts/audits/codex_local_context_gateway.rb`](/Users/jsakoman/Desktop/themuffinman/scripts/audits/codex_local_context_gateway.rb)

## Implementation Plan

- [x] Inspect the current Java parser-backed span builder and its fallback paths.
- [x] Improve symbol naming and span selection for nested types, methods, and fields.
- [x] Keep the JSON shape stable so the gateway can keep consuming the pack without changes.
- [x] Re-run the Java-related context flow on representative backend files.

## Expected Validation

- `make codex-context topic=java-ast`
- `make audit-plan-completion plan=.agents/todo-plans/98-codex-local-java-ast-context-tightening.md`
- `make audit-todo`

## Completion Evidence

- Status: complete
- Backlog update: complete.
- Residual risk: Java AST changes should stay compatible with the gateway's pack consumer shape.

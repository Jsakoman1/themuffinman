# Codex Context Optimization Core Plan

## Goals

- [x] Introduce one shared changeset snapshot for gateway and local tooling.
- [x] Replace heuristic Java AST diff extraction with parser-backed output.
- [x] Upgrade gateway relevance scoring to evidence-driven ranking and downgrade decisions.
- [x] Add deterministic required-surface doc-sync reporting that reuses existing ownership and validation logic.

## Implementation Tasks

- [x] Add reusable changeset snapshot helpers to shared local-tooling code.
- [x] Refactor gateway diff, AST, hotspot, targeted-test, and related packs to reuse the shared snapshot.
- [x] Extend `scripts/audits/codex_ast_context.mjs` to parse Java changed symbols.
- [x] Update gateway AST collection and coverage metadata for the new Java parser-backed output.
- [x] Add evidence-driven relevance scoring inputs and apply them during composition.
- [x] Add or extend a local audit/helper for required doc/generated-artifact/validation surfaces.
- [x] Expose any new helper entrypoints through the registry and `Makefile` if needed.

## Validation

- [x] Run syntax validation for modified Ruby and Node scripts.
- [x] Run focused gateway generation and explain commands.
- [x] Run focused doc-sync helper generation if a new report is added.

## Completion Evidence

- Status: complete
- Validation evidence:
  - `ruby -c scripts/local_tooling_common.rb`
  - `ruby -c scripts/audits/local_tooling_extended_tools.rb`
  - `ruby -c scripts/audits/codex_local_context_gateway.rb`
  - `node --check scripts/audits/codex_ast_context.mjs`
  - `javac -d /tmp/codex-java-ast-check scripts/audits/CodexJavaAstContext.java`
  - `make generate-audit-registry-artifacts`
  - `make audit-doc-sync-required-surfaces`
  - `make codex-context`
  - `make codex-context-explain`
  - direct Java parser smoke via `scripts/audits/codex_ast_context.mjs`
  - `make generate-agent-artifacts`

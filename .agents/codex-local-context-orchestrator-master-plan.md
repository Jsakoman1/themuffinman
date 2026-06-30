# Codex Local Context Orchestrator Master Plan

Purpose: tighten the local Codex context and audit workflow so the repo can answer more work with fewer tokens, less repetition, and better cache reuse.

## Goal

Turn the existing local context gateway into a more decisive orchestration layer that:

- builds a one-shot context chain for the common read-first workflow
- reuses unchanged local context packs instead of recomputing them
- reduces duplicated workflow guidance across the main Codex docs
- improves Java-side context extraction quality
- shortens noisy generated summaries so closeout and review are faster to scan

## Execution Model

- Run the child plans in the order listed below.
- Do not pause between child plans unless a real blocker, unsafe ambiguity, or conflicting user change appears.
- Keep each child plan narrow and auditable.
- If a child plan grows too broad, split the remainder into a stable follow-up plan and keep moving.
- Finish with a final closeout pass that checks code, docs, generated artifacts, and validation together.

## Child Plans

- [x] `CODEX-LOCAL-CONTEXT-CHAIN` - `.agents/todo-plans/95-codex-local-context-pack-chain.md`
- [x] `CODEX-LOCAL-CONTEXT-CACHE` - `.agents/todo-plans/96-codex-local-context-cache-invalidation.md`
- [x] `CODEX-LOCAL-WORKFLOW-DOC-DEDUP` - `.agents/todo-plans/97-codex-local-workflow-doc-dedup.md`
- [x] `CODEX-JAVA-AST-CONTEXT-TIGHTENING` - `.agents/todo-plans/98-codex-local-java-ast-context-tightening.md`
- [x] `CODEX-LOCAL-NOISE-FILTER-TIGHTENING` - `.agents/todo-plans/99-codex-local-noise-filter-tightening.md`

## Execution Sequence

1. Build the one-shot context chain and concise evidence bundle.
2. Add cache reuse and invalidation for unchanged local context packs.
3. Reduce duplicate workflow wording in the agent-facing docs.
4. Tighten Java context extraction and the fallback story for AST spans.
5. Make generated summaries more decision-first and less noisy.
6. Regenerate affected artifacts and run the final closeout validation.

## Closeout Rules

- Keep the persistent backlog item open until all child plans are complete and validated.
- Update living docs when workflow, local context, or closeout behavior changes.
- Regenerate affected local-tooling artifacts when source behavior changes.
- Record any newly deferred remainder as a new stable backlog item before closing a child plan.

## Completion Evidence

- Status: complete
- Execution status: complete
- Persistent backlog item: resolved and removed from `docs/agent-improvement-backlog.md`
- Primary source files: `scripts/audits/codex_local_context_gateway.rb`, `scripts/audits/local_tooling_extended_tools.rb`, `scripts/audits/clean-text-noise.rb`, `scripts/audits/CodexJavaAstContext.java`, `docs/codex-fast-path.md`, `docs/feature-delivery-workflow.md`, `docs/documentation-sync-policy.md`, `docs/agent-operating-model.md`, `docs/change-completion-checklist.md`

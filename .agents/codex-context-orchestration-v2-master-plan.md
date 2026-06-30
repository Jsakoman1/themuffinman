# Codex Context Orchestration V2 Master Plan

Purpose: push the local Codex orchestration stack past the current one-shot context chain into a stronger fingerprinted cache, better routing, more parallel local execution, cleaner summary contracts, and stricter invalidation for workflow drift.

## Goal

Reduce token spend and repeated recomposition cost for repeated Codex sessions by making the local context gateway more deterministic, more selective, and more parallel where the work is independent.

## Scope

- Content-address the full `codex-context` payload so identical requests can reuse the composed pack instead of re-running the full assembly path.
- Prefer the smallest useful audit chain before widening to repo or symbol inventories.
- Make plan and tooling requests route toward workflow/doc and generated-artifact evidence first.
- Fan out safe local audits in parallel when they are independent.
- Standardize generated summary format toward decision-first output.
- Harden cache invalidation for docs, scripts, generated summaries, and plan files.

## Child Plans

- [x] `CODEX-CONTEXT-CACHE-KEY` - `.agents/todo-plans/100-codex-context-cache-key.md`
- [x] `CODEX-CONTEXT-EARLY-FILTERING` - `.agents/todo-plans/101-codex-context-early-filtering.md`
- [x] `CODEX-CONTEXT-PLAN-AWARE-ROUTING` - `.agents/todo-plans/102-codex-context-plan-aware-routing.md`
- [x] `CODEX-CONTEXT-PARALLEL-FANOUT` - `.agents/todo-plans/103-codex-context-parallel-fanout.md`
- [x] `CODEX-CONTEXT-SUMMARY-CONTRACT` - `.agents/todo-plans/104-codex-context-summary-contract.md`
- [x] `CODEX-CONTEXT-INVALIDATION-HARDENING` - `.agents/todo-plans/105-codex-context-invalidation-hardening.md`

## Execution Order

1. Add a content-addressed cache key and payload reuse for `codex-context`.
2. Tighten early filtering so the first read chain stays small by default.
3. Prefer plan-aware routing for tooling, workflow, and generated-artifact work.
4. Parallelize independent local audits and inventory builders.
5. Standardize summary shape toward `decision`, `why`, `next action`, and `evidence`.
6. Add stricter cache invalidation for docs, scripts, generated summaries, and plan files.
7. Re-run the local context flow and close out with validation plus plan completion audits.

## Coordination Notes

- Keep this plan aligned with `.agents/codex-summary-compactification-master-plan.md` because both work on output size and review speed.
- Do not duplicate the existing one-shot context chain work from `.agents/codex-local-context-orchestrator-master-plan.md`; this plan assumes that baseline already exists.
- If one child plan reveals a larger subproblem, split the remainder into another stable child plan and keep the master sequence moving.

## Closeout Rules

- Keep the backlog item open until all child plans are complete and validated.
- Regenerate affected generated artifacts when summary or routing behavior changes.
- Record any new deferred work in `docs/agent-improvement-backlog.md` before closing the plan.

## Completion Evidence

- Status: complete
- Execution status: complete
- Persistent backlog item: resolved and removed from `docs/agent-improvement-backlog.md`
- Primary source files: `scripts/audits/codex_local_context_gateway.rb`, `scripts/audits/local_tooling_extended_tools.rb`, `scripts/local_tooling_common.rb`, `scripts/audits/CodexJavaAstContext.java`, `docs/codex-fast-path.md`, `docs/feature-delivery-workflow.md`, `docs/documentation-sync-policy.md`, `docs/agent-operating-model.md`, `docs/change-completion-checklist.md`

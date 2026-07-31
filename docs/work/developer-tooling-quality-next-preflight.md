# Developer tooling quality next-pass preflight

Date: 2026-07-31
Baseline: a18b811e65001f68d174b42d1574f6c0f2ffc18d

## Scope reconciliation

Reused baseline:

- verified developer-tooling-intellij-mcp master;
- existing work verifier and strict inventory semantics;
- existing product routes and SideJobs language;
- existing runtime evidence and all non-pilot Playwright scripts;
- current System Map, truth, capability, and runtime authorities.

Residual work:

- one stale product-contract assertion;
- template canonical-path drift and missing freshness audit;
- noisy successful self-test output;
- missing category-aware work-artifact schema audit;
- duplicated runtime setup addressed through one harness pilot;
- missing shared IntelliJ run configurations and readiness evidence.

## Dirty-worktree boundary

The working tree contains broad user changes. Several selected paths are already
modified or untracked. Every serial task must start before editing, preserve current
content, and merge only its declared outcome. No task may normalize unrelated files.

## Runtime boundary

The brand-header pilot requires the owned local stack and fresh desktop/mobile JSON
and screenshot evidence. The task remains pending or blocked if the owned runtime
cannot be started safely. Existing screenshots never satisfy the new start snapshot.

## Entry decision

The package passes YAML loading, atomic-task hardening, strict serial-inventory,
control-source, plan-coverage, and recursion checks. Future files are first referenced
by their owning tasks. Existing modified/untracked paths have explicit narrow-merge and
fresh-snapshot rules.

Goal pursuit may begin only after verifier-controlled hardening is verified. The first
implementation item must be reconcile-modern-surface-sidejob-contract. The runtime
pilot may stop only for an unavailable owned stack or a genuine runtime failure.

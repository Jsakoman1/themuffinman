# Codex Local Audits

Use local audits before broad repo discovery when the question can be answered by a generated inventory or summary.

## Recommended Order

1. `make audit-change-impact-preflight`
2. `make audit-endpoint-callsite-linker` or `make audit-frontend-route-surfaces`
3. choose a focused audit from the target index

`audit-change-impact-preflight` includes report-only `scope_guardrails` that warn when one changeset mixes multiple product domains, runtime code with tooling or infrastructure, broad generated-report churn, or generated reports that were not predicted by the changed source files.

## Context-First Session Start

Before broad repository searches, read the compact local context in this order:

1. `docs/generated/local-tooling/diff-summary.md` for the current changed-file shape.
2. `docs/generated/local-tooling/control-start-summary.md` for the compact operator snapshot.
3. `docs/generated/local-tooling/audit-summary-index.md` to choose the smallest relevant generated report from the operator-core targets first.
4. `make context-pack topic=<topic>` when the task has a clear feature, domain, or changed-file focus.
5. `docs/generated/local-tooling/repo-map-summary.md` or `symbol-index-summary.md` only when the first four sources do not identify the needed files.

Treat `docs/generated/local-tooling/.history/` and `docs/generated/local-tooling/.cache/` as archive-only support
material rather than as default session context.

## Available Targets

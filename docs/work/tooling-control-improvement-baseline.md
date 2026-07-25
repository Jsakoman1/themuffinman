# Tooling control improvement baseline

Reviewed: 2026-07-25
Baseline revision: `98dc69af37354f08773c919d81d28c68ef53d0f5`

## Current inventory

The local tool catalog contains 50 referenced tools:

- 29 focused Ruby audits for backend, frontend, documentation, capability, runtime, dependency, and control integrity;
- 7 control helpers: audit support, cleanup, context search, repository map, TODO audit, source validation, and work verification;
- 3 generators for capability evidence, System Map impact, and target capability slices;
- 2 AST tools: Babel frontend AST and JDK Java Compiler AST;
- 9 frontend validators/runtime/contract tools;
- 1 tool catalog audit and 1 tool self-test included in the control layer.

Database fixture SQL remains a separate development-only helper surface and is not part of the executable tool catalog.

## Test boundary

Every Ruby helper is syntax-checked. Every frontend `.mjs` helper is checked with Node syntax validation. YAML sources are parsed during the self-test. The self-test also executes the frontend contract validators, both AST indexes, the repository map, the tool catalog, and bounded context search.

The broad audit path executes backend, frontend, documentation, and tool self-tests. It writes only disposable reports under `docs/audit-output/`, which are removed by `make clean-generated`.

Chromium runtime scripts are syntax-checked and retained as environment-bound tools. Their end-to-end execution requires the local application stack and appropriate runtime fixtures; runtime acceptance status remains owned by `docs/runtime-acceptance-matrix.yaml`.

## Current gaps for later slices

- Most focused audits are individually runnable but do not yet expose a shared structured result schema.
- The repository map provides AST/import/capability relationships but does not replace the endpoint-callsite audit for consumer proof.
- Database fixture helpers have no automated dry-run safety check.
- Chromium scripts have no single environment preflight that reports missing stack, credentials, or fixture prerequisites before execution.

These are residual improvement candidates, not new status authorities or product gaps.

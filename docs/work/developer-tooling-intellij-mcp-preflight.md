# Developer tooling and IntelliJ MCP optimization preflight

Date: 2026-07-31  
Baseline: `a18b811e65001f68d174b42d1574f6c0f2ffc18d`

## Baseline reconciliation

- `docs/work/system-map-optimization-master.yaml` is verified and reused only as
  baseline evidence for local context, repository mapping, and compact control.
- `make audit-tool-catalog` passes: 65 local tools, five categories, zero
  unreferenced tools.
- IntelliJ MCP is reachable, recognizes the project/module, and exposes 55 tools.
- `make tool-self-test` is not green at this baseline because the product contract
  stage reports `offer work route is discoverable`.
- The working tree contains broad unrelated user changes. This program is bounded to
  its declared tooling, documentation, test, and plan paths and must not normalize or
  revert unrelated files.

## Scope classification

Baseline-only:

- verified System Map optimization controls;
- work-plan verifier and status semantics;
- existing audit meanings and product validators;
- Maven/npm commands and runtime evidence rules;
- product behavior and current dirty product implementation.

Residual:

- compact repository-map query response;
- intent-aware bounded context search;
- deterministic changed-path validation recommendations;
- IntelliJ MCP decision/fallback contract;
- attributable tool self-test stages;
- before/after efficiency and precision evidence.

Retest only when triggered:

- product validators when their owning source/contract changes or the final staged
  self-test explicitly executes them;
- backend/frontend builds only when a tooling change executes or interprets those
  commands;
- System Map and truth registries only when their ownership or state claims change.

## Atomic readiness

Each inventory item has one observable outcome, exact required paths, one previous
inventory dependency, a repository-root-safe leaf validation, and an explicit
evidence boundary. The hardening item is first and no implementation item may start
before it is verifier-verified.

## Entry decision

The complete master package has passed YAML loading, atomic-task hardening, strict
serial inventory, control-source, and work-plan recursion checks. All future paths
are owned by the first task that references them, and the only pre-existing dirty
required path is `docs/implementation-control.md`, which has an explicit
preserve-and-merge rule.

Goal pursuit may begin only through the strict execution inventory, starting with
`compact-repository-map-query` after verifier-confirmed hardening. The current
frontend contract failure is a known external boundary and must not be hidden,
reclassified as passing, or repaired incidentally by the tooling tasks.

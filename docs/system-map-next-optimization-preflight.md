# Next System Map Optimization Preflight

Status: draft, ready after the previous optimization program is committed. Baseline:
`62cdb7f` plus the required clean-worktree reconciliation.

## Program order

1. dependency boundaries;
2. endpoint review batches;
3. canonical-source lint;
4. change-impact closeout integration;
5. capability/test/runtime evidence coverage;
6. configuration/environment drift;
7. final closeout.

## Why the clean baseline is mandatory

The previous System Map optimization program created and verified many documentation,
registry, audit, and plan changes. They must be committed before this program starts so
strict verifier start fingerprints can distinguish new work from prior work.

## Current boundaries

- 62 non-Web endpoints remain owner-review items.
- Native/device/offline runtime is not proven.
- Provider failure injection, process-crash replay, and production operations remain
  externally or operationally pending.
- Static reports remain advisory unless a work plan explicitly consumes their evidence.

## Success model

The program is successful when every new audit is actionable, every endpoint batch has
explicit evidence boundaries, current claims have one owner, change-impact is part of
closeout, capability evidence gaps are navigable, and configuration drift is detected
without pretending to validate external deployment state.

## Entry commands

```text
make audit-runtime-acceptance
make audit-capability-evidence
make audit-truth-registry
make audit-docs
make audit-plan-coverage
git status --short --branch
git diff --check
```

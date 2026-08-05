# Dora v0.5 goal-pursuit readiness review

Reviewed on 2026-08-05 against baseline `8349ce4`.

## Plan graph result

All structured YAML plans parse. The Dora v0.5 master has four children and one
serial inventory with sixteen unique, contiguous items. Repository-level
atomic-task hardening and serial-inventory audits both pass for the new graph.

All completed Dora masters are baseline-only: the original separation, tooling
extraction, v0.3 one-click, and v0.4 operational plans. Their implementation
evidence is retained but is not reused as v0.5 completion evidence.

The only historical Dora graph inconsistency was the separation inventory marked
`active` while every item and its master were already verified. The inventory is
now marked `verified`; no Dora serial queue is in progress.

## Scope conflict result

The remaining draft masters concern product/UX work or independent operational
areas. They do not require any Dora v0.5 paths. Dora v0.5 may therefore start
without merging, cancelling, or re-verifying those programs.

Active non-work documents (`action-contract-matrix`, `home-query-scope-contract`,
and `ui-action-integrity-runtime-scenarios`) are product control registries. They
remain outside Dora v0.5 scope and must not be treated as work-plan status.

## Start gate

The program is ready to become a goal. Its first permitted action is
`harden-v05-reusable-audits-plan`; this task must add the plan-specific audit and
verify the master, child, and inventory boundaries before a code or wrapper
migration begins.

No external approval is needed for tasks 1–14. Task 15 (Dora v0.5 publication)
and task 16 (MuffinMan pin) remain explicitly approval-gated.

# Dora v0.4 operational one-click preflight

Date: 2026-08-05
Baseline: `f68da56`
Status: prepared for atomic-task hardening; not yet an active goal.

## Reviewed boundary

Dora v0.3.0 is verified baseline evidence. The v0.4 plan changes only portable
launcher, configuration, CLI, CI, generic-audit, adoption, release, and pin
surfaces. MuffinMan domain code, product audits, runtime evidence, and business
documentation remain baseline-only and are excluded from v0.4 completion.

## Findings resolved in the plan

- The serial inventory has fourteen contiguous entries, each mapped to one task.
- Every task has a single observable outcome, exact required paths, one leaf
  validation, a serial dependency, and an evidence boundary.
- The independent consumer task names a file instead of an unverifiable directory
  as its strict required path.
- The release and pin remain separate; the release task explicitly requires
  external approval.
- A dedicated `audit-dora-v04-operational-plan.rb` validates child/inventory
  mapping, strict paths, serial dependencies, non-recursive validations, and the
  external release gate before implementation begins.

## Required first execution action

Start and verify only `harden-v04-operational-plan`. During that task, record
the fresh hardening review in the master, inventory, and child plans, then run
the dedicated audit. No Dora implementation task may start before that evidence
is recorded.

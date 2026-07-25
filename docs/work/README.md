# Work-plan directory

This directory contains machine-readable implementation and validation plans.

## Current state

- `docs/capability-inventory.yaml` is the capability status authority.
- `open_plans` is currently empty; the plans in this directory are verified or
  historical evidence, not an instruction to start new work automatically.
- Product and design direction belongs in `docs/design-and-vision-index.md` and
  the canonical documents linked from it.
- Temporary screenshots, command output, and disposable audit snapshots do not
  belong here after closeout.

## How to use this directory

Before creating a plan, search for an existing verified plan covering the same
scope. Extend the canonical plan or backlog instead of creating a parallel plan.
Every new plan must have one observable outcome, bounded paths, dependencies, a
leaf validation command, and a clear evidence boundary.

Verified plans are removed during closeout by default. Keep one only when a
current plan explicitly depends on it or it is the designated historical source;
otherwise retain the resulting canonical docs, registries, and runtime evidence.

The directory contains only current plans, explicit control mappings, and
machine-readable contracts. Completed plans are removed during closeout unless
an active canonical source still depends on them.

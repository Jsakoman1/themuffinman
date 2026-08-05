# Dora and MuffinMan separation analysis

Date: 2026-08-05
Baseline: `a35a18dec5f58a2c2409e72dfb5dd88ee921df57`

## Decision

Create **Dora** as the reusable implementation, validation, documentation, and
system-mapping toolkit. Keep **MuffinMan** as the product application and the
owner of all product meaning, domain constraints, implementation state, and
product-specific evidence.

The first releasable shape is a portable local package at `dora/`. It must be
usable by a second fixture project before it is moved to a separate Git
repository. A later repository split is a release operation and requires an
explicit remote/ownership decision; it must preserve the original repository
history and leave the pinned Dora revision recorded in MuffinMan.

## Observed coupling

The reusable control concepts already exist, but their implementation is not
yet portable:

- `scripts/verify-work.rb`, work-plan templates, and the execution-inventory
  protocol are broadly reusable.
- `scripts/audit_support.rb` and many scripts under `scripts/audits/` assume
  the repository root and write to `docs/audit-output/`.
- several audits directly inspect `apps/themuffinman` backend or frontend
  paths, and some directly name MuffinMan documentation registries.
- the root `Makefile` exposes both generic workflow commands and
  MuffinMan-specific build, runtime, and audit commands from one surface.

Therefore scripts cannot simply be moved unchanged. Dora needs a stable
project-adapter contract that supplies paths, commands, retained evidence
locations, and enabled product-specific checks.

## Ownership after separation

| Concern | Dora owns | MuffinMan owns |
| --- | --- | --- |
| Work-plan protocol | schemas, verifier, serial inventory rules | plans and their evidence |
| Audit framework | runner, catalog format, path/config loading | domain/API/UI/runtime audit implementations and declarations |
| System map | navigation and change-impact framework | product map, registries, capabilities, domain relationships |
| Documentation workflow | templates and canonical-source conventions | all product, business, technical, and evidence documents |
| Runtime/build | adapter invocation only | commands, services, fixtures, credentials, browser evidence |

## Non-negotiable preservation rules

1. No product source, evidence, historical plan, audit result, or Git history
   is deleted as part of Dora creation.
2. Existing MuffinMan `make` entry points remain compatibility delegates until
   equivalent Dora-driven commands are characterized and accepted.
3. Dora must not infer product completion, capability state, permissions, or
   runtime success. Those remain product-owned facts.
4. An audit is generic only when it works from the adapter contract. Anything
   that names a MuffinMan domain, endpoint, route, entity, or registry remains
   a MuffinMan extension.
5. The repository split is not attempted until the local portable package and
   an independent fixture both pass their declared contract checks.

## Migration gates

1. Characterize the current control surface and classify every tool as Dora
   core, Dora adapter, MuffinMan extension, or retained historical artifact.
2. Implement the Dora adapter/schema and prove the core verifier on an
   independent fixture.
3. Route MuffinMan through its adapter while keeping the existing commands as
   delegates; compare command outcomes and generated evidence paths.
4. Create a complete extraction manifest and a reversible Git-history
   preservation procedure.
5. Only after an explicit remote destination/ownership approval, publish or
   split Dora. MuffinMan then pins Dora's immutable revision and retains only
   its application/configuration/extensions.

## Explicitly out of scope for the first local migration

- changing MuffinMan product behavior, backend, frontend, or database schema;
- deleting historical documents or generated evidence;
- publishing Dora to a remote host or changing repository ownership;
- treating a static audit/build as replacement for existing browser evidence.


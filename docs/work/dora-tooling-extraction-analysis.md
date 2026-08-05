# Dora tooling extraction analysis

Date: 2026-08-05
Baseline: `35093d9`

## Current boundary

Dora `v0.1.0` is a portable package with an adapter schema, adapter validator,
work-plan structural validator, template, and independent fixture. MuffinMan
consumes it from the pinned `dora/` Git subtree through `.dora/project.yaml`.

The current boundary is intentionally incomplete. The full stateful work
verifier remains at `scripts/verify-work.rb`; generic repository helpers remain
at `scripts/`; and most audit scripts are still located in MuffinMan. The
ownership inventory records 131 current control subjects: 8 Dora-core
candidates, 38 adapter candidates, and 85 MuffinMan extensions.

## Extraction rule

Move an item to Dora only when it can operate from a project adapter and makes
no product decision. Keep it in MuffinMan when it interprets a MuffinMan domain,
route, entity, runtime scenario, capability, or evidence claim.

```text
Dora core
  protocol, state machine, schema, generic runner
       ↓
Dora plugin
  Java/Spring, TypeScript/Vue, Playwright mechanics
       ↓
MuffinMan extension
  business meaning, module map, scenarios, commands, evidence
```

## Ranked extraction inventory

### Release v0.2 — executable core

- Move the full serial `verify-work` protocol, task snapshotting, evidence
  recording, and artifact schema validation into Dora.
- Add a reusable project context that resolves the adapter root, work-plan
  path, generated-output paths, and declared commands.
- Keep leaf validation commands and product evidence declarations in MuffinMan.

### Release v0.3 — local tooling primitives

- Move tool-catalog schema/help rendering, TODO/backlog linkage, generated
  artifact cleanup, artifact retention checks, and workspace-change snapshots.
- Parameterize change-validation routing and bounded context search through
  adapter-owned configuration.

### Release v0.4 — analysis and documentation engines

- Move generic documentation-to-evidence matching, registry integrity checks,
  canonical-source checks, and System Map impact traversal.
- Retain MuffinMan registries, domain maps, capability records, and business
  documentation as extension inputs.

### Release v0.5 — optional language and runtime plugins

- Extract Java/Spring and TypeScript/Vue repository-mapping mechanics as Dora
  plugins with explicit source-root configuration.
- Extract browser-runtime trace mechanics as a Playwright plugin.
- Retain all MuffinMan endpoint contracts, selectors, seeds, screenshots, and
  acceptance scenarios.

## Explicit non-moves

Do not move product audits unchanged: API contract drift, mutation safety,
repository-fetch semantics, frontend interaction contracts, capability/runtime
acceptance, target capability catalog, database-workflow impact, and Vision
contract generation. They may call Dora engines later, but remain MuffinMan
extensions until another product proves the same invariant.

## Delivery safeguards

1. Preserve the existing MuffinMan command as a compatibility delegate before
   retiring any local implementation.
2. Prove every Dora engine against a standalone fixture and MuffinMan adapter.
3. Keep generic core tests in Dora and product assertions in MuffinMan.
4. Publish each Dora release as an immutable tag, then update MuffinMan through
   one reviewed subtree commit.
5. Never turn a static audit or schema check into product completion evidence.


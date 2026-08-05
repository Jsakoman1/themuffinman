# Dora v0.3 one-click bootstrap analysis

Date: 2026-08-05
Baseline: `835cbd2`

## Decision

Dora v0.2 is a verified portable control toolkit, but a new project still has to
write its adapter, copy command wiring, choose templates, and manually connect
its Dora engines. Dora v0.3 should remove that setup work without importing any
MuffinMan product behaviour.

The v0.3 outcome is a developer can initialise a project, choose an optional
technology pack, run a deterministic doctor check, and use standard Dora CLI
commands before adding product code.

## Current reusable baseline

Dora v0.2 already provides project-context resolution, stateful work execution,
tool catalog, artifact policy, TODO/backlog linkage, change routing, bounded
search, workspace inventory, documentation evidence, System Map impact,
Java/Spring and TypeScript/Vue source discovery, and runtime-trace contracts.
The v0.2 release tag and MuffinMan adapter pin are baseline-only evidence.

## Residual gaps

1. There is no `dora init`; a project has no safe standard starting structure.
2. Existing per-tool templates are not one validated project configuration.
3. Existing Dora engines are mostly Ruby APIs rather than CLI commands.
4. There is no `dora doctor` to diagnose an incomplete local environment.
5. Spring/Vue setup and CI are still MuffinMan command knowledge, not optional
   Dora packs.
6. Generic strict-plan, retention, cleanup, and template checks remain local
   scripts although their policy inputs can be project-declared.
7. Repository map output remains tied to MuffinMan source roots and capability
   inventory shape.

## Ownership boundary

Move only protocol, generation, validation, and configuration mechanics to Dora.
Keep every domain entity, API, permission, migration, UI interaction, seed,
browser scenario, capability status, and runtime acceptance conclusion in the
consuming project.

## Acceptance for a new project

The fixture proof must show these commands work with no MuffinMan path or name:

```text
dora init --project sample --stack spring-vue
dora doctor .dora/project.yaml
dora help .dora/project.yaml
dora work-start .dora/project.yaml plan=docs/work/example.yaml task=one
dora work-verify .dora/project.yaml plan=docs/work/example.yaml task=one
```

`dora init` may create control configuration and a selected technology pack; it
must not claim a new application is business-complete, runtime-verified, or
ready for production.

## Explicit non-goals

- Generate MuffinMan modules or any product domain.
- Move product-specific audits unchanged into Dora.
- Treat generated CI, build success, or a static source map as runtime evidence.
- Create or publish a real second product repository during this program.

The later first real Dora consumer is a separate product initiative and is the
required practical adoption step after v0.3 is published.

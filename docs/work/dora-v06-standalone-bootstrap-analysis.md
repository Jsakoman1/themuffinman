# Dora v0.6 standalone bootstrap analysis

## Current truth

Dora v0.5.0 is an independently released, pinned package with portable controls,
optional static-analysis plugins, and independent consumer fixtures. It is not yet
a complete zero-knowledge bootstrap because a consumer must first place `dora/` in
the project, supply command declarations, configure controls, and invoke plugin
libraries through project-specific glue.

## v0.6 decision

v0.6 delivers the missing installation and adoption layer, not an application
generator that guesses product architecture. It will provide a local-source-safe
bootstrap, explicit starter packs, one declared plugin runner, a beginner-oriented
onboarding guide, and full MuffinMan manifest adoption.

## Non-goals

- Dora will not create arbitrary business domains, authentication, database schemas,
  or user-facing product features.
- Dora will not download, execute, or upgrade remote code without an explicit source
  ref and user approval.
- Product runtime acceptance, permissions, capability truth, and business documents
  remain consumer-owned.

## Success definition

A clean directory, given an explicit local Dora source and declared starter, reaches
`./bin/dora doctor`, project commands, CI generation, and declared plugin execution
without PATH setup or MuffinMan references. MuffinMan uses the same manifest/runner
contract rather than bespoke plugin wrappers for reusable static analysis.

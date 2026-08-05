# Dora v0.8 agent-first operating-system analysis

## Verified v0.7.1 baseline

Dora owns bootstrap, typed project controls, strict work verification, report writing,
technical starters, and six reusable static-analysis libraries. MuffinMan is pinned
to Dora v0.7.1 and every current audit has an ownership classification.

## Remaining extraction gap

The static analysis runner is portable, but several MuffinMan wrappers still contain
their product-specific invocation and report assembly. `RepositoryJavaAstIndex.java`
and the Babel-based TypeScript/Vue AST index are useful generic engines but are
hard-coded to MuffinMan roots. v0.8 must move engines, not merely wrap subprocesses.

## Agent-first gap

Dora currently creates technical control files but does not create structured product
intent, domain invariants, workflow/permission rules, or a Codex-oriented project
profile. Existing MuffinMan product-memory and Vision documents contain valuable
patterns, but their entities, customer meaning, and execution adapters must remain
product-owned.

## Decision

Export schemas, templates, validation, fixtures, and neutral blueprints. Do not copy
MuffinMan product memory, Vision intents, execution adapters, entity resolvers, or
runtime credentials into Dora.

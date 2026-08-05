# Dora v0.4 operational one-click analysis

Date: 2026-08-05
Baseline: `f68da56`

## Decision

Dora v0.3 proved portable control mechanics and optional packs, but it does not
yet make a newly generated project operational without local knowledge. The
generated adapter uses the `dora` command while the reviewed consuming-project
form is `dora/bin/dora`; the CI pack similarly assumes Dora is already
available. Some retained generic engines also remain Ruby APIs instead of
consistent CLI controls.

Dora v0.4 closes those operational gaps before moving more MuffinMan audits.
It must produce a project-local launcher, validate every generated control
contract, expose retained generic controls through the CLI, and generate CI
that can invoke Dora from the project checkout. It does not generate business
software or reclassify MuffinMan status.

## Ownership boundary

Dora owns:

- installation and project-local command mechanics;
- generic configuration, validation, evidence, retention, provenance, and
  workspace protocols;
- optional technology and CI packs;
- plugin contracts that receive only declared project inputs.

MuffinMan retains:

- all domain behaviour, source, product audits, business documentation,
  runtime acceptance conclusions, routes, entities, permissions, and seeds;
- the decision whether a project-specific check belongs in a future optional
  Dora plugin.

## Sequenced decisions

1. Make `./dora/bin/dora` the generated project-local canonical invocation;
   do not require PATH installation to use a generated project.
2. Validate all generated controls before attempting to run them. Empty
   configuration remains an explicit incomplete state, not a passing command.
3. Expose retention, cleanup, template freshness, and repository map via
   adapter-owned CLI commands with no implicit deletion.
4. Make the GitHub Actions pack use the project-local Dora launcher and only
   project-declared setup, test, and build commands.
5. Move generic snapshot and provenance mechanics only after standalone
   fixture proof.
6. Classify each remaining MuffinMan audit as product-owned, generic core, or
   optional stack plugin before extracting any code.
7. Prove the workflow in an independent consumer fixture before the separate
   v0.4 release and consuming-project pin.

## Non-goals

- Generate a real MuffinMan-like product, backend, or frontend.
- Treat CI success as runtime or release acceptance.
- Move MuffinMan-specific capability, endpoint, UI, or runtime audits unchanged.
- Delete or rewrite MuffinMan paths during Dora extraction.

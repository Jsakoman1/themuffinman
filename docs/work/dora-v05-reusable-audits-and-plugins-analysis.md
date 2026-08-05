# Dora v0.5 reusable audits and plugins analysis

## Decision

The next Dora release should extract reusable audit mechanics in two layers:

1. portable plan-governance controls, configured by a consuming project; and
2. optional static-analysis plugins for declared Spring and Vue source roots.

MuffinMan domain truth, permission semantics, capability status, browser acceptance,
and product documentation remain consumer-owned. Dora may validate a declared
contract, but must not infer that a product capability is complete.

## Current baseline

Dora v0.4.0 already provides the local launcher, project adapter validation,
configured controls, strict work execution, project-local CI bootstrap, workspace
snapshot, delivery provenance, and an independent consumer fixture. Its verified
plan is baseline evidence only for this release.

The audit classification has three relevant groups:

- generic-core candidates: atomic-task hardening, plan coverage, serial inventory,
  control ownership, and release-manifest mechanics;
- optional stack plugins: Spring, Vue, and Spring/Vue static analyses; and
- product-owned audits: capability, permission, runtime, and product documentation
  truth, which are explicitly out of scope.

## Extraction rule

Each migration follows this order: define the Dora input contract, implement the
portable engine and isolated fixture, keep the MuffinMan audit as a compatibility
wrapper, then classify any remaining MuffinMan-only assumptions. No product-owned
audit is deleted or moved in this master.

## Delivery order

1. Harden the plan and serial queue.
2. Extract configurable plan-governance and ownership controls.
3. Create the optional plugin contract and migrate one bounded static-analysis
   concern per task.
4. Prove generic and Spring/Vue consumer adoption without MuffinMan paths.
5. Publish an immutable Dora v0.5 release and pin MuffinMan only after approval.

## Known follow-up boundary

Release-manifest automation remains a separate release plugin candidate. It is not
part of v0.5 because its remote/tag authority requires a distinct, explicitly
approved release workflow.

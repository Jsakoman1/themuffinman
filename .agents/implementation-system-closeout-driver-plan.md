---
machine_kind: plan
machine_status: complete
machine_title: Closeout Driver
machine_goal: Build one deterministic closeout driver that runs cleanup, temp-product handling, autofill, plan audit, closeout audit, and report generation in order.
---

# Closeout Driver

## Status

Complete.

## Goal

Build one deterministic closeout driver that runs cleanup, temp-product handling, autofill, plan audit, closeout audit, and report generation in order.

## Parent Master Plan

- Master plan: `.agents/implementation-system-closeout-hardening-master-plan.md`

## Scope

- Included: orchestration order, fail-fast behavior, and reusable driver entrypoint.
- Included: the new driver should own the canonical `closeout` sequence for broad batches and emit a single blocker when the first step fails.
- Excluded: plan state rules and evidence semantics.

## Implementation Notes

- Prefer a single reusable `make closeout-driver` or equivalent script entrypoint over multiple ad hoc shell sequences.
- The driver should call the preflight result before any mutating cleanup or audit step.
- The driver should keep the existing closeout commands usable on their own, but the master batch path should prefer the driver.

## Validation

- Targeted checks: driver command order and fail-fast behavior.
- Broader checks: closeout flow still reaches the same reports and audits as the existing commands.
- Closeout checks: the driver stops on the first drift.
- The driver should be exercised against one broad batch and one manifest-backed batch once implemented.

## Completion Evidence

- Status: complete
- Changed files: `scripts/audits/closeout-driver.rb`, `Makefile`, `scripts/audits/local_tooling_extended_tools.rb`, `scripts/local_tooling_common.rb`, `docs/generated/local-tooling/closeout-driver/*`
- Validation evidence: `make closeout-driver plan=.agents/chat-prod-ready-plan.md manifest=.agents/feature-manifests/chat-prod-ready-manifest.yaml`
- Doc delta summary: the deterministic closeout driver now runs cleanup, temp work product closeout, autofill, plan audit, feature closeout audit, and reporting in order.
- Deferred work: none

# Dora v0.9 Codex delivery acceleration analysis

## Current verified baseline

Dora v0.8 is a standalone, tagged package that bootstraps an independent project, validates a product brief, domain library, and agent profile, executes built-in static analyses, enforces serial atomic work verification, and proves an idea-to-verified-work fixture. MuffinMan is pinned to that release and retains product meaning, product runtime evidence, and local operational inputs.

The gap is not a missing foundational control. It is the cost of operating those controls repeatedly: a Codex agent must still select context manually, infer the next allowed action from several files, translate changed paths into validations, and make audit output understandable to a beginner.

## Decision

Prioritize the feedback loop used on every change before broadening stack support or adding a real voice runtime:

1. Give Codex a bounded, task-scoped context pack and an explicit next action.
2. Give a user one understandable project status and a guided project setup path.
3. Give every static finding one portable severity, location, explanation, and repair contract.
4. Link changes to required validation, documentation, decisions, and evidence.
5. Make repeated analysis faster and integrate exports only after their contracts are stable.

## Scope boundary

v0.9 may add neutral contracts, command mechanics, fixtures, and adapters. It must not copy MuffinMan entity names, permission matrices, workflows, Vision behavior, real voice providers, credentials, product mutation adapters, or production evidence into Dora.

## Success evidence

- A fresh independent project can request a bounded agent context and next safe action without a MuffinMan reference.
- A changed-path fixture receives deterministic validation and documentation guidance.
- A beginner-facing status report identifies missing knowledge, active work, evidence gaps, and open decisions without claiming product completion.
- Findings from each built-in analysis share a validated portable envelope.
- A repeated analysis fixture proves cache correctness against an uncached result.
- Any GitHub annotation or voice evaluation export remains optional and does not grant release, mutation, or product authority.

## Explicitly deferred

- New product or business capability packs.
- A general-purpose workflow engine.
- Real provider credentials, audio processing, or automatic state mutation.
- Broad “AI decides the plan” behavior; Dora may recommend only deterministic, declared next steps.

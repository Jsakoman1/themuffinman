# Dora v1.1 Codex-first ergonomics analysis

## Baseline

Dora v1.0.0 is published at immutable commit
`70522a8fc21c775b2d34e715c913531623669552` and MuffinMan is pinned to that
commit. The v1.0 serial inventory contains 20 verified items. Its reusable
baseline includes explicit project intake, project memory, bounded agent
context, next-action selection, status, change impact, standard findings,
read-only export, verified local source descriptors, and upgrade preview.

## Confirmed residual gaps

1. `dora new` writes a local launcher but does not copy a local Dora package.
   A fresh generated project's `bin/dora help` fails unless a separately pinned
   package is supplied through the bootstrap route. The one-click route must
   optionally create a verified local package and source record.
2. `PluginRunner` fingerprints declared source roots but not a custom plugin
   entrypoint's content or the Dora package revision. A plugin or Dora upgrade
   can therefore reuse stale diagnostic output.
3. Project memory is created from intake answers but has no refresh, drift
   report, or reconciliation route when product knowledge, domain knowledge,
   decisions, or current work change.
4. Codex currently composes multiple public commands to obtain a safe working
   view. It needs one cited, machine-readable session payload containing health,
   knowledge, decisions, work, evidence, tools, and approval boundaries.
5. Public command output is primarily YAML or prose and errors are free text.
   Codex needs a stable envelope, format choice, error identifiers, remediation,
   and declared side-effect metadata.
6. New-project input is safe but YAML-heavy. The answer-file contract must stay
   canonical while an interactive or Codex-guided wrapper makes it easier to
   produce valid explicit answers.
7. Source upgrade is intentionally preview-only. A separately approved apply
   route requires a backup, source re-verification, explicit migration selection,
   durable record, and rollback instructions.
8. Local plugins run as trusted local processes without a uniform timeout,
   execution-class declaration, or policy boundary.
9. Dora has mappings and language-specific analysis, but lacks one declared
   capability-to-domain-to-plan-to-code-to-test-to-runtime-evidence trace.
10. Strict serial work protects one agent, but concurrent agents lack task
    leases, handoff records, and safe path-conflict detection.
11. Dora generates project guidance but has no opt-in Codex integration pack
    for a project-local skill, stable session entrypoint, and durable command
    discovery. It must never overwrite user-owned Codex configuration.
12. The completed v1.0 backlog entry remains open and should be closed as part
    of v1.1 baseline hygiene.

## Product decision

Dora v1.1 optimizes for a Codex-first developer loop, not for autonomous
product invention:

```text
explicit idea and constraints
-> verified self-contained project
-> agent session
-> one bounded task
-> implementation and declared validation
-> evidence and documented handoff
```

The implementation must preserve consumer ownership of product meaning,
architecture choices, permissions, runtime acceptance, and external approval.

## Sequencing decision

Self-contained creation, cache correctness, memory drift, and the agent session
are release blockers for the v1.1 core. Upgrade apply and plugin safety follow
only after those contracts are stable. Capability tracing, coordination, and
optional Codex integration require two independent consumer proofs before any
release claim; they remain explicitly optional until then.

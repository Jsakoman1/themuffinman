# Dora v1.0 one-click agent operating system analysis

## Verified v0.9 baseline

Dora v0.9 is an independently published package with a pinned MuffinMan consumer.
It can bootstrap a local project, validate project knowledge, run strict serial work
plans, guide a task-scoped agent, report next work and closeout gaps, classify change
impact, record decisions, cache analysis, export read-only findings, and evaluate
voice safety fixtures without credentials or execution adapters.

The v0.9 independent consumer fixture proves that these mechanics work without
MuffinMan data. Its release tag is `v0.9.0` at
`6ec1fc4f3a5678296aae8b01caaf13b71d142719`.

## Current operational gaps

1. The beginner journey is fragmented. Bootstrap, product intake, domain library,
   agent profile, plan, inventory, change-impact configuration, and decision log are
   individual artifacts. A beginner or Codex agent must still know which artifacts to
   create and in which order.
2. The public command surface is incomplete. `agent-next`, `agent-closeout`,
   `status`, and `plugins` exist in the executable, but the command registry, README,
   and agent-first guide do not expose the full v0.9 operating loop. Agent context and
   project intake are library capabilities rather than supported CLI entrypoints.
3. Evidence mechanics remain separate. Plugin reports do not yet automatically emit
   standard findings; finding export is not part of a declared run; status does not
   summarize findings or decision traceability; and change-check and change-impact
   parse the same project contract through separate interfaces.
4. The cache is correct but opt-in. It is not yet integrated into declared built-in
   analysis runs, exposes no bounded cache inspection, and is not visible in project
   status.
5. Distribution is safe but not beginner-simple. Bootstrap intentionally requires an
   explicit local reviewed source descriptor. Dora has no supported release-source
   installer, checksum-verified upgrade preview, or project-owned migration report.

## Design decision

Make v1.0 a productization release, not a broad expansion of stack-specific audits.
The primary experience must become:

```text
idea and explicit answers
  -> dora new
  -> product/domain/agent/capability project memory
  -> first atomic work item
  -> Codex context and next action
  -> implementation
  -> findings, impact, tests, runtime evidence, decisions
  -> read-only closeout and verifier-recorded completion
```

Every transition remains explicit and project-owned. Dora may generate neutral
structure, validate declarations, and recommend declared next steps. It must not
invent business rules, select a product architecture without a declaration, execute
product mutations, silently download code, or treat static evidence as release proof.

## Priorities

### P0 — one coherent agent-first workflow

- A `dora new` input contract that turns explicit answers into project knowledge and
  the first neutral delivery artifacts without fabricating product meaning.
- Supported CLI commands for project intake, agent context, next action, status,
  impact, closeout, and plugin discovery, with registry/README/guide parity tests.
- A project memory contract that connects product intent, domain rules, open decisions,
  capability map, and current work without making completion claims.

### P1 — one evidence pipeline

- One canonical path-impact service used by both change guidance and closeout.
- Standard findings emitted by declared plugin runs, then available to status and a
  read-only portable/SARIF-compatible export.
- Decision references and evidence gaps visible in one project status report.
- Bounded cache integration for declared static analysis, with transparent hit/miss
  reporting and safe invalidation.

### P2 — repeatable consumption and lifecycle

- A reviewed release-source descriptor with tag, immutable commit, and checksum.
- A dry-run upgrade report that identifies Dora control migrations before changing a
  consumer project.
- Release/compatibility automation and independent fresh-consumer proof.

## Explicit non-goals

- No MuffinMan entities, workflows, permissions, provider credentials, runtime
  evidence, or mutation adapters in Dora.
- No autonomous product design or automatic implementation approval.
- No default network download or unpinned package upgrade.
- No new stack packs until the coherent v1.0 workflow is independently proven.

## Success evidence

- A fresh independent project can use one supported entrypoint to create explicit
  project knowledge and receive a first bounded Codex task.
- `dora help`, the command registry, README, and agent guide expose the same supported
  commands and input boundaries.
- A declared plugin finding flows through standard finding, path impact, status, and
  read-only export without claiming runtime acceptance or release readiness.
- A repeat declared analysis reports a cache hit; changed declared inputs invalidate it.
- An upgrade preview proves its source tag, immutable commit, checksum, and proposed
  migrations without changing the project.
- A new independent consumer completes the full loop without a MuffinMan reference.

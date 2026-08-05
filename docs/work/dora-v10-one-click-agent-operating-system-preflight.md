# Dora v1.0 preflight

## Baseline and reuse

`d941c20f7fc274d1773703954dd9c5f23b0749ef` is the clean MuffinMan baseline after
Dora v0.9.0 release and consumer pin. The verified v0.9 master is baseline evidence
for separate neutral mechanics: project intake, task context, next-step selection,
closeout, status, findings, impact, decisions, cache, exports, voice evaluation, and
the release/pin procedure.

Those mechanics are not v1.0 completion evidence. v1.0 must prove a new public,
coherent workflow over them with fresh independent fixtures.

## Scope reconciliation

- `dora new` remains an explicit-answer operation. It may create neutral documents and
  the first plan, but cannot select a product stack, infer domain rules, or generate
  product code.
- Agent command exposure changes the Dora executable, registry, and documentation;
  each command needs a machine-readable output and unsafe-input fixture.
- Evidence work must preserve the static/test/runtime distinction and migrate
  ChangeCheck through a compatibility contract before existing consumers change.
- Cache integration is limited to declared static-analysis inputs and must expose
  invalidation; it cannot become verification evidence.
- Source lifecycle work verifies immutable inputs and previews migrations only. Remote
  source acquisition, release, and consumer pin remain approval-gated.

## Resolved architecture decisions

1. The answer file is the canonical `dora new` contract because Codex and CI require
   deterministic non-interactive input. An optional interactive wrapper may ask the
   same questions and write the same answer file; it may not create hidden defaults.
2. `ChangeImpact.assess!` becomes the canonical path-impact API. `ChangeCheck` stays
   as a tested compatibility facade until its existing callers use the canonical
   service, preventing an uncontrolled flag-day migration.
3. A source descriptor must identify a reviewed local source, immutable tag, commit,
   and SHA-256 content checksum. Upgrade preview remains local and read-only; it can
   describe a remote source but cannot download, write, tag, push, or pin by default.

## Risks and controls

1. A convenient `dora new` could become an invention engine. Require complete answers,
   preserve unanswered decisions, and reject missing domain/authority declarations.
2. Public commands could drift from documentation. Add a registry-help-guide parity
   fixture before claiming a supported agent surface.
3. Impact migration could break closeout. Keep a two-project compatibility fixture and
   change one canonical service before changing downstream commands.
4. Cached analysis could hide stale source. Cache keys must include versioned declared
   inputs; changed paths, schema, and parser inputs must miss deterministically.
5. Upgrade support could weaken supply-chain safety. Require immutable tag, commit,
   checksum, explicit source descriptor, dry-run output, and no default network write.

## Start condition

The master now references six strict child work plans and one 20-item direct serial
inventory. Every planned implementation item has one observable outcome, exact
required paths, one non-recursive leaf validation, explicit evidence boundary, and a
direct predecessor. A new goal may execute only the first documentation hardening task.

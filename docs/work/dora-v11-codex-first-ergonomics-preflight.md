# Dora v1.1 preflight

## Baseline and ownership

Baseline is MuffinMan commit `25a1425b9d245627c15f3c4927e52dea96eccc4d`,
which pins Dora v1.0.0. v1.0 evidence is baseline-only and is not new v1.1
completion evidence. Dora owns portable contracts, commands, templates, tests,
and local control mechanics. Consumers own product meaning, architecture,
runtime acceptance, source approval, and external side effects.

## Recorded decisions

1. The answer file remains canonical. A future interactive route only writes a
   reviewable answer file before invoking the same validation path.
2. The supported self-contained route requires an explicit reviewed local source
   descriptor. `dora new` copies that declared package, records its checksum and
   immutable ref, and does not download anything.
3. Git initialization remains opt-in through an explicit command or flag; Dora
   otherwise reports readiness and remediation only.
4. Existing YAML command output remains compatible. New commands expose a stable
   envelope and JSON/YAML format choice; errors have identifiers, remediation,
   citations, and side-effect metadata.
5. Memory refresh produces a proposal. It cannot overwrite product knowledge;
   an explicit scoped approval is required for any apply route.
6. Plugin cache identity includes declared inputs, source files, custom entrypoint
   content, and a Dora implementation fingerprint. A cache hit remains diagnostic.
7. Upgrade apply is local-only, requires verified source plus an approval record,
   creates a backup, records selected migrations, and leaves rollback instructions.
8. Plugin policies declare trust and timeout. Dora must state unsupported isolation
   rather than claiming a sandbox it does not enforce.
9. Capability trace, leases, and Codex integration are opt-in experimental
   extensions until two independent consumer proofs pass.

## Readiness result

The proposed work has 31 direct serial inventory items. Every implementation item
has one outcome, bounded paths, a leaf test, and an evidence boundary. The only
permitted first implementation step is atomic-task hardening.

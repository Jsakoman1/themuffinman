# ARCH-004 Read-Model Reliability Analysis

Reviewed: ARCH-004 baseline pass.

## Current state

The backend generally assembles viewer-scoped DTOs in services and uses read-only transactions. Repository fetch audits identify 14 high-risk and 24 medium-risk methods based on relation breadth and mapper coupling. These findings are prioritization signals, not proof of a defect or performance regression. A 2026-07-22 Chromium measurement found 11 rows and approximately 616 KB for the Work quest list, with 96 ms list latency, 44 ms search latency, and 38 ms detail latency in local-dev; query count and SQL timing were not instrumented, so no optimization or scale conclusion follows.

## Main risk classes

| Risk | Evidence | Meaning |
|---|---|---|
| Fetch/mapper mismatch | repository-fetch and mapper audits | A mapper may dereference a relation not guaranteed by a sibling fetch method |
| Transaction boundary escape | `open-in-view=false` and read-surface audit | Returning entities or mapping after a transaction can expose lazy-loading failures |
| Fan-in read model | activity, chat, Vision, dashboard assemblers | One endpoint can compose several repositories and hide query multiplication |
| Viewer-scope drift | cross-module profile, visibility, and activity reads | A new read path can bypass the canonical authorization-aware assembler |
| Missing empirical evidence | performance catalog and observability registry | Query count, latency, row volume, and provider timing remain unknown |

## Highest-value surfaces

- Business booking/calendar reads: detailed joins, availability derivation, and concurrent state.
- Chat conversation/message sync: participant state, sender, attachments, pagination, and reconnect recovery.
- Work discovery/detail/application/dashboard reads: viewer actions, pagination, and relation-heavy DTOs.
- Social/profile/circle reads: visibility and relationship policy interaction.
- Vision context assembly: database, provider, and response-assembly timing must be separated.

## Reliability contract decision

1. Every read endpoint has one authoritative service-level DTO assembly path per viewer/use case.
2. Repository fetch graphs are implementation evidence and must be reviewed with every mapper change.
3. Read-only transaction annotations protect assembly boundaries but do not prove bounded query count or latency.
4. Performance claims require a fresh runtime artifact containing dataset shape, revision, environment, sample count, query count, timings, and result volume.
5. No caching or broad fetch expansion should be introduced solely from static risk scores.

## Explicit unknowns

The repository does not currently prove production p50/p95/p99 latency, N+1 absence, memory behavior, scale limits, cache hit rates, or multi-instance consistency. These require runtime instrumentation and repeatable datasets.

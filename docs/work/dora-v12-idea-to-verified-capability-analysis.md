# Dora v1.2 analysis: idea to verified capability

## Decision

Dora v1.2 should reduce the work required before Codex can safely implement a first capability. It must turn a user conversation into declared project knowledge and one bounded, evidence-aware work item without turning Dora into a product-requirements inference engine.

## Current strength

v1.1 already provides self-contained project creation, declared knowledge, agent sessions, stable command envelopes, traceability, coordination, safe lifecycle records, and optional Codex guidance. Its remaining friction is that users or agents still assemble several input artifacts manually before implementation can begin.

## Primary outcome

One supported route should take a new project from a reviewed idea to a ready first capability:

`idea interview -> product brief -> domain/permission/workflow library -> project profile -> capability blueprint -> atomic work -> cited Codex session -> implementation evidence`

## Non-goals

- Dora must not invent business policy, permissions, acceptance criteria, or completion.
- A guided interview is not approval to implement or publish.
- Stack packs must remain technical and neutral; no MuffinMan behavior may enter Dora.
- Plugin hardening must not claim a sandbox until one is actually enforced.

## Main risks and controls

| Risk | Control |
| --- | --- |
| An interview fabricates requirements | Preserve unanswered decisions and require explicit answer provenance. |
| Generated project looks ready while domain gaps remain | Expose deterministic readiness blockers and a no-implementation boundary. |
| One-command setup overwrites user configuration | Create only Dora-owned paths; reject changed Dora-owned files; preserve user instructions. |
| Stack packs become product-specific | Require two neutral consumer proofs and source-boundary checks. |
| Trace becomes a completion claim | Retain separate implementation, validation, runtime, and release boundaries. |

## Release condition

v1.2 may be proposed only after each new command and generated artifact has atomic evidence, two independent non-MuffinMan consumer proofs where it crosses project boundaries, a compatibility audit, and separate explicit approval for publishing and pinning.

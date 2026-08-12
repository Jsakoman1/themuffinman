# Operational IDC v0 analysis

## Decision

Build IDC v0 as a local, owner-started, advisory capability adjacent to Dora. Dora
remains the sole canonical owner of decisions, plans, work lifecycle, evidence,
ProjectMemory, and verification. IDC validates and renders only explicitly supplied
advisory material; it cannot discover sources, decide conclusions, promote content,
or write Dora state.

The current checkout contains the Dora source tree at `dora/` and records the
independent-source materialization contract. Before implementation, the owner must
confirm the exact Dora-owned implementation directory and repository identity. The
program must not select a parent/shared Git worktree that would widen scope to a
consumer project, and it must not migrate, adopt, modify, or otherwise use the
existing uncommitted local pilot workspace as an implementation target.

## Evidence from the IDC v0 quality tests

- A cited current Dora context prevented a false greenfield re-plan by exposing that
  the relevant DoomsDayStorage purchase loop was already verifier-verified.
- A synthetic land dossier retained four unresolved conflicts as explicit stop
  conditions instead of converting marketing claims into parcel facts.
- A web-grounded Croatian legal dossier kept national rules, local-plan dependency,
  jurisdiction, source freshness, and the absence of a parcel-level answer visible.
- The tests did not show that IDC can discover conflicts or research automatically:
  Codex prepared the claims and source envelopes. IDC v0's proven value is preserving
  provenance, uncertainty, conflict, owner questions, and manual promotion gates.

## Input distinction

Structural invalidity and evidence incompleteness are intentionally different:

- A source-manifest object holds only provenance: identity, allowed kind, locator,
  revision/digest, and `observed_at`. Reject one missing any required provenance field,
  a malformed request, a forbidden source kind, an unknown claim source, a path escape,
  or an unapproved selector.
- A claim holds the assessment: status, `source_refs`, wording, and confidence and/or
  freshness assessment where relevant. Confidence is not an intrinsic universal source
  field.
- `missing_context` is valid only when an owner-selected source manifest explicitly
  records an expected-but-not-provided proof, or a supplied and cited source explicitly
  states that the evidence is absent. It is not inferred from a lack of filesystem or
  repository searching.
- When neither basis exists, the unresolved matter is an `open_question`, not
  `missing_context`. A valid evidenced gap remains visible rather than becoming a
  malformed input or hidden risk.

## Four serial slices

### IDC-01 — Contract and directory boundary

Formalize the v0 request, source-manifest, dossier, and promotion-proposal contract.
The contract preserves the current distinction among `owner_confirmed`,
`source_fact`, `external_research`, `assumption`, `open_question`,
`missing_context`, `conflict`, `alternative`, `scenario`, and
`advisory_recommendation`; it does not add a vague `confirmed` state. It also records
the atomic-boundary review for all later slices.

The decisive negative tests are that a structurally incomplete source is rejected;
that `missing_context` needs an explicit manifest-or-cited-source basis; and that an
otherwise unresolved matter becomes `open_question` rather than an inferred absence.

### IDC-02 — Local validator and deterministic renderer

Implement the program's sole local `idc` CLI. It reads only owner-provided request and
manifest paths, validates a Codex/owner-authored dossier payload, and renders a stable
owner-readable dossier to its declared destination. It is a validator and renderer,
not a reasoning engine or autonomous dossier generator. Code changes are permitted
only in IDC-02's explicit target paths; at runtime it has no repository discovery,
Git/repository, subprocess/shell, web, network, database, Codex-start, or Dora-write
behavior.

### IDC-03 — Explicit Dora envelope exporter

Add a read-only Dora CLI exporter that accepts only fixed, owner-selected selectors
for safe ProjectReadModel fields, accepted/open decision identifiers, and named
canonical artifact references. It returns a versioned, digest-bearing envelope on
stdout; it does not write a file, expose raw source, ProjectMemory, terminal output,
absolute paths, secrets, or an automatically selected “all relevant context”.

### IDC-04 — Owner-started local workflow and Bridge read profile

Prove the existing IDC-02 CLI together with the IDC-03 envelope. IDC-04 adds no owner
command, renderer, or filesystem writer: its integration test only exercises the
existing command and declared output behavior. The Bridge profile may return the same
allow-listed envelope as a read-only response; it cannot create files, accept paths,
browse sources, run IDC, start Codex, inspect a terminal, or write Dora. ChatGPT may
compose an owner-readable handoff, but the owner starts the existing local command and
separately promotes any conclusion through existing Dora workflow.

## Rejected in this program

- Hosted service, database, embeddings, vector index, MCP server for IDC, agent
  runtime, generic personal memory, automatic conflict detection, automatic source
  selection, automatic web research, web access from IDC, and any Dora or consumer
  write path from an IDC dossier.
- A new generic `confirmed` status, because it could hide whether the owner, a source,
  or a researcher supplied the statement.
- Consumer-project ingestion of another project's dossier or duplicated IDC logic.

## Program gate

No slice starts merely because this master exists. Before IDC-01, the owner must
confirm the exact Dora-owned implementation directory and repository identity, that it
is not a parent/shared consumer worktree, and that the existing uncommitted pilot
workspace is neither migrated nor touched. Before each eligible later slice, the owner
must confirm the exact scope and that no new authority is being granted. IDC-04 is
eligible only after IDC-01 through IDC-03 are independently verifier-verified and an
owner reviews the actual envelope shape.

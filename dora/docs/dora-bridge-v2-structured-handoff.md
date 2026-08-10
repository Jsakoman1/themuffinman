# Dora Bridge V2 Structured Handoff

V2 is a narrowly scoped ingress queue from ChatGPT to an owner-run local Codex workflow. It is not a project filesystem API, a generic Dora mutation API, or a remote execution channel.

The flow is:

```text
ChatGPT -> Secure MCP Tunnel -> create_handoff -> private local Handoff Store
        -> owner runs codex-both -> Codex uses Dora handoff CLI -> normal Dora work/evidence -> completion readback
```

The launcher contract command is `dora/bridge/bin/dora-bridge-codex-both-contract`. It returns only `no_ready_handoff` or a validated `{project, handoff_id}` pair. It never returns handoff instructions, private paths, or credentials. The owner-local launcher may pass the resulting ID in a fixed Codex startup instruction; Codex must retrieve and claim it through the Dora CLI before any implementation work.

V2 does not start Codex from an MCP request. The local owner starts `codex-both`; the existing runtime readiness and Keychain isolation remain the execution gate.

## Storage and configuration

The owner-local runtime enables V2 with `--handoff-state-root ~/.local/share/dora-bridge`. The store creates that directory at mode `0700` and its lock/event files at mode `0600`; it rejects symlinked or non-owner objects. The only persisted transport data is an append-only JSON-lines event log in that owner-only directory. No project repository, source tree, DoomsDayStorage checkout, or application working tree receives handoff runtime files.

The private bridge registry has an independent capability per project:

```yaml
capabilities:
  handoff_write: true
```

It defaults to `false`; read access does not imply handoff access. The current owner-local registry explicitly enables only `dora` and `doomsday-storage`.

## MCP and local commands

With V2 state-root configuration, MCP adds `create_handoff`, `list_handoffs`, `get_handoff`, `get_next_handoff`, and `get_handoff_status`. `create_handoff` accepts only bounded structured fields and a `client_request_id`; an equivalent retry returns the existing handoff and conflicting reuse fails. All readbacks omit private paths, raw verifier output, and credentials.

Codex uses `dora/bin/dora handoff-next`, `handoff-claim`, `handoff-link`, `handoff-block`, and `handoff-complete` with the owner-local registry and state root. Completion requires a linked Dora master/work plan and passing Dora task evidence. `READY → CLAIMED → COMPLETED` is the normal lifecycle; `BLOCKED`, `CANCELLED`, and `SUPERSEDED` preserve an append-only terminal history. The next resolver sorts eligible READY handoffs by creation timestamp and ID, so it never silently chooses an arbitrary item.

## V2.1 collaboration brief

`create_handoff` now requires one bounded `brief` alongside the existing objective, acceptance criteria, constraints, and project-relative references. It records the request mode, expected result, locked owner decisions, non-goals, relevant context, stop conditions, required verification, and explicit unresolved owner decisions. This lets ChatGPT turn a short owner request into a predictable implementation record without adding an execution tool or a second planning system.

For `implementation`, unresolved owner decisions must be empty and verification duties must be stated. When a product choice is unresolved, ChatGPT uses `owner_decision_required` with the exact open decision and stop condition. Codex must stop and record a normal Dora blocker rather than infer the choice. A correction or follow-up is a new handoff linked by the immutable `supersedes` or `follows_up` reference; the original record is never rewritten.

All V2 handoff tool results now have object-shaped `structuredContent` with an output schema. In particular, `list_handoffs` returns `{handoffs: [...]}` and `get_next_handoff` returns `{handoff: record-or-null}`. This avoids the array/null structured-content validation failure while leaving every V1 tool contract unchanged.

## V2.2 collaboration feedback loop

After a handoff is claimed, local Codex may append at most twenty concise, semantic milestone feedback events. Each event declares one of `DISCOVERY`, `PLANNING`, `IMPLEMENTING`, or `VERIFYING`, one milestone, the explicit verification state (`IMPLEMENTED_UNVERIFIED`, `VERIFICATION_IN_PROGRESS`, `VERIFICATION_FAILED`, or `VERIFIED` when applicable), and optional meaningful progress, material finding, deviations, or residual risks. It has no terminal-output field and rejects multiline transcript-style content.

ChatGPT reads this through the existing `get_handoff` or `get_handoff_status` objects; no extra collaboration/status MCP tool exists. The handoff lifecycle status and normal Dora delivery/evidence links are authoritative over older handoff snapshot text. On completion, the status readback shows `COMPLETED` beside a `VERIFIED` collaboration state and the Dora verification reference. Completion never replaces verifier evidence.

When Codex needs an owner/product decision, local `handoff-block-owner-decision` produces terminal `BLOCKED` state with the question, why it is required, known realistic options, an optional recommendation, blocked work, and work that may continue. ChatGPT can read this structured object without reconstructing terminal output. Corrections and follow-ups still use immutable existing `supersedes` / `follows_up` lineage; the original handoff is not rewritten.

Local `handoff-complete` can attach a compact result containing work performed, interpretations, acceptance results, deviations, residual risks, and follow-up need. It still requires the linked Dora delivery and passing task evidence. V3 remains out of scope: ChatGPT cannot remotely start, steer, or control Codex.

## V3.1 lifecycle-only readback

`get_handoff_lifecycle_readback` is an additive read-only MCP tool for an explicitly
handoff-enabled project. It accepts exactly one validated project and handoff ID, then
returns only `handoff_id`, `status`, `outcome_category`, verification state with
project-relative Dora evidence references when available, and a fixed owner-safe summary.
It distinguishes a completed handoff, a blocked handoff, and a claimed handoff whose
verification state is `VERIFICATION_FAILED` without returning immutable handoff content,
owner-decision prose, lifecycle events, child output, secrets, environment values, or
private filesystem paths.

Malformed IDs, unknown IDs, unauthorized projects, and malformed arguments all return
the same structured `UNAVAILABLE` readback. The tool cannot create, claim, start, edit,
link, complete, block, or otherwise change a handoff; the existing V2/V2.2 reads and the
owner-local V3 runner contracts remain unchanged.

## V3.2 safe handoff progress

The existing local `handoff-feedback` command is the one Dora-native way for Codex to
record a claimed handoff's active phase. It still requires the complete bounded V2.2
feedback object and the owner-local registry/state-root arguments; it is not an MCP tool
and cannot modify a READY, BLOCKED, COMPLETED, CANCELLED, or SUPERSEDED handoff.

Its `phase` and `verification_state` values are independently allowlisted and progress
through a finite, monotonic model:

```text
START -> DISCOVERY or PLANNING -> IMPLEMENTING -> VERIFYING
verification state: NOT_STARTED -> IMPLEMENTED_UNVERIFIED
  -> VERIFICATION_IN_PROGRESS or VERIFICATION_FAILED
```

The runner records `START` when it claims work. A duplicate, regressive, malformed,
oversized, unauthorized, or regressive phase/status submission fails closed and does not
append an event. `VERIFIED` is owned by evidence-gated completion. `COMPLETED` and
`BLOCKED` remain lifecycle outcomes: normal Dora
evidence-gated completion and blocker commands retain their existing semantics, and no
terminal handoff can be changed.

Both `get_handoff_status` and `get_handoff_lifecycle_readback` now include a derived
`progress` object. Its only fields are allowlisted `phase`, a fixed human-friendly
`label`, a fixed bounded `summary`, and a stable `material_change_token`. Active values
are `START`, `ANALYSIS`, `IMPLEMENTATION`, or `VERIFICATION`; terminal results are
`BLOCKED` or `COMPLETED`; a ready or claimed record without a valid active phase is
`INCOMPLETE`. The token changes only when the externally visible lifecycle, projected
phase, or verification state changes, so a condition watch can suppress duplicates by
simple equality. The object deliberately never copies `milestone`, `progress`,
`finding`, evidence text, terminal output, source content, paths, command lines,
environment values, or secrets.

## MP-01 Intent Plan alignment

`align_intent_plan` is an additive read-only bridge tool. It accepts one explicitly
allowlisted project and one bounded ChatGPT Intent Plan proposal. The proposal is
transient: it is not stored and cannot create or revise a Dora Master Plan, task,
decision, handoff, lifecycle event, or verification evidence.

Its proposal contract has an intent-plan identifier, intended outcome, in-scope work,
non-goals, zero or more fixed owner decisions, candidate slices, and an exact requested
owner readback. A fixed owner decision includes an accepted Dora decision identifier and
its current text; any unknown or mismatched decision needs owner resolution. Candidate
slices are intentionally linear in this first slice. The first has no dependency and
declares `no_owner_decision_pending`; every later candidate depends on its predecessor
and declares both `no_owner_decision_pending` and `prior_slice_verification`.

Dora evaluates that contract against current canonical project health, active delivery,
latest verified delivery, unresolved owner decisions, and accepted decisions. It returns
only `ALIGNMENT` phase, `ACCEPTED`, `RECONCILED`, or `OWNER_DECISION_NEEDED`, fixed next
action and blocker language, the first eligible candidate identifier when applicable,
and blocked identifiers for later candidates. It never echoes proposal prose, decision
text, source content, paths, commands, raw evidence, environment values, or secrets.

`ACCEPTED` applies when there is no active or prior verified delivery; `RECONCILED`
retains a prior verified delivery as canonical baseline. In both cases only the first
candidate is eligible for a future Dora-owned Master Plan creation step. Later candidates
remain blocked until their predecessor has declared Dora verification evidence and no
owner decision is pending. Active work, open decisions, invalid/incomplete proposals,
or fixed-decision conflicts return `OWNER_DECISION_NEEDED`; the tool makes no mutation
or automatic release.

## Acceptance and external proof

The repository suite proves synthetic MCP creation, private persistence, one READY readback, exclusive claim, Dora delivery/evidence linkage, completed readback, V1 tool compatibility, no generic execution tool, containment, idempotency, malformed/oversize rejection, cross-project rejection, locking, and owner-only modes.

The real local no-ready `codex-both --version` path was exercised after V2 install. The ready path was also exercised with a temporary fake `codex`: the actual launcher passed only the fixed instruction containing the validated project ID and handoff ID, not handoff content, then the synthetic handoff was cancelled in the append-only local history. The runtime was restarted and the actual configured local MCP process advertised exactly the five V2 handoff tools and no generic execution tool.

The real V2 acceptance flow is complete: ChatGPT discovered the tool, created a real handoff, the owner-gated Codex flow claimed it, Dora recorded passing verification evidence, and the completed handoff was read back. The private outbound transport remains documented without inventing any cache-refresh behavior.

## V3 local autonomous runner

V3 is an owner-local convenience runner, not remote Codex control. The owner explicitly
starts it in the foreground with `dora-bridge-handoff-runner watch` and an explicit
allowlist of handoff-enabled projects. It holds one private singleton lock, selects only
the oldest READY handoff in that allowlist, claims it atomically through the existing
Dora CLI, and invokes only the fixed owner-local `codex-both` launcher with one validated
claimed handoff UUID. It never accepts a shell command, prompt, path, git
operation, file operation, or remote instruction.

The runner records only V2.2 milestone feedback and then relies on normal Dora completion
or structured owner-decision blocking. A non-zero Codex exit, an exit without a terminal
Dora outcome, an interrupted verification, stale runner state, a duplicate runner, or a
temporary local state error fails closed: it does not reclaim or blindly retry CLAIMED
work. `status` exposes a small owner-local health record; `stop` requests an intentional
local stop. Neither is an MCP capability or completion evidence. There is no autostart,
launch agent, ChatGPT process control, or terminal-output retention.

The owner terminal prints one concise readiness message and then remains quiet when no
eligible READY handoff exists. For claimed work it prints only one detected/claimed
message, each changed fixed V3.2 phase (`Start`, `Analysis`, `Implementation`,
`Verification`, `Blocked`, or `Complete`), and a fixed terminal outcome. When the
handoff has a valid delivery link to one canonical Master Plan, it may additionally
print a deduplicated ordered checklist of that plan's curated title/identifier and task
title/identifier with `verified`, `current`, `pending`, or `blocked` markers. The
checklist is a read-time projection of the canonical Master Plan, execution inventory,
work task, and passing task evidence: lifecycle phase, timestamp, and terminal text
never establish verification. A standalone, missing, malformed, or inconsistent link
prints no checklist. It polls the existing lifecycle/progress readback at the configured
local cadence while the child runs, deduplicates unchanged state, and maps invalid or
unavailable readback to one fixed unavailable message. It never relays or retains child
output, feedback prose, tokens, paths, commands, environment values, or secrets. The runner opens the child with stdin
and both output streams connected to the null device. When a launched child exits without a Dora terminal outcome, the existing
handoff `blocked` readback receives a fixed `runner_failure` object containing only an
allowlisted failure `code`, the wrapper `exit_code` when available, and a fixed
`recovery_hint`. Fixed wrapper exit classes distinguish launcher preflight failure from a
non-interactive Codex execution failure. The runner recognizes the managed-runtime Login
Keychain prerequisite as a launcher preflight failure; the owner runs
`dora-bridge-keychain-setup` in their login session when needed, reviews the blocked
handoff, and creates a new follow-up instead of retrying it. Other child text, secrets,
environment values, terminal transcripts, and private paths are discarded.

The owner installs the preclaimed mode once with the explicit
`dora-bridge-install-codex-both-runner-mode --install` operation. It creates a V2 backup
and configures no service. This lets the runner pass only an already claimed UUID to the
same local runtime/keychain-isolated `codex-both` workflow. Runner mode calls the pinned
Codex binary as `codex exec` with one fixed non-empty bootstrap instruction; Codex reads
the record through `handoff-get`, follows normal Dora preflight/work/evidence actions,
and records completion or a blocker through Dora. With no arguments, `codex-both` remains
the normal interactive local Codex TUI.

The generated launcher closes no authority gap: it still accepts only its fixed mode and
validated identifiers. It resolves Codex through the owner-local executable installed
beside `codex-both`, rather than an incidental caller `PATH`; the runner closes child stdin
immediately so the fixed contract is non-interactive.

ChatGPT completion awareness remains an existing scheduled condition check of the V2.2
`get_handoff_status` readback. It is not real-time push and does not grant ChatGPT access
to runner health, terminal output, or remote start/stop/steering.

V3 does not remotely start, steer, or control a Codex process.

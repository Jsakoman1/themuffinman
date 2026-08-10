# Dora Bridge V2 Structured Handoff preflight

Status: approved by the first serial work item on 2026-08-10. This preflight is the security gate before V2 write-tool implementation.

## Actual V1 baseline

- `dora/bridge/lib/dora_bridge/server.rb` is a local stdio JSON-RPC MCP server. It authorizes only trusted registry project IDs and delegates all project semantics to `Dora::ProjectReadModel`.
- `Dora::ProjectReadModel` opens only project-relative declared artifacts beneath a canonicalized adapter root and rejects traversal and symlink escape. It is a read-only projection and will remain separate from V2 mutation.
- `dora/bridge/lib/dora_bridge/project_registry.rb` maps a public project ID to a trusted adapter path; unknown IDs fail before adapter loading. It currently has no write capability.
- `dora/bin/dora` owns local workflow commands. `WorkExecution` and `WorkVerifier` provide the existing serial `work-start` / `work-verify` evidence path. `TaskLease` demonstrates local `flock`-based mutation, but it mutates a YAML document in place and is not suitable for an immutable handoff event history.
- The owner-local `~/.local/bin/codex-both` first invokes `~/.local/libexec/dora-bridge-runtime ensure`, removes the tunnel credential from Codex's environment, then starts Codex. The runtime helper receives its credential only from Keychain and launches the existing bridge stdio command.
- The private registry is outside repositories and currently lists `doomsday-storage` and `dora`. It has no V2 capability declarations.

## V2 design choice

V2 uses a dedicated private append-only handoff store rather than changing `ProjectReadModel`, any project working tree, or existing task leases. This preserves V1 compatibility, allows storage-level locking and permissions to be audited independently, and keeps handoffs an ingress queue rather than a second plan/evidence system.

## Threat model

| Threat | Boundary / mitigation |
| --- | --- |
| Unknown or read-only project used as a write target | Registry resolves a known ID before filesystem use; `handoff_write` defaults false and is checked server-side. |
| Path traversal, symlink escape, caller-selected store path | Client schema has no path fields; state root comes only from owner config; every store path is generated IDs under an owner-only canonical root. |
| Retry/concurrent create duplicates work | A lock protects idempotency-key lookup and atomic record/event creation; equivalent repeat returns the existing record, conflicting reuse fails. |
| Concurrent Codex consumers double-claim a handoff | A lock and immutable lifecycle event enforce one READY → CLAIMED transition. |
| Handoff becomes a generic filesystem/shell/patch primitive | The data model accepts only bounded structured fields; MCP exposes no generic mutation or execution tool; no data field is interpreted as a path or command. |
| Private paths, raw evidence, or secrets leak to ChatGPT | Sanitized read models omit storage paths, raw verifier output, environment data, and arbitrary project internals; no secret field exists. |
| ChatGPT starts or steers a Codex process | MCP creates a queue item only. `codex-both`, started by the local owner, remains the sole process launcher. V3 retains remote execution as deferred. |
| Ambiguous pending work silently executes | A single resolver returns exactly one eligible READY handoff by ordered creation time only when no tie/invalid state exists; otherwise it reports ambiguity and the launcher does not inject an instruction. |
| Completion without delivery evidence | `COMPLETED` requires a linked Dora work/master reference plus sanitized passing verification reference. `BLOCKED` records a structured reason instead. |

## Atomic delivery inventory

The master and inventory define seven serial outcomes: hardening, private store, capability registry, MCP surface, local CLI, owner gate, and end-to-end/governance closeout. No code or MCP write surface is enabled before this preflight item is verified.

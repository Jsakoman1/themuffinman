# Dora Bridge V3 local runner preflight

## Discovery record

V2.2 is the authoritative lifecycle. Its private owner-only append-only handoff store
already provides deterministic READY selection, atomic `READY -> CLAIMED`, bounded
milestone feedback, structured owner-decision blocks, evidence-gated completion, and
sanitized `get_handoff` / `get_handoff_status` readback. V3 must reuse these records;
it must not add an MCP execution tool, terminal transcript, mutable assistant status
store, or a second evidence system.

The installed owner-local `/Users/jsakoman/.local/bin/codex-both` is the approved
execution entrypoint. It first asks the local runtime helper to ensure the managed
tunnel, removes the tunnel credential from Codex's environment, uses the repository
resolver to select a handoff, and starts Codex only with a fixed prompt containing
validated identifiers. The launcher presently has no preclaimed-handoff mode. V3 will
add that narrow mode through an owner-installed adapter: it accepts only exactly one
project/id pair validated by the local runner and never accepts a prompt or command.

The host has `launchctl`, but V3 will not install a launch agent or enable autostart.
The supported owner action is a foreground `watch` command. `SIGINT` is sufficient for
the owner to stop it; a local `status` command reads only the runner's private health
record. No MCP tool, ChatGPT request, or network client can start, stop, attach to, or
inspect the child process.

## Security and recovery decisions

- The runner receives an explicit owner-entered project allowlist and validates every
  member against the bridge registry's existing `handoff_write` authorization.
- It considers only the deterministic oldest READY record across that allowlist and
  claims through the existing local Handoff CLI before launching anything.
- It uses a non-blocking private singleton lock. A duplicate runner exits without
  launching or changing a handoff.
- Its only child is the fixed owner-local `codex-both` executable with a fixed
  preclaimed-handoff argument vector. There is no shell interpolation, arbitrary path,
  arbitrary command, git, file, or remote instruction input.
- It records only V2.2 semantic milestones. A non-zero child exit or an exit that leaves
  the handoff CLAIMED is blocked with a concise structured reason, never blindly retried.
- A restart does not reclaim CLAIMED work. Missing/stale runner state is reported as
  `RECOVERY_REQUIRED` for owner review and normal Dora blocking or follow-up; only a new
  READY handoff may be claimed. Temporary state read/write failure stops the runner
  safely instead of continuing with unknown state.

## Delivery sequence

1. Implement and test the local singleton runner and fixed child invocation.
2. Add a preclaimed local launcher contract and a narrow owner-install/update path.
3. Prove normal completion/readback, owner-decision blocking, injection/cross-project/
   duplicate/restart failures, then update Dora-native product, domain, memory, and
   operating documentation.

The only owner-visible asynchronous completion mechanism remains an existing scheduled
ChatGPT condition check that reads V2.2 handoff status. Dora makes no real-time push
claim.

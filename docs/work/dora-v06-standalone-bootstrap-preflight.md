# Dora v0.6 standalone bootstrap preflight

Baseline: `3c10315`, with Dora v0.5.0 pinned at
`4c9271cbe4d6798ea4124d85896129b0e51941e1`.

The verified v0.5 master is baseline-only. v0.6 changes bootstrap, manifest,
plugin execution, consumer adoption, and release/pin surfaces only. The first task
must harden the serial graph before any code change. Remote distribution publication
and the final MuffinMan pin remain separate explicit-approval tasks.

## Execution readiness

The inventory has thirteen unique, contiguous items and each item maps to one child
task with an observable outcome, bounded required paths, a leaf validation command,
and an evidence boundary. The generic atomic-task and serial-inventory audits pass
for the draft graph.

The first item, `dora-v06-harden`, is still pending because it must add and run a
v0.6-specific semantic plan audit. It is the only permitted entrypoint after goal
activation. No bootstrap, starter, runner, or MuffinMan adapter task may start until
the work-plan verifier records passing evidence for that hardening task.

## Approval and safety boundary

Tasks 1–11 are local, reversible repository work. Task 12 creates a remote immutable
release and task 13 changes the consumer pin after that release; both remain
explicitly approval-gated. The bootstrap contract must use an explicit local source
and immutable ref, so neither plan preparation nor implementation may silently fetch
or execute remote Dora code.

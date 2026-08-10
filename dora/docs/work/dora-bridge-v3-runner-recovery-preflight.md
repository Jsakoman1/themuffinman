# Dora Bridge V3 runner recovery preflight

## Audit boundary

`handoff-119c7dab-306b-494f-b5cd-ba15bc3f21a0` and
`handoff-833ee058-a837-4d27-bab6-e7d999e6217d` are retained unchanged as historical
audit evidence. Each was claimed and blocked about one second later. Neither is a
candidate for retry, correction, or synthetic proof.

## Sanitized diagnosis

The fixed owner-local `codex-both` launcher begins by running its managed Dora Bridge
runtime preflight. In the runner process, that preflight exited with code `1` before
the `codex` executable was reached because the owner Login Keychain item required to
start or repair the managed runtime was unavailable. The prior runner drained the
child output and retained only a generic non-zero outcome, so the owner terminal was
blank and lifecycle/readback did not identify the recovery action.

The launcher is also invoked through `Open3.popen2e` with its stdin pipe left open.
That does not cause the observed Keychain failure, but it violates the intended
non-interactive runner contract and can leave a child waiting for input. The launcher
relies on an inherited `PATH` to find `codex`; that is ambiguous for a foreground
owner command started outside an interactive shell.

## Bounded repair

- Close child stdin immediately and run the fixed launcher from its own directory.
- Preserve the fixed argument vector and do not execute a shell.
- Make owner-terminal messages fixed runner milestones only; never relay child output.
- Map only allowlisted launcher failure signatures to fixed `code` and recovery hint
  values; retain the child exit code when it exists.
- Store that compact structure in the existing `blocked` lifecycle/readback object.
- Pin the generated owner-local launcher to the owner-local Codex executable rather
  than relying on inherited `PATH`.

The recovery does not bypass the Login Keychain gate. If the managed runtime is not
already usable, the owner must run the existing local Keychain setup in their login
session and start a new follow-up handoff after reviewing the blocked one.

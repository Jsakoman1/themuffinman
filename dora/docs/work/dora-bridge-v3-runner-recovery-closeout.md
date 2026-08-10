# Dora Bridge V3 runner recovery closeout

## Result

The recovery proof uses only temporary private fixture state. It creates a synthetic
READY handoff, lets the repaired fixed runner claim it, closes child stdin, completes
through the existing Dora lifecycle, and verifies the structured completion/readback.
No historical handoff was read through a mutating lock, retried, or modified.

The same proof covers an immediate child exit with a Keychain-like preflight marker. The
owner terminal receives fixed milestones and a fixed recovery hint, while the lifecycle
stores only `keychain_unavailable`, exit code `1`, and that fixed hint. The fake child
includes a secret-like marker; neither terminal output nor handoff readback contains it.

## Owner recovery command

Run this in the owner login session after reviewing the two historical BLOCKED handoffs:

```text
/Users/jsakoman/.local/bin/dora-bridge-keychain-setup && ruby /Users/jsakoman/Desktop/themuffinman/dora/bridge/bin/dora-bridge-install-codex-both-runner-mode --update && ruby /Users/jsakoman/Desktop/themuffinman/dora/bridge/bin/dora-bridge-handoff-runner watch /Users/jsakoman/.config/dora-bridge/projects.yaml /Users/jsakoman/.local/share/dora-bridge --project dora
```

The command explicitly refreshes the owner-local V3 launcher and starts one foreground
allowlisted runner. It does not retry a BLOCKED handoff. If future work is needed, create
a new linked follow-up handoff after the owner reviews the structured failure readback.

## Remaining boundary

The Login Keychain is intentionally still required to start or repair the managed local
runtime. The runner reports that prerequisite but cannot bypass it. There is no remote
start/stop/steering, terminal streaming, service installation, or ChatGPT write-authority
change.

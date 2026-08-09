# Dora Bridge V1 dogfooding

The parent control plane is the minimal correct dogfooding mechanism for this
program. The checked-out `dora/` directory is a pinned Dora subtree inside the
parent repository, not an independent Git worktree: its source changes are
therefore governed by the parent `.dora/project.yaml`, `docs/work/`, and
`dora/bin/dora work-start` / `work-verify` commands.

The Bridge V1 master keeps every task bounded to `dora/` source, tests, and
documentation, except for the parent-owned work-plan records that provide the
required verification evidence. It does not create a second `.dora` control
tree inside the portable package and does not alter consumer projects.

The serial order is intentional: harden the plan, establish the read-model
contract, prove it in an isolated consumer, expose it through a read-only
allow-listed MCP adapter, then perform the separately authorized external
ChatGPT connection proof. The final task remains an external acceptance
boundary; it cannot be substituted by local protocol tests.

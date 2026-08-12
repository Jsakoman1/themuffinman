# TheMuffinMan capability-documentation adoption hardening review

## Product-status authority

`docs/capability-inventory.yaml` is the existing canonical machine-readable
product-status authority. This program never rewrites it, copies its capability
states into Dora, or treats a Dora control as evidence that a product feature is
implemented or verified.

## Package boundary

The only package source is reviewed immutable Dora commit
`922dbf5e0f9d32d52e546ccdd6145c592b1ab01a` with its explicit checksum. Upgrade
creates a local Dora rollback backup and changes only the consumer's vendored
Dora package and truthful distribution pin. No consumer code, parent worktree,
private context or IDC workspace is copied.

## Documentation boundary

The later `docs/dora-capability-inventory.yaml` is a separate, cited navigation
layer. It may reference existing product, domain, system-map and evidence
documents but cannot resolve owner decisions, change work lifecycle, duplicate
ProjectMemory, or make an implementation claim.

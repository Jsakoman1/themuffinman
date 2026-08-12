# Dora capability documentation boundary

This document is the Dora-facing navigation layer for TheMuffinMan. It makes the
existing authoritative documentation easier to locate; it does not replace any
product source of truth.

## Authoritative sources

- `docs/capability-inventory.yaml` is the machine-readable authority for current
  product capability status, gaps and evidence links.
- `docs/business-logic.md` and `docs/domain-technical.md` own product and domain
  meaning.
- `docs/work/*.yaml` and their recorded verifier evidence own delivery lifecycle
  and implementation verification.
- `docs/project-memory.yaml` is derived agent navigation only.

## Dora boundary

The separate `docs/dora-capability-inventory.yaml` declares only the availability
of this documentation-control layer. It does not copy capability states from the
product inventory, mark any capability verified, or enable IDC, Private Context,
CPPE, remote execution, Git operations or Dora write access.

Any future product, documentation or control change must use the normal
owner-approved Dora workflow and update the relevant authoritative source.

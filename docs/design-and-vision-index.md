# Design and vision index

This is the starting point for product-direction and interface-design work. It
keeps stable decisions separate from implementation plans and historical analysis.

## Read these first

1. `docs/product-vision.md` — product purpose, experience direction, and durable UX principles.
2. `docs/apple-desktop-design-reference.md` — canonical Web workspace design and interaction contract.
3. `docs/product-memory.md` — durable lessons and decisions that should survive implementation sessions.

For Vision-specific backend, API, prompt, canvas, or executor work, also read
`docs/vision-architecture-patterns.md` and its compact companion documents.

## Source boundaries

- Product meaning belongs in `docs/product-vision.md` and `docs/business-logic.md`.
- Visual and interaction rules belong in `docs/apple-desktop-design-reference.md`.
- Durable implementation lessons belong in `docs/product-memory.md`.
- Vision architecture belongs in `docs/vision-architecture-patterns.md`.
- Current capability status belongs in `docs/capability-inventory.yaml`.
- Implementation plans and validation belong in `docs/work/`.
- Dated audits and superseded design exploration are historical evidence, not new requirements.

## Working rule

When a design idea becomes a durable product rule, update the canonical document
above and remove or mark the idea's temporary plan copy. Do not create another
parallel vision or design authority.

Design and vision content is kept only when it is still referenced by a current
plan or canonical source. Superseded exploration is removed during closeout.

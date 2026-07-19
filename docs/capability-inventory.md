# Capability Inventory

`docs/capability-inventory.yaml` is the canonical inventory of TheMuffinMan product capabilities.

It connects four questions that were previously scattered across vision documents, READMEs, code, plans, and audits:

1. What should the product do?
2. What does the backend currently support?
3. Where can a user actually access it?
4. What is missing, broken, partial, or unverified?

The YAML file is the source of truth. This document explains how to maintain it.

The current audit is [`docs/work/capability-inventory-audit.yaml`](work/capability-inventory-audit.yaml), and the remaining implementation queue is [`docs/work/capability-inventory-planner.yaml`](work/capability-inventory-planner.yaml). The planner covers every currently open capability exactly once; it is not a redesign master plan.

## Status rules

- `planned`: intended but not started
- `not_offered`: supporting code may exist, but there is no usable user surface
- `partial`: a meaningful slice exists, but important workflow/client coverage is missing
- `broken`: offered but unreliable or currently unusable
- `implemented`: primary backend and web workflow exists
- `verified`: implemented and checked against explicit acceptance evidence
- `deprecated`: deliberately being replaced or removed

Do not mark a capability `implemented` because an endpoint or component exists. The capability needs a backend boundary, a user-facing entry point, current behavior, known gaps, and validation evidence.

## Planning workflow

Before creating a new master plan:

1. Find the capability IDs in `docs/capability-inventory.yaml`.
2. Confirm their current status from code and validation evidence.
3. Create plan tasks for every `partial`, `broken`, or relevant `not_offered` gap.
4. Put the capability IDs in the plan metadata or task evidence.
5. After implementation, update the capability status and evidence in the same change.
6. Do not close a plan while its capability inventory still says `broken` or while an explicitly scoped gap remains unaccounted for.

## Evidence expectations

Use stable references:

- backend classes or endpoints
- frontend routes/components
- tests and acceptance commands
- verified work plans
- product or technical documents for rules

Avoid using a commit hash as the only evidence. A commit identifies a point in time; the inventory should explain what currently exists.

## Scope boundary

This inventory is not a replacement for:

- `docs/product-vision.md`, which defines product direction
- `docs/business-logic.md`, which defines user-facing rules
- `docs/domain-technical.md`, which defines technical invariants
- `docs/implementation-backlog.md`, which tracks executable open work
- `docs/work/*.yaml`, which records plan execution and validation

The inventory links those surfaces so future implementation work can start from a reliable capability map instead of rediscovering the product from code.

## Target capability contract

`docs/target-capability-catalog.yaml` is the separate machine-readable target
contract for the complete production-ready product. It describes atomic user
capabilities and required surfaces such as Vision, web UI, mobile, Watch, and API.

The target catalog must not copy current statuses or evidence. Its
`current_inventory_ids` fields link back to this inventory; an empty list means
that a target capability is currently unmapped. Validate the relationship with
`make audit-target-capability-catalog`.

Use `make audit-target-capability-coverage` to print a machine-readable YAML
coverage report. It aggregates linked current statuses and detects current web UI
and Vision evidence. It also identifies broad mappings that must not be treated
as exact implementation coverage. The generated report is diagnostic; this inventory remains
the canonical current-state status and evidence source.

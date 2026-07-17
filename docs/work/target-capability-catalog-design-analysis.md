# Target Capability Catalog Design Review

## Findings

1. `docs/capability-inventory.yaml` is already the current-state source of truth;
   copying status, gaps, and evidence into a new file would create drift.
2. The target model needs smaller capabilities than the current inventory. A
   broad record such as `work.application` cannot drive complete acceptance.
3. `vision: true` and `web_ui: true` must mean required finished-product surfaces,
   not claims that those surfaces exist today.
4. CRUD alone is insufficient. Discovery, ownership views, participant actions,
   lifecycle transitions, notifications, privacy, consent, and failure behavior
   are part of production completeness.
5. Actor, access scope, and consent belong on each capability because the same
   object can be visible differently to owner, participant, member, or public.

## Decisions

- Canonical target file: `docs/target-capability-catalog.yaml`.
- Current implementation truth remains `docs/capability-inventory.yaml`.
- Stable IDs use `module.object.action` where practical.
- Capabilities are top-level records with a module/object index for navigation.
- `target_surfaces` is the required production surface matrix, not status.
- `current_inventory_ids` is a relationship only; current status and evidence stay
  in the inventory.
- One capability represents one coherent user outcome or acceptance slice.

## Risks and mitigations

| Risk | Mitigation |
| --- | --- |
| Target/current drift | Link by IDs and audit unresolved current IDs. |
| Endpoint checklist instead of product model | Require actor, outcome, failure behavior, and acceptance. |
| False completeness from booleans | Require lifecycle, permission, privacy, and acceptance metadata. |
| Vision becomes a generic command list | Record confirmation and clarification requirements for Vision mutations. |
| Modules are forgotten | Audit that every current inventory module exists in the target index. |

## Exit criteria

- Schema and catalog are deterministic and machine-readable.
- IDs, module/object references, required fields, and surface matrix validate.
- Current inventory links resolve or are explicitly empty for target-only work.
- Both creator and consumer/participant perspectives are represented.

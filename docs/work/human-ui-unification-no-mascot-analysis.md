# Human UI unification without mascot assets

## Decision

The next visual program can move materially closer to the two supplied references without a mascot or new bitmap artwork. It should use the existing logo, lighter destination colours, code-native SVG purpose icons, stronger landing-page composition, and one shared set of screen archetypes.

## What is already strong

- Home already has the correct mobile information shape: greeting, four primary destinations, attention, schedule, and bottom navigation.
- The authenticated shell, logo, module tabs, collection rows, detail surfaces, and backend-prepared actions already provide stable product structure.
- Business established the correct human hierarchy: orient the person, expose one next action, keep secondary setup quiet, and preserve factual status semantics.

## Residual UX and visual gaps

1. Home is structurally close to the mobile reference but its system-dark destination tokens can render as dark saturated blocks inside an otherwise light sand shell. The reference uses lighter, optimistic surfaces with dark navy text.
2. The large Home logo header is visually detached from the greeting and launcher. On desktop it can feel like a separate banner; on mobile it consumes a large first-screen area before the useful greeting.
3. Purpose icons are placeholder glyphs rather than one coherent code-native icon family.
4. Authenticated routes still use parallel page-header grammars: `FriendlyCollectionHeader`, `SurfaceHeader`, and bespoke route headers.
5. Collection, workspace, detail, and setup screens have shared components, but route-local borders and extra style blocks still override them inconsistently.
6. More than twenty Vue files contain multiple scoped style blocks. `AuthenticatedShellView.vue` and `SurfaceContentView.vue` are especially large, making global visual changes harder to reason about.
7. The previous browser matrix proved navigation and overflow, but its screenshots show Home only. Visual parity across representative subpages is still insufficiently evidenced.

## Target without mascot

- Preserve the supplied logo and sidebar.
- Make Home feel like a friendly landing page through composition, pastel destination surfaces, code-native SVG icons, useful whitespace, and a compact human greeting.
- Use colour only for destination identity and contextual orientation; keep prices, permissions, statuses, data rows, forms, and lifecycle facts neutral.
- Use one page-orientation component beneath compatibility wrappers so existing routes migrate without a risky all-at-once rewrite.
- Standardize four screen archetypes: collection, contextual workspace, detail, and guided setup.
- End with one style block per Vue component and a small automated guard against renewed style fragmentation.

## Explicit exclusions

- No mascot, generated bitmap, new illustration pack, impact/gamification metric, backend workflow, permission, pricing, booking, navigation, or API-contract change.
- No literal clone of the reference images.
- No reimplementation of domain logic in Vue.

## Goal-pursuing readiness review

The serial inventory is hardened by the first verifier-controlled task. This analysis records its design rationale only; completion remains exclusively verifier evidence in the YAML work plans.

The program is intentionally ordered as a dependency chain rather than a visual big bang:

1. Harden the complete eleven-task inventory before implementation.
2. Establish theme tokens and one purpose-icon vocabulary.
3. Introduce the canonical page-orientation header behind compatibility wrappers.
4. Recompose Home on those foundations.
5. Standardize collection/workspace and detail/setup archetypes.
6. Migrate bespoke authenticated contexts to those archetypes.
7. Consolidate style fragments in two bounded passes.
8. Close with fresh desktop and mobile evidence across five distinct route families.

Repeated required paths are deliberate sequential handoffs, not accidental duplicate ownership. `AuthenticatedShellView.vue` first receives theme/icon support and later Home composition. Header wrappers first establish compatibility and later participate in route adoption. Business and shared view files are migrated before their style blocks are mechanically consolidated.

The style audit has two modes because a whole-app guard cannot pass while the next serial cleanup task still owns known fragmented files. The shared/Business task therefore runs `--scope=shared-business`; the successor runs `--scope=all`, and both final runtime audits repeat the whole-app guard.

The visual claim is bounded and observable. Desktop and mobile closeout each require separate Home, Work collection, Calendar context, object-detail, and Business-setup screenshots plus an authenticated runtime trace. The runtime script must discover representative detail and setup destinations from canonical links or authenticated seeded data, never from hardcoded identifiers.

The plan remains frontend-only. Product meaning, permissions, booking rules, pricing rules, and API contracts are explicit non-goals; any discovered need to change them must become separately planned work rather than silently widening this goal.

# Desktop product redesign analysis

## Evidence reviewed

- Product direction: `docs/product-memory.md` and `docs/product-vision.md`.
- Control system: `AGENTS.md`, `docs/codex-fast-path.md`, `docs/implementation-control.md`, `docs/agent-operating-model.yaml`, and `docs/capability-inventory.yaml`.
- Existing frontend work: the verified `frontend-ux-modernization-master` and `main-surfaces-modernization-master` plans, their child Work plan, and current route/surface ownership.
- Reference images: eight screenshots in `docs/work/redesign-frontend/`. They show an application UI with a persistent dark rail, thin separators, compact labels, restrained active states, dense scan-friendly lists, and strong detail hierarchy. They are reference for qualities only; no Linear branding, marks, assets, text, or exact screen structure will be copied.

## Current architecture and reusable UI

- Vue 3 + TypeScript + Vue Router live under `apps/themuffinman/frontend/src`.
- `AuthenticatedShellView.vue` owns the authenticated desktop rail, responsive mobile navigation, compact global actions, contextual Vision entry, and `RouterView` frame.
- `shellRouteRegistry.ts` and `shellDefinitions.ts` own navigation grouping, canonical entry routes, labels, and contextual ownership. No navigation rules will be duplicated in the redesign.
- `styles/base.css` is the shared token and global-control layer. Existing reusable primitives include `AppCard`, `SurfaceHeader`, `SurfaceSection`, `SurfaceRow`, `AppDialog`, `AppActionMenu`, `AppStatus`, and form/editor controls.
- Existing global surfaces (`GlobalSearchEntry`, `UniversalCreateMenu`, `AccountMenu`, `GlobalVisionEntry`) already expose real actions and remain the appropriate components to restyle rather than replace.
- `WorkDiscoveryView.vue` is the first redesign target because it is a complete backend-backed discovery surface: it preserves the `AVAILABLE`/`MY_VISIBLE` preset boundary, search, sorting, scheduled-only filter, pagination, result links, and explicit loading/error/empty recovery.

## Priority order

1. Shared dark-first token layer and persistent desktop shell. This upgrades every authenticated route while retaining the existing responsive fallback and navigation ownership.
2. Global overlay/entry components and `AppCard`, so cross-module controls share the same compact border, hover, focus, and surface language.
3. Work discovery. It has the most complete high-frequency scan-and-act journey and validates the language against real data states.
4. Follow-up slices: Home attention surface, Chat conversation layout, Business operations, Things/Rides discovery, and profile/detail surfaces. These remain separate increments so feature behavior and existing verified plans are not disrupted.

## Safe sequence and exact paths

1. Update `src/styles/base.css` with dark-first semantic tokens, neutral borders, restrained indigo/amber accents, focus visibility, density, and reduced-motion compatibility.
2. Restyle `AuthenticatedShellView.vue` and its existing global entry/menu components for a persistent desktop rail, compact top bar, consistent popovers, and the already implemented mobile fallback. Do not change routes, labels, handlers, or API calls.
3. Restyle `AppCard.vue` and `WorkDiscoveryView.vue` for a list-first Work surface with a clear title/action row, compact filters, structured facts, and deliberately designed loading/error/empty states. Do not change search parameters, event handling, result destination resolution, or backend presentation values.
4. Run type-check, build, static surface checks, then start the existing local services and inspect `/work` and `/work/find` in a real browser. Record verifier-generated evidence only through `make work-verify`.

## Design decisions

- Dark-first means dark is the default authenticated product shell, not a browser-controlled theme toggle. Vision keeps its own existing canvas semantics.
- Surfaces use low-contrast charcoal layering and thin neutral dividers, not gradients, glass, heavy shadows, or promotional cards.
- Sidebar active state is a quiet filled row plus a small status marker; it does not rely on color alone.
- Buttons and filters are compact and clear. Keyboard focus remains prominent, and the existing semantic HTML and labels are retained.
- No API, Java, Spring, persistence, route, business rule, permission, or state-transition change is in scope.

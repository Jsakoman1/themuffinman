# Structural Frontend Redesign Preflight

Date: 2026-07-24
Baseline: `dcb983f4ad2b0354101caa8c1cdc47c624fb8b71`

## Findings

- The authenticated shell has both generic surface rendering and bespoke module views. This creates parallel layout, action, state, and navigation behavior.
- `shellDefinitions.ts` and `shellRouteRegistry.ts` both describe navigation and route semantics. They must converge on one ownership model.
- Work uses one view for Discover and My work. Backend presets are different, but the visual contract does not make scope separation obvious enough.
- Business currently combines discovery, customer bookings, owner profile, services, bookings, calendar, availability, resources, and setup in one mental space.
- `details` and show/hide controls are being used as navigation for important functions. They will be replaced with tabs only where sibling views are equivalent.
- The redesign must preserve backend ownership, permissions, visibility, pricing, capacity, booking, and resource authority.

## Decisions

- One module page owns each top-level module; tabs select a bounded read model and route.
- Every tab definition must declare route, backend scope, permission boundary, empty state, and primary action.
- Work will be one page with Discover, My work, and Applications tabs.
- Business will have explicit customer and owner modes, then owner tabs for Overview, Calendar, Bookings, Services, and Profile.
- Things and Rides will share a Share workspace; Profile and Settings will share a Profile workspace.
- A legacy surface is removed only after its replacement route and runtime evidence are verified.

## Design direction

Apple Human Interface Guidelines are the primary design reference for the web application, adapted for a responsive workspace rather than copied literally from iOS. The implementation must favor clarity, hierarchy, calm surfaces, progressive disclosure, stable labeled tabs, direct manipulation, visible focus, and a small number of predictable actions. Apple guidance explicitly treats tab bars as navigation between sections rather than action controls and recommends limiting tab complexity; the same rule applies to the web tab registry.

The assistant will use a persistent contextual composer pattern: one quiet input anchored to the shell, contextual suggestions when useful, and an inline response bubble or sheet near the current task. It will not be represented by repeated Ask Vision buttons on every page.

References:

- Apple Human Interface Guidelines, Tab bars: https://developer.apple.com/design/human-interface-guidelines/tab-bars
- Material navigation patterns: https://m1.material.io/patterns/navigation.html
- 2026 contextual assistant composer patterns: https://aiuxplayground.com/guides/how-to-design-ai-chat-composer/

## Control disposition

- Prior UX simplification, UX next-evolution, Business booking, and app-remaster plans are baseline-only.
- New scope is structural IA, tab ownership, visible backend scope separation, legacy retirement, and new browser evidence.
- Any route, tab, shared component, API preset, permission display, or responsive behavior change triggers retesting.

## Deep-cut decision

This is a hard-cut structural redesign, not a cosmetic migration. The new Apple-inspired information architecture becomes the only canonical authenticated Web experience. Existing generic and bespoke surfaces may remain temporarily only as compatibility redirects or bounded adapters while their replacements are verified; they may not continue as competing page grammars.

VisionForWeb is mandatory and remains a first-class product feature. The redesign removes repeated Ask Vision buttons and redundant command launchers, not VisionForWeb itself. One persistent contextual composer will provide the Web entry point, preserve current-page context, show inline answers or task sheets, and retain backend-governed routing, permissions, and confirmation.

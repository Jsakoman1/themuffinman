# Shell and information-architecture analysis

## Findings

- `AuthenticatedShellView` already owns primary navigation, account text, logout, and a header-level Vision handoff, while `SurfaceContentView` and individual views still repeat parts of that language.
- The current shell has a useful Core/More grouping, but module labels and page titles can still produce sequences such as `Work / Work`.
- A global account affordance and global Vision affordance should be shell concerns; page content should only expose contextual actions.

## Design decisions

- Keep one shell-owned `h1` contract: the route surface supplies the canonical title, and page content does not repeat it.
- Keep breadcrumbs/eyebrows only when they communicate hierarchy, never as a second title.
- Use a route metadata registry for title, section, contextual Vision prompt, and primary action labels.
- Preserve route URLs and backend APIs; this slice is primarily an ownership and rendering change.

## Risks and mitigations

- Specialized Chat/Calendar/Business branches may bypass the shared header: add route-level contract checks and runtime assertions.
- Removing a duplicated button can remove the only discoverable entry: map each action to a sidebar, page CTA, or detail action before deleting it.
- Responsive shell changes can hide account/Vision controls: test keyboard and narrow viewport behavior.

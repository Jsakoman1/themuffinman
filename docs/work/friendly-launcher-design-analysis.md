# Friendly launcher design analysis

Reference: `docs/work/examples/mobile_example.png`.
Baseline: `a18b811e65001f68d174b42d1574f6c0f2ffc18d`.

## Intent inferred from the reference

The reference is a personal, community-oriented launcher. It answers one question
before anything else: “What would you like to do now?” A person sees a greeting,
four large purpose cards, a small positive personal summary, and a stable five-item
tab bar. Its colour is structural: each purpose has one soft semantic colour and
the card itself is the touch target. The illustration, large icon, and short label
make scanning possible without reading dense summaries.

This is compatible with the existing human-first product direction, but it is not a
literal Apple interface. The retained Apple discipline is clarity, touch target size,
stable navigation, progressive disclosure, and restrained material use. The new
visual character should be warmer, more playful, and more recognisable.

## Current-state comparison

The current mobile Home (`HomeHubView.vue`) is an operational dashboard. It starts
with an attention panel, then repeats Work, Business, Services, Things, People,
Rides, and Calendar sections. This accurately exposes backend-prepared state but
creates several competing reading tasks before a person can start a common action.

The current mobile shell exposes the first three backend navigation modules plus a
text Menu control. That preserves route reachability but does not form a memorable,
purpose-led bottom navigation system. Its text controls are small relative to the
reference's icon-led fixed tabs.

The existing token system is deliberately neutral and mostly monochrome. It has a
single accent plus a few status colours, not a semantic palette with approved module
roles. Adding arbitrary local gradients or per-page colours would fragment that
system rather than establish the desired design language.

## Design gap

| Area | Current behaviour | Target behaviour |
|---|---|---|
| Home priority | Review all activity and sections | Choose one common purpose immediately |
| Main controls | Compact action links inside dense cards | Four to six large, whole-card action targets |
| Attention | Dominant first dashboard panel | One compact, actionable summary beneath the launcher |
| Module detail | Repeated module rows on Home | Detail remains in its owned module route |
| Colour | Neutral surfaces plus a blue accent | Small semantic pastel palette on launch cards only |
| Mobile navigation | Dynamic module labels plus Menu | Fixed, icon-led five-item tab bar with accessible More drawer |
| Desktop | Sidebar and content canvas only | Same launcher grammar with a wider content composition, not a scaled mobile screen |

## Product and technical constraints

- Route destinations, visibility, permissions, counts, status, booking state, and
  allowed actions remain backend-owned. A coloured card cannot imply that a blocked
  action is available.
- The launcher maps only to existing canonical routes. It does not introduce a new
  “Services” or “Share” business domain.
- Attention remains a recovery surface. It is compacted on Home, not discarded.
- Calendar remains a dedicated coordination surface. It is removed from the first
  Home viewport and linked as an intentional follow-up.
- The shared shell must keep keyboard reachability, safe-area padding, dark mode,
  reduced motion, and the contextual Vision composer.
- The palette uses semantic tokens and one shared `HomeActionTile` component. No
  route may invent local colours, radii, or icon treatment.

## Target Home composition

1. Greeting with the current person's name and one friendly purpose sentence.
2. A responsive launcher grid. The first release has four primary cards:
   `Mini jobs & help` → Work, `Share & lend` → Things, `Book services` → Business
   discovery, and `Car sharing` → Rides. Cards use existing canonical routes.
3. A compact “Needs your attention” card only when backend data has an actionable
   item. It shows one count/summary and one route, never a second task list.
4. A small “Your impact” or “Your week” summary only when a backend-owned summary
   is available. It must not fabricate sustainability or activity data.
5. Optional “Continue where you left off” and Calendar entry links below the first
   viewport. Existing detailed section cards do not remain on Home by default.

## Delivery recommendation

Treat this as a shared visual-system change with a focused Home redesign, not a
global restyle. First establish tokens/components and Home data assembly; then adapt
mobile navigation and desktop layout; finally capture fresh light/dark desktop and
mobile evidence. The implementation master is
`docs/work/friendly-launcher-design-master.yaml`.

# Round 24: Frontend Route, Component, Action, and Parity Map

Status: source-trace analysis. Reviewed: 2026-07-22.

## Conclusion

The Web client has 45 routes, 44 concrete surfaces, and one redirect according to
the route-surface audit. Authenticated module navigation is centralized in the app
shell; Vision is a complementary client and handoff layer, not a replacement for a
discoverable Web action.

## Surface classes

| Class | Ownership | Evidence boundary |
|---|---|---|
| Shared shell | app-shell route registry, shell definitions, router | navigation presence is not completion evidence |
| Direct Web modules | Work, Business, Chat, Circles, Things, Rides, profile/settings | API link and visible action are both needed |
| VisionForWeb | shell-hosted semantic client and backend contract | may open or guide canonical surfaces; does not bypass authority |
| Transitional Vision console | `/vision` route | not the authenticated app-shell dependency or landing surface |
| Admin | protected backend/admin consumer class | separate authority model from normal user navigation |
| Native handoff | backend and contract boundary | no native implementation or device proof in this workspace |

## Static review signals

The frontend audit reports no detached routes and one likely-unused component
(`ActivityRail.vue`). It also reports a small set of repeated workflow-action and
permission-presentation candidates. These are review signals; they do not prove
dead code or duplicated business authority. Backend services remain the required
authority for permissions and transitions.

## Parity rule

Web and Vision are equal production clients in product intent, but parity must be
recorded per capability. A Web route alone is insufficient; a Vision intent alone
is insufficient; native handoff alone is contract readiness only. Round 25 joins
these source paths to tests and runtime evidence.

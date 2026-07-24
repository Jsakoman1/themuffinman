# Frontend Remaster — Formatting and State Matrix

Date: 2026-07-23  
Status: implementation recovery in progress; calendar timezone discovery remains intentionally local browser metadata.

## Shared contract

`apps/themuffinman/frontend/src/services/formatters.ts` is the presentation entry point for locale-aware date/time output. `formatDateTime` remains the general formatter; `formatDate`, `formatTime`, `formatCalendarTitle`, and `formatCalendarDay` make common display intent explicit. They do not alter the timestamp or timezone supplied by the backend.

## Residual mapping

| Surface | Residual | Decision | Authority / verification |
| --- | --- | --- | --- |
| Ride detail | Local `toLocaleString` | Use shared `formatDateTime` with explicit medium date/short time options. | Ride DTO timestamp; type-check/build. |
| Thing detail | Local `toLocaleDateString` | Use shared `formatDate` for listed date. | Thing DTO timestamp; type-check/build. |
| Work discovery | Already uses shared `formatDateTime`. | Retain and use fallback for missing schedule. | Work DTO/presentation state. |
| Work applications/detail | Local short date/date-time helpers | Migrated to shared `formatDateTime` with explicit fallbacks/options. | Application DTO; no workflow inference. |
| Chat | Local message timestamp helper | Migrated to shared `formatDateTime`; empty-message fallback preserved. | Chat DTO/realtime state. |
| Notifications | Local update timestamp helper | Migrated to shared formatter; unread/read labels remain DTO-driven. | Notification DTO. |
| Business bookings/availability | Local `en-US` helpers | Migrated to shared formatter; explicit booking timezone display retained. | Booking/schedule DTO timezone and backend conflict rules. |
| Shell calendar | Local calendar title/day/event helpers | Migrated to shared calendar/date/time helpers; browser timezone discovery remains explicit metadata. | Existing calendar route and event DTOs. |
| Shell surface data | Local number/date/date-time helpers | Use shared `formatNumber`, `formatDate`, and `formatDateTime`; locale-specific `en-US` is retained only where the existing shell contract expects it. | Backend DTO values; no state inference. |
| Activity rail | Local activity timestamp helper | Use shared `formatDateTime` with an invalid/missing fallback. | Activity DTO timestamp. |
| Work currency | Local EUR number formatters in quest detail/create | Use shared `formatCurrency` with the existing EUR contract and fallback. | Backend award amount; no business-rule change. |
| Thing and quest detail states | Local loading/error/success markup | Use shared `AppStatus` busy/error/retry/success presentation. | Existing load/action state only; no workflow inference. |

## State vocabulary

Loading uses an `AppStatus` busy state; empty means a successful response with zero items; error preserves retry; stale means displayed data exists while refresh/recovery is pending; success is local action feedback. These labels describe presentation state only. Backend status labels, allowed actions, workflow transitions, visibility, and permissions remain untouched.

## Deferred follow-up

The residual formatter search now finds only the intentional browser timezone lookup used to label calendar output. Thing and quest detail now consume the shared state vocabulary; remaining domain-specific state surfaces require explicit matrix entries before this child closes. No local formatter is treated as permission or state-transition logic.

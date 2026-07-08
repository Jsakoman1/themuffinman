# Plan Index

- Total entries: 149
- Open entries: 19
- Open master plans: 4
- Open regular plans: 15

## Open Master Plans

- `.agents/vision-open-items-implementation-master-plan.md` | `active` | Implement the current open `/vision` hardening items in safe slices while keeping backend-owned orchestration and review behavior explicit.
- `.agents/business-booking-implementation-master-plan.md` | `proposed` | Implement the business booking backend as a modular service-booking domain built on top of the current business profile root while reusing the repository's existing backend-centric layering patterns.
- `.agents/chat-advanced-hardening-master-plan.md` | `unknown` | Extend the shared chat backend toward a more production-ready lifecycle with:
- `.agents/chat-next-master-plan.md` | `unknown` | Turn the current flexible chat foundation into an operationally strong, scalable, feature-complete backend suitable for production growth across direct chat, group chat, circle rooms, quest threads, and application threads.

## Open Plans

- `.agents/business-booking-availability-plan.md` | `proposed` | Introduce recurring availability rules and exceptions plus a backend-owned availability computation surface without creating customer bookings yet.
- `.agents/business-booking-foundation-plan.md` | `proposed` | Establish the booking-ready business root by extending business profiles and introducing owner-managed offerings and booking policy defaults without opening real reservations yet.
- `.agents/business-booking-hardening-and-integrations-plan.md` | `proposed` | Harden the business booking domain with audit history, public media support, event hooks, operational reads, and integration-ready boundaries after the core reservation workflow is stable.
- `.agents/business-booking-public-read-plan.md` | `proposed` | Deliver the public business read surface for mini-site profile, offering catalog, and availability preview using backend-prepared DTOs before reservation mutation workflows are introduced.
- `.agents/business-booking-workflow-plan.md` | `proposed` | Implement real reservation writes and lifecycle transitions for customers and owners using capacity-aware validation, explicit status changes, and separate read surfaces for customer and owner workflows.
- `.agents/chat-compliance-ops-plan.md` | `unknown` | Prepare chat for retention, support, and abuse-handling requirements.
- `.agents/chat-conversation-state-plan.md` | `unknown` | Chat Conversation State Plan
- `.agents/chat-governance-observability-plan.md` | `unknown` | Make chat actions traceable, reviewable, and moderation-ready.
- `.agents/chat-message-receipts-plan.md` | `unknown` | Chat Message Receipts Plan
- `.agents/chat-object-storage-plan.md` | `unknown` | 
- `.agents/chat-prod-hardening-followup-plan.md` | `unknown` | Extend the current chat backend with the next production-facing API surfaces that are still missing:
- `.agents/chat-realtime-reliability-plan.md` | `unknown` | Improve realtime quality and reduce unnecessary event fan-out.
- `.agents/chat-rich-messaging-plan.md` | `unknown` | Expand chat beyond plain text plus one image data URL.
- `.agents/chat-scale-discovery-plan.md` | `unknown` | Remove early scalability bottlenecks from workspace and conversation discovery.
- `.agents/chat-telemetry-audit-plan.md` | `unknown` | Chat Telemetry Audit Plan

_Routing aid only. Use the underlying plan file or plan-completion report for final status._
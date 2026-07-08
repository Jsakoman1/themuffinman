# Control Start

- Plan index: `docs/generated/local-tooling/plan-index-summary.md`
- Audit summary index: `docs/generated/local-tooling/audit-summary-index.md`
- Plan count: `149`
- Open count: `19`
- Open master plans: `4`
- Open plans: `10`
- Codex context review: `docs/generated/local-tooling/codex-context/latest.review.md`
- Temp work products: `0`
- Layered-analysis artifacts: `0`
- Operator-core surfaces: `docs/generated/local-tooling/control-start-summary.md`, `docs/generated/local-tooling/plan-index-summary.md`, `docs/generated/local-tooling/audit-summary-index.md`, `docs/generated/local-tooling/codex-context/latest.review.md`, `docs/generated/local-tooling/targeted-tests-summary.md`
- Archive-only surfaces: `docs/generated/local-tooling/.history/`, `docs/generated/local-tooling/.cache/`

## Operator-Core Audit Targets

- `audit-router` -> `docs/generated/local-tooling/audit-router-summary.md`
- `codex-context` -> `docs/generated/local-tooling/codex-context/latest.review.md`
- `recommend-targeted-tests` -> `docs/generated/local-tooling/targeted-tests-summary.md`
- `audit-doc-sync-required-surfaces` -> `docs/generated/local-tooling/doc-sync-required-surfaces-summary.md`
- `audit-manifest-decision` -> `docs/generated/local-tooling/manifest-decision-summary.md`
- `recommend-validation-preset` -> `docs/generated/local-tooling/validation-preset-summary.md`
- `diff-summary` -> `docs/generated/local-tooling/diff-summary.md`
- `plan-index` -> `docs/generated/local-tooling/plan-index-summary.md`

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

## Temp Work Products

- none

## Layered Analysis Artifacts

- none

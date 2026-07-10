---
machine_kind: plan
machine_status: complete
machine_title: Chat Prod Ready Plan
---

# Chat Prod Ready Plan

Track one feature or workflow change from scope selection through final validation.
For shared planning rules, read `docs/program-planning-model.md` and `docs/change-completion-checklist.md` first.

## Status

Complete.

## Workflow Frame

- Feature tier: tier3-high-risk-multi-layer
- Scope: Establish the first production-ready baseline for chat schema and API behavior, including pagination, idempotency, rate limiting, validation hardening, and message lifecycle endpoints.
- Out of scope: Frontend implementation, non-chat product features, and multi-member group chat.
- Manifest decision: required
- Manifest path: .agents/feature-manifests/chat-prod-ready-manifest.yaml
- Master plan: .agents/chat-prod-ready-master-plan.md

## Routing Snapshot

- Context commands: make diff-summary; make audit-summary-index; make codex-context topic=<topic> intent='<intent>'; make codex-context-explain
- Routing commands: make audit-router files=<csv>; make audit-doc-sync-required-surfaces files=<csv>; make audit-manifest-decision files=<csv>; make resolve-manifest-path files=<csv>; make recommend-validation-preset files=<csv>
- Validation commands: make recommend-validation-preset files=<csv>; make clean-text-noise max_lines=80; make record-validation manifest=.agents/feature-manifests/chat-prod-ready-manifest.yaml command='<command>'
- Closeout commands: make autofill-feature-closeout manifest=.agents/feature-manifests/chat-prod-ready-manifest.yaml files=<csv> generated=<csv> docs=<csv>; make audit-todo; make audit-plan-completion plan=.agents/chat-prod-ready-plan.md manifest=.agents/feature-manifests/chat-prod-ready-manifest.yaml; make audit-validation-evidence-quality; make feature-closeout-audit manifest=.agents/feature-manifests/chat-prod-ready-manifest.yaml; make closeout-report manifest=.agents/feature-manifests/chat-prod-ready-manifest.yaml
- Doc sync commands: TBD
- Generated artifact commands: TBD

## Implementation Slices

- [x] Lock the schema and endpoint baseline for paginated message history, richer conversation summaries, and workspace conversation limiting.
- [x] Add idempotent send-message handling, stronger image validation, and operational config-backed rate limits.
- [x] Add first-class message lifecycle endpoints for update and soft delete with backend-side permissions.
- [x] Add targeted tests for the new schema and API behavior before handing off to the realtime hardening slice.
- [x] Sync required docs and generated contracts touched by this slice.

## Validation Plan

- Targeted checks: chat service and controller tests focused on pagination, idempotency, validation, and message lifecycle.
- Broader checks: Use manifest-backed evidence for every required command and artifact refresh.
- Skipped checks or reasons: none identified yet
- Validation preset: backend-heavy targeted first, full backend suite in closeout.

## Docs and Artifacts

- Expected docs: `docs/business-logic.md`, `docs/domain-technical.md`, and any resolver-reported chat/API docs.
- Expected generated artifacts: frontend contracts and manifest-backed evidence artifacts.
- Temporary work products: none planned unless routing or validation requires them.

## Closeout Gates

- Required closeout checks: make autofill-feature-closeout manifest=.agents/feature-manifests/chat-prod-ready-manifest.yaml files=<csv> generated=<csv> docs=<csv>; make audit-todo; make audit-plan-completion plan=.agents/chat-prod-ready-plan.md manifest=.agents/feature-manifests/chat-prod-ready-manifest.yaml; make audit-validation-evidence-quality; make feature-closeout-audit manifest=.agents/feature-manifests/chat-prod-ready-manifest.yaml; make closeout-report manifest=.agents/feature-manifests/chat-prod-ready-manifest.yaml
- Final response evidence: What changed.; What was validated.; Any remaining risks or not-run checks.
- Backlog follow-up rule: record any intentionally deferred production-hardening work in `docs/implementation-backlog.md` before final closeout.
- Do not mark the plan complete until the scope is actually implemented, the required validation has passed or been explicitly skipped with a reason, and the completion evidence matches reality.

## Open Questions

- Resolver outputs still needed: Resolver still needs the changed-file list to lock docs, artifacts, and validation scope.
- Risks or approvals: none identified yet; implement additively and avoid destructive data rewrites.

## Completion Evidence

- Status: complete
- Changed files: chat controller/service/model/repository/DTO surfaces, new `ChatProperties`, new `ChatRateLimitService`, and `V38__harden_chat_tables.sql`
- Validation evidence: `cd apps/themuffinman && ./mvnw -q -Dtest=ChatServiceTest test`; generated frontend contracts refreshed
- Doc delta summary: synced chat business/domain docs plus agent-operating endpoint, intent, and contract inventories
- Backlog update: none
- Residual risk: attachments still use data URLs rather than external object storage

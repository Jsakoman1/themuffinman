---
machine_kind: plan
machine_status: complete
machine_title: Chat Governance Observability Plan
machine_goal: Make chat actions traceable, reviewable, and moderation-ready.
---

# Chat Governance Observability Plan

Status: complete
Parent: `.agents/chat-next-master-plan.md`

## Goal

Make chat actions traceable, reviewable, and moderation-ready.

## Scope

- Audit events for group rename, participant add/remove, role change, owner transfer, and leave
- Operational reason codes for management mutations
- Admin/debug read surfaces for recent chat audit activity
- Tests and doc sync

## Deliverables

- Expanded `ChatAuditEventType`
- `ChatService` audit integration for management mutations
- Audit filtering/query surface
- Documentation and validation updates

## Completion Evidence

- Status: complete
- Outcome: group rename, participant add/remove, role change, owner transfer, leave, and admin moderation actions are already audited.
- Validation evidence: chat service/controller/admin tests plus backend validation already cover the implemented audit and support surfaces.

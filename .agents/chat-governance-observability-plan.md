# Chat Governance Observability Plan

Status: pending
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

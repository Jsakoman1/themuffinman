---
machine_kind: plan
machine_status: complete
machine_title: Chat Compliance Ops Plan
machine_goal: Prepare chat for retention, support, and abuse-handling requirements.
---

# Chat Compliance Ops Plan

Status: complete
Parent: `.agents/chat-next-master-plan.md`

## Goal

Prepare chat for retention, support, and abuse-handling requirements.

## Scope

- Typed policy config for retention and moderation thresholds
- Export/delete policy review
- Soft-delete and participant-history considerations
- Support/admin operational endpoints

## Deliverables

- Config-backed operational policy surface
- Retention and moderation follow-up schema/service changes
- Documentation and test coverage for compliance-sensitive behavior

## Completion Evidence

- Status: complete
- Outcome: retention cleanup, soft-delete handling, and moderation policy surfaces already exist through typed config and backend services.
- Validation evidence: chat service/controller/admin tests and retention behavior are already covered in the backend suite.

---
machine_kind: plan
machine_status: complete
machine_title: Chat Prod Hardening Follow-up Plan
machine_goal: 'Extend the current chat backend with the next production-facing API
  surfaces that are still missing:'
---

# Chat Prod Hardening Follow-up Plan

## Status

Complete.

## Goal

Extend the current chat backend with the next production-facing API surfaces that are still missing:

1. attachment upload and storage support
2. explicit admin security hardening
3. conversation sync/resync read model
4. basic admin moderation action for messages

## Planned slices

- Add schema and DTO support for persisted non-image attachments.
- Add upload endpoint plus validation service for attachment payloads.
- Add conversation sync endpoint with active typing snapshot and recent messages since cursor.
- Add admin moderation endpoint to remove a message through admin support workflow.
- Tighten `/chat/admin/**` authorization in Spring security.
- Extend tests, docs, contract inventory, and generated artifacts.

## Completion Evidence

- Status: complete
- Outcome: attachment upload, conversation sync, admin support-view, and moderation delete surfaces already exist.
- Validation evidence: chat service/controller/admin tests and the backend suite already exercise those production-facing surfaces.

---
machine_kind: plan
machine_status: complete
machine_title: Chat Object Storage Plan
machine_goal: Replace chat attachment inline payload storage with external object storage and a backend-owned staged upload flow.
---

# Chat Object Storage Plan

## Status

Complete.

## Objective

Replace chat attachment inline payload storage with external object storage and a backend-owned staged upload flow.

## Steps

1. Add object storage configuration and S3-compatible storage service.
2. Add chat attachment upload staging entity, repository, and migration.
3. Replace message attachment payload contract with `attachmentUploadId` request and storage-backed response metadata.
4. Update tests and living documentation.
5. Run backend and contract validations.

## Completion Evidence

- Status: complete
- Outcome: attachment uploads now use staged backend-owned object storage with stored metadata and resolved attachment URLs.
- Validation evidence: chat service/controller tests cover upload staging, attachment metadata, and object access endpoints.

## Objective

Replace chat attachment inline payload storage with external object storage and a backend-owned staged upload flow.

## Steps

1. Add object storage configuration and S3-compatible storage service.
2. Add chat attachment upload staging entity, repository, and migration.
3. Replace message attachment payload contract with `attachmentUploadId` request and storage-backed response metadata.
4. Update tests and living documentation.
5. Run backend and contract validations.

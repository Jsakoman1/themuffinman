# Dora release lifecycle v1 preflight

Observed on 2026-08-12 for the owner-approved release lifecycle remediation.

## Verified baseline

- Dora source `main` was released as `v1.11.0` at
  `249ea82a6b98bd79d63bf08a407e5a9eb188766e`; it includes the owner-gated IDC v0
  capability and does not change a consumer's default workflow.
- The owner-provided known consumers are `DoomsDayStorage` and `TheMuffinMan`.
  Both contain a vendored `dora/` package whose tree matches their declared
  historical release, but neither repository contains Git subtree ancestry for
  that package.
- Both consumer adapters therefore declare a `git_subtree` update command that
  fails with `can't squash-merge: 'dora' was never added`.
- Dora already contains an owner-approved, local-only `upgrade-preview` /
  `upgrade-apply` / `upgrade-rollback` path. It requires a reviewed local source
  descriptor, a scoped approval record, a package backup, and a consumer-local
  `.dora/bootstrap-source.yaml` pin record.
- Existing historical snapshot consumers lack the required bootstrap source
  record. The upgrade implementation also copies the reviewed target package
  without persisting the target ref/checksum, so the current pin would remain
  stale after a successful apply.

## Remediation boundary

1. Repair and test target-pin persistence in Dora source first.
2. Publish the repair as a new immutable Dora patch release.
3. Migrate each owner-selected snapshot consumer only through an explicit
   release-pinned package record, approval, backup, preview, apply, validation,
   and isolated consumer commit.
4. Do not enter an existing consumer worktree containing unrelated uncommitted
   pilot material. Use a clean, explicitly selected worktree for its release
   update.

This preflight is evidence of the release-process defect and its boundaries. It
is not a consumer compatibility result, a consumer update, an IDC execution, or
a release approval.

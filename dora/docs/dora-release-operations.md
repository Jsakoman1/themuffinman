# Dora release and consumer update operations

This is Dora's durable procedure for publishing Dora/IDC changes and updating
known consumer packages. It replaces no consumer's own release evidence,
approval, pin record, or Git history.

## Authority and scope

- Dora source owns release code, release manifests, this procedure, and the
  owner-maintained known-consumer registry.
- Each consumer owns its `dora/` package, `.dora/bootstrap-source.yaml`, adapter,
  validation evidence, Git commit, rollback decision, and product worktree.
- IDC is released with the Dora package. Updating a package makes IDC available;
  it does not authorize or automatically run IDC.
- Private Context, Bridge authority changes, remote execution, consumer discovery,
  and consumer product code are separate scopes.

The registry contains only owner-named consumers. It is coordination metadata,
not a discovery mechanism or a statement that a consumer is up to date.

## Every Dora release

1. Start from a clean Dora source change-set and run its declared leaf tests,
   Doctor, plan/inventory validation, work verifier, and `git diff --check`.
2. Create `docs/releases/<version>.yaml` from
   `templates/dora-release-manifest.yaml`. Record immutable source commit, scope,
   compatibility posture, affected Dora/IDC contracts, required consumer review,
   and rollback statement. Never claim consumers updated in this source manifest.
3. Commit the verified source and manifest, push `main`, then create and push an
   immutable release tag. A tag is never moved or reused.
4. Compare the release manifest with the owner-maintained consumer registry.
   Select only explicitly approved consumers; do not scan disks or repositories.

## Every selected consumer update

1. Work in a clean, explicitly selected consumer worktree. Do not enter a
   worktree containing unrelated uncommitted material.
2. For a historical snapshot consumer, create its initial
   `.dora/bootstrap-source.yaml` only after proving the installed `dora/` tree
   matches the recorded historical release. The record contains package path,
   immutable ref and checksum; it does not retain a workstation source path.
3. Replace obsolete `distribution: git_subtree` metadata only if the consumer is
   proved to be a snapshot rather than a real subtree. Use
   `release_pinned_package`, the pinned tag/commit and the documented rollback
   commit. Do not pretend a failed subtree command is a valid lifecycle.
4. Create a temporary, reviewed **local** source descriptor for the target tag and
   a scoped, expiring owner approval record. These are inputs to the update, not
   consumer runtime configuration and should not be committed.
5. Run the released Dora `upgrade-preview`. Review added, changed and removed
   package paths before writing anything.
6. Run `upgrade-apply`. It makes a local package backup, replaces only `dora/`,
   and persists the target immutable ref/checksum in the consumer bootstrap
   record. It has no consumer-code, Git, network, Bridge, IDC, Codex or shell
   authority beyond its fixed local copy operation.
7. Run consumer Doctor plus consumer-declared relevant validation. Inspect
   `git diff --check` and the exact changed paths. Commit only the `dora/`
   package, adapter/distribution migration when applicable, and bootstrap pin.
   Keep backup material, temporary descriptors and approvals out of the commit.
8. Push the consumer's approved release branch only after its validation passes.
   Roll back by restoring the recorded backup before commit, or revert the
   isolated consumer release commit after commit.

## What changes require consumer review

| Dora source change | Consumer action |
| --- | --- |
| Dora/IDC contract, CLI, renderer, envelope or Bridge read-profile change | Review release manifest and package diff; update only if the consumer wants that opt-in capability. |
| Project adapter/schema/control contract change | Preview, update package and consumer adapter together; run Doctor and affected controls. |
| Safety/upgrade/rollback repair | Update the package/pin path before relying on the repaired operation. |
| Documentation-only source change | No consumer package update unless the release manifest explicitly says otherwise. |
| Private Context or a future external component | No Dora consumer update unless a separately approved integration contract requires one. |

## Durable evidence locations

- Dora release facts: `docs/releases/<version>.yaml` and immutable tag.
- Known-consumer coordination: `docs/dora-consumer-release-registry.yaml`.
- Actual consumer pin: consumer `.dora/bootstrap-source.yaml`.
- Actual consumer update/rollback: consumer Git commit and its own validation
  evidence. These are never copied into Dora's registry.

This procedure is intentionally manual and owner-mediated. It makes the
repeated steps explicit without creating unattended fleet management.

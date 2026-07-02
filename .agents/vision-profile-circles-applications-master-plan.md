# Vision Profile Circles Applications Master Plan

## Goal

Tighten `/vision` so profile, circles, and applications behave like one terminal-first workspace instead of isolated read-only snapshots plus separate mutation pilots.

## Sequence

1. Audit the current backend read-only and mutation flows for profile, circles, and applications.
2. Upgrade read-only workspace turns so each snapshot carries backend-owned next-step guidance for the same entity family.
3. Keep the floating preview fully backend-driven so the same changing model works for all three entity families.
4. Add targeted tests for workspace guidance and route/catalog continuity.
5. Sync the Vision status ledger after behavior changes.

## Child Slices

### Slice 1: Profile workspace
- strengthen the self-profile read-only message with explicit next-step guidance for username, bio, and location changes
- verify that the preview remains entity-driven

### Slice 2: Circles workspace
- strengthen circles and circle-detail read-only messages with explicit next-step guidance for create, invite, rename, accept, cancel, and delete flows

### Slice 3: Applications workspace
- strengthen applications and application-detail read-only messages with explicit next-step guidance for apply, update, withdraw, approve, and decline flows

## Closeout

- run targeted backend tests
- run frontend type-check and build
- update `docs/vision-status-ledger.md`

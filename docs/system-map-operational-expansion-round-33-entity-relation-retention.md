# Round 33: Entity Relation and Retention Graph

Status: source-trace analysis. Reviewed: 2026-07-22.

## Conclusion

The relation graph identifies the principal aggregate roots and cross-domain actor,
visibility, storage, and audit dependencies. `AppUser` is the shared actor root;
domain services retain authority over relationships and deletion/retention behavior.
Foreign-key and cascade behavior must still be confirmed from individual entities and
Flyway migrations before a destructive change.

## Retention boundary

Retention is explicit for chat content/uploads, revoked authentication tokens, and
Vision conversations/memory compaction through typed configuration and scheduled
services. Other domain records are not assumed to be deleted or retained by a shared
policy merely because they reference an actor.

## Use in feature work

Before changing an entity or migration, review its aggregate owner, outgoing and
incoming relationships, audit/storage records, visibility authority, and any typed
retention configuration. This graph is a navigation index, not a live schema dump.

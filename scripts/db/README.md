# Local database fixtures

These scripts are for the disposable local `side_quest_db` only.

- `cleanup-smoke-fixtures.sql` removes the audited generated smoke/runtime graph, standalone lookup logs, and the current Vision probe history. It preserves the four real local accounts (`jsak`, `admin`, `Nikol`, `test`).
- `seed-meaningful-fixtures.sql` creates a small cross-module dataset owned by `jsak`: five Work records with realistic amounts and applications, two Thing listings with a borrow request, one Ride offer, and one Business offering.

Recommended reset:

```sh
psql -d side_quest_db -v ON_ERROR_STOP=1 -f scripts/db/cleanup-smoke-fixtures.sql
psql -d side_quest_db -v ON_ERROR_STOP=1 -f scripts/db/seed-meaningful-fixtures.sql
```

Take a local `pg_dump` before running cleanup when the database contains data that must be recoverable.

# Mutation Safety

- Generated at: `2026-07-03T09:17:39Z`
- Mutation surfaces scanned: `152`
- Endpoints: `51`
- Service methods: `101`
- Review needed: `1`
- High priority: `0`
- Medium priority: `1`

- `medium` `QuestNewsRetentionService#deleteExpiredNotifications` apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/QuestNewsRetentionService.java missing=missing_side_effect_test_signal

Advisory only: static signals choose review candidates; they do not prove test absence.

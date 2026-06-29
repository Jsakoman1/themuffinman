# Test Fixture Duplication Audit

- Generated at: `2026-06-29T19:55:00Z`
- Tests scanned: `49`
- Extraction candidates: `5`

## Extraction Candidates

- `users` files=37 hits=346: Extend existing `TestFixtures` coverage for repeated users setup instead of adding another local helper.
- `quests` files=15 hits=126: Extend existing `TestFixtures` coverage for repeated quests setup instead of adding another local helper.
- `applications` files=9 hits=70: Extend existing `TestFixtures` coverage for repeated applications setup instead of adding another local helper.
- `circles` files=7 hits=44: Extend existing `TestFixtures` coverage for repeated circles setup instead of adding another local helper.
- `location-settings` files=7 hits=28: Extend existing `TestFixtures` coverage for repeated location-settings setup instead of adding another local helper.

## High Setup Files

- `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java` score=351 focus=users,circles,quests,applications,location-settings
- `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java` score=207 focus=users,quests,applications
- `apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleServiceTest.java` score=158 focus=users,circles
- `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentPlaygroundServiceTest.java` score=110 focus=users
- `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java` score=73 focus=users,quests,applications
- `apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleRelationServiceTest.java` score=57 focus=users,circles
- `apps/themuffinman/src/test/java/com/themuffinman/app/chat/service/ChatServiceTest.java` score=56 focus=users,circles,chat
- `apps/themuffinman/src/test/java/com/themuffinman/app/identity/service/AppUserServiceTest.java` score=51 focus=users,quests,location-settings
- `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java` score=38 focus=users,quests,applications
- `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java` score=34 focus=users,quests,applications
- `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/dto/RequestDtoValidationTest.java` score=32 focus=quests,applications
- `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyServiceTest.java` score=30 focus=users,quests,applications

# Test Fixture Duplication Audit

- Generated at: `2026-07-03T09:17:38Z`
- Tests scanned: `69`
- Extraction candidates: `5`

## Extraction Candidates

- `users` files=50 hits=358: Extend existing `TestFixtures` coverage for repeated users setup instead of adding another local helper.
- `quests` files=24 hits=162: Extend existing `TestFixtures` coverage for repeated quests setup instead of adding another local helper.
- `applications` files=11 hits=78: Extend existing `TestFixtures` coverage for repeated applications setup instead of adding another local helper.
- `circles` files=10 hits=50: Extend existing `TestFixtures` coverage for repeated circles setup instead of adding another local helper.
- `location-settings` files=10 hits=41: Extend existing `TestFixtures` coverage for repeated location-settings setup instead of adding another local helper.

## High Setup Files

- `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/QuestServiceTest.java` score=351 focus=users,circles,quests,applications,location-settings
- `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionConversationServiceTest.java` score=303 focus=users,circles,quests,applications,location-settings
- `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/QuestApplicationServiceTest.java` score=207 focus=users,quests,applications
- `apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleServiceTest.java` score=158 focus=users,circles
- `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/DashboardServiceTest.java` score=82 focus=users,quests,applications
- `apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleRelationServiceTest.java` score=57 focus=users,circles
- `apps/themuffinman/src/test/java/com/themuffinman/app/chat/service/ChatServiceTest.java` score=56 focus=users,circles,chat
- `apps/themuffinman/src/test/java/com/themuffinman/app/identity/service/AppUserServiceTest.java` score=51 focus=users,quests,location-settings
- `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/QuestWorkflowScenarioTest.java` score=38 focus=users,quests,applications
- `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/UserReviewServiceTest.java` score=34 focus=users,quests,applications
- `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/DashboardSummaryAssemblerTest.java` score=34 focus=users,quests,applications
- `apps/themuffinman/src/test/java/com/themuffinman/app/vision/dto/RequestDtoValidationTest.java` score=32 focus=quests,applications

# Test Fixture Duplication Audit

- Generated at: `2026-07-01T14:49:41Z`
- Tests scanned: `69`
- Extraction candidates: `5`

## Extraction Candidates

- `users` files=50 hits=379: Extend existing `TestFixtures` coverage for repeated users setup instead of adding another local helper.
- `quests` files=24 hits=155: Extend existing `TestFixtures` coverage for repeated quests setup instead of adding another local helper.
- `applications` files=10 hits=76: Extend existing `TestFixtures` coverage for repeated applications setup instead of adding another local helper.
- `circles` files=8 hits=47: Extend existing `TestFixtures` coverage for repeated circles setup instead of adding another local helper.
- `location-settings` files=8 hits=31: Extend existing `TestFixtures` coverage for repeated location-settings setup instead of adding another local helper.

## High Setup Files

- `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/QuestServiceTest.java` score=351 focus=users,circles,quests,applications,location-settings
- `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/QuestApplicationServiceTest.java` score=207 focus=users,quests,applications
- `apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleServiceTest.java` score=158 focus=users,circles
- `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentPlaygroundServiceTest.java` score=124 focus=users
- `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionConversationServiceTest.java` score=104 focus=users,quests
- `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/DashboardServiceTest.java` score=82 focus=users,quests,applications
- `apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleRelationServiceTest.java` score=57 focus=users,circles
- `apps/themuffinman/src/test/java/com/themuffinman/app/chat/service/ChatServiceTest.java` score=56 focus=users,circles,chat
- `apps/themuffinman/src/test/java/com/themuffinman/app/identity/service/AppUserServiceTest.java` score=51 focus=users,quests,location-settings
- `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/QuestWorkflowScenarioTest.java` score=38 focus=users,quests,applications
- `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/DashboardSummaryAssemblerTest.java` score=34 focus=users,quests,applications
- `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/UserReviewServiceTest.java` score=34 focus=users,quests,applications

# Symbol Test Links DashboardService

- Next action: `cd apps/themuffinman && ./mvnw test -Dtest=DashboardServiceTest,DashboardReadQueryServiceTest,DashboardSummaryAssemblerTest,DashboardVisionPromptServiceTest,DashboardVoiceServiceTest`, `cd apps/themuffinman && ./mvnw test -Dtest=DashboardServiceTest`, `cd apps/themuffinman && ./mvnw test -Dtest=QuestQueryServiceTest,QuestNewsServiceTest,DashboardServiceTest`

## Details

- Direct tests: apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/DashboardServiceTest.java
- Nearby tests: apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/DashboardReadQueryServiceTest.java | apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/DashboardSummaryAssemblerTest.java | apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/DashboardVisionPromptServiceTest.java
- Nearby tests more: 1
- Scenario hits: id: workmarket-dashboard-read-model | domain: workmarket | risk: medium | scenario: Dashboard sections, applicant actions, open-work groups, and notification destinations must stay backend-prepared and role-aware. | test_files: apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/DashboardServiceTest.java | commands: cd apps/themuffinman && ./mvnw test -Dtest=DashboardServiceTest | id: workmarket-quest-read-model | domain: workmarket | risk: medium | scenario: Quest browsing, recommendation ranking, and quest news read state must stay backend-prepared and viewer-aware. | test_files: apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/QuestQueryServiceTest.java, apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/QuestNewsServiceTest.java, apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/DashboardServiceTest.java | commands: cd apps/themuffinman && ./mvnw test -Dtest=QuestQueryServiceTest,QuestNewsServiceTest,DashboardServiceTest

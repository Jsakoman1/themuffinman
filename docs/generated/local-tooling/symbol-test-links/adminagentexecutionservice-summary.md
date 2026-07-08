# Symbol Test Links AdminAgentExecutionService

- Next action: `cd apps/themuffinman && ./mvnw test -Dtest=AdminAgentExecutionServiceTest,AdminAgentPromptPreparationServiceTest,AdminSyntheticQuestExecutionPlannerTest,AgentOperatingModelValidationTest`, `cd apps/themuffinman && ./mvnw test -Dtest=AdminAgentExecutionServiceTest,AdminSyntheticQuestExecutionPlannerTest`

## Details

- Direct tests: apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentExecutionServiceTest.java
- Nearby tests: apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentPromptPreparationServiceTest.java | apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminSyntheticQuestExecutionPlannerTest.java | apps/themuffinman/src/test/java/com/themuffinman/app/docs/AgentOperatingModelValidationTest.java
- Scenario hits: id: agent-exact-target-fail-closed | domain: agent | risk: high | scenario: Admin-agent mutating prompts must fail closed until exact target resolution and required confirmation exist. | test_files: apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentExecutionServiceTest.java, apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminSyntheticQuestExecutionPlannerTest.java | commands: cd apps/themuffinman && ./mvnw test -Dtest=AdminAgentExecutionServiceTest,AdminSyntheticQuestExecutionPlannerTest

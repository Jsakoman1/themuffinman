# Workflow Slice quest-application

- Generated At: `2026-06-29T12:47:30Z`
- Workflow: `quest-application`
- `state_machine`: `8` entries
## `service_files`

- `{:path: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationWorkflowSupport.java", :category: "backend_service", :domain: "workmarket", :lines: 128}`
- `{:path: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/service/AdminAgentPlaygroundService.java", :category: "backend_service", :domain: "agent", :lines: 971}`

## `tests`

- `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationUseCaseContractTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentPlaygroundServiceTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AgentOperatingScenarioTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/docs/AgentOperatingModelValidationTest.java`

## `frontend_actions`

- `{:path: "apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts", :category: "frontend_contract", :domain: "shared", :lines: 1395}`

## `docs_refs`

- `docs/agent-operating-model.yaml`
- `docs/agent-operating-model/sections/source_of_truth.yaml`
- `docs/domain-technical.md`
- `docs/generated/local-tooling/change-impact-preflight-summary.md`
- `docs/generated/local-tooling/diff-summary.md`
- `docs/generated/local-tooling/domain-packs/workmarket-summary.md`
- `docs/generated/local-tooling/workflow-slices/quest-application-summary.md`
- `docs/workflow-state-machines.md`
- `docs/workflow-state-machines.yaml`

## `recommended_commands`

- `cd apps/themuffinman && ./mvnw test -Dtest=QuestUseCaseContractTest,QuestWorkflowScenarioTest,QuestApplicationUseCaseContractTest,QuestApplicationServiceTest,DashboardServiceTest,AdminAgentPlaygroundServiceTest,AgentOperatingScenarioTest,AgentOperatingModelValidationTest`
- `cd apps/themuffinman && ./mvnw test -Dtest=QuestUseCaseContractTest,QuestWorkflowScenarioTest`
- `cd apps/themuffinman && ./mvnw test -Dtest=QuestApplicationUseCaseContractTest,QuestApplicationServiceTest`
- `cd apps/themuffinman && ./mvnw test -Dtest=DashboardServiceTest`

## `residual_risk`



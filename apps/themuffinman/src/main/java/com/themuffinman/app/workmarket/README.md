# Workmarket Backend Capsule

## Responsibility

Owns quests, applications, quest workflow transitions, reviews, dashboard read models, workmarket notifications, and backend-prepared presentation sections.

## Main Entry Points

- Controllers: `controller/`
- Query/read services: `service/QuestService.java`, `service/DashboardService.java`, `service/DashboardVisionPromptService.java`, `service/DashboardSummaryAssembler.java`, `service/QuestViewAssembler.java`, `service/QuestApplicationViewAssembler.java`
- Mutation use cases: `service/*UseCase.java`
- Domain events and side-effect handlers: `event/`
- Policies and workflow support: `service/QuestAccessPolicyService.java`, `service/QuestStateTransitionService.java`, `service/QuestApplicationWorkflowSupport.java`
- Repositories and mappers: `repository/`, `mapper/`

## Tests

- `src/test/java/com/themuffinman/app/workmarket/service/`
- Prefer scenario coverage for workflow transitions and targeted unit coverage for service policy changes.

## Living Docs

- `docs/business-logic.md`
- `docs/domain-technical.md`
- `docs/agent-operating-model/sections/api.yaml`
- `docs/agent-operating-model/sections/source_of_truth.yaml`

## Forbidden Shortcuts

- Do not put workflow rules or permission decisions in controllers or frontend-only code.
- Do not hardcode adaptive voice defaults in the frontend when `DashboardService`, `DashboardVoiceService`, or typed config can provide the source-of-truth contract instead.
- Do not bypass `DashboardVisionPromptService` or the shared admin-agent planning core when the vision screen needs to decode typed or voice-derived prompts.
- Do not duplicate quest owner, admin, application detail, execution, or term-decision checks outside `QuestAccessPolicyService`.
- Do not map application or quest DTOs from lazy entities without using fetch-safe repository paths.
- Do not add a new quest/application state transition without updating tests and living docs.
- Do not add public methods to `*UseCase` classes other than the single `execute` orchestration entrypoint.
- Do not call notification side effects directly from new mutation use cases when a domain event boundary already fits the workflow.

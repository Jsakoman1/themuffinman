# Workflow State Machines

`docs/workflow-state-machines.yaml` is the machine-readable catalog for named state-machine contracts.

Use this catalog when changing workflow states, allowed transitions, or actor authority for quests, applications, circle relations, chat access, thing borrowing, or future booking flows.

## Maintenance Rules

- Keep enum-backed `states` exactly aligned with the Java enum named by `stateSource.javaClass`.
- Keep transition ids stable and descriptive.
- Use `previous_status`, `any_declared_state`, or `future_booking_flow` only when the runtime behavior is intentionally dynamic or planned.
- Update this catalog with the same change that edits workflow services, statuses, permissions, or intent coverage.
- Keep `WorkflowStateMachineCatalogTest` passing after edits.

## Current Owners

- `quest_lifecycle`: `QuestStateTransitionService`
- `quest_application_lifecycle`: `QuestApplicationWorkflowSupport`
- `circle_relation_lifecycle`: `CircleRelationService`
- `chat_conversation_access_lifecycle`: `ChatService`
- `thing_borrow_request_lifecycle`: `ThingSharingService`
- `booking_lifecycle`: planned booking module contract

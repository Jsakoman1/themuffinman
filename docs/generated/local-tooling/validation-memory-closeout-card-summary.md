# Validation Memory Closeout Card

- Source: `docs/validation-memory.json`
- Generated at: `2026-07-12T09:18:21Z`

## Recommended Read Order During Closeout

## Default Closeout
- `make cleanup-generated-history`
- `make audit-todo`
- `make audit-validation-memory-drift`
- `make validation-memory-closeout-card`
- `make audit-plan-completion plan=<plan-file> [manifest=<manifest-file>]`
- `make feature-closeout-audit manifest=<manifest-file>`

## Add By Change Profile
- Backend logic: `./mvnw test`, `make audit-agent-safety`
- Frontend contract: `npm run validate:contracts`, `npm run type-check`, `npm run build`
- Agent contract: `make generate-agent-operating-model`, `make generate-agent-artifacts`, `make audit-agent-safety`
- Workflow expansion: `./mvnw test -Dtest=AdminAgentExecutionServiceTest,AdminSyntheticQuestExecutionPlannerTest`

## Rule
- Record the exact canonical command string in manifest evidence when validators expect it.

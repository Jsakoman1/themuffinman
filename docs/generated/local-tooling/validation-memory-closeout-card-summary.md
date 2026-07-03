# Validation Memory Closeout Card

- Source: `docs/validation-memory.json`
- Generated at: `2026-07-03T10:04:33Z`

## Recommended Read Order During Closeout
1. docs/codex-fast-path.md
2. docs/validation-memory.md
3. the active plan or master plan
4. the active manifest
5. docs/generated/local-tooling/audit-summary-index.md when a failure needs a smaller focused audit

## Default Closeout
- `make audit-todo`
- `make audit-plan-completion plan=<plan-file> [manifest=<manifest-file>]`

## Add By Change Profile
- Backend logic: `./mvnw test`, `make audit-agent-safety`
- Frontend contract: `npm run validate:contracts`, `npm run type-check`, `npm run build`
- Agent contract: `make generate-agent-operating-model`, `make generate-agent-artifacts`, `make audit-agent-safety`
- Workflow expansion: `./mvnw test -Dtest=AdminAgentExecutionServiceTest,AdminSyntheticQuestExecutionPlannerTest`

## Rule
- Record the exact canonical command string in manifest evidence when validators expect it.

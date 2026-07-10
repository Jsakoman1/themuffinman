# Validation Memory Drift

- Markdown: `docs/validation-memory.md`
- JSON: `docs/validation-memory.json`
- Issue Count: `0`

## Checks

- `ok` frontend contract canonical commands: `npm run validate:contracts`
- `ok` frontend contract canonical commands: `npm run type-check`
- `ok` frontend contract canonical commands: `npm run build`
- `ok` backend logic canonical commands: `./mvnw test`
- `ok` backend logic canonical commands: `make audit-agent-safety`
- `ok` agent contract canonical commands: `make generate-agent-operating-model`
- `ok` agent contract canonical commands: `make generate-agent-artifacts`
- `ok` agent contract canonical commands: `make audit-agent-safety`
- `ok` workflow expansion canonical commands: `./mvnw test -Dtest=AdminAgentExecutionServiceTest,AdminSyntheticQuestExecutionPlannerTest`
- `ok` closeout canonical commands: `make closeout-driver plan=<plan-file> manifest=<manifest-file>`
- `ok` closeout canonical commands: `make cleanup-generated-history`
- `ok` closeout canonical commands: `make audit-todo`
- `ok` closeout canonical commands: `make audit-plan-completion plan=<plan-file> [manifest=<manifest-file>]`
- `ok` primary human doc: `docs/validation-memory.md`
- `ok` schema reference: `docs/validation-memory.schema.json`
- `ok` gateway auto-include note: `auto-include`

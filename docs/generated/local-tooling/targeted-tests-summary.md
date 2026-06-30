# Targeted Tests

- Why: This is a targeted recommendation report, not a replacement for full validation.; Use full `cd apps/themuffinman && ./mvnw test` for high-risk backend behavior, schema, or broad cross-domain changes.
- Next action: `make audit-documentation`, `make audit-doc-canonical-phrases`, `make audit-generated-artifact-freshness`

## Details

- Original File Count: 25
- Filtered File Count: 0
- Excluded File Count: 0
- Recommended commands: command: make audit-documentation | reason: Docs, plans, or agent artifacts changed. | confidence: high | covers: docs/agent-operating-model.md, docs/change-completion-checklist.md, docs/codex-fast-path.md | command: make audit-doc-canonical-phrases | reason: Protected documentation wording may be affected by docs or agent-safety edits. | confidence: medium | covers: docs/agent-operating-model.md, docs/change-completion-checklist.md, docs/codex-fast-path.md | uncovered: Does not validate Java-side agent operating model tests. | command: make audit-generated-artifact-freshness | reason: Generated artifacts, generation scripts, or Make targets changed. | confidence: high | covers: scripts/audits/CodexJavaAstContext.java, scripts/audits/audit-api-contract-drift.rb, scripts/audits/audit-change-impact-preflight.rb | command: make audit-generated-commit-scope | reason: Classifies changed generated artifacts before closeout. | confidence: medium | uncovered: Advisory only; reviewer still chooses which generated files belong in the changeset. | command: cd apps/themuffinman && ./mvnw test -Dtest=AgentOperatingScenarioTest,AdminAgentCapabilityBoundaryTest | reason: Regression scenario `agent-exact-target-fail-closed` covers Admin-agent mutating prompts must fail closed until exact target resolution and required confirmation exist. | confidence: high | covers: apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AgentOperatingScenarioTest.java, apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentCapabilityBoundaryTest.java
- Direct tests: apps/themuffinman/src/test/java/com/themuffinman/app/agent/sandbox/SandboxGenerationPlannerTest.java | apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentCapabilityBoundaryTest.java | apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentGoldenPromptMatrixTest.java
- Direct tests more: 3
- Notes: This is a targeted recommendation report, not a replacement for full validation. | Use full `cd apps/themuffinman && ./mvnw test` for high-risk backend behavior, schema, or broad cross-domain changes.

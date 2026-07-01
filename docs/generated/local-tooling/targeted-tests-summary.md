# Targeted Tests

- Why: This is a targeted recommendation report, not a replacement for full validation.; Use full `cd apps/themuffinman && ./mvnw test` for high-risk backend behavior, schema, or broad cross-domain changes.
- Next action: `npm --prefix apps/themuffinman/frontend run type-check`, `npm --prefix apps/themuffinman/frontend run build`, `make audit-documentation`

## Details

- Original File Count: 19
- Filtered File Count: 0
- Excluded File Count: 0
- Recommended commands: command: npm --prefix apps/themuffinman/frontend run type-check | reason: Frontend TypeScript or Vue files changed. | confidence: high | covers: apps/themuffinman/frontend/src/components/app/useAppTopbarState.ts, apps/themuffinman/frontend/src/modules/chat/README.md, apps/themuffinman/frontend/src/modules/social/composables/useCirclesView.ts | command: npm --prefix apps/themuffinman/frontend run build | reason: Covers Vite/build-time imports, generated contract usage, and production bundling. | confidence: medium | covers: apps/themuffinman/frontend/src/components/app/useAppTopbarState.ts, apps/themuffinman/frontend/src/modules/chat/README.md, apps/themuffinman/frontend/src/modules/social/composables/useCirclesView.ts | uncovered: Does not replace browser-level interaction checks for visual or workflow changes. | command: make audit-documentation | reason: Docs, plans, or agent artifacts changed. | confidence: high | covers: .agents/god-plans/vision-god-plan.yaml, docs/domain-technical.md, docs/product-memory.md | command: make audit-doc-canonical-phrases | reason: Protected documentation wording may be affected by docs or agent-safety edits. | confidence: medium | covers: docs/domain-technical.md, docs/product-memory.md, docs/source-of-truth-inventory.md | uncovered: Does not validate Java-side agent operating model tests. | command: cd apps/themuffinman && ./mvnw test -Dtest=QuestUseCaseContractTest,QuestWorkflowScenarioTest | reason: Regression scenario `workmarket-quest-lifecycle` covers Quest create, update, delete, start, complete, and term-change flows must resolve actors, validate state, persist changes, and publish expected notifications. | confidence: high | covers: apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/QuestUseCaseContractTest.java, apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/QuestWorkflowScenarioTest.java
- Recommended commands more: 3
- Direct tests: apps/themuffinman/src/test/java/com/themuffinman/app/chat/service/ChatServiceTest.java | apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleAdminOverviewAssemblerTest.java | apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleDiscoveryServiceTest.java
- Direct tests more: 9
- Notes: This is a targeted recommendation report, not a replacement for full validation. | Use full `cd apps/themuffinman && ./mvnw test` for high-risk backend behavior, schema, or broad cross-domain changes.

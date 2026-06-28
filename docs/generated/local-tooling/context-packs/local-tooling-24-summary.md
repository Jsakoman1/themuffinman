# Context Pack local-tooling-24

- Generated At: `2026-06-28T21:04:13Z`
- Topic: `local-tooling-24`
- File Count: `80`
## `domains`

- `agent`
- `identity`
- `location`
- `shared`
- `social`
- `workmarket`

## `files`

- `{:path: ".agents/feature-manifests/agent-control-phase-two-manifest.yaml", :category: "other", :domain: "agent", :lines: 66}`
- `{:path: ".agents/feature-manifests/agent-operating-refactor-manifest.yaml", :category: "other", :domain: "agent", :lines: 68}`
- `{:path: ".agents/feature-manifests/agent-safety-enforcement-round2-manifest.yaml", :category: "other", :domain: "agent", :lines: 51}`
- `{:path: ".agents/feature-manifests/agent-safety-upgrade-manifest.yaml", :category: "other", :domain: "agent", :lines: 51}`
- `{:path: ".agents/feature-manifests/backend-audit-domain-tagging-manifest.yaml", :category: "other", :domain: "shared", :lines: 49}`
- `{:path: ".agents/feature-manifests/backend-audit-identity-dto-tightening-manifest.yaml", :category: "other", :domain: "shared", :lines: 45}`
- `{:path: ".agents/feature-manifests/backend-audit-location-dto-tightening-manifest.yaml", :category: "other", :domain: "shared", :lines: 45}`
- `{:path: ".agents/feature-manifests/backend-audit-manifest-cleanup-manifest.yaml", :category: "other", :domain: "shared", :lines: 44}`
- `{:path: ".agents/feature-manifests/backend-audit-tiering-manifest.yaml", :category: "other", :domain: "shared", :lines: 51}`
- `{:path: ".agents/feature-manifests/backend-audit-tightening-manifest.yaml", :category: "other", :domain: "shared", :lines: 49}`
- `{:path: ".agents/feature-manifests/executor-readiness-program-manifest.yaml", :category: "other", :domain: "shared", :lines: 71}`
- `{:path: ".agents/templates/feature-completion-manifest.template.yaml", :category: "other", :domain: "shared", :lines: 30}`
- `{:path: ".agents/templates/feature-implementation-plan.template.md", :category: "other", :domain: "shared", :lines: 30}`
- `{:path: "AGENTS.md", :category: "other", :domain: "shared", :lines: 152}`
- `{:path: "Makefile", :category: "other", :domain: "shared", :lines: 274}`
- `{:path: "apps/themuffinman/frontend/src/modules/workmarket/composables/quest-detail/createQuestDetailViewState.ts", :category: "frontend_composable", :domain: "workmarket", :lines: 64}`
- `{:path: "apps/themuffinman/frontend/src/modules/workmarket/composables/quest-detail/useQuestDetailMutationActions.ts", :category: "frontend_composable", :domain: "workmarket", :lines: 150}`
- `{:path: "apps/themuffinman/frontend/src/modules/workmarket/composables/quest-detail/useQuestDetailUiActions.ts", :category: "frontend_composable", :domain: "workmarket", :lines: 165}`
- `{:path: "apps/themuffinman/frontend/src/modules/workmarket/views/QuestDetailView.vue", :category: "frontend_view", :domain: "workmarket", :lines: 395}`
- `{:path: "apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/AdminUserDetailService.java", :category: "backend_service", :domain: "identity", :lines: 52}`
- `{:path: "apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/AppUserService.java", :category: "backend_service", :domain: "identity", :lines: 221}`
- `{:path: "apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/UserProfileViewService.java", :category: "backend_service", :domain: "identity", :lines: 69}`
- `{:path: "apps/themuffinman/src/main/java/com/themuffinman/app/location/service/AdminDatabaseMetricsService.java", :category: "backend_service", :domain: "location", :lines: 54}`
- `{:path: "apps/themuffinman/src/main/java/com/themuffinman/app/location/service/LocationLookupService.java", :category: "backend_service", :domain: "location", :lines: 242}`
- `{:path: "apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleDiscoveryService.java", :category: "backend_service", :domain: "social", :lines: 77}`

## `related_tests`

- `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentCapabilityBoundaryTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentGoldenPromptMatrixTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentPlaygroundServiceTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AgentOperatingScenarioTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/docs/AgentOperatingModelValidationTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/identity/controller/AuthControllerTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/identity/mapper/AppUserMgrTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/identity/security/JwtAuthFilterTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/identity/security/JwtServiceTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/identity/service/AdminUserDetailServiceTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/identity/service/AppUserServiceTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/identity/service/AuthServiceTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/identity/service/UserProfileViewServiceTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/location/service/LocationLookupServiceTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleDiscoveryServiceTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleMembershipServiceTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleRelationServiceTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleServiceTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/dto/RequestDtoValidationTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/mapper/QuestMgrTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/mapper/QuestNewsMgrTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`

## `related_docs`

- `docs/agent-operating-model.md`
- `docs/agent-operating-model.yaml`
- `docs/business-logic.md`
- `docs/domain-technical.md`
- `docs/location-services.md`

## `related_migrations`

- `apps/themuffinman/src/main/resources/db/migration/V29__add_circle_request_reference_to_news.sql`
- `apps/themuffinman/src/main/resources/db/migration/V2__create_quest_tables.sql`
- `apps/themuffinman/src/main/resources/db/migration/V30__add_public_approved_workers_flag.sql`
- `apps/themuffinman/src/main/resources/db/migration/V3__create_quest_application_table.sql`
- `apps/themuffinman/src/main/resources/db/migration/V4__insert_column_hashPassword_on_appUser.sql`
- `apps/themuffinman/src/main/resources/db/migration/V5__add_role_to_app_user.sql`
- `apps/themuffinman/src/main/resources/db/migration/V6__normalize_quest_application_statuses.sql`
- `apps/themuffinman/src/main/resources/db/migration/V7__add_quest_term_fields.sql`
- `apps/themuffinman/src/main/resources/db/migration/V8__add_quest_reopened_at.sql`
- `apps/themuffinman/src/main/resources/db/migration/V9__create_quest_news_item_table.sql`

## `relevant_audits`

- `{:summary: "docs/generated/local-tooling/diff-summary.md", :score: 86}`
- `{:summary: "docs/generated/local-tooling/change-impact-preflight-summary.md", :score: 83}`
- `{:summary: "docs/generated/local-tooling/closeout-bundle-summary.md", :score: 31}`
- `{:summary: "docs/generated/local-tooling/audit-router-summary.md", :score: 30}`
- `{:summary: "docs/generated/local-tooling/fast-check-report-summary.md", :score: 30}`
- `{:summary: "docs/generated/local-tooling/test-surface-inventory-summary.md", :score: 20}`
- `{:summary: "docs/generated/local-tooling/read-surface-inventory-summary.md", :score: 19}`
- `{:summary: "docs/generated/local-tooling/repo-map-summary.md", :score: 10}`
- `{:summary: "docs/generated/local-tooling/duplicate-logic-audit-summary.md", :score: 9}`
- `{:summary: "docs/generated/local-tooling/doc-canonical-phrases-summary.md", :score: 8}`


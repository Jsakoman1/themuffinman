# Changeset Playbook

- Generated At: `2026-06-29T19:55:06Z`
- Original File Count: `86`
- Filtered File Count: `0`
- Excluded File Count: `0`
## `excluded_files_sample`


- Changed File Count: `86`
## `categories`

- `backend_controller`
- `backend_dto`
- `backend_mapper`
- `backend_service`
- `docs`
- `other`
- `script`

## `domains`

- `common`
- `identity`
- `location`
- `shared`
- `social`
- `workmarket`

## `files`

- `.agents/templates/feature-completion-manifest.template.yaml`
- `.agents/templates/feature-implementation-plan.template.md`
- `AGENTS.md`
- `apps/themuffinman/src/main/java/com/themuffinman/app/identity/controller/AuthController.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/identity/mapper/AppUserMgr.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/identity/mapper/AuthMgr.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/AuthService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/UserProfileViewService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/location/controller/LocationLookupController.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/location/service/AdminDatabaseMetricsService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/location/service/LocationLookupService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/location/service/LocationSettingsService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/social/dto/BulkCircleMembershipUpdateDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/social/dto/CircleRelationDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/social/dto/CircleSearchResultDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleDiscoveryService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleViewAssembler.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/SocialPresentationHelper.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/SocialRelationActionHelper.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/QuestController.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/DashboardNotificationItemDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/QuestApplicationResponseDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/QuestDetailExecutionSectionDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/QuestNewsItemResponseDTO.java`

- `manifest_decision`: `9` entries
- `manifest_resolution`: `7` entries
- `validation_preset`: `9` entries
## `doc_targets`

- `docs/agent-operating-model.md`
- `docs/agent-operating-model.yaml`
- `docs/business-logic.md`
- `docs/codex-local-tooling-todo.md`
- `docs/domain-technical.md`
- `docs/feature-delivery-workflow.md`
- `docs/location-services.md`

## `source_reports`

- `docs/generated/local-tooling/diff-summary.md`
- `docs/generated/local-tooling/audit-router-summary.md`
- `docs/generated/local-tooling/doc-sync-preflight-summary.md`
- `docs/generated/local-tooling/doc-sync-required-surfaces-summary.md`
- `docs/generated/local-tooling/manifest-decision-summary.md`
- `docs/generated/local-tooling/manifest-path-resolution-summary.md`
- `docs/generated/local-tooling/validation-preset-summary.md`

## `ordered_actions`

- `{:step: 1, :kind: "read", :title: "Review changeset shape", :commands: ["make diff-summary"], :outputs: ["docs/generated/local-tooling/diff-summary.md"], :purpose: "See domain/category grouping before opening files."}`
- `{:step: 2, :kind: "decide", :title: "Confirm manifest path", :commands: ["make audit-manifest-decision", "make resolve-manifest-path"], :outputs: ["docs/generated/local-tooling/manifest-decision-summary.md", "docs/generated/local-tooling/manifest-path-resolution-summary.md"], :purpose: "Manifest is required before final closeout.", :decision: "required", :resolved_manifest: ".agents/feature-manifests/codex-workflow-lean-context-manifest.yaml"}`
- `{:step: 3, :kind: "audit", :title: "Run focused audits", :commands: ["make audit-read-surface-inventory", "make audit-repository-fetch", "make audit-mapper-usage", "make audit-api-contract-drift", "make audit-endpoint-callsite-linker", "make endpoint-contract-packs", "make audit-doc-sync-preflight", "make audit-doc-sync-required-surfaces", "make audit-documentation", "make audit-doc-canonical-phrases", "make audit-test-gap-recommendations", "make audit-summary-index"], :outputs: ["docs/generated/local-tooling/audit-router-summary.md", "docs/generated/local-tooling/doc-sync-preflight-summary.md"], :purpose: "Pick the smallest report set that matches the current files."}`
- `{:step: 4, :kind: "update", :title: "Update living docs and agent artifacts", :files: ["docs/feature-delivery-workflow.md", "docs/business-logic.md", "docs/domain-technical.md", "docs/agent-operating-model.md", "docs/agent-operating-model.yaml", "docs/location-services.md", "docs/codex-local-tooling-todo.md"], :purpose: "Keep business, technical, and agent-safety docs synchronized with the changeset."}`
- `{:step: 5, :kind: "validate", :title: "Run preset validation", :commands: ["make recommend-targeted-tests", "make closeout-bundle manifest=.agents/feature-manifests/codex-workflow-lean-context-manifest.yaml", "make feature-closeout-audit manifest=.agents/feature-manifests/codex-workflow-lean-context-manifest.yaml", "cd apps/themuffinman && ./mvnw test -Dtest=CoreConceptsTest,GlobalExceptionHandlerTest,RichTextInputValidatorTest,AuthControllerTest,AppUserMgrTest,JwtAuthFilterTest,JwtServiceTest,AdminUserDetailServiceTest", "make audit-documentation", "make audit-doc-canonical-phrases", "make audit-generated-artifact-freshness", "make audit-generated-commit-scope", "make audit-agent-safety"], :outputs: ["docs/generated/local-tooling/targeted-tests-summary.md", "docs/generated/local-tooling/audit-router-summary.md", "docs/generated/local-tooling/doc-sync-required-surfaces-summary.md", "docs/generated/local-tooling/closeout-bundle-summary.md"], :purpose: "Use one preset instead of manually assembling commands."}`
- `{:step: 6, :kind: "closeout", :title: "Use resolved manifest path for final closeout", :commands: ["make closeout-bundle manifest=.agents/feature-manifests/codex-workflow-lean-context-manifest.yaml"], :purpose: "Resolved manifest path can be reused directly in closeout commands."}`


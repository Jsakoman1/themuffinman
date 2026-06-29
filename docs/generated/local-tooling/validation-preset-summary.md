# Validation Preset

- Preset: `manifest-required`
## `reasons`

- `Manifest decision is `required`.`
- `Changed files span 7 categories.`
- `Changed files span 6 domains.`
- `Preset `manifest-required` keeps validation selection deterministic for 86 files.`

## `commands`

- `make recommend-targeted-tests`
- `make closeout-bundle manifest=.agents/feature-manifests/codex-workflow-lean-context-manifest.yaml`
- `make feature-closeout-audit manifest=.agents/feature-manifests/codex-workflow-lean-context-manifest.yaml`
- `make audit-documentation`
- `make audit-doc-canonical-phrases`
- `make audit-generated-artifact-freshness`
- `make audit-generated-commit-scope`
- `make audit-agent-safety`

## `supporting_reports`

- `docs/generated/local-tooling/targeted-tests-summary.md`
- `docs/generated/local-tooling/audit-router-summary.md`
- `docs/generated/local-tooling/doc-sync-required-surfaces-summary.md`
- `docs/generated/local-tooling/closeout-bundle-summary.md`

- Manifest Decision: `required`
- `manifest_resolution`: `7` entries
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

- Changed File Count: `86`
- Generated At: `2026-06-29T19:55:13Z`
- Original File Count: `86`
- Filtered File Count: `0`
- Excluded File Count: `0`
## `excluded_files_sample`


## `files_considered`

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


# STD-IDENTITY-SURFACE-STANDARDIZATION Plan

Source: `docs/implementation-backlog.md`
Group: `implementation`
Risk: `medium`
Master order: 85 of 89

## Backlog Item

Standardize identity transport naming and profile/read surfaces so auth, app-user, and profile-view contracts follow one consistent model.

## Source Findings

- [`.agents/system-standardization-audit-findings.md`](/Users/jsakoman/Desktop/themuffinman/.agents/system-standardization-audit-findings.md)
- Identity services/controllers: `AuthService`, `AppUserService`, `AppUserLookupService`, `UserProfileViewService`, `AuthController`, `AppUserController`
- Identity contracts: `AuthResponseDTO`, `LoginRequestDTO`, `RegisterRequestDTO`, `AppUserResponseDTO`, `AppUserRequestDTO`, `UserProfileViewDTO`, `AdminUserDetailDTO`
- Naming drift: request/response suffix usage, profile-navigation and profile-view consistency

## Implementation Plan

- [x] Inventory identity request, response, and detail/view DTO families.
- [x] Normalize naming and shape rules for auth and app-user contracts.
- [x] Tighten read-model assembly for profile views and admin views.
- [x] Update frontend identity surfaces only where they consume the normalized contract shape.
- [x] Sync docs and generated artifacts after the contract update.

## Expected Validation

- `cd apps/themuffinman && ./mvnw test -Dtest=AuthServiceTest,AppUserServiceTest,UserProfileViewServiceTest,AdminUserDetailServiceTest,AuthControllerTest,AppUserControllerTest`
- `npm --prefix apps/themuffinman/frontend run type-check`
- `ruby scripts/todo-audit.rb`

## Completion Evidence

- Status: complete
- Changed files: `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/IdentityUserSummaryAssembler.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/UserProfileViewService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/AdminUserDetailService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/identity/README.md`, `docs/domain-technical.md`, `apps/themuffinman/src/test/java/com/themuffinman/app/identity/service/IdentityUserSummaryAssemblerTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/identity/service/UserProfileViewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/identity/service/AdminUserDetailServiceTest.java`
- Validation evidence: `./mvnw test -Dtest=IdentityUserSummaryAssemblerTest,UserProfileViewServiceTest,AdminUserDetailServiceTest,AuthServiceTest,AppUserServiceTest,AuthControllerTest,AppUserControllerTest` passed; `npm run type-check` passed
- Doc delta summary: identity backend now documents a dedicated `IdentityUserSummaryAssembler` boundary that owns enriched `AppUserResponseDTO` assembly for profile-view and admin-detail surfaces
- Backlog update: removed from open backlog.
- Residual risk: none known

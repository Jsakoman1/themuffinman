# Identity Backend Capsule

## Responsibility

Owns application users, authentication-facing user data, roles, profiles, account lookup, and security integration points used by other domains.

## Main Entry Points

- Controllers: `controller/`
- Services: `service/AppUserService.java`, `service/AppUserLookupService.java`, `service/AdminUserDetailService.java`, `service/IdentityUserSummaryAssembler.java`, `service/UserProfileViewService.java`
- Repository: `repository/AppUserRepository.java`
- Mapper: `mapper/AppUserMgr.java`
- Security: `security/`

## Tests

- `src/test/java/com/themuffinman/app/identity/`
- Cross-domain user behavior may also be covered by workmarket and social scenario tests.

## Living Docs

- `docs/business-logic.md`
- `docs/domain-technical.md`
- `docs/agent-operating-model/sections/api.yaml`

## Forbidden Shortcuts

- Do not duplicate user lookup logic in feature services; use identity services or repositories intentionally.
- Do not expose password or credential details through DTOs.
- Do not bypass role checks in admin-facing user flows.

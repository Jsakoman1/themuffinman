# Vision Voice Backend Contract

Purpose: add a typed backend contract for voice capability and default behavior before frontend speech features rely on it.

## Goal

Expose one authenticated voice config surface from the backend so `/vision` reads a canonical capability contract instead of hardcoding speech defaults in the frontend.

## Scope

- `apps/themuffinman/src/main/java/com/themuffinman/app/config/VoiceProperties.java`
- `apps/themuffinman/src/main/resources/application.properties`
- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/DashboardVoiceConfigDTO.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/DashboardController.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardService.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`

## Checklist

- [x] Add typed voice properties.
- [x] Add a dashboard voice-config DTO and service mapping.
- [x] Add an authenticated dashboard endpoint for the voice config.
- [x] Cover the mapping with backend tests.

## Validation

- `./mvnw test -Dtest=DashboardServiceTest,AgentOperatingModelValidationTest`

## Completion Evidence

- Status: complete
- Execution status: complete
- Validation:
  - `./mvnw test -Dtest=DashboardServiceTest`
- Notes:
  - Added typed `app.voice.*` configuration through `VoiceProperties`.
  - Exposed authenticated `GET /dashboard/me/voice-config` through `DashboardController` and `DashboardService`.
  - Kept anonymous access fail-closed by disabling voice capability flags when no authenticated user exists.
- Persistent backlog item: none

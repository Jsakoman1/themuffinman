# Backend Social/Location DTO Standardization Plan

Purpose: normalize the remaining social and location backend DTO families so shared relation, request, response, option, and debug/report shapes follow one naming taxonomy.

## Scope

- Keep social relation/read-model naming consistent across circle, profile, search, request, and bulk-update flows.
- Keep location request/response/option/report DTO naming consistent across lookup, settings, and debug surfaces.
- Preserve existing endpoint contracts unless a rename is purely internal or all references are updated in one pass.

## Current Progress

- Renamed `BulkCircleMembershipAction` to `BulkCircleMembershipActionDTO`.
- Renamed `CircleRelationStatus` to `CircleRelationStatusDTO`.
- Renamed `LocationDebugStatusDTO` to `LocationDebugStatusViewDTO`.
- Renamed `DatabaseTableStatusDTO` to `DatabaseTableStatusViewDTO`.

## Completion Evidence

- Status: complete
- Validation evidence: `CircleServiceTest`, `UserProfileViewServiceTest`, `LocationLookupServiceTest`, `AuthControllerTest`, `AuthServiceTest`
- Backlog update: the remaining social/location naming and shape drift is now covered by the completed standardization passes.
- Residual risk: future social or location DTO additions should continue to follow the same suffix taxonomy.

## Validation

- `CircleServiceTest`
- `UserProfileViewServiceTest`
- `LocationLookupServiceTest`
- `AuthControllerTest`
- `AuthServiceTest`

## Completion Criteria

- Social relation and bulk membership contract types follow the same suffix taxonomy as the rest of the backend.
- Location debug/report types are separated from request/response/option payloads by naming.
- Remaining social/location DTOs are either standardized or explicitly left as deliberate exceptions.

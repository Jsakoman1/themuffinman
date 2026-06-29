# Backend Model Standardization Master Plan

Purpose: reduce backend drift by making similar domains share the same model shapes, naming rules, assembler patterns, and service boundaries.

## Goal

Make backend transport models and service layers look and behave the same across domains unless there is a clear domain-specific reason not to.

The first pass standardizes the patterns; it does not force a wholesale rename of every existing type in one batch.

## Execution Model

- Use this master plan to coordinate a sequence of narrower child plans.
- Keep each child plan scoped to one model family or one service-boundary cleanup slice.
- Finish one family completely before moving to the next family unless a dependency forces overlap.
- If a child plan expands beyond one safe pass, split it further and record the remainder with a stable backlog ID.
- While this master plan is active, continue through every listed child plan and the final closeout pass without
  asking for user intervention unless a real blocker, unsafe ambiguity, or conflicting user change appears.

## What is currently non-standard

- DTO naming is inconsistent across packages.
- Some transport types use `RequestDTO` / `ResponseDTO` / `ListResponseDTO`, while others use naked names like `LoginRequest`, `RegisterRequest`, `AuthResponse`, `ActionResults`, `BulkCircleMembershipAction`, and `CircleRelationStatus`.
- `dto/` packages mix request/response payloads, option enums, status enums, view models, and section models without one consistent taxonomy.
- Workmarket has a dense family of dashboard, quest detail, application detail, presentation, and section DTOs, but the naming rules are not fully normalized.
- Social, location, identity, and chat each have their own local conventions for summaries, lists, options, and admin-facing read models.
- Several backend services and controllers are too broad and mix query, mutation, policy, mapping, and presentation responsibilities.
- Mapping and assembler logic is already partially factored, but the pattern is not yet consistent across domains.

## Standardization Rules

1. Keep persistence models in `model/` and transport models in `dto/`.
2. Use one clear suffix family for transport types:
   - `RequestDTO` for inbound payloads
   - `ResponseDTO` for outbound payloads
   - `ListResponseDTO` for list wrappers
   - `DetailResponseDTO` or `ViewDTO` for composite read views
   - `SectionDTO`, `ItemDTO`, `SummaryDTO`, or `OptionDTO` only when the shape is semantically distinct
3. Move naked transport classes in `dto/` toward the same suffix rules unless they are deliberately shared primitives.
4. Keep domain enums in `model/` when they are persistence/business state.
5. Keep UI-choice enums and filter options in `dto/` when they are API contract shapes.
6. Keep controllers thin.
7. Prefer one query service, one mutation path, and one assembler/factory per read-model family.
8. If two backend modules represent the same pattern, make the code look the same before introducing a new pattern.

## Priority Order

1. Workmarket first.
2. Identity and auth next.
3. Social and location next.
4. Chat, business, rides, and things after the main families are aligned.
5. Remaining outliers last.

This order matches the current drift density and the amount of shared pattern reuse available.

## Child Plans

- [x] `BACKEND-WORKMARKET-PRESENTATION-ASSEMBLY` - completed as the initial pilot slice by extracting `QuestPresentationAssembler`.
- [x] `BACKEND-WORKMARKET-READ-MODEL-STANDARDIZATION` - align quest, application, and dashboard read-model families around shared DTO shapes and assemblers.
- [ ] `BACKEND-IDENTITY-AUTH-NAMING-STANDARDIZATION` - normalize auth and app-user transport naming.
- [ ] `BACKEND-SOCIAL-LOCATION-DTO-STANDARDIZATION` - normalize social and location transport families and option/status payloads; work has started on the relation and debug/status subsets in `.agents/backend-social-location-dto-standardization-plan.md`.
- [ ] `BACKEND-SERVICE-BOUNDARY-CLEANUP` - trim oversized services/controllers after the model families are stabilized.
- [ ] `BACKEND-VALIDATION-AND-CONTRACT-SAFETY` - lock naming, assembler, and contract rules in tests and audit checks.

## Phase 1: Inventory and taxonomy

- Build a canonical naming map for backend transport types.
- Group every DTO-like type into one of these buckets:
  - request
  - response
  - list response
  - detail/view
  - section/item/summary
  - option/filter/status
  - admin/report/debug
- Flag all types that do not match the chosen naming rules.
- Separate transport enums from domain enums.
- Record the outlier list for auth, common, social, location, and workmarket packages.

## Phase 2: Canonical model families

- Normalize the workmarket families first:
  - dashboard
  - quest detail
  - quest list/search
  - quest application detail
  - quest application list/view
  - user review
  - workmarket options
- Normalize identity families:
  - auth request/response
  - app user request/response
  - profile/admin detail views
- Normalize social families:
  - circle summary/overview/contact/member/search/request/admin rows
  - relation/request/block/bulk update payloads
- Normalize location families:
  - lookup request/response
  - settings request/view
  - option/debug/status payloads

The target is not to remove domain specificity. The target is to make the shapes predictable.

## Phase 3: Shared backend patterns

- Standardize one assembler or view-builder per read-model family.
- Standardize one policy service for authorization decisions.
- Standardize one query service for read-heavy screens.
- Standardize one mutation/use-case service for writes and state transitions.
- Pull repeated navigation, relation, sanitization, and viewer-context logic into shared helpers where that logic is already repeated.

Candidate hotspots from the current audit:

- `workmarket/service/DashboardService.java`
- `workmarket/service/QuestService.java`
- `workmarket/service/QuestApplicationService.java`
- `workmarket/service/QuestValidationService.java`
- `workmarket/service/QuestStateTransitionService.java`
- `social/service/CircleService.java`
- `social/service/CircleReadService.java`
- `social/service/CircleDiscoveryService.java`
- `social/controller/CircleController.java`
- `location/service/LocationSettingsService.java`
- `location/service/LocationSettingsViewService.java`
- `location/service/LocationGeoService.java`
- `location/service/LocationQuestPresentationService.java`
- `identity/service/AppUserService.java`
- `agent/service/AdminAgentPlaygroundService.java`

## Phase 4: Service boundary cleanup

- Split broad services only where the split reduces duplication or removes mixed responsibilities.
- Keep orchestration in thin services and move pure read assembly, validation, policy, and state-transition logic into dedicated collaborators.
- Preserve existing public endpoints while refactoring internals.
- Do not introduce new abstractions unless they replace repeated code in at least two places.

## Phase 5: Validation and contract safety

- Add or extend tests that lock the new naming and shape rules in place.
- Keep the existing contract tests for workmarket and identity paths green.
- Re-run the architecture, naming, and mapper audits after each major family is standardized.
- If a refactor changes a DTO contract, update the frontend contract surfaces and generated artifacts together.

## Execution Sequence

1. Freeze the inventory of current outliers.
2. Standardize workmarket DTO families and shared assemblers.
3. Standardize identity and auth DTO naming.
4. Standardize social and location family naming.
5. Trim broad services into thin orchestrators plus dedicated helpers.
6. Run validation and re-audit drift.

## Success Criteria

- Backend transport types follow a small set of predictable naming patterns.
- Similar read models share the same section/item/summary structure.
- Controllers are thin and do not own view assembly.
- Services are separated into clear query, mutation, policy, and assembler responsibilities.
- New backend features can reuse existing patterns without inventing a new local convention.

## Open Questions

- Which existing naked DTO names should be renamed immediately versus kept for compatibility.
- Whether `ActionResults`, `BulkCircleMembershipAction`, and similar types stay in `dto/` or move into a dedicated contract namespace.
- How much of the workmarket section model family should be flattened into shared base types versus left as domain-specific composites.
- Whether any existing DTO renames need compatibility aliases for frontend contracts.

## Validation Plan

- Run backend tests for the touched domain slices.
- Run naming and architecture drift audits.
- Run mapper usage checks for the read-model families being standardized.
- Re-run frontend contract validation only if a backend DTO contract changes.

## Completion Evidence

- Plan status: draft
- Implementation status: pending
- Persistent backlog item: `BACKEND-MODEL-STANDARDIZATION-001`
- Primary source files: backend DTOs, services, controllers, mappers, and generated audit summaries

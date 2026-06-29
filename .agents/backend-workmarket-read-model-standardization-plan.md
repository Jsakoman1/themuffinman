# Backend Workmarket Read-Model Standardization Plan

Purpose: standardize the workmarket backend read-model family so quest, dashboard, application, and review views share the same DTO shape conventions and assembler patterns.

## Scope

- Normalize quest presentation building into a single shared assembler path.
- Align quest read-response, quest detail, dashboard, and application read models around one naming taxonomy.
- Identify DTO names that should stay domain-specific versus names that should move toward shared section/item/summary patterns.
- Keep public endpoint contracts stable unless a deliberate contract change is required and documented.

## Current Findings

- `QuestPresentationDTO` is the main shared view model for quest screens.
- `QuestMgr` and `QuestViewAssembler` previously duplicated the same presentation-building logic.
- Workmarket already uses section/read-model families, but the naming is inconsistent across dashboard, quest detail, application detail, and options payloads.
- Current outliers include many `*SectionDTO`, `*ResponseDTO`, `*ViewDTO`, and enum-like contract types with different naming habits.

## Implementation Slices

- [x] Slice 1: extract a shared quest presentation assembler.
- [x] Slice 2: normalize quest application presentation assembly into a shared helper.
- [x] Slice 3: normalize quest presentation and detail read-model shape helpers.
- [x] Slice 4: standardize dashboard read-model composition and section naming.
- [x] Slice 5: review request/response/option naming for remaining workmarket DTO outliers.

## Standardization Targets

- One shared assembler for quest presentation state.
- One shared pattern for section-style read models.
- Clear split between request DTOs, response DTOs, detail/view DTOs, and option/status payloads.
- Shared helper methods for label, action, and viewer-context derivation where repeated.

## Validation

- `./mvnw test -Dtest=QuestMgrTest,QuestPresentationAssemblerTest,QuestServiceTest`
- Additional workmarket-focused tests if any DTO or assembler contract is changed in later slices.

## Completion Criteria

- Quest presentation state is built from one shared path.
- Remaining workmarket read-model families follow the same naming and assembly conventions.
- No duplicated presentation logic remains in mapper and service layers for the same view shape.

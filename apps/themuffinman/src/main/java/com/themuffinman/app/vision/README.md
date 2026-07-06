# Vision Backend Capsule

## Responsibility

Owns `/vision` conversation orchestration, semantic understanding, slot validation, review coordination, execution gating, read-only query surfaces, and backend-prepared canvas state.

## Main Entry Points

- Controllers: `controller/`
- Conversation orchestration: `service/VisionConversationService.java`
- Read-only query surfaces: `service/VisionConversationQueryService.java`, `service/VisionConversationReadModelAssembler.java`
- Mutation lifecycle boundaries: `service/VisionConversationLifecycleService.java`, `service/VisionConversationMutationSupport.java`
- Semantic and review support: `service/VisionPromptUnderstandingService.java`, `service/VisionSemanticRouteCatalogService.java`, `service/VisionSemanticEnvelopeSupport.java`, `service/VisionQuestReviewSupport.java`
- Execution boundaries: `service/VisionExecutionService.java`, `service/VisionSurfacePolicy.java`, `service/VisionCapabilityExecutionAdapter.java`
- Canvas assembly and presentation helpers: `service/VisionCanvasAssembler.java`, `service/VisionConversationSnapshotSupport.java`

## Tests

- `src/test/java/com/themuffinman/app/vision/service/`
- Prefer scenario coverage for orchestration and execution boundaries, plus targeted unit coverage for route catalog and semantic sanitizer changes.

## Living Docs

- `docs/business-logic.md`
- `docs/domain-technical.md`
- `docs/vision-status-ledger.md`
- `docs/vision-architecture-patterns.md`

## Forbidden Shortcuts

- Do not put orchestration, slot validation, review selection, or execution gating in controllers or frontend-only code.
- Do not duplicate semantic route metadata, entity-family mapping, or capability gating outside the shared vision services.
- Do not add a new execution adapter without registering it through the explicit typed execution gate.
- Do not add a new semantic route or validator path without updating route-catalog and sanitizer tests.
- Do not let the frontend decide whether a route can execute or which entity family is active.

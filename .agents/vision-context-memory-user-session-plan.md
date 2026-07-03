# Vision Context Memory User And Session Plan

## Status

Draft.

## Goal

Add a backend-owned memory context to `/vision` that carries stable user context, current session context, and recent turn context into semantic understanding.

## Scope

- Included: semantic request contract, context builder, recent conversation and turn snapshots, focused tests, and docs sync.
- Excluded: filesystem runtime storage, schema-wide executor changes, and client-managed memory.

## Implementation Slices

1. Add a compact memory section to the semantic orchestration request.
2. Build memory snapshots from persisted conversations and turns.
3. Feed the memory layer into prompt understanding.
4. Cover the new memory path with targeted tests.
5. Update living docs and the vision status ledger.

## Validation

- `./mvnw -q -Dtest=VisionSemanticOrchestrationContextServiceTest,VisionPromptUnderstandingServiceTest test`
- `./mvnw -q -Dtest=VisionConversationServiceTest,VisionSemanticOrchestrationContextServiceTest,VisionPromptUnderstandingServiceTest test`

## Completion Evidence

- Status: draft
- Validation evidence: pending
- Doc delta summary: pending

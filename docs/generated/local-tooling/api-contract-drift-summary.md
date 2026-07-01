# API Contract Drift Audit

- Generated at: `2026-07-01T14:49:22Z`
- Backend DTOs scanned: `155`
- DTOs missing generated contracts: `9`
- Zero-usage backend fields: `301`

## `AdminAgentExecutionRequestDTO`

- Generated contract present: `true`
- `confirmed` -> `unused_in_frontend` | usage=`0`

## `AdminAgentExecutionResponseDTO`

- Generated contract present: `true`
- `blockingReasons` -> `unused_in_frontend` | usage=`0`
- `createdQuestIds` -> `unused_in_frontend` | usage=`0`
- `effectiveCount` -> `unused_in_frontend` | usage=`0`
- `executed` -> `unused_in_frontend` | usage=`0`
- `questTitles` -> `unused_in_frontend` | usage=`0`
- `requestedCount` -> `unused_in_frontend` | usage=`0`
- `targetUserId` -> `unused_in_frontend` | usage=`0`
- `targetUserLabel` -> `unused_in_frontend` | usage=`0`
- ... 2 more drift fields

## `AdminAgentPlaygroundResponseDTO`

- Generated contract present: `true`
- `clarificationContract` -> `unused_in_frontend` | usage=`0`
- `directExecutionAvailable` -> `unused_in_frontend` | usage=`0`
- `directExecutionCapabilityId` -> `unused_in_frontend` | usage=`0`
- `directExecutionSummary` -> `unused_in_frontend` | usage=`0`
- `executionReadiness` -> `unused_in_frontend` | usage=`0`
- `externalLlmConfigured` -> `unused_in_frontend` | usage=`0`
- `nextSteps` -> `unused_in_frontend` | usage=`0`
- `originalPrompt` -> `unused_in_frontend` | usage=`0`
- ... 9 more drift fields

## `AdminAgentSimulationResponseDTO`

- Generated contract present: `true`
- `blockingReasons` -> `unused_in_frontend` | usage=`0`
- `capabilityAssessments` -> `unused_in_frontend` | usage=`0`
- `clarificationContract` -> `unused_in_frontend` | usage=`0`
- `endpointPlan` -> `unused_in_frontend` | usage=`0`
- `executionReadiness` -> `unused_in_frontend` | usage=`0`
- `intentLineage` -> `unused_in_frontend` | usage=`0`
- `planningOnly` -> `unused_in_frontend` | usage=`0`
- `promptSourceLanguage` -> `unused_in_frontend` | usage=`0`
- ... 6 more drift fields

## `AdminCircleGroupResponseDTO`

- Generated contract present: `true`
- `memberPreviewLabel` -> `unused_in_frontend` | usage=`0`
- `members` -> `unused_in_frontend` | usage=`0`
- `ownerId` -> `unused_in_frontend` | usage=`0`
- `ownerUsername` -> `unused_in_frontend` | usage=`0`

## `AdminCircleOverviewDTO`

- Generated contract present: `true`
- `acceptedConnections` -> `unused_in_frontend` | usage=`0`
- `blockedRelations` -> `unused_in_frontend` | usage=`0`
- `pendingRequests` -> `unused_in_frontend` | usage=`0`

## `AdminCircleRelationRowDTO`

- Generated contract present: `true`
- `recipientUsername` -> `unused_in_frontend` | usage=`0`
- `requesterUsername` -> `unused_in_frontend` | usage=`0`

## `AdminUserDetailDTO`

- Generated contract present: `true`
- `appUserRoles` -> `unused_in_frontend` | usage=`0`

- ... 58 more DTOs
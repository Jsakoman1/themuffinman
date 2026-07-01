# Vision Generic Semantic Planning Plan

Purpose: start the first Vision God Plan implementation slice by separating generic turn planning signals from create_quest-specific slot extraction.

## Workflow Frame

- Feature tier: tier3-high-risk-multi-layer
- Scope: `/vision` backend prompt understanding, semantic plan model, targeted docs and plan reconciliation
- Out of scope: new executors, frontend UI changes, broad intent expansion beyond safe planning metadata
- Manifest decision: skipped with reason; this slice changes backend-internal planning metadata and docs but does not change public API or runtime mutation behavior
- Manifest path: not used

## Routing Snapshot

- Context commands: read Vision God Plan, adaptive architecture master plan, architecture patterns, status ledger, and current prompt understanding code
- Routing commands: manual scope routing based on God Plan priority and existing vision docs
- Validation commands: targeted vision prompt understanding tests and broader backend tests if constructor impact requires it
- Closeout commands: `make audit-todo`, `make audit-plan-completion plan=.agents/vision-generic-semantic-planning-plan.md`

## Implementation Slices

- [x] Slice 1: Reconcile stale Vision God Plan and master-plan current reality.
- [x] Slice 2: Add generic semantic plan model to prompt understanding.
- [x] Slice 3: Add tests for local semantic plan defaults and intent routing from semantic plan metadata.
- [x] Slice 4: Update vision docs/status ledger with the new planning boundary.

## Validation Plan

- Targeted checks: `./mvnw test -Dtest=VisionPromptUnderstandingServiceTest,VisionSemanticMapperTest,VisionSlotServiceTest`
- Broader checks: `./mvnw test` only if targeted constructor or behavior changes indicate broader risk
- Skipped checks or reasons: frontend validation is not applicable because this slice does not change frontend code or API DTOs

## Docs and Artifacts

- Expected docs: `.agents/god-plans/vision-god-plan.yaml`, `.agents/vision-adaptive-architecture-master-plan.md`, `docs/vision-architecture-patterns.md`, `docs/vision-status-ledger.md`
- Expected generated artifacts: none

## Closeout Gates

- Required closeout checks: targeted tests, `make audit-todo`, `make audit-plan-completion plan=.agents/vision-generic-semantic-planning-plan.md`
- Final response evidence: state backend planning boundary, validation, and residual risk

## Open Questions

- Resolver outputs still needed: none for this narrow first slice
- Risks or approvals: no external approval expected

## Completion Evidence

- Status: complete
- Changed files: vision God Plan, vision adaptive architecture master plan, prompt understanding services, intent router, conversation service, targeted tests, and vision docs
- Validation evidence: `./mvnw test -Dtest=VisionPromptUnderstandingServiceTest,VisionIntentRouterTest,VisionSemanticMapperTest,VisionSlotServiceTest,VisionConversationServiceTest` passed; `./mvnw test` passed; `make audit-todo` passed; `make audit-plan-completion plan=.agents/vision-generic-semantic-planning-plan.md` passed
- Doc delta summary: documented the semantic planning boundary above create_quest slots and reconciled stale master-plan current reality
- Backlog update: no persistent backlog changes expected
- Residual risk: generic semantic plan is metadata only; full `VisionExecutionPlanner` and non-create_quest executors remain deferred

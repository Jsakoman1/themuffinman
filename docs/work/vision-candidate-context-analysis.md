# Vision candidate context analysis

## Baseline

At baseline `f637bef`, Vision already has OpenAI semantic orchestration, user and
session memory, entity-family aliases, several backend entity resolvers, typed
`vision-web-action-v2` responses, and module-specific read services. The existing
semantic personal context contains locale, timezone, location, user role, learned
preferences, recent intents/entity families, conversation state, and slot data. It
does not contain a request-scoped inventory of the viewer's quests, applications,
people, things, rides, businesses, bookings, or conversations.

## Why durable memory is not the right inventory

Entity inventories are volatile, permission-sensitive, and potentially large. They
must not be stored as long-lived personal memory or copied into frontend state as an
authorization source. The plan introduces a request-scoped `candidateContext` built
from authoritative backend read services. It is compact, redacted, scope-labelled,
bounded, and discarded after semantic resolution.

## Complete versus partial candidate sets

There is no universal candidate count. If the eligible collection is small, the
backend sends every eligible candidate and marks the set complete. If it is large,
the backend performs retrieval and sends a partial result with total count, strategy,
query constraints, and a broaden-search path. A missing item from a partial set is
never treated as proof that the entity does not exist.

## Resolution protocol

1. Backend derives the viewer scope from the authenticated user and requested route.
2. Backend builds a complete candidate set or authorized retrieval result.
3. OpenAI selects an allowed intent, extracts target constraints, and optionally
   selects a supplied candidate.
4. Backend re-queries the authoritative service and checks visibility, ownership,
   freshness, and action permission.
5. One unique authorized target emits a typed detail action; zero, ambiguous, stale,
   or inaccessible targets return privacy-safe recovery or clarification.
6. Mutation intents continue through the existing review and explicit confirmation
   boundary.

## Example: owned Work

`otvori moj posao sa rostiljom` should receive a current-user-owned quest candidate
set. OpenAI should select `VIEW_QUEST_DETAIL` and a topic query or candidate id. The
backend must still verify that the selected quest belongs to the current user and is
currently visible to that viewer before emitting `/work/quests/{id}`.

## Example: another person's Work

`show Frank's lawn-mowing job from yesterday` should first produce a scope-aware
semantic plan containing person, topic, and date constraints. Backend retrieval then
returns only quests the viewer may see. OpenAI can rank those candidates or request a
clarification. The backend never leaks whether an inaccessible Frank quest exists.

## Current gaps

- Candidate inventories are not part of the semantic request contract.
- Existing resolvers use family-specific queries but do not share completeness,
  retrieval, confidence, or candidate provenance metadata.
- OpenAI receives route examples and memory but not authorized target candidates.
- Detail actions are typed only after capability-specific resolution and have no
  common partial-set or broaden-search contract.
- Runtime evidence does not yet cover candidate selection, complete/partial sets,
  stale targets, or cross-family ambiguity as one matrix.

## Plan-by-plan implementation analysis

### 1. Contract and scope

Current implementation: `VisionSemanticOrchestrationRequest` carries user, memory,
conversation, runtime, and allowed-route context. `VisionSemanticPlan` currently
contains intent, capability, planning note, search query, and a small number of
target query fields. `VisionSemanticResponseValidator` and
`VisionSemanticContractSanitizer` already fail closed against routes outside the
catalog.

Missing: a typed candidate-context object, candidate provenance/completeness, target
scope, selected candidate id, selected confidence, and broaden-search state. This is
the first slice because every later provider and resolver must share these fields.

Implementation boundary: extend the semantic DTO/model contract and route catalog;
do not put candidate rows into the durable learning-memory entities.

### 2. Candidate providers and retrieval

Current implementation: family-specific providers exist in
`VisionCapabilityEntityResolutionSupport`, `QuestEntityResolver`,
`ApplicationEntityResolver`, `UserEntityResolver`, and `CircleEntityResolver`.
Existing read services already enforce much of the domain visibility contract.

Missing: a common provider interface and a scope-aware result envelope. Providers
need complete-set detection for small collections, bounded retrieval for large
collections, stable candidate ids, privacy-safe summaries, total counts, and a
retrieval strategy. Existing family resolvers must remain the final authority and
must not be replaced by LLM-selected ids.

Implementation boundary: reuse domain read services and add adapters/assemblers in
Vision; do not create a second permission system or a frontend inventory cache.

### 3. OpenAI semantic integration

Current implementation: `VisionPromptUnderstandingService` builds the OpenAI request
through `VisionSemanticOrchestrationContextService`,
`VisionSemanticPromptPayloadBuilder`, route catalog descriptors, and a strict response
validator. The prompt already instructs OpenAI to use route examples and semantic
aliases, but it receives no authorized entity candidates.

Missing: candidate context in the request, candidate-selection fields in the plan,
explicit complete/partial instructions, and multilingual examples such as “otvori
moj posao sa rostiljem”. An unsupported OpenAI response must remain a provider
failure/recovery state; production code must not silently invoke the local parser.

Implementation boundary: OpenAI interprets wording and chooses among supplied
candidates; backend retrieval and authorization remain outside the model.

### 4. Re-resolution, authorization, and recovery

Current implementation: `VisionSemanticEnvelopeSupport` and
`VisionEntityResolverRegistry` connect semantic target queries to family resolvers;
`VisionConversationService` resolves quest detail, profiles, circles, applications,
and other targets before snapshots/actions. `VisionCapabilityEntityResolutionSupport`
already handles exact, zero, and multiple matches for several families.

Missing: one shared protocol for candidate id re-query, stale candidate detection,
partial-set broadening, privacy-safe no-access responses, and confidence/ambiguity
thresholds. The existing `resolveVisibleQuest` path also assumes the semantic layer
has already reduced natural wording to a useful query.

Implementation boundary: preserve family-specific authorization and add shared
orchestration around it; never return a route merely because OpenAI selected an id.

### 5. Web and native-client actions

Current implementation: `VisionCanvasAssembler` publishes `vision-web-action-v2`,
and `shellRouteRegistry.ts` plus `visionWebAction.ts` validate and execute typed
internal routes. Mutation execution remains behind the existing review system.

Missing: action metadata for candidate provenance, clarification/recovery, and
partial retrieval; a consistent response shape that future native clients can use
without importing Vue route logic. The web client should only execute or render the
backend action and candidate choices.

Implementation boundary: extend the portable response contract and preserve the
existing route allow-list. No natural-language-to-URL mapping belongs in Vue.

### 6. Runtime and closeout

Current implementation: runtime JSON evidence already proves several module entry
routes and selected detail routes for VisionForWeb. Existing plan verification,
runtime evidence, and cleanup rules are available in `docs/implementation-control.md`.

Missing: fresh authenticated evidence for complete small sets, partial large sets,
unique selection, ambiguity, stale/no-access targets, broaden-search, provider
outage, and text/voice parity across every candidate-enabled family. The final slice
must prove that runtime evidence came from the current stack and then remove only
temporary artifacts.

## Recommended execution order

The order is intentionally serial: contract first, then providers, then OpenAI
payloads, then authorization/recovery, then clients, and only then runtime proof.
This prevents a testing loop where the same unsupported semantic response is replayed
without adding a candidate contract or backend implementation slice.

The contract slice is now frozen as `vision-candidate-context-v1`; provider work may
start only against this envelope and its explicit complete/partial semantics.

The first provider implementation uses the existing authorized Work read models for
viewer-owned/visible quests and viewer-owned applications. It publishes compact
candidate items and keeps navigation-only intents without a candidate context. The
remaining entity families use the same provider interface in the next provider
expansion rather than introducing family-specific OpenAI payload formats.

## Non-negotiable safety rules

- OpenAI is the production interpreter; local parser behavior is development-only.
- Candidate context is not permission. Authorization remains backend-owned.
- Candidate labels and summaries must be privacy-safe and minimal.
- No raw URL is accepted from semantic output.
- No detail route is emitted without a fresh authorized target id.
- Mutations never bypass review and confirmation.

## Implementation closeout

The candidate-context contract is now implemented for Work and applicant-owned
Applications. OpenAI receives request-scoped authorized candidates, including
complete-versus-partial metadata. Candidate IDs are never trusted as permission;
detail actions perform a fresh backend read under the current viewer before a
canonical route is emitted. Large sets are explicitly marked partial at the
backend limit so OpenAI can clarify or request broader retrieval instead of
interpreting omission as absence.

The Web client executes only `vision-web-action-v2` payloads accepted by the
shared route registry. Text and voice inputs converge on the same backend
semantic and authorization pipeline, leaving the response shape portable for
future native clients.

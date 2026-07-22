# System Analysis Round 5 — Vision Orchestration and Execution

Status: completed analysis snapshot, 2026-07-22. This round traces Vision from text/voice input through semantic understanding, context, intent, clarification, review, confirmation, execution, recovery, and future-client contracts.

## 1. Executive finding

Vision is not a thin chat UI and not a generic OpenAI wrapper. It is a backend-owned orchestration subsystem that coordinates many existing domain capabilities through typed conversation state, slot filling, semantic routing, candidate resolution, review/confirmation, and capability execution adapters.

The current Vision package contains approximately 195 Java files: 3 controllers, 34 DTOs, 15 models, 5 repositories, and about 138 service/collaborator files. It has 58 execution adapters and roughly 65 Vision tests. This is a substantial orchestration platform inside the modular monolith.

```text
text / voice transcript / typed Web handoff
                  ↓
persisted conversation + turn
                  ↓
context + semantic understanding
                  ↓
intent route + entity resolution + slot merge
                  ↓
one clarification / preview / discovery / review
                  ↓
explicit confirmation
                  ↓
typed capability execution adapter
                  ↓
existing domain service/use case
                  ↓
typed result, recovery, and canvas state
```

The governing rule is stable: backend decides meaning, frontend renders state.

## 2. Locked architecture decisions

`docs/vision-decision-record.md` and `docs/vision-architecture-patterns.md` establish the following:

- OpenAI may suggest meaning; deterministic backend logic decides execution meaning.
- One primary next step is returned per turn.
- Execution is feature-flagged and confirmation-gated.
- Conversation state is persisted in the backend.
- Clarification is progressive and asks for one high-value missing/ambiguous field.
- Existing domain services remain authoritative.
- Planning may be flexible; execution must fail closed.
- Review edits use typed actions and targets, not free-text reinterpretation.
- Continuity is summary-first; raw turns are for detail/audit rather than state reconstruction.
- Legacy surfaces are not the design baseline for the adaptive blank canvas.

These are architectural constraints, not merely UX guidance. They define the boundary between semantic assistance and trusted mutation.

## 3. Orchestration layers

### 3.1 Conversation and lifecycle

`VisionConversationService` is the top-level facade for turns, lifecycle, discovery, planning, review, and execution coordination. Supporting collaborators split lifecycle, read-model assembly, slot resolution, mutation support, detail turns, and review interaction.

The conversation and turn models are persisted. The backend stores status, intent, turns, slot data, request IDs, and summaries. This supports resume, retry, voice/text continuity, review loops, and future auditability.

`clientRequestId` is a replay boundary for turn submission retries, preventing duplicate conversation steps when a request is repeated.

### 3.2 Semantic understanding

Semantic services build a bounded orchestration context from user/session/runtime/memory data, construct a semantic prompt payload, validate/sanitize the response, map it into typed intent/slot structures, and route the intent through a semantic route catalog.

The external model is constrained by:

- max prompt and turn lengths;
- provider/model configuration;
- semantic response validation;
- deterministic route catalog and capability IDs;
- backend entity resolution and authorization after interpretation.

The model output is therefore a candidate signal, not an authority token.

### 3.3 Slot and clarification state

`VisionSlotService` normalizes and merges typed slot data. `VisionClarificationService` selects the next question/choice. Schedule and location have dedicated deterministic parsers and resolution services.

The progressive slot model prevents a natural-language prompt from becoming an implicit full-form mutation. The user can correct/review typed fields before execution, and the backend decides which fields are required for a particular capability.

### 3.4 Candidate context and entity resolution

Candidate providers and entity resolvers cover users, quests, applications, circles, business objects, Things, Chat, and discovery. The candidate context layer is critical because a prompt such as “update that quest” is not safe until the backend resolves one authorized target or asks for clarification.

This is where Vision depends on most product domains. The dependency is intentional orchestration, but it means candidate context must remain viewer-scoped, bounded, and explicit about ambiguity.

### 3.5 Preview, discovery, and review

Vision has separate preview renderers and discovery services for profiles, Work, Business, Social, Chat/feed, Things, quest search, and cross-family search. Discovery DTOs carry result state, recovery codes, pagination, detail routes, and comparison data.

Review-ready state exposes typed execution candidates, review targets, allowed actions, and confirmation requirements. The Web renderer shows explicit Confirm/Cancel controls when the backend says review is ready; copy alone cannot execute a mutation.

### 3.6 Execution

`VisionExecutionService` performs deterministic gates:

1. conversation must exist;
2. intent must be typed and supported;
3. conversation must be `REVIEW_READY`;
4. semantic route must resolve a capability ID;
5. feature policy must allow that capability;
6. a unique adapter must exist;
7. adapter delegates to an existing domain service/use case.

The service rejects unsupported state, disabled configuration, missing adapters, and invalid capability routes. Adapters are registered by capability ID and duplicate/unsupported registrations fail application startup.

There are 58 adapters at this snapshot. This is a strong explicit extension mechanism, but also a scale risk: adapter coverage, failure semantics, slot contracts, and tests must remain catalogued.

## 4. Execution result and recovery model

`VisionExecutionResult` classifies blocked execution into typed categories including `CONFIGURATION`, `VALIDATION`, `STATE`, `UNAVAILABLE`, and `EXECUTION_FAILED`. Retryability is not inferred from arbitrary frontend copy.

The intended behavior is:

| State | Meaning | Retry? |
|---|---|---|
| Configuration | feature/provider/rollout disabled | no automatic retry |
| Validation | malformed/missing/unsafe data | correct input |
| State | conversation/domain object not in required state | refresh/recover |
| Unavailable | adapter/provider capability unavailable | explicit retry may be allowed |
| Execution failed | domain/provider failure after gate | explicit retry/recovery may be allowed |

The frontend renders typed failure/recovery state and does not decide whether a mutation is safe to repeat. Review/mutation actions are intentionally not auto-retried; read/discovery requests may expose a safe retry.

## 5. Configuration and rollout boundary

Production defaults currently include:

- `app.vision.enabled=true`;
- `app.vision.execution-enabled=false`;
- `app.vision.create-quest-enabled=true`;
- persisted conversation TTL of 14 days;
- max 30 turns per conversation;
- max prompt length of 2000;
- scheduled memory compaction;
- agent provider default `mock`, with OpenAI credentials optional;
- local emergency parser disabled in production but enabled in development by default;
- voice enabled/configured through typed `app.voice` properties.

Development overrides turn Vision execution on and enable the local deterministic emergency path. This makes local development usable, but creates a critical safety boundary: development defaults must never be copied into production configuration.

The intended rollout is capability-specific and flag-controlled, not a single global “AI enabled” switch. `VisionSurfacePolicy` and semantic route catalog enforce the supported set.

## 6. Client architecture

The Web Vision module includes:

- route-level blank-canvas view;
- one conversation composable/API boundary;
- canvas renderer;
- focused terminal rows, typing, voice control, intent preview, flow debug, and candidate/review components;
- typed generated contracts.

The renderer consumes backend blocks, runtime context, execution candidates, review targets, handoffs, result state, recovery state, and action hints. It provides explicit confirm/cancel, retry, show-more, detail-route links, and voice state presentation.

Shell-to-Vision entry uses typed handoff query data (`prompt`, `autorun`, `context`, `source`, `returnTo`). Stable module/detail ownership remains with the shell routes; Vision is the adaptive deep-work surface.

Future mobile/Watch clients are contract consumers, not implemented native clients. Device role, density, attention state, audio/haptic cues, action hints, and handoff contracts are backend-prepared so native clients do not re-derive semantic or permission meaning.

## 7. Strengths

1. Persisted conversations make text, voice, review, retry, and resume share one backend timeline.
2. Typed execution adapters keep Vision from replacing domain services.
3. Deterministic review/confirmation prevents model confidence or frontend state from directly causing mutation.
4. Candidate context and route catalogs make target resolution explicit.
5. Typed result/recovery states prevent copy-based client inference.
6. Generated contracts and extensive tests provide strong static/contract coverage.
7. Feature flags separate development/local fixtures from production rollout.

## 8. Risks and open edges

### 8.1 Orchestration facade size

`VisionConversationService` coordinates a very large dependency set. Supporting collaborators have already extracted lifecycle, mutation, read-model, slot, detail, and review responsibilities, but continued growth could make it a second domain layer. New capability work should add named providers/renderers/adapters rather than placing all logic in the facade.

### 8.2 Adapter consistency

58 adapters create a powerful but repetitive surface. Each adapter should have the same contract for capability ID, exact target resolution, domain delegation, failure classification, idempotency/replay expectations, and tests. “Adapter exists” is not the same as “capability is production-ready.”

### 8.3 Model/provider observability

The architecture has provider/model and flag controls, but the production readiness question is broader: latency, token/cost budgets, provider failure rates, semantic validation rejection, ambiguity rate, execution failure rate, and retry outcomes need operational visibility.

### 8.4 Runtime proof

Static Vision tests are extensive, but pending runtime scenarios include confirmation, execution, search/detail/create/read discovery, voice/text parity, and cross-client contracts. These should be treated as distinct acceptance slices.

### 8.5 Memory and privacy

Persisted conversations, preference rows, feedback events, and compact summaries support continuity and learning. They also increase retention/privacy obligations. TTL and compaction exist, but future production readiness should verify deletion semantics, redaction, consent boundaries, and provider payload minimization.

### 8.6 Cross-module authority

Vision imports many domains. The architecture remains safe only if adapters call domain services and do not copy permission/state rules into Vision. Any new Vision capability should be checked against the owning domain's service and regression scenario.

## 9. Recommended controls

- Maintain a machine-readable Vision capability/adapter matrix with capability ID, required slots, target resolver, owning domain use case, confirmation policy, idempotency boundary, failure classes, Web evidence, and native status.
- Keep execution disabled by default in production until capability-specific runtime evidence exists.
- Add provider/semantic/execution telemetry before broad rollout.
- Treat conversation TTL, memory compaction, and request replay as privacy and correctness contracts, not only operational settings.
- Keep candidate context viewer-scoped and bounded; ambiguity must produce clarification, never best-effort mutation.
- Keep adapter tests beside domain workflow tests and include stale-state/permission/failure cases.
- Keep `/vision` route audit metadata explicit so generic route inventory does not misclassify the blank-canvas surface.

## 10. Round 5 conclusion

Vision is a typed, persisted, backend-authoritative orchestration layer with broad cross-domain reach and a carefully gated mutation boundary. Its static architecture is mature enough to support many product capabilities, but its production readiness depends on runtime/provider observability, adapter consistency, privacy/retention verification, and explicit native-client acceptance. The correct next step is not to make Vision more autonomous; it is to make its existing contracts measurable and operationally trustworthy.

No production code or Vision contract was changed in this analysis round.

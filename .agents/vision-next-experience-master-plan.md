---
machine_kind: master-plan
machine_status: complete
machine_title: Vision Next Experience Master Plan
machine_goal: Turn `/vision` into a mobile and audio-first command cockpit while keeping backend-owned meaning, terminal identity, and morphing preview as the core product language.
---

# Vision Next Experience Master Plan

## Status

Complete.

Completed phases so far:

- runtime contract
- continuity and safety foundation
- command cockpit
- business read-model foundation
- audio and haptic runtime foundation
- shared guided-flow conventions
- validation and ledger sync for the implemented slice
- quest guided flow
- business booking guided flow
- side-job search guided flow
- person outreach guided flow

## Goal

Turn `/vision` into a mobile and audio-first command cockpit while keeping backend-owned meaning, terminal identity, and morphing preview as the core product language.

## Parent God Plan

- God Plan: `Plan System God Plan`
- Machine-readable path: `.agents/god-plans/plan-system-god-plan.yaml`

## Scope

- Included: backend-owned runtime contract, continuity and safety semantics, mobile/iPhone cockpit ergonomics, watch-friendly reduced interaction shape, audio and haptic cues, guided task flows for work and business actions, and validation/docs closeout.
- Excluded: unrelated domain rewrites, visual-only polish that does not improve control or comprehension, and standalone frontend inference logic that duplicates backend meaning.

## Product Direction

The next Vision version should feel like one coherent command system rather than a generic app shell.

- The backend remains the source of truth for meaning, mode, availability, action hints, and execution boundaries.
- The frontend renders state, density, and interaction affordances for the current device.
- Audio is a first-class output channel, not a record-button accessory.
- iPhone is the primary mobile surface.
- Apple Watch is a reduced companion surface for short turns, confirmations, and low-attention interactions.
- Terminal feel and morphing preview stay as the recognizable visual identity.

## Implementation Directives

- Implement backend-first and API-first. The frontend must consume backend-owned meaning, never reconstruct it.
- Keep one conversation system across chat, quest, business, and people-related actions.
- Treat iPhone as the primary mobile density and Watch as a reduced companion density.
- Keep all voice, haptic, retry, cancel, resume, and confirmation semantics backend-owned.
- Preserve the terminal feed and morphing preview as the default visual grammar.
- Move shared helpers, config, and validation rules into reusable backend services instead of duplicating them in client logic.
- Add new state, contract, and persistence only when the current child plan needs it.
- Keep the implementation slice order stable unless a genuine dependency forces a change.

## Implementation Roadmap

The implementation should land in this order and each phase should close before the next begins:

1. runtime contract
2. continuity and safety
3. business read-model
4. command cockpit
5. audio and haptic runtime
6. quest guided flow
7. business booking guided flow
8. side-job search guided flow
9. person outreach guided flow
10. validation and ledger

Each phase should produce a backend-owned deliverable:

- runtime contract: DTO and state model updates for attention, mode, action hints, and device role
- continuity and safety: idempotency, retry, interruption, resume, cancel, and consent rules
- command cockpit: responsive shell behavior and control placement rules for iPhone-first use
- audio and haptic runtime: spoken output, audible summaries, and haptic cue semantics
- guided task flows: clearer turn guidance for create work, booking, side-job search, and person outreach
- validation and ledger: tests, docs, status ledger updates, and any generated contracts

### Implementation Outputs By Phase

Each child plan should leave behind the following concrete outputs before it is considered done:

1. runtime contract
- expected outputs: DTO field map, backend state model, endpoint contract notes, and contract tests

2. continuity and safety
- expected outputs: idempotency rules, interruption/recovery state model, consent boundaries, retry tests, and any persistence changes required to support them

3. business read-model
- expected outputs: owner schedule summary, public business page read model, booking availability projection, and backend read contract notes

4. command cockpit
- expected outputs: responsive shell rules, control-placement decisions, and frontend contract usage notes

5. audio and haptic runtime
- expected outputs: audio/haptic semantics, voice fallback behavior, and watch-friendly turn behavior notes

6. quest guided flow
- expected outputs: quest-intake guidance rules, next-step selection rules, and route/catalog updates for create-work flows

7. business booking guided flow
- expected outputs: business-booking guidance rules, availability/capacity prompts, and route/catalog updates for business booking flows

8. side-job search guided flow
- expected outputs: discovery guidance rules, ranking/comparison next-step rules, and route/catalog updates for side-job search flows

9. person outreach guided flow
- expected outputs: outreach guidance rules, contact-target resolution rules, and route/catalog updates for person outreach flows

10. validation and ledger
- expected outputs: updated docs, ledger entries, validation evidence, and generated artifact refreshes

## Priority Slices

### Must Have

- Backend runtime contract for attention state, action hints, mode state, and mobile/wearable role hints.
- Continuity, interruption recovery, and idempotency semantics for voice and watch-driven turns.
- Backend business read-models for owner schedules, public business pages, booking availability, and calendar projection when the flow needs them.
- Explicit audio and haptic cue semantics for a turn.
- iPhone-first command cockpit layout that keeps the terminal central and preserves the morphing preview.
- Guided task flows for the core intents:
  - create work / quest
  - schedule a business appointment
  - find a side job
  - contact a person or circle member
- Clear consent, privacy, and social-boundary cues for actions that contact other people or touch shared business state.
- Validation, docs, and ledger alignment for the new interaction model.

## Child Plans

1. `.agents/vision-runtime-contract-plan.md`
2. `.agents/vision-continuity-and-safety-plan.md`
3. `.agents/vision-business-read-model-plan.md`
4. `.agents/vision-command-cockpit-plan.md`
5. `.agents/vision-audio-haptic-runtime-plan.md`
6. `.agents/vision-quest-guided-flow-plan.md`
7. `.agents/vision-business-booking-guided-flow-plan.md`
8. `.agents/vision-side-job-search-guided-flow-plan.md`
9. `.agents/vision-person-outreach-guided-flow-plan.md`
10. `.agents/vision-validation-and-ledger-plan.md`

## Completion Evidence

- Status: complete
- Evidence: all child plans are complete, the corresponding backend and frontend changes are validated, and the shared `/vision` runtime now includes the mobile/audio-first cockpit plus the guided quest, business booking, search, and outreach flows.

## Child Plan Instructions

- `vision-runtime-contract-plan` must define the shared state and DTO shape before any other phase invents new view behavior.
- `vision-continuity-and-safety-plan` must lock retry, resume, idempotency, cancel, and consent behavior before mobile flows depend on them.
- `vision-business-read-model-plan` must define the backend read surfaces for owner schedules, public business pages, and booking availability before guided business flows depend on them.
- `vision-command-cockpit-plan` must keep the terminal and morphing preview central while making the surface work on iPhone-sized screens.
- `vision-audio-haptic-runtime-plan` must make spoken output and haptic hints first-class while keeping visible state primary.
- `vision-quest-guided-flow-plan` must improve create-work guidance with backend-guided next-step flows.
- `vision-business-booking-guided-flow-plan` must improve business booking guidance with availability, capacity, and booking-specific next steps.
- `vision-side-job-search-guided-flow-plan` must improve side-job discovery with search and compare next-step flows.
- `vision-person-outreach-guided-flow-plan` must improve person outreach with contact-resolution and social-boundary guidance.
- `vision-validation-and-ledger-plan` must close the docs, tests, and status ledger against the implemented reality.

## Execution Order

1. Runtime contract
- Priority: must-have
- Reason: the rest of the surface should consume one backend-owned state model before any more UI or voice work lands.

2. Continuity and safety
- Priority: must-have
- Reason: interruption recovery, idempotency, and consent need to be locked before the mobile surface starts depending on them.

3. Business read-model
- Priority: must-have
- Reason: business pages, availability, and owner schedule projections need a stable backend surface before guided business flows can stay thin.

4. Command cockpit
- Priority: must-have
- Reason: the cockpit is the visible control surface that must stay coherent across mobile densities.

5. Audio and haptic runtime
- Priority: must-have
- Reason: spoken output and turn feedback only work well once the state contract and cockpit shape are stable.

6. Quest guided flow
- Priority: must-have
- Reason: create-work guidance should be stable before other task families are tuned.

7. Business booking guided flow
- Priority: must-have
- Reason: booking guidance depends on the business read model and should be tuned directly against that surface.

8. Side-job search guided flow
- Priority: must-have
- Reason: discovery and compare flows should stay separate from booking and outreach so their next-step logic stays clean.

9. Person outreach guided flow
- Priority: must-have
- Reason: contact resolution and social-boundary cues need their own flow-specific guidance.

10. Validation and ledger
- Priority: must-have
- Reason: the implementation should close with docs, contracts, and tests proving the new runtime shape.

## Audio Fallback Rule

- If spoken output cannot be produced for a turn, the backend should still return a complete visual state and a concise text summary.
- If audio playback is interrupted, the user should be able to resume or retry without creating a duplicate action.
- If a turn is safe to execute but audio is unavailable, the visual confirmation remains authoritative.

## Execution Continuation Rule

- After one child plan closes, continue automatically into the next child plan in the declared order without pausing for routine confirmation.
- Stop early only for a real blocker, conflicting user changes, required approval, or a genuine scope change.

## Cross-Phase Dependency Map

### Runtime Contract -> Command Cockpit

- The shell should consume a stable backend shape for mode, attention, and action hints before the layout is tuned.

### Runtime Contract -> Continuity and Safety

- Interruptions, duplicate submit protection, and action retry behavior should be explicit in the backend state before the shell depends on them.

### Continuity and Safety -> Command Cockpit

- The mobile shell should not invent its own retry or cancel semantics when the backend can describe them directly.

### Continuity and Safety -> Business Read-Model

- The business read surfaces should inherit the same retry and consent rules so owner schedule and booking flows stay safe under interruption.

### Business Read-Model -> Command Cockpit

- The cockpit should render business state without inventing a frontend-only summary layer when the backend can already prepare the read model.

### Command Cockpit -> Audio and Haptic Runtime

- The cockpit layout should expose audio and haptic cues without forcing a separate control stack for voice and watch turns.

### Audio and Haptic Runtime -> Guided Task Flows

- The guided flows should reuse the same command semantics and only specialize the next-step guidance per task family.

### Guided Task Flows -> Validation and Ledger

- Each flow should close with validation evidence, docs sync, and any generated contract refresh that the new behavior requires.

## Closeout Expectations

- The master plan should close only after all child plans have been implemented, documented, and validated in order.
- Temporary `.agents/tmp/` analysis artifacts should be promoted, archived, or deleted before closeout.
- Any leftover UX or control-surface improvement that is real but intentionally deferred should be captured in the appropriate durable backlog before closeout.

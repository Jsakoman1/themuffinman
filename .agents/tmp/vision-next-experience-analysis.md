Owning plan: `.agents/vision-next-experience-master-plan.md`

# Vision Next Experience Analysis

## Current Strengths

- Backend `vision` orchestration is real and persisted.
- The current contract already covers stepwise conversation turns, review states, execution candidates, read-only discovery, and entity resolution across the main modules.
- `business`, `workmarket`, `chat`, `identity`, `location`, and `semantic` are already exposed through backend-owned flows rather than frontend-invented logic.
- The current frontend shell already has a terminal-feel canvas, animated agent orb, voice control, and backend-driven preview blocks.
- The product docs already agree that `/vision` should be backend-governed, adaptive, and voice-aware.

## Main Gaps Versus The Desired Vision

- The surface is still mostly a desktop-style adaptive web shell, not a true iPhone-first command cockpit.
- Voice exists, but audio output is not yet a first-class control channel for the whole experience.
- There is no explicit watch-friendly runtime contract for short prompts, short audio replies, haptics, or reduced interaction loops.
- The user can see state, but the user does not yet have enough immediate control over `chat`, `quest`, `business`, and related mode switches.
- The current prompt flow is strong at orchestration, but not yet shaped into a guided "intent to action" experience for:
  - create work
  - schedule a business appointment
  - find a side job
  - contact a person
- The frontend still carries some orchestration-adjacent heuristics for route handling, voice behavior, and preview selection.

## Product Direction Check

The current implementation mostly matches the architectural direction, but not yet the interaction finish:

- aligned: backend-owned meaning, persisted conversation state, explicit review/confirm, entity resolution, adaptive canvas, terminal-first previews
- partially aligned: voice feedback, command awareness, task-family switching, mobile ergonomics
- not yet aligned enough: audio-first control loop, watch/iPhone-specific runtime shape, easy command affordances across modules, single-gesture mode switching

## Recommended Master Plan Shape

The next implementation batch should focus on one outcome:

- turn `/vision` into a mobile/audio-first command cockpit while keeping the terminal and morphing preview as the signature visual language

## Additional Improvements To Include

- Explicit continuity and interruption recovery semantics for voice, iPhone, and Watch turns.
- Idempotency and duplicate-action protection for confirm/retry flows.
- Consent and privacy cues when a turn contacts another person or affects shared business state.
- Accessibility-safe defaults for reduced motion, readable density, and voice-output fallback.
- Latency-aware partial acknowledgements for long-running turns.
- Locale-aware phrasing and multilingual voice preference hints as a future-safe boundary.

That means:

- backend still decides meaning
- frontend still renders state
- audio becomes a real output, not just a record button
- iPhone becomes the primary interaction surface
- Apple Watch becomes a reduced companion surface
- the same backend conversation contract drives both

## Planning Implications

- This is not a "new feature" batch in the old sense.
- It is a control-surface redesign plus interaction-contract expansion.
- It should be implemented in ordered slices, not as one mixed frontend rewrite.
- The slices should start from the runtime contract, then the cockpit shell, then audio/haptic runtime, then guided task flows, then validation and docs sync.

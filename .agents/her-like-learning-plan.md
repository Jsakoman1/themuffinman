---
machine_kind: plan
machine_status: draft
machine_title: Her-like Learning Loop
machine_goal: Build a target architecture and implementation plan for an assistant that improves from user interactions without losing backend control.
---

# Her-like Learning Loop

## Goal

Design a target architecture for a Her-like assistant that can:

- understand voice and text
- keep durable conversation continuity
- learn stable user preferences
- capture corrections as feedback
- improve future routing and suggestions
- stay safe, explainable, and backend-governed

## Target Architecture

```
User voice/text
  -> transcription
  -> semantic understanding
  -> orchestration and policy
  -> memory read
  -> clarification or execution planning
  -> review / confirm
  -> domain execution
  -> persistence
  -> feedback capture
  -> memory update and summarization
  -> future turn reuse
```

### Layer 1: Input Surfaces

- Voice recordings from the UI
- Typed prompts from the adaptive canvas
- Explicit review actions and correction actions

### Layer 2: Transcription

- STT converts audio to text
- The raw audio stays transient
- The transcript is returned with provider metadata

### Layer 3: Semantic Understanding

- Prompt normalization
- Intent classification
- Slot extraction
- Ambiguity detection
- Language and timezone context
- Confidence scoring

### Layer 4: Orchestration and Policy

- Chooses the next step
- Enforces clarification before mutation
- Keeps read-only and mutating paths separate
- Resolves the current conversation state
- Gates execution behind typed backend flags

### Layer 5: Memory Stack

- Session memory: current turn state
- User preference memory: stable personal defaults
- Task history memory: repeated workflows and prior outcomes
- Feedback memory: corrections, misses, and confirmations
- Summary memory: compressed long-term context

### Layer 6: Execution

- Review candidate is built first
- User confirms explicitly
- Domain adapter maps semantic slots to domain DTOs
- Domain service persists the real business object

### Layer 7: Observability

- Store why an intent was chosen
- Store why a slot was filled
- Store why clarification was required
- Store why execution was blocked or allowed

### Layer 8: Learning Loop

- Capture user correction
- Compare predicted meaning vs actual correction
- Update preference memory
- Update summarization memory
- Feed regression scenarios and evals

## What Already Exists

- Persisted conversation and turn storage
- Voice transcription boundary
- Semantic understanding boundary
- Review / confirm execution gate
- Domain adapter for create quest
- Backend-owned conversation state

## What Is Missing

- Explicit preference memory model
- Explicit feedback capture model
- Memory summarization and compression job
- Structured correction events
- Evaluation harness for learning quality
- Clear separation between transient session memory and durable user memory
- Explainability records for intent and slot decisions

## Implementation Phases

### Phase 1: Memory foundations

- Add structured preference memory entities or tables
- Add feedback event storage
- Add summary records for long-lived context
- Define retention and compaction rules

### Phase 2: Capture corrections

- Capture review edits, rejected suggestions, and user overrides
- Record why the system was wrong
- Link corrections to prompt, intent, slots, and conversation

### Phase 3: Summarize and compress

- Periodically compress old turns into stable summaries
- Extract stable user preferences
- Keep only useful history in active context

### Phase 4: Personalize runtime decisions

- Use preference memory in prompt understanding
- Use memory in clarification choice
- Use memory in prompt wording and suggestion ranking
- Keep hard policy checks in backend services

### Phase 5: Evaluation harness

- Build scenario tests for routing, memory, and correction handling
- Add regression cases for topic switching and preference recall
- Add scorecards for memory precision and false recall

### Phase 6: Controlled learning upgrades

- Introduce better ranking or retrieval if needed
- Consider fine-tuning only after memory + evals are stable
- Keep execution boundaries unchanged

## Constraints

- Do not let learning bypass backend policy
- Do not let memory override exact target resolution
- Do not let preference learning change business rules
- Do not let the frontend infer hidden state
- Do not auto-execute from raw natural language

## Documentation Impact

Expected durable docs to update when implementation starts:

- `docs/business-logic.md`
- `docs/domain-technical.md`
- `docs/product-memory.md`
- `docs/product-vision.md`
- `docs/vision-architecture-patterns.md`
- `docs/agent-operating-model.md`
- `docs/agent-operating-model.yaml` if machine rules change

## Validation Direction

- Backend unit tests for memory write and read paths
- Scenario tests for correction capture
- Regression tests for intent routing and topic switching
- Validation of docs and agent-operating drift

## Open Questions

- Which memory should be persisted as structured rows versus compressed JSON
- Which corrections should update preference memory automatically
- What decay policy should apply to old preferences
- Whether learning should be per user, per circle, or per task family
- Whether summaries should be generated synchronously or by job

## Status

- Draft only
- No code changes yet
- Plan target: evolve current `vision` stack into a durable learning loop without losing control safety

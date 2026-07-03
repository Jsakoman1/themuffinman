---
machine_kind: plan
machine_status: unknown
machine_title: Vision Prompt Frontend Plan
---

# Vision Prompt Frontend Plan

## Scope

Redesign the vision screen around an animated agent and a bottom prompt composer.

## Tasks

1. Remove the current dense voice control layout.
2. Keep the central area focused on an abstract animated agent.
3. Add a bottom prompt composer that:
- appears when needed
- submits typed prompts to the backend ingest endpoint
- can show the latest recognized prompt and backend response
4. Reuse the same submit path for audio transcripts.
5. Keep the UI thin and state-driven.
6. Run type-check and build after implementation.

## Output

- A calmer, more intentional vision screen.
- One shared prompt action for typed and voice input.


# Vision Modern Prompt Master Plan

## Goal

Replace the current vision screen with a minimalist agent-centered surface:

- a single abstract animated agent in the middle
- a modern prompt composer pinned to the bottom
- one backend-first ingest path for text and audio
- reuse of the same processing path for typed prompts and voice transcripts
- full documentation and validation coverage

## Principles

- Backend first.
- Reuse existing OpenAI and agent-operating-model infrastructure where possible.
- Keep the frontend thin and presentation-focused.
- Make the processing path testable with DTOs, services, controller tests, and doc validation.

## Phases

1. Backend ingest path
- Add a reusable vision prompt ingest service and DTOs.
- Accept text prompts and audio-derived text through one canonical backend surface.
- Map the ingest result into deterministic frontend state.

2. Frontend surface redesign
- Replace the current dense vision layout with a central animated agent.
- Add a bottom prompt composer that appears only when needed.
- Hook typed prompts and audio transcripts into the same backend ingest action.

3. Documentation and validation
- Update business and technical docs.
- Update agent operating model source-of-truth, intent coverage, and endpoint coverage.
- Refresh generated endpoint and contract artifacts.
- Add/extend tests for backend behavior and frontend compile safety.

## Acceptance Criteria

- `vision` remains functional with typed prompt entry.
- Audio transcription and typed input go through the same backend processing path.
- The screen is visually centered on the animated agent, not on form chrome.
- Backend tests, frontend type-check, and frontend build pass.
- Docs and generated artifacts stay in sync with the new behavior.


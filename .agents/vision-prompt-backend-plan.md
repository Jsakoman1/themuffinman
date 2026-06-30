# Vision Prompt Backend Plan

## Scope

Implement the canonical backend ingest path for vision prompts.

## Tasks

1. Add DTOs for prompt ingest and processing response.
2. Add a backend service that:
- validates authenticated access
- normalizes text input
- reuses OpenAI-backed processing where appropriate
- applies deterministic backend rules for vision state selection
3. Add a controller endpoint for prompt ingest.
4. Add tests for text prompts and empty/invalid input.
5. Update endpoint inventory and agent operating model coverage.

## Output

- One reusable backend entrypoint for text and audio-derived text.
- A predictable response shape the frontend can render without duplicating logic.


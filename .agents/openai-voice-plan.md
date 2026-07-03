---
machine_kind: plan
machine_status: unknown
machine_title: OpenAI Voice Plan
---

# OpenAI Voice Plan

Objective: replace browser STT/TTS on the vision surface with backend-managed OpenAI voice handling.

Steps:
1. Add typed voice provider settings and OpenAI-backed transcribe/speak services on the backend.
2. Expose dashboard voice endpoints for transcription and audio synthesis.
3. Switch the vision surface to record audio in the browser, send it to the backend, and play backend-generated audio.
4. Update tests, contracts, and living docs for the new voice path.

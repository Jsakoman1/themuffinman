---
machine_kind: plan
machine_status: unknown
machine_title: Dynamic Agent Model Plan
---

# Dynamic Agent Model Plan

Objective: make the admin OpenAI-backed planner default to `gpt-5.4-mini` and use `gpt-5.4-medium` only for the heaviest or most creative planning slices.

Steps:
1. Extend `AgentProperties` and `application.properties` with separate default and creative model settings plus the reasoning-effort default.
2. Add a lightweight model-profile selector in the admin agent service layer and route only the summary generation step through the creative model when the prompt is complex enough.
3. Keep prompt translation on the default model path.
4. Update tests and living docs to reflect the new model-selection behavior.

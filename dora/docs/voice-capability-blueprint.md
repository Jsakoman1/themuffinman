# Safe voice capability blueprint

Voice is an input surface, not an authority. A safe flow is:

`audio → transcription → semantic interpretation → typed capability candidate → deterministic validation → review → explicit confirmation → execution`

The model may help interpret language, but it must not grant permission, decide whether a transition is valid, or directly mutate product state. The consuming application owns its intent catalogue, entity resolution, mutation adapters, consent policy, retention policy, and provider credentials.

Treat uncertainty as a user-facing condition: ask for clarification or show a review. For a consequential action, lack of explicit confirmation always means no execution. Retain no raw conversational memory by default; evaluation should use approved fixtures and measure each stage separately.

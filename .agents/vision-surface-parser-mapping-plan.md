# Vision Surface, Parser, and Mapping Plan

## Scope
- Unify the vision surface so the composer, canvas, and recent state read as one adaptive surface instead of competing popups.
- Reduce parser leakage so description, location, and reward values land in the correct slots.
- Add or tighten an OpenAI semantic mapping layer between STT output and backend slot merging.

## Phases
1. Frontend surface review and layout simplification.
2. Backend semantic understanding and slot merge refinement.
3. Validation, generated artifacts, and docs sync.

## Notes
- Prefer incremental edits with existing vision components.
- Keep backend deterministic validation as the final authority.
- Reuse the current voice transcription endpoint and insert semantic understanding before slot merge rather than duplicating voice flow.

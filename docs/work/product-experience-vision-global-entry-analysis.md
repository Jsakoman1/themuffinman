# Global Vision entry analysis

## Findings

- Vision already has backend-governed conversation, review, confirmation, and voice configuration contracts.
- Repeated page-level Ask Vision buttons make Vision look like a competing navigation rail and create inconsistent prompt behavior.
- The shell can provide one persistent entry while each route supplies optional context metadata.

## Design decisions

- Implement a compact shell entry/popover or drawer with text composer, microphone button, transcript, processing state, result state, and link to the full Vision surface.
- Keep microphone permission and speech recognition state in a reusable client adapter; keep capability eligibility, intent classification, mutation confirmation, and execution in backend/Vision services.
- Context handoff carries route id and safe user-visible context, not private entity data inferred by the frontend.

## Risks and mitigations

- A global composer can become visually noisy: collapsed by default, explicit open state, and one primary action.
- Browser microphone support varies: always retain text fallback and explain denial/unsupported states.
- Global Vision can accidentally duplicate navigation: contextual links must be secondary and route metadata must be audited.

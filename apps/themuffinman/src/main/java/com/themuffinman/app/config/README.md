# Config Backend Capsule

## Responsibility

Owns typed operational configuration for security, bootstrap, retention, location provider settings, websocket wiring, admin agent defaults, and adaptive OpenAI voice defaults.

## Main Entry Points

- Properties: `AgentProperties.java`, `BootstrapProperties.java`, `LocationProviderProperties.java`, `RetentionProperties.java`, `SecurityProperties.java`, `VoiceProperties.java`
- Runtime wiring: `AdminBootstrapConfig.java`, `SecurityConfig.java`, `WebSocketConfig.java`

## Tests

- `src/test/java/com/themuffinman/app/config/`

## Living Docs

- `docs/domain-technical.md`
- `docs/implementation-control.md`

## Forbidden Shortcuts

- Do not scatter runtime settings across ad hoc `@Value` fields when a typed properties class fits.
- Do not hide operational defaults in unrelated services or controllers.
- Do not change operational defaults without updating the matching documentation.
- Admin agent model defaults now include a routine summary model, a creative fallback model, and a shared reasoning-effort setting.
- Voice defaults now include OpenAI transcription and speech synthesis models, local recording and payload limits, plus the shared API endpoint and key mapping.
- `OPENAI_API_KEY` is the shared credential for both the admin agent summary path and the adaptive voice path.

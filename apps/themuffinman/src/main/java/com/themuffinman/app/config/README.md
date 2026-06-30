# Config Backend Capsule

## Responsibility

Owns typed operational configuration for security, bootstrap, retention, location provider settings, websocket wiring, and admin agent defaults.

## Main Entry Points

- Properties: `AgentProperties.java`, `BootstrapProperties.java`, `LocationProviderProperties.java`, `RetentionProperties.java`, `SecurityProperties.java`
- Runtime wiring: `AdminBootstrapConfig.java`, `SecurityConfig.java`, `WebSocketConfig.java`

## Tests

- `src/test/java/com/themuffinman/app/config/`

## Living Docs

- `docs/domain-technical.md`
- `docs/documentation-sync-policy.md`

## Forbidden Shortcuts

- Do not scatter runtime settings across ad hoc `@Value` fields when a typed properties class fits.
- Do not hide operational defaults in unrelated services or controllers.
- Do not change operational defaults without updating the matching documentation.

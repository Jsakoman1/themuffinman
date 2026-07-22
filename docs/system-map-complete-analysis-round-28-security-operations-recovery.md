# Round 28: Security, Operations, and Recovery Boundaries

Status: source-trace analysis. Reviewed: 2026-07-22.

## Conclusion

The backend centralizes operational configuration in typed `@ConfigurationProperties`
objects and environment-variable mappings. Security includes JWT authentication,
CORS configuration, backend permission services, visibility/consent policy, token
revocation, retention settings, and provider/storage configuration. These source
controls do not prove deployment secret handling, backups, restore drills, provider
SLAs, or incident response execution.

## Control groups

| Group | Source control | Evidence limit |
|---|---|---|
| Authentication/session | security config, JWT properties, revoked-token records | runtime expiry and deployment key rotation need evidence |
| Authorization/visibility | backend services, domain rules, circle/location/profile policies | policy path must be checked per domain flow |
| Retention | typed retention properties and scheduled cleanup services | job execution and deletion outcomes need operator/runtime proof |
| Providers | location, OpenAI agent/voice, object storage properties | configured fallback does not prove provider recovery |
| Storage | object-storage configuration, attachment lifecycle, local fallback | bucket policy, backup and restore are outside workspace proof |
| Handoff | native handoff token and TTL properties | native client/device behavior is unproven |

## Operational gaps to retain

- No workspace evidence establishes deployed secret rotation, backup retention,
  restore testing, deployment topology, or operational alert response.
- Visibility and consent policy is distributed across identity, social, location,
  workmarket, chat, and sharing flows; the registry identifies the policy owners but
  does not replace their domain-specific enforcement.
- Synthetic/admin generation must remain separate from production-like user actions
  under the agent operating model.

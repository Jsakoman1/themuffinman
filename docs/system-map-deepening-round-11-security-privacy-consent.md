# System Map Deepening — Round 11: Security, Privacy, and Consent

Date: 2026-07-22  
Status: **observed analysis**  
Scope: authentication, authorization, profile/circle/location consent, retention, secrets, abuse controls, and cross-module threat boundaries

## Executive finding

Security is primarily backend-owned and implemented through stateless JWT
authentication, Spring Security route gates, service-level ownership checks,
circle/visibility policies, validation/sanitization, rate limits, retention, and
runtime acceptance scenarios. The strongest protection boundary is the backend
service layer; the frontend is a presentation and consent-capture client.

The main risk is distributed policy interpretation. Identity owns the user and
profile fields, social owns relationship/circle state, and consuming domains
decide whether a resource is visible or actionable. Exact location, circle
visibility, chat context, attachments, Vision actions, and admin operations must
therefore be audited as cross-domain policies rather than isolated controller
rules.

## Authentication boundary

The observed authentication flow is:

```text
register/login -> JWT issued -> Authorization: Bearer token
                         |
                         v
                 JwtAuthFilter
                         |
       revoked-token check + AppUser lookup + role authority
                         |
                    stateless request
```

Observed controls:

- BCrypt password encoding;
- stateless Spring Security sessions;
- JWT signature and expiration validation;
- database-backed revoked-token checks;
- role authority derived from the current `AppUser` record;
- unauthenticated access limited to auth bootstrap endpoints and selected
  explicitly public surfaces;
- authenticated principal passed into controllers and services.

The token contains user identity claims, while authorization still resolves the
current user from the repository. That is preferable to trusting all mutable
authorization facts embedded in the token. Token expiration and cleanup are
configured; secret rotation and multi-key rollover are not evidenced.

## Route and role boundary

The global security configuration permits registration, login, recovery/reset,
selected preflight requests, and WebSocket handshake paths. Chat routes require
authentication, chat admin routes require `ADMIN`, Quest routes require
authentication, and broad app-user admin paths require `ADMIN`. Domain services
then perform finer ownership and workflow checks.

This two-layer model is important:

1. Spring Security answers whether the request has a suitable coarse authority.
2. The domain service answers whether this actor owns, participates in, or may
   view/change the exact resource and state.

The route gate alone is therefore not sufficient evidence of authorization.

## Privacy and visibility model

### Profile visibility

Profile description and avatar visibility are represented by visibility enums and
explicit circle/user allow-lists. Profile gallery and profile fields are identity
owned. Social relationships provide the circle membership context used by
visibility decisions.

### Circle-scoped content

Quest audience and ride audience can reference circles. Circle membership and
relationship state are social-owned, while Workmarket/Rides enforce whether a
viewer is eligible to see or act on the resource. This avoids putting product
authorization in the circle package alone, but increases the need for sibling
surface audits.

### Location privacy

Location has preference, lookup, visibility, and exactness concepts. Runtime
evidence covers permission denial, preference revoke, owner visibility policy,
and location context flows. Exact-location decisions are high-risk because they
combine user consent, display scope, circle/user allow-lists, and external lookup
provider behavior.

### Chat and attachments

Conversation membership, participant roles, attachment access, expiry, and audit
events are separate concerns. Attachment access is not equivalent to knowing a
conversation ID; the backend applies authorization before resolving storage
objects. Expiry and storage metadata are operational privacy controls as well as
retention concerns.

### Vision and agent actions

Vision has explicit execution planning, candidate resolution, confidence,
blocking, and retry concepts. The operating model requires authority, exact
targets, and destructive confirmation for sensitive actions. Vision should remain
an authorized orchestration client; product services remain the final policy
boundary.

## Consent and data lifecycle

Observed consent/lifecycle controls include:

- browser location permission and user location preference;
- visibility settings for profile and exact location;
- circle membership and relation acceptance/blocking;
- retention TTLs for Vision conversations, chat messages/images, notifications,
  and revoked auth tokens;
- expired attachment behavior and placeholder handling;
- explicit reset/cancel/retry behavior for Vision conversations;
- safety-report creation and moderation-oriented chat audit surfaces.

These controls are concrete, but the repository does not yet present a single
data-classification register mapping every persisted field to sensitivity,
retention owner, deletion behavior, export behavior, and audit requirement.

## Abuse and boundary controls

The source includes rate limits for chat opening, sending, delivery, reads,
typing, reactions, moderation removal, attachment sizes/MIME types, and related
presence behavior. Rich text is sanitized in relevant product services. Safety
reports provide a user-facing trust boundary across user, Quest, ride, business,
thing, circle, and chat families.

These are meaningful abuse controls. Remaining questions include global per-user
limits across all modules, IP/device controls, brute-force protection around auth,
provider abuse/cost limits, and operational alerting when limits are repeatedly
hit.

## Threat-boundary matrix

| Boundary | Backend control | Evidence strength | Open question |
|---|---|---|---|
| JWT/session | signature, expiry, revocation, stateless filter | source + auth runtime | secret rotation and key rollover |
| Admin | role route gates + service checks | source + agent safety | complete admin endpoint matrix |
| Profile | visibility enums and allow-lists | source + profile runtime | field-level classification/deletion |
| Circles | membership/relation services and consumer checks | source + visibility docs | consistency across every consumer |
| Location | preference, exactness, scope, consent | source + runtime | provider retention and audit trail |
| Chat | membership, participant roles, attachment authorization | source + runtime | cross-instance WebSocket and abuse operations |
| Vision | typed plans, resolution, confirmation, blocking | source + agent/runtime evidence | production provider and prompt threat model |
| Storage | metadata, expiry, presigned/local access | source + attachment runtime | bucket policy, encryption, lifecycle ops |
| Retention | typed config and scheduled cleanup | source | job failure/replay and deletion proof |

## High-risk findings

1. **Policy fan-out.** Visibility and consent are not owned by one service;
   changes require identity, social, product, chat, Vision, and runtime review.
2. **JWT secret lifecycle gap.** Configuration mapping exists, but rotation and
   invalidation across active sessions are not documented as an operational
   procedure.
3. **CSRF/CORS deployment sensitivity.** CSRF is disabled because the API is
   bearer-token oriented, while CORS allows credentials. Production origin
   configuration must be explicit and not rely on localhost defaults.
4. **Retention evidence gap.** Scheduled cleanup code and TTL settings exist, but
   there is no end-to-end proof that all related blobs, audit rows, indexes, and
   derived records are deleted or anonymized correctly.
5. **Agent destructive boundary.** Agent metadata requires confirmation and exact
   targets, but every destructive intent needs ongoing parity checks against
   backend authorization and UI confirmation behavior.
6. **Native handoff sensitivity.** Handoff tokens cross a client boundary and
   need explicit one-time use, expiry, audience, replay, and logging guarantees.

## Required follow-up

1. Create a canonical data-classification and retention matrix.
2. Build a cross-module visibility/consent matrix keyed by field/resource,
   viewer relationship, scope, endpoint, service, test, and runtime evidence.
3. Document JWT secret/key rotation and handoff-token replay procedures.
4. Add negative tests and runtime evidence for every high-risk visibility class.
5. Add global abuse-control and provider-cost boundaries to the operational map.
6. Audit Vision intents and native handoff against the same authorization source
   used by direct Web actions.

## Source evidence

- `apps/themuffinman/src/main/java/com/themuffinman/app/config/SecurityConfig.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/config/SecurityProperties.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/identity/security/*`
- `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/ProfileVisibilityService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/*`
- `apps/themuffinman/src/main/java/com/themuffinman/app/location/**`
- `apps/themuffinman/src/main/java/com/themuffinman/app/chat/**`
- `apps/themuffinman/src/main/java/com/themuffinman/app/trust/**`
- `apps/themuffinman/src/main/java/com/themuffinman/app/config/*Properties.java`
- `docs/cross-module-visibility-acceptance.yaml`
- `docs/agent-operating-model.yaml` and referenced sections
- `docs/runtime-evidence/profile-visibility-2026-07-19.json`
- `docs/runtime-evidence/location-*.json`
- `docs/runtime-evidence/chat-attachment-authorization-boundary-2026-07-19.json`

## Conclusion

Round 11 confirms a serious backend-centered security model with meaningful
privacy, retention, authorization, and abuse controls. The principal improvement
needed is consolidation of distributed policy evidence into canonical matrices,
especially for circles, exact location, chat attachments, Vision actions, and
native handoff.

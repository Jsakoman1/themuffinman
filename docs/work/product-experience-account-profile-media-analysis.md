# Account and profile-media analysis

## Findings

- The current authenticated shell displays the username and logout as separate shell controls, while profile settings already owns location and identity settings.
- Existing object storage and media patterns can be reused, but avatar and gallery semantics are not interchangeable.
- Viewer-specific visibility and consent are existing domain concerns and must not be reproduced in Vue.

## Contract proposal

- Add an avatar reference to the user profile projection and a separate `ProfileGalleryImage` aggregate owned by the user.
- Expose viewer DTOs with public URL/thumbnail, ordering, alt text/caption, allowed actions, and visibility result.
- Enforce content type, byte size, dimensions, ownership, deletion, and retention in backend services; store binaries through the existing object-storage abstraction.

## Risks and mitigations

- Exposing original media URLs can bypass visibility: return signed or policy-checked representations.
- Gallery uploads can create unbounded storage: add limits, count/size quotas, and cleanup rules before UI work.
- Synthetic/admin generation must create media metadata without relying on production upload flows.

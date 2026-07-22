# Round 21: Endpoint, DTO, Route, and Client Consumer Registry

Status: source-trace analysis. Reviewed: 2026-07-22.

## Conclusion

The backend exposes 214 statically discovered REST endpoints through 39 controller
files. The frontend audit finds 194 client methods and 140 endpoint links. Linkage
is evidence of a source call path, not proof that a user can discover, complete, or
recover from the related flow.

The 74 unlinked endpoints are a classification queue. They include plausible admin,
internal, native-handoff, server-only, or not-yet-consumed surfaces. They must not
be treated as missing Web support without a consumer and authority review.

## Consumer classes

| Class | Evidence required | Current interpretation |
|---|---|---|
| Web | API method plus reachable surface/action | source call link may exist; runtime remains separate |
| Vision | backend-published semantic or execution contract | direct route opening is not automatic mutation authority |
| Admin | admin controller and protected admin surface/test | not expected to have a normal user Web consumer |
| Native handoff | handoff contract and backend endpoint | contract-ready does not prove a native implementation |
| Server-only | event, provider, scheduler, or internal integration path | no browser consumer required |
| Unknown | endpoint has not yet been classified | backlog or registry follow-up required |

## Interface boundary findings

- Controllers are organized by domain and remain the HTTP boundary; services remain
  the intended authority layer for state and permission decisions.
- The endpoint linker only scans frontend module `*Api.ts` call patterns. WebSocket,
  browser-native voice, generated contract use, and indirect transport adapters need
  their own evidence class rather than being called unlinked REST endpoints.
- The generated TypeScript contract is derived from supported backend DTO sources
  through `generate-vision-contracts.mjs`; it is a contract artifact, not a complete
  endpoint registry.
- Native handoff has an explicit controller and contract, but no native source tree
  or device proof is available in this workspace.

## Required follow-up disposition

Round 24 classifies discoverability and UI completion for linked Web surfaces.
Round 25 links endpoint classes to tests and runtime evidence. Unclassified exposed
endpoints should receive an ARCH-001-compatible registry row or stable delivery
follow-up; no capability status is changed by this analysis.

# System Map Third Pass — Round 18: Cross-Client Parity and Contract Readiness

Date: 2026-07-22  
Status: **observed analysis**

## Executive finding

The backend capability model is intentionally client-neutral: Web, Vision, admin,
native handoff, mobile, and Watch are modeled as different surfaces over shared
backend rules and contracts. Current Web and Vision linkage is broad and mature;
admin linkage is explicit for selected operations; native/mobile/Watch remain
contract and handoff surfaces rather than implemented clients.

The key distinction is between **contract parity** and **runtime parity**. The
repository can document and generate a shared contract while still lacking a
device implementation or passing browser/device evidence.

## Client surface model

```text
                       backend capability
                    /        |          \
                   /         |           \
              Web shell    Vision       Admin
                   |         |             |
             direct UI   semantic/typed   privileged UI/agent
                         orchestration
                              |
                    native handoff contract
                         /             \
                    iPhone             Watch
```

Web and Vision are equal production clients. Vision can navigate or execute only
through backend-published capabilities, resolved targets, permission results,
review, and confirmation. Native handoff provides a future client boundary, not a
second business-rule implementation.

## Capability parity evidence

The capability inventory provides current backend, web, vision, native, plan, and
gap fields. It explicitly records that many Work, Business, Things, Rides,
Circles, Chat, and platform capabilities have Web and Vision representations.
It also marks gaps such as native iPhone and Watch clients, native shell and
notification consumers, typed binary attachment handoff, upload recovery,
runtime acceptance across all discovery families, full Vision execution/recovery
proof, and production-scale ranking/indexing.

This is useful parity evidence because `/vision` references or handoff contracts
are not treated as equivalent to a native client.

## Web and Vision parity

The Web client has a shared authenticated shell and module surfaces for Work,
Business, Things, Rides, Chat, Circles, notifications, activity, profile, and
settings. Static audits confirm route/action entrypoint connectivity and client
callsite linkage for most mapped capabilities. Web parity remains incomplete
where a route lacks full happy-path, validation, recovery, or runtime proof.

Vision has the broadest semantic coverage. Intents map to API endpoint IDs and
typed execution adapters cover many product mutations. Vision supports review,
clarification, target resolution, confirmation, cancellation, blocking, and
recovery. Its parity is strongest for contract shape and weaker where provider
availability, real execution, or full end-to-end recovery remains pending.

Vision must not be treated as a privileged shortcut around direct Web policy.

## Admin parity

Admin surfaces are a separate authority class. The operating model defines admin
intents, exact target resolution, destructive confirmation, and failure cases.
Admin coverage should be measured against role, target, auditability, and
destructive-action controls rather than ordinary Web consumer parity.

## Native/mobile/Watch readiness

The native handoff domain exposes presentation and token boundaries. Contract-level
readiness exists through device/density metadata, typed presentation/handoff
contracts, expiry concepts, and reusable backend actions.

Implementation/evidence readiness does not yet exist: no native iPhone or Watch
application source was found in the active product tree, and no device trace
proves handoff consumption, offline read, reconnect mutation, or safe confirmation.

## Contract layers

| Contract layer | Canonical source | Consumer status |
|---|---|---|
| backend DTO/enums | Java models/DTOs | Web/Vision generated transport types |
| agent API catalog | referenced `api.yaml` | agent and generated frontend constants |
| intent catalog | referenced `intents.yaml` | semantic/agent orchestration |
| capability inventory | `capability-inventory.yaml` | cross-layer evidence/status map |
| target catalog | `target-capability-catalog.yaml` | target scope and client expectations |
| native handoff | handoff/presentation docs and backend endpoints | future native consumer |
| runtime acceptance | runtime matrix + evidence JSON | browser/device proof |

The generated frontend contract is fresh and consumes referenced API/intent
sections. This protects schema/catalog parity, not complete endpoint usage or
client behavior.

## Main parity risks

1. Broad current mappings can conceal partial interaction flows.
2. Vision coverage can be mistaken for Web completeness or native readiness.
3. Native handoff contracts can become stale without an actual device consumer.
4. Admin capabilities require separate role/evidence criteria.
5. Runtime scenarios cover selected capabilities, not every endpoint/client.
6. Attachments, notifications, realtime, exact location, and recovery are the
   hardest cross-client parity surfaces.

## Recommendations

1. Maintain a parity matrix keyed by capability, client, entrypoint, contract,
   permission, state transitions, recovery, and runtime evidence.
2. Split status into contract-ready, implementation-present, and runtime-verified.
3. Prioritize device proof for handoff, Watch-safe actions, offline read, reconnect
   mutation, attachment transfer, and notification read state.
4. Keep Web and Vision acceptance criteria symmetric for production capabilities.
5. Require admin-specific target/confirmation/audit evidence for admin actions.

## Source evidence

- `docs/target-capability-catalog.yaml`
- `docs/capability-inventory.yaml`
- `docs/agent-operating-model/sections/api.yaml`
- `docs/agent-operating-model/sections/intents.yaml`
- `apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts`
- `apps/themuffinman/frontend/src/modules/app-shell/**`
- `apps/themuffinman/frontend/src/modules/vision/**`
- `apps/themuffinman/src/main/java/com/themuffinman/app/nativehandoff/**`
- `docs/native-client-handoff-contract.yaml`
- `docs/runtime-acceptance-matrix.yaml`

## Conclusion

Round 18 confirms strong Web/Vision contract parity and explicit future-client
planning, but not native runtime parity. The next architectural decision should
be driven by a matrix separating contract, implementation, and runtime proof for
every client surface.

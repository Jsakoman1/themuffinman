# Round 26: Performance Evidence Design

Status: measurement-design analysis. Reviewed: 2026-07-22.

## Conclusion

Static inspection identifies fetch and read-model complexity but cannot establish
latency, row volume, memory pressure, or scale behavior. This round creates the
measurement catalog required before production-scale performance claims or service
extraction decisions.

## Priority measurement families

| Family | Candidate surfaces | Why it is a candidate | Required evidence |
|---|---|---|---|
| Business booking/calendar | detailed owner/customer reads, availability windows | multiple joined booking/profile/offering relations | query count, SQL timing, rows, concurrent booking result |
| Chat sync | detailed conversation and message pages | participant, state, sender, attachment and realtime recovery joins | query count, page rows, reconnect sync timing |
| Work discovery/detail | quest search, application/detail/dashboard reads | viewer-specific actions, location and application relations | query count, pagination timing, representative result size |
| Social/profile | relationship-aware profile and circles reads | visibility and relationship policy dependencies | query count, authorization-safe result inspection |
| Vision conversations | conversation/turn/memory context assembly | high fan-in and request correlation behavior | turn latency split by provider, DB and response assembly |

## Measurement rule

Every future trace must state dataset shape, authenticated viewer role, endpoint or
service operation, query count, wall time, row volume where available, cache state,
provider participation, and environment. A static fetch audit score is retained as
a prioritization signal only.

## Boundary

No timing or memory result was collected by this round. The catalog is the input to
a dedicated runtime measurement plan with new evidence artifacts.

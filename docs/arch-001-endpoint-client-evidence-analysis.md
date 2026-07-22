# ARCH-001 Endpoint-to-Client-to-Evidence Analysis

Reviewed: ARCH-001 baseline pass.

## Existing sources reconciled

The repository already has several specialized registries: endpoint capability families, interface reconciliation, client surfaces, capability evidence, runtime acceptance, permission/visibility, and native handoff. They answer different questions and must remain specialized. The missing layer is a canonical cross-reference that links one endpoint family to its capability family, consumers, authority, tests, runtime scenarios, protocol class, and evidence state.

## Baseline

The interface reconciliation audit currently classifies 214 backend endpoints: 140 Web-linked, 4 admin/agent, 3 native-handoff, and 67 unclassified non-Web. Classification is static source evidence; it does not prove route reachability, user-flow completion, native implementation, or runtime success.

## Registry design

The canonical registry is family-level first, with endpoint-level diagnostic references retained in the generated audit artifact. This avoids copying 214 rows into a hand-maintained document while preserving a deterministic link to the per-endpoint diagnostic output.

Every family row must identify: capability prefix, endpoint family, owning module, protocol class, expected consumers, authority source, test sources, runtime scenarios, native status, and coverage state. Unknown and unclassified states are first-class values.

## Coverage interpretation

- `source_traced`: code and contract links exist.
- `partially_evidenced`: some tests or runtime artifacts exist.
- `runtime_pending`: required runtime proof is not present.
- `unclassified_review`: endpoint/consumer relationship needs an explicit decision.
- `native_contract_only`: handoff contract exists without native/device proof.

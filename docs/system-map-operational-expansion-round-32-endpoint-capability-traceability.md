# Round 32: Endpoint to Capability Traceability

Status: source-trace analysis. Reviewed: 2026-07-22.

## Conclusion

Endpoint traceability is now represented at capability-family level, with the
existing reconciliation report retaining per-endpoint entries. This avoids a
duplicate 214-row source while allowing feature work to navigate from a capability
to its API family, consumer class, test/runtimes sources, and unknown boundary.

## Traceability rules

- A `web_source_linked` endpoint has a statically discovered frontend call path;
  it is not automatically a discoverable, recoverable, or runtime-proven feature.
- Admin, agent, native-handoff, and unclassified non-Web classes are explicit
  consumer-evidence states, not missing-feature or security conclusions.
- Capability IDs remain owned by `capability-inventory.yaml`; this registry only
  joins them to interface and evidence sources.

## Coverage boundary

The registry maps the principal endpoint families for the current eight modules.
The current endpoint reconciliation output remains the per-endpoint source for the
214 discovered rows and its 67 unclassified non-Web review items.

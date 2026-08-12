# Intent & Delivery Context pilot v0

This local module is an advisory workspace, not a Dora extension or a second source of truth.

It validates and deterministically renders file-backed dossiers supplied by an owner or an owner-mediated process. It has two profiles:

- `research_dossier` for non-coding research and option comparison.
- `greenfield_product_delivery_baseline` for a new software product before a Dora Master Plan.

Every claim has a status, confidence, and cited source. Sources record a locator, revision or digest, observation time, confidence, and freshness. `conflict`, `missing_context`, `open_question`, and non-current source freshness are rendered as visible alerts.

## Authority boundary

Dora remains the only canonical owner of decisions, plans, work lifecycle, evidence, ProjectMemory, and verified status. IDC has no automatic Dora read, no Dora write, no ProjectMemory copy, and no authority to promote an advisory statement. A Dora reference reaches this module only as a manually supplied, sanitized `idc_dora_read_envelope` with provenance.

An IDC promotion proposal is text for owner review only. It can become Dora state only after explicit owner confirmation through Dora's existing authorized workflow.

The pilot does not contain a service, database, vector index, MCP server, agent runtime, personal-memory store, external research fetcher, Git capability, production-code capability, Codex invocation, or shell runtime.

## Local contract

`IDC::Dossier.load!` validates a YAML dossier and `#render` produces stable Markdown. `IDC::DoraEnvelope.load!` validates only a manually imported sanitized read-envelope; it does not resolve or read the referenced Dora files.

Run the pilot contract test with:

```text
ruby idc/test/dossier_test.rb
```

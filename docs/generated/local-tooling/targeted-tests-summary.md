# Targeted Tests

- Why: This is a targeted recommendation report, not a replacement for full validation.; Use full `cd apps/themuffinman && ./mvnw test` for high-risk backend behavior, schema, or broad cross-domain changes.
- Next action: `make audit-documentation`, `make audit-doc-canonical-phrases`

## Details

- Original File Count: 1
- Filtered File Count: 0
- Excluded File Count: 0
- Recommended commands: command: make audit-documentation | reason: Docs, plans, or agent artifacts changed. | confidence: high | covers: .agents/feature-manifests/chat-prod-ready-manifest.yaml | command: make audit-doc-canonical-phrases | reason: Protected documentation wording may be affected by docs or agent-safety edits. | confidence: medium | uncovered: Does not validate Java-side agent operating model tests.
- Notes: This is a targeted recommendation report, not a replacement for full validation. | Use full `cd apps/themuffinman && ./mvnw test` for high-risk backend behavior, schema, or broad cross-domain changes.

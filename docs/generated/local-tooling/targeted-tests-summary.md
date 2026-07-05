# Targeted Tests

- Why: This is a targeted recommendation report, not a replacement for full validation.; Use full `cd apps/themuffinman && ./mvnw test` for high-risk backend behavior, schema, or broad cross-domain changes.
- Next action: `make audit-generated-artifact-freshness`, `make audit-generated-commit-scope`

## Details

- Original File Count: 5
- Filtered File Count: 0
- Excluded File Count: 0
- Recommended commands: command: make audit-generated-artifact-freshness | reason: Generated artifacts, generation scripts, or Make targets changed. | confidence: high | covers: Makefile, scripts/audits/audit-generated-artifact-freshness.rb, scripts/implementation-batch.sh | command: make audit-generated-commit-scope | reason: Classifies changed generated artifacts before closeout. | confidence: medium | uncovered: Advisory only; reviewer still chooses which generated files belong in the changeset.
- Notes: This is a targeted recommendation report, not a replacement for full validation. | Use full `cd apps/themuffinman && ./mvnw test` for high-risk backend behavior, schema, or broad cross-domain changes.

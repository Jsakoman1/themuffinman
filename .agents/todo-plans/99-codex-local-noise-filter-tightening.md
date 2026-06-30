# CODEX-LOCAL-NOISE-FILTER-TIGHTENING Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `medium`
Master order: 99 of 99

## Backlog Item

Make generated report summaries and closeout text more decision-first by stripping repetitive build noise and keeping only the signal needed for fast review.

## Source Findings

- [`scripts/audits/clean-text-noise.rb`](/Users/jsakoman/Desktop/themuffinman/scripts/audits/clean-text-noise.rb)
- [`scripts/audits/local_tooling_extended_tools.rb`](/Users/jsakoman/Desktop/themuffinman/scripts/audits/local_tooling_extended_tools.rb)
- [`docs/generated/local-tooling/generated-artifact-freshness-summary.md`](/Users/jsakoman/Desktop/themuffinman/docs/generated/local-tooling/generated-artifact-freshness-summary.md)
- [`docs/generated/local-tooling/closeout-report-summary.md`](/Users/jsakoman/Desktop/themuffinman/docs/generated/local-tooling/closeout-report-summary.md)

## Implementation Plan

- [x] Expand the noise filter so it trims common generated-report chatter more aggressively.
- [x] Apply the cleaner to the generated summaries that feed closeout and review workflows.
- [x] Keep raw outputs available where they matter for diagnostics.
- [x] Ensure the short summaries still point to the underlying artifact paths.

## Expected Validation

- `make clean-text-noise max_lines=80 aggressive=true`
- `make audit-generated-artifact-freshness`
- `make audit-plan-completion plan=.agents/todo-plans/99-codex-local-noise-filter-tightening.md`

## Completion Evidence

- Status: complete
- Backlog update: complete.
- Residual risk: the cleaner must not remove failure anchors that people still need during debugging.

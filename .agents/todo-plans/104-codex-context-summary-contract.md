# CODEX-CONTEXT-SUMMARY-CONTRACT Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `medium`
Master order: 104 of 105

## Backlog Item

Standardize generated summary output toward decision-first blocks so each audit says what matters, why it matters, what to do next, and what evidence backs it up.

## Source Findings

- [`scripts/audits/clean-text-noise.rb`](/Users/jsakoman/Desktop/themuffinman/scripts/audits/clean-text-noise.rb)
- [`scripts/audits/local_tooling_extended_tools.rb`](/Users/jsakoman/Desktop/themuffinman/scripts/audits/local_tooling_extended_tools.rb)
- [`docs/generated/local-tooling/generated-artifact-freshness-summary.md`](/Users/jsakoman/Desktop/themuffinman/docs/generated/local-tooling/generated-artifact-freshness-summary.md)
- [`docs/generated/local-tooling/closeout-report-summary.md`](/Users/jsakoman/Desktop/themuffinman/docs/generated/local-tooling/closeout-report-summary.md)

## Implementation Plan

- [x] Normalize the generated summary shape toward decision, why, next action, and evidence sections.
- [x] Keep raw JSON available for machine consumers while shortening the human summaries.
- [x] Apply the format to the high-noise local-tooling summaries first.
- [x] Keep failure anchors and path references intact.

## Expected Validation

- `make clean-text-noise max_lines=80 aggressive=true`
- `make audit-generated-artifact-freshness`
- `make audit-plan-completion plan=.agents/todo-plans/104-codex-context-summary-contract.md`

## Completion Evidence

- Status: complete
- Backlog update: resolved.
- Residual risk: aggressive summarization must not remove the anchors needed for debugging.

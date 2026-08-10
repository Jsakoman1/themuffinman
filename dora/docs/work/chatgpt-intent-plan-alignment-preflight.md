# MP-01 intent-plan alignment preflight

## Canonical boundary

An owner-authored ChatGPT Intent Plan is an input proposal, not a Dora plan. It is
validated only in memory and never becomes a work plan, Master Plan, decision, task,
lifecycle event, or evidence record. Dora's existing project read model supplies the
only comparison state: declared active delivery, latest verified delivery, unresolved
owner decisions, and accepted decision-log entries.

## Bounded proposal contract

The proposal contains one identifier, intended outcome, in-scope work, non-goals,
fixed owner decisions, candidate slices, and the required owner lifecycle readback
fields. Each candidate slice has an identifier, a short outcome, declared prior-slice
dependencies, and allowlisted gates. The readback declaration must request exactly
phase, alignment result, first safe next action, and actionable blocker or decision.
No references, paths, commands, source material, logs, environment values, or free-form
readback text are accepted.

## Deterministic reconciliation

The evaluator rejects malformed, incomplete, unsafe, oversized, cyclic, or duplicate
proposals without echoing their content. A declared fixed decision must refer to an
accepted Dora decision and match its current canonical text. Active delivery, an
unresolved owner decision, or a fixed-decision mismatch returns
`OWNER_DECISION_NEEDED`. A valid proposal with no prior delivery is `ACCEPTED`; one
with a prior verified Dora delivery is `RECONCILED` and retains that delivery only as a
baseline. Neither result mutates canonical state.

Only the first candidate may be identified as eligible. It must be dependency-free and
gated by `no_owner_decision_pending`. Every later candidate must depend only on earlier
candidates and also declare `prior_slice_verification`; it remains blocked until the
declared predecessor has Dora verification evidence and no owner decision is pending.

## Authority and redaction

Evaluation is an additive read-only bridge operation for an explicitly allowlisted
project. It does not add a generic mutation, handoff mutation, shell, filesystem, git,
patch, source retrieval, process control, terminal streaming, remote runner, or Codex
control path. Its result contains only fixed phase/outcome/action/blocker language and
validated candidate identifiers; it does not echo proposal prose, canonical decision
text, source content, raw evidence, paths, commands, environment data, or secrets.

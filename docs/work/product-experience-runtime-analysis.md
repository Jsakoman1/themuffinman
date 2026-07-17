# Product experience runtime and closeout analysis

## Required evidence

- Shell: one title, one Vision entry, account popover, responsive keyboard access.
- Vision: text, microphone permission denial, processing/error recovery, contextual handoff.
- Profile media: avatar upload/replace/remove, gallery add/order/delete, visibility from a second user.
- Discovery: all six explicit entries, empty/error/retry, external-versus-owned separation.
- Business: two owned businesses, switch context, favorite public business, favorite-to-book flow.
- Commute: opt-in, pause/revoke, safe suggestion, blocked/hidden user exclusion, explicit ride confirmation.

## Closeout rule

Static tests and builds prove contracts, not user flows. Every runtime scenario must record browser evidence in `docs/runtime-acceptance-matrix.yaml`; the master closes only after child verifiers and runtime audit pass.

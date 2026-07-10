---
machine_kind: plan
machine_status: complete
machine_title: Frontend User Shell Validation And Docs Plan
machine_goal: Close the shell/navigation work with aligned docs, validation evidence,
  and any required contract updates.
---

# Frontend User Shell Validation And Docs Plan

## Status

Complete.

## Workflow Frame

- Feature tier: tier2 or tier3 depending on whether the implementation changes route contracts, navigation DTOs, and living docs together
- Scope: docs sync, validation evidence, route ownership proof, and closeout
- Out of scope: net-new product behavior unrelated to shell and navigation
- Manifest decision: resolver-driven, with elevated chance of required manifest if the broader workstream lands as a multi-surface change
- Manifest path: TBD
- Master plan: `.agents/frontend-user-shell-navigation-master-plan.md`

## Routing Snapshot

- Context commands:
  - inspect changed route files, shell files, Vision integration files, and required living docs
  - `make control-start`
- Routing commands:
  - `make audit-doc-sync-required-surfaces files=<csv>`
  - `make audit-manifest-decision files=<csv>`
  - `make resolve-manifest-path files=<csv>` if needed
- Validation commands:
  - `npm run type-check`
  - `npm run build`
  - any targeted backend checks introduced by the implemented route/surface changes
- Closeout commands:
  - `make audit-todo`
  - `make audit-plan-completion plan=.agents/frontend-user-shell-validation-and-docs-plan.md`
  - manifest-driven closeout commands if the resolver requires them
- Doc sync commands:
  - update required living docs in the same change
- Generated artifact commands:
  - resolver-driven

## Preferred Local Tooling

- Tooling map: `.agents/tmp/frontend-user-shell-local-tooling-map.md`
- Most relevant generated surfaces:
  - `docs/generated/local-tooling/doc-sync-preflight-summary.md`
  - `docs/generated/local-tooling/doc-sync-required-surfaces-summary.md`
  - `docs/generated/local-tooling/manifest-decision-summary.md`
  - `docs/generated/local-tooling/validation-preset-summary.md`
  - `docs/generated/local-tooling/audit-summary-index.md`
- Most relevant commands:
  - `make audit-documentation`
  - `make audit-doc-canonical-phrases`
  - `make recommend-targeted-tests`

## Execution Discipline

- This plan should remain a later closeout slice, not an early parallel implementation stream.
- When this plan becomes the active slice, treat it as the only practical active child slice unless a real dependency forces overlap.
- Start from `docs/generated/local-tooling/codex-context/latest.review.md` plus the actual changed-file scope and resolver outputs, not from assumed propagation.
- During the active master-plan batch, do not stop after partial closeout; finish validation, doc sync, manifest sync, and final plan-state updates before ending the batch unless a real blocker appears.

## Goal

Close the shell/navigation work with aligned docs, validation evidence, and any required contract updates.

## Scope

- Included:
  - doc sync
  - validation evidence
  - route ownership documentation
  - product-direction alignment notes
  - technical-source updates required by the implemented shell
- Excluded:
  - unrelated workflow-doc rewrites
  - non-shell generated artifact cleanup outside scope

## Key Outputs

- updated living docs
- validation record
- closeout summary
- deferred follow-up capture if gaps remain

## Implementation Slices

- [x] Slice 1: determine final doc-sync surface based on implemented files
- [x] Slice 2: align route ownership, shell behavior, and Vision continuity documentation
- [x] Slice 3: record validation evidence and any skipped-check reasons
- [x] Slice 4: capture deferred follow-up items into durable backlog surfaces if needed
- [x] Slice 5: close the master-plan batch with plan-completion evidence

## Docs and Artifacts

- Expected docs:
  - `docs/business-logic.md`
  - `docs/domain-technical.md`
  - possibly `docs/product-vision.md`
  - possibly `docs/vision-architecture-patterns.md`
- Expected generated artifacts:
  - any changed contract or audit artifacts required by resolver output
- Temporary work products:
  - shared child-plan analysis from `.agents/tmp/frontend-user-shell-child-plan-analysis.md`

## Closeout Gates

- Required closeout checks:
  - docs reflect canonical route ownership and Vision continuity accurately
  - validation evidence matches the actual implemented scope
  - deferred work is explicitly recorded, not implied
- Final response evidence:
  - changed docs, checks run, and residual risks
- Backlog follow-up rule:
  - any deferred module promotion, Home backend summary, or Vision polish work must land in a persistent backlog if not implemented

## Validation

- required frontend checks
- any backend checks introduced by route or DTO changes
- plan closeout audit(s) required by actual implementation tier

## First Code Slice Rule

- This plan should consume the actual outputs of:
  - `make audit-manifest-decision files=<csv>`
  - `make audit-doc-sync-required-surfaces files=<csv>`
- Do not substitute assumptions for resolver output.

## Open Questions

- Which living docs become canonical for the new shell/navigation meaning after implementation?
- Does the final implementation require explicit contract registration or generated-artifact refresh for navigation DTO changes?
- Are there any route or shell decisions that should move from temporary plan artifacts into durable product-memory or product-vision lessons?

## Completion Evidence

- Status: complete
- Evidence: docs, manifest evidence, route audits, and frontend validation are now synchronized to the implemented shell and Vision handoff behavior.

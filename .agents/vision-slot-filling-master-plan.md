---
machine_kind: master-plan
machine_status: complete
machine_title: Vision Slot Filling Master Plan
machine_goal: Make OpenAI more self-sufficient when extracting fields from Vision
  input so the backend can rely less on ad hoc parsing and more on a standardized
  route and slot contract.
---

# Vision Slot Filling Master Plan

## Status

Completed.

## Goal

Make OpenAI more self-sufficient when extracting fields from Vision input so the backend can rely less on ad hoc parsing and more on a standardized route and slot contract.

## Parent God Plan

- God Plan: `Plan System God Plan`
- Machine-readable path: `.agents/god-plans/plan-system-god-plan.yaml`

## Scope

- Included:
  - route-level examples for intent and slot extraction
  - slot aliases and anti-examples
  - slot-filling manifest structure in the semantic contract
  - explicit active draft and active slot context for follow-up turns
  - stronger route catalog authoritativeness
  - confidence-aware merge behavior
  - slot-specific normalization as a last step
  - repair pass logic for weak or incomplete semantic outputs
  - learning from user corrections
  - standardized create/view/update/delete contract coverage
  - regression coverage for real voice utterances
- Excluded:
  - unrelated non-Vision product flows
  - visual redesign work outside the semantic contract and Vision surfaces

## Child Slices

1. Route metadata enrichment: add route examples, slot aliases, and slot anti-examples to the catalog and ship them in the semantic request contract.
2. Slot-filling manifest and active draft context: expose explicit manifest state, active slot, and current draft snapshot to the model for follow-up turns.
3. Confidence and normalization: improve merge behavior, add slot-specific normalizers, and keep backend-only cleanup as the last layer.
4. Repair and learning: add a deterministic repair pass for weak extractions and persist correction signals for future few-shot guidance.
5. Standardization and regression catalog: standardize CRUD contracts across entity families and add real utterance regression coverage.

## Execution Order

1. Enrich the route catalog and prompt contract first so the model gets stronger evidence immediately.
2. Add manifest and active-draft context next so follow-up turns are easier to resolve.
3. Tighten merge and normalization behavior after the contract is stable.
4. Add repair and learning once the core slot shape is reliable.
5. Finish with cross-entity standardization and regression coverage.

## Validation

- Targeted checks:
  - route catalog tests
  - prompt-understanding contract tests
  - semantic audit matrix tests
- Broader checks:
  - create/view/update/delete flow regressions for circles, applications, profiles, chat, and discovery
  - real voice utterance coverage
- Closeout checks:
  - the semantic contract is richer without becoming ambiguous
  - new fields are deterministic and validated

## Completion Evidence

- Status: completed
- Current slices: route metadata enrichment, active draft context, confidence and normalization, repair and learning, standardization and regression catalog
- Deferred work: none

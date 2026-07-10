---
machine_kind: plan
machine_status: complete
machine_title: Plan State Machine
machine_goal: Tighten plan statuses so complete is only reachable from a real evidence-backed path.
---

# Plan State Machine

## Status

Complete.

## Goal

Tighten plan statuses so `complete` is only reachable from a real evidence-backed path.

## Parent Master Plan

- Master plan: `.agents/implementation-system-closeout-hardening-master-plan.md`

## Scope

- Included: plan status vocabulary, status transitions, and completion gates.
- Included: retrofitting plan templates, audit helpers, and plan docs so `draft`, `active`, `blocked`, `deferred`, and `complete` mean the same thing everywhere.
- Excluded: driver orchestration and artifact cleanup.

## Implementation Notes

- Treat `blocked` and `deferred` as distinct user-facing states, but allow the machine layer to preserve a compact transition model.
- Update plan templates and closeout helpers together so the new vocabulary does not drift across new and old plans.
- Keep the transition rules simple enough that `audit-plan-completion` can enforce them without custom one-off exceptions.

## Validation

- Targeted checks: plan files can be parsed for the new state vocabulary.
- Broader checks: completion audits still reject premature `complete` states.
- Closeout checks: plan completion requires code, tests, docs, and changed-files evidence.
- The audit should flag any plan that reaches `complete` without a visible evidence trail.

## Completion Evidence

- Status: complete
- Changed files: `scripts/audits/audit-plan-completion.rb`, `docs/program-planning-model.md`, `docs/validation-memory.md`, `docs/validation-memory.json`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `.agents/templates/feature-implementation-plan.template.md`
- Validation evidence: `ruby -c scripts/audits/audit-plan-completion.rb`, `make audit-plan-completion plan=.agents/chat-prod-ready-plan.md manifest=.agents/feature-manifests/chat-prod-ready-manifest.yaml`
- Doc delta summary: plan statuses now consistently distinguish draft, active, blocked, deferred, and complete, and completion audits require real evidence-backed closeout data.
- Deferred work: none

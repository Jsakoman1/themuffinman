# SideJobs human-first analysis

## Product decision

The current Work module becomes **SideJobs** in customer-facing language. A SideJob is a concrete, bounded request for help or a small paid task; it is not a generic project-management item. The same person can find help and post a SideJob without choosing a permanent role.

Internal Java entities, database tables, endpoint paths, and existing lifecycle authority may retain `Quest` during the first delivery. Public copy, DTO labels, route titles, and browser semantics move to SideJobs first. This avoids a risky data migration while giving people one understandable vocabulary.

## Current evidence

- `WorkPage.vue` divides the module into Find work, My work, and Applications. This exposes implementation role buckets rather than a person's goal.
- `WorkDiscoveryView.vue` shows a terse status/location/amount/date row. It does not consistently state what help is needed, when it matters, the commitment, or the next safe action.
- `WorkQuestDetailView.vue` has a generic decision guide and a large action rail. Its most useful facts and its next decision should be the top-level content.
- `WorkQuestCreateView.vue` has a guided draft followed by a large review form. Its client-only `offerType` is not sent in `QuestRequestDTO`, so it promises a category that the persisted model does not represent.
- `WorkApplicationsView.vue` and `WorkQuestApplicationsView.vue` correctly use backend allowed actions, but both call the same concept an application even where a person is simply offering help.

## Human model

```
Need help ──> Post a SideJob ──> Review people ──> Agree / do it ──> Complete / review
                  ↑                                      │
Find a SideJob ──> Send a request to help ────────────────┘
```

The module begins with three person-facing scopes:

1. **Find SideJobs** — browse opportunities; opening one never commits the person.
2. **Post a SideJob** — ask for help in a short guided flow.
3. **My activity** — the person's requests, posted SideJobs, active commitments, and things requiring a decision.

The owner-only applicant queue remains inside the selected SideJob detail, where its meaning is clear.

## Information hierarchy

A discovery card and a detail header must answer, in order:

1. What needs doing?
2. When and where?
3. What is the reward or agreement state?
4. What is the scale: one person, a team, or a flexible request?
5. What can I safely do next?

Status terms are audience-facing: `Waiting for a reply`, `You are selected`, `In progress`, `Completed`, or an equivalent backend-prepared label. Raw lifecycle names are never the primary human label.

## Contract decisions

- SideJob discovery and detail stay backend-prepared. The browser may format local date/time and keep selection state, but may not infer relevance, allowed actions, payment state, or lifecycle consequences.
- A new summary/read presentation contract is required before surfacing duration, commitment, payment agreement, and attention grouping. UI must not derive these facts from title text or raw timestamps.
- Existing `Quest` writes and booking-like conflict protections remain authoritative. Public terminology does not imply a schema rename or a change to consent, visibility, or review rules.
- `offerType` is removed until a persisted backend `SideJobType` contract exists. A later type model is a separate product decision, not an unchecked select field.

## Non-goals

- No database/table rename in the first migration.
- No recommendation, skills, proximity, or pay filter unless a backend query and truthful explanation are introduced in the same slice.
- No client-side matching score.
- No change to application/assignment permissions, lifecycle transitions, review eligibility, or Circle visibility without a dedicated backend task.

## Delivery slices

1. Harden the plan and record vocabulary/transition contract.
2. Create a backend-prepared SideJob presentation summary and attention grouping.
3. Reframe navigation and discovery around person intent.
4. Rebuild detail/request and owner review hierarchy over the new contract.
5. Simplify posting and remove the non-persisted type control.
6. Document, validate, and capture desktop/mobile runtime evidence.

## Goal-pursuit readiness

The verifier-controlled inventory is hardened to one serial task at a time. New focused test paths are intentional deliverables, existing Quest persistence is baseline-only, and no product implementation task may start before the hardening item is verified.

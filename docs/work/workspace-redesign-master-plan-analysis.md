# Workspace redesign master-plan analysis

## Scope and source hierarchy

This plan treats `docs/work/workspace-redesign-component-atomization.yaml` as the
component decomposition and `docs/work/workspace-redesign-adoption-gap-audit.yaml`
as the current adoption baseline. Product meaning remains governed by
`docs/product-vision.md`; Vision boundaries remain governed by
`docs/vision-architecture-patterns.md`.

The eight reference screenshots under `docs/work/redesign-frontend/` were reviewed
as visual interaction references only. They show a dark, low-chrome desktop
application with a stable navigation rail, compact top context bar, dense rows or
cards, subtle borders, restrained status color, contextual right rails, and action
overflow. They must not be copied as Linear branding, product terminology, board
semantics, workflows, or layout pixels.

Public Linear documentation was used only to validate general interaction patterns:

- [Concepts](https://linear.app/docs/conceptual-model): actions can have button,
  keyboard, contextual-menu, and command-menu entry points; command results are
  context dependent.
- [Inbox](https://linear.app/docs/inbox): list selection can open a focused item
  view while preserving inbox actions and keyboard navigation.
- [Custom views](https://linear.app/docs/custom-views): filters define dynamic
  membership and right sidebars clarify view context.
- [Display options](https://linear.app/docs/display-options): presentation choices
  are distinct from filters/grouping/order.

These patterns are adapted only where TheMuffinMan already has backend authority.
No generic boards, drag/drop, saved shared views, bulk mutation, client-side
permission logic, or copied Linear domain model is in scope without a separate
backend contract.

## Audit findings

Preflight commands on 2026-07-18:

- `make audit-frontend`: 42 concrete route surfaces; endpoint linkage is partial;
  state/action audit reports duplicate circle block/unblock and action review sites;
  frontend permission audit still reports one local gate.
- `make audit-docs`: docs-as-tests and work-plan recursion pass.
- `make audit-impact`: current changeset is high risk and mixed-domain, so redesign
  must remain serial and evidence-driven.
- `make audit-plan-coverage`: current historical master graph is structurally valid,
  but is not evidence that the new scope is delivered.
- `make audit-target-capability-coverage`: 123 target capabilities are mapped, but
  28 are not ready and several Vision/web gaps remain. This redesign plan may not
  declare a capability complete solely because its route exists.
- `make audit-runtime-acceptance`: current catalog passes, but it does not prove
  adoption of this new plan's route/component contracts.

## Planning decision

Create a new draft master instead of reopening the prior verified redesign master.
It starts with a fresh baseline and a strict serial inventory. Every implementation
slice must be decomposed into exact paths before it can be started. Completion is
prohibited until the child plan records fresh desktop and narrow screenshots,
intentionally-started browser runtime trace/HAR where applicable, and the verifier
has marked every inventory item verified.

## Second-order execution atomization

The initial 28-item queue was deliberately split again in
`workspace-redesign-execution-atomization-audit.yaml`. The resulting queue keeps
backend contracts, reusable components, individual route adoption, and proof
separate. This prevents large areas such as Business, account/attention, Work
details, and shell personalization from being marked complete after only a
partial visual migration.

## Visual-fidelity decision

The product vision now makes the authenticated Web application a dark graphite,
dense, low-chrome workspace and retains `/vision` as the distinct adaptive
voice/context assistant. `workspace-redesign-visual-reference-spec.yaml` converts
the screenshot references into original palette, geometry, hierarchy, interaction,
and evidence constraints. The `VFY-*` workstream is a dependency of foundation and
route adoption, so visual fidelity cannot be deferred as a final CSS pass. It does
not add Linear's issue/board/product semantics.

## Architecture decisions

- Web workspace is a direct, dense, keyboard-capable client. Backend services remain
  authoritative for filters, permissions, actions, read models, workflow state,
  personal shortcuts, attention, and realtime visibility.
- Three columns are an optional workspace archetype, not a mandatory template:
  collection context + list + preview for high-volume object collections; main +
  activity + utility for substantial details; short confirmations and settings stay
  intentionally simpler.
- Shared primitives are delivered before route migration. Local styles cannot be
  replaced with a global CSS override and called migrated.
- Vision remains a first-class adaptive voice/context-aware client. It receives typed
  handoff and return context, but never inherits a persistent workspace rail,
  command-center semantics, frontend-derived personal memory, or mutation authority.
- Any missing backend projection or action must be added through a domain-owned
  read/action service and DTO contract, with tests, before its UI consumer is built.

## Explicit non-completion rules

- A component existing without adoption on named routes is incomplete.
- A route restyled without the required interaction behavior is incomplete.
- A green type-check/build/static audit is not screenshot or runtime evidence.
- Existing screenshots, HARs, or a user-owned development server are not fresh
  evidence.
- A child may not be bulk-verified. The inventory allows exactly one active atomic
  task.

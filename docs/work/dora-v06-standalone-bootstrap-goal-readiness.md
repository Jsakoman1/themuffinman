# Dora v0.6 goal-pursuit readiness review

Reviewed on 2026-08-05 against baseline `3c10315`.

## Plan graph result

The Dora v0.6 master has three delivery children, one atomic-hardening child, and a
single serial inventory of thirteen unique contiguous items. Each item maps exactly
once to a child task. Each task declares one observable outcome, bounded required
paths, a direct predecessor (except the first task), a leaf validation command, and
an evidence boundary.

All plan YAML parses. The Dora work-plan validator accepts the three delivery
children, while the repository atomic-task hardening and serial-inventory audits
accept the entire draft graph. These checks establish structural readiness only; they
are not evidence that the v0.6 implementation exists.

## Scope and ownership result

Dora owns portable bootstrap mechanics, technical starter packs, declared plugin
execution, report shapes, and beginner onboarding. MuffinMan owns its adapter,
project commands, plugin manifest values, product-only audits, runtime acceptance,
and business/domain documentation. The plan therefore improves the seam without
moving product authority into Dora.

The only current workspace changes are this v0.6 planning set and the matching
backlog item. No existing application implementation, Dora release tag, consumer
pin, or external repository state has been changed during preparation.

The generated System Map impact report is advisory and maps this planning-only diff
to `docs/system-truth-registry.yaml`, `docs/system-drift-control-registry.yaml`, and
`docs/system-map.md`. They are reviewed as unchanged ownership context; any later
implementation change that affects one of them must refresh the report and record a
specific closeout disposition.

## Start gate

The plan is ready to be set as a goal, with exactly one allowed first action:
`harden-v06-bootstrap-plan`. That task must create the plan-specific semantic audit,
be started through `make work-start`, and receive verifier evidence before any Dora
or MuffinMan implementation task begins.

Tasks 1–11 require no external side effects. Task 12 publishes Dora v0.6 and task
13 pins MuffinMan to that release; both are explicitly approval-gated and must stop
for user authorization when reached.

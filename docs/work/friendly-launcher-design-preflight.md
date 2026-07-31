# Friendly launcher goal-activation preflight

Date: 2026-07-29
Baseline: `a18b811e65001f68d174b42d1574f6c0f2ffc18d`

## Scope classification

- Reused baseline: verified Business owner/public-booking flows and the prior
  human-first authenticated-screen audit. They are not delivery targets here.
- New presentation scope: launcher palette, shared whole-card action primitive,
  purpose-led Home, fixed mobile tabs, desktop launcher composition, and light/dark
  runtime evidence.
- Explicit boundary: the reference supplies visual intent only. It does not authorise
  fabricated impact figures, replacement product domains, client-side permission
  rules, or a literal product copy.

## Architecture decision

Current `loadHomeData` client-composes a dashboard from ten endpoint families and
per-quest application reads. That conflicts with the backend-prepared read-model
direction and makes the proposed Home first view expensive and hard to keep
consistent. The plan therefore introduces one dedicated Activity-domain read model
at `GET /workspace/home`, assembled by `WorkspaceHomeReadService`. It provides only
the greeting, permitted canonical launcher destinations, and optional factual
summary. Attention and Calendar retain their existing dedicated reads until a later
contract change explicitly includes them.

The first release has four immutable product-intent tile roles: Work, Things,
Business discovery, and Rides. “Share & lend” is a presentation label for Things;
“Book services” is a presentation label for Business discovery. No new domain is
created by those names.

## Atomic hardening review

The inventory has 12 serial items. Backend read-model creation and frontend contract
consumption are separate tasks. Desktop visual audit and program closeout are also
separate tasks. All implementation tasks name bounded paths, explicit predecessors,
leaf validation, and a distinct evidence boundary. Visual tasks declare stable
screenshot and runtime-evidence paths.

## Activation conditions

1. The plan-control audits below must pass against the recorded baseline.
2. Goal activation starts only the inventory hardening task.
3. No product task may start until that hardening task is verifier-verified.
4. Each later task is started and verified singly using `make work-start` and
   `make work-verify`; prior screenshot evidence is baseline-only.

## Preflight results

- Control-source validation: passed.
- Atomic-task hardening audit: passed after the 12-item split.
- Plan coverage audit: passed.
- Work-plan recursion audit: passed.
- System Map impact report: reviewed at
  `docs/audit-output/system-map-change-impact-summary.md`. It reports only the new
  planning files, so the registry recommendations are not applicable until a code
  task starts; the Activity, Web contract, and runtime-evidence owners are already
  explicit in the corresponding future atomic tasks.

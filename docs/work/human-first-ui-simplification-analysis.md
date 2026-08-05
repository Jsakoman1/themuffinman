# Human-first UI simplification preflight

Prepared: 2026-08-04  
Baseline: `a35a18dec5f58a2c2409e72dfb5dd88ee921df57`

## Decision

The program can start as a strict serial UI-delivery plan after the atomic-task
hardening item is verifier-verified. It has seven queue items, each with one
observable outcome. It must not be bulk-verified.

## Product and implementation review

- The existing design direction already requires calm, intent-first navigation,
  one useful Home action, progressive disclosure, and a quiet Vision entry.
  This program repairs gaps in those existing contracts; it does not introduce a
  new product domain or relax backend authority.
- Navigation labels are backend-prepared through `WorkspaceNavigationService` and
  mirrored by shell definitions. The navigation task therefore changes both
  owners and its contract test before any client presentation work proceeds.
- Home already uses a viewer-scoped launcher and permitted activity feed. Its
  task is presentation-only: it must use the existing permitted title, summary,
  and route rather than derive a new workflow in the browser.
- Vision remains a complementary assistant. The composer task may change dock
  placement and contextual placeholder copy only; semantic interpretation,
  permissions, and mutations remain out of scope.
- Calendar event ownership and business timezones remain unchanged. The Calendar
  task is restricted to reader-facing local-time explanation and control
  hierarchy.

## Validation and evidence boundary

- The advisory system-map report recommends reviewing system-map, truth/drift,
  and runtime registries because this workspace already contains unrelated
  runtime-harness edits. Those unrelated edits are baseline-only and must not be
  included in this program's evidence.
- The selected UI files route to backend test, frontend type-check, and frontend
  build. Each task additionally has a tighter leaf command in its owning plan.
- Existing screenshots are historical baseline only. The closeout requires a
  fresh owned-stack Playwright trace plus new desktop and mobile screenshots.

## Queue and overlap review

1. Hardening changes only the plan graph and verifies queue integrity.
2. Navigation labels precede SideJobs labels because both touch shell wording.
3. Home precedes Vision docking so the Home screenshot is captured after its own
   presentation work, while shared-shell changes are separately evidenced.
4. Calendar follows shared-shell work to avoid a stale mobile bottom-clearance
   baseline.
5. Closeout is the only task that promotes browser evidence.

No endpoint, schema, permission, or persisted-state change is planned. If one is
discovered, stop the affected task, update this plan and the execution inventory,
and re-run atomic-task hardening before continuing.

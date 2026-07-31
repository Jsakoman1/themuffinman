# Human-first Web experience preflight

The baseline revision is `a18b811e65001f68d174b42d1574f6c0f2ffc18d`.
The repository has existing user changes; execution must preserve unrelated work
and use verifier start snapshots before editing each atomic task.

The prior human UI, Business parity, main-surface, and local-tab masters are
verified baseline. Their evidence must not be counted again. This program owns
only the residual defects listed in the master scope control.

The execution inventory is the sole queue and contains thirty-eight one-to-one atomic
items. No implementation begins until `harden-human-first-web-inventory` is
started and verifier-verified. Every later task must be started individually and
verified before the next item advances.

Shared files intentionally appear in multiple serial tasks where they represent a
controlled handoff. AuthenticatedShellView first changes navigation and later
receives the final theme/header treatment. userShellApi receives bounded endpoint
support before its final behavior-preserving decomposition. ChatSurfaceView and
CalendarPage are changed in explicitly separated timezone, grid, mobile-mode, and
event-popover slices.

The hardening review rejected three unsafe shortcuts:

- creating a new ActionDescriptor beside the existing `ClientActionDTO`
- serving production assets directly from `docs/work/assets`
- combining all Chat feedback or all Calendar behavior into one verification
  boundary

Planned new files are allowed only when their parent module already owns the
responsibility. Compatibility facades are mandatory during API decomposition;
there is no big-bang import rewrite.

The advisory System Map preparation report reviewed 76 currently existing
planned paths. It identifies endpoint/capability, entity-retention, permission,
release, truth, drift, runtime-acceptance, regression, business, technical, and
System Map surfaces for final disposition. This is a review map, not completion
evidence; closeout must regenerate it from the actual final diff.

Backend behavior changes require targeted JUnit tests and updates to
`docs/business-logic.md` or `docs/domain-technical.md` where product or domain
meaning changes. Generated frontend contracts must be refreshed after DTO changes.

Runtime closeout must use the owned `make dev` stack and stop it with
`make dev-stop`. Existing screenshots are comparison context only. New desktop
and mobile screenshots and JSON traces must be generated from the final source
state, resolve test objects dynamically, include browser-error and overflow
results, and include a contrast check for changed representative routes.

Before goal pursuing, rerun:

1. `ruby scripts/audits/audit-atomic-task-hardening.rb docs/work/human-first-web-experience-master.yaml docs/work/human-first-web-experience-execution-inventory.yaml`
2. `ruby scripts/audits/audit-work-plan-recursion.rb`
3. YAML parsing for every new plan.
4. Confirm every inventory dependency names the immediately preceding item.
5. Confirm every current implementation path exists and every planned path has
   an existing owning parent directory.
6. Regenerate the System Map impact report from the final implementation diff
   before closeout; the preparation report is advisory only.

After hardening verifies, the exact goal-pursuing entrypoint is:

`make work-start plan=docs/work/human-first-web-navigation-notifications.yaml task=simplify-shell-navigation`

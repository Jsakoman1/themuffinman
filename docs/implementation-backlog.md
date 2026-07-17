# Implementation Backlog

This file is the persistent source of truth for open implementation and product-delivery work that should survive across Codex sessions.

Control-system improvements belong in the current work plan and are not maintained in a second control backlog.

Keep only currently open items here. When an item is implemented, remove it from the open backlog in the same change and clear matching inline `TODO(<ID>):` or `FIXME(<ID>):` references.

The target capability slice queue is generated with `make generate-target-capability-slices`.
Generated queue rows are not implementation evidence; promote a selected critical
or high row into a dedicated `docs/work/*.yaml` plan before implementation.

## Open Items

WEB-UX-MODERNIZATION-MASTER [DONE]: Executed and verified `docs/work/frontend-ux-modernization-master.yaml` and its six child plans. Navigation IA, card/action primitives, shared form patterns, responsive Chat/Calendar, accessibility tokens/contracts, and runtime closeout are complete.

MAIN-SURFACES-MODERNIZATION [DONE]: Verified `docs/work/main-surfaces-modernization-master.yaml`; Home, Business landing, Find Work, shared clickability/action hierarchy, surface archetypes, and authenticated runtime evidence are complete.

WEB-VISION-PARITY [ACTIVE]: Make web UI and Vision equal production clients. Audit every target capability for discoverable entry, complete happy path, validation/recovery, and runtime acceptance on both web UI and Vision; prioritize missing Work creation entry, Chat entry/group creation, and other flows that are route-backed but not practically reachable.

WEB-UI-FUNCTIONAL-COMPLETION [ACTIVE]: Execute `docs/work/web-ui-functional-completion-master.yaml` to make the authenticated Web UI usable end to end: diagnose Chat and Circles runtime failures, expose Create new work and Find Work, add Find People and Find Business contracts/surfaces, replace the Calendar list with a visual calendar workspace, and require browser-runtime evidence for closeout.

WEB-BUSINESS-BOOKING-HANDOFF [ACTIVE]: Complete authenticated browser evidence for Find Business -> public business detail -> offering availability -> booking handoff, using a seeded public business fixture and backend-owned booking contracts. Tracked in `docs/work/web-ui-functional-completion-runtime-evidence.yaml`.

WEB-AUTH-RECOVERY-TRACE [ACTIVE]: Add authenticated browser evidence for permission-denied, Chat reconnect/resync, and cross-surface recovery states; keep these distinct from compile/static validation. Tracked in `docs/work/web-ui-functional-completion-runtime-evidence.yaml` and `docs/runtime-acceptance-matrix.yaml`.

CAPABILITY-COVERAGE-RELIABILITY [DEFERRED]: Close the remaining partial and not-offered capabilities recorded in `docs/capability-inventory.yaml`. Resume `docs/work/capability-coverage-and-reliability.yaml` when browser/device runtime tooling or native client implementation workspace is available.

ACCOUNT-RECOVERY-CONTRACT [DEFERRED]: Define the secure account recovery token, delivery, verification, and reset contract before implementing `platform.account.recover`.

SEARCH-COMPARISON-CONTRACT [DEFERRED]: Define bounded comparable fields, privacy rules, selection limits, and shared Web/Vision DTOs before implementing `vision.discovery.compare` or `cross_module.search.compare`.

RIDE-MODULE-CONTRACT [COMPLETED]: Complete voluntary Rides module is implemented and verified through `docs/work/rides-production-completion-master.yaml`, including domain lifecycle, circle consent/privacy, backend API, Web, Vision, runtime evidence, notifications, and closeout verification.

RIDE-VISION-PARITY [COMPLETED]: Canonical Vision ride read/mutation intents, review/confirmation, read results, and recovery are implemented; tracked by `docs/work/rides-production-completion.yaml`.

RIDE-NOTIFICATION-FANOUT [COMPLETED]: Ride join/leave/update/cancel/start/complete events fan out to the shared in-app notification/news surface without embedding ride visibility details; tracked by `docs/work/rides-production-completion.yaml`.

VISION-GAP-CLOSURE [ACTIVE]: Close the 19 remaining Vision surface gaps using the prioritized queue, the contract bundle in `docs/work/vision-deferred-contract-bundle.yaml`, and readiness gates in `docs/work/vision-gap-readiness.yaml`. All seven gates are contract-ready; promote each into an implementation plan before claiming coverage.

WORK-LIFECYCLE-CONTRACT [ACTIVE]: The product/backend contract is resolved in `docs/work/vision-deferred-contract-bundle.yaml`; worker-management backend commands are implemented in `docs/work/worker-lifecycle-backend-batch.yaml`, while Web/Vision exposure and runtime evidence remain open.

## Intake Rules

- Record new deferred implementation or control-system work in the appropriate persistent backlog with a stable ID before closing the change that discovered it.
- Record new persistent implementation follow-ups here with a stable uppercase ID before closing the change that discovered them.
- If a deferred follow-up also needs an inline code note, use `TODO(<ID>):` or `FIXME(<ID>):` and make sure the same ID exists in an open backlog entry.
- When a backlog item is implemented, remove it from the open backlog and clear matching inline `TODO(<ID>):` or `FIXME(<ID>):` references in the same change.
- Do not keep resolved items here as historical checkboxes. Remove them once the implementing change is complete.

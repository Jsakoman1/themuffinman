# Dora v0.5 reusable audits and plugins preflight

Baseline: `8349ce4` on `main`, with Dora v0.4.0 pinned at
`17482278ea939aebca08e71f6a93cd807185c35d`.

## Reviewed layers

- Product: MuffinMan capability, permission, runtime, and documentation audits stay
  product-owned and are excluded.
- Control system: Dora owns reusable plan mechanics and accepts only declared
  project paths, commands, controls, and plugin source roots.
- Implementation workflow: this master is strict and serial. The execution inventory
  is the only queue, and atomic-task hardening is the first required task.

## Preconditions before execution

1. Run the v0.5 plan-hardening audit and verify the hardening task.
2. Confirm each legacy audit has an isolated fixture before changing its wrapper.
3. Keep current Dora v0.4 tests as baseline; run only new contract and fixture tests
   for the changed area, then the complete Dora suite at release closeout.
4. Do not start publishing or pinning without a new explicit external approval.

## Goal-pursuit readiness

- Every structured plan YAML currently parses successfully.
- The v0.5 inventory passes the repository atomic-task and serial-inventory audits
  with sixteen one-to-one mappings.
- The historical Dora separation inventory had all items verified but a stale
  `active` status; it is corrected to `verified`, so it cannot appear as a parallel
  serial workstream.
- Other draft masters are unrelated product or UX work and do not share Dora v0.5
  required paths. Their existing evidence is baseline-only for this program.
- The first v0.5 task remains intentionally pending: it creates and runs the
  plan-specific hardening audit before any Dora implementation task starts.

## Non-goals

- No MuffinMan domain, permission, capability, runtime-acceptance, or business-doc
  audit becomes Dora code.
- No generic plugin infers a source root, build command, or product completion.
- No current MuffinMan audit is removed before its replacement has portable parity
  evidence and the remaining wrapper responsibility is documented.

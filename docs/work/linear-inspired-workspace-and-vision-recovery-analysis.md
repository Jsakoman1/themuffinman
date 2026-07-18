# Redesign recovery analysis

## Finding

The previous frontend redesign master was incorrectly treated as complete. Its verifier
required only one changed path from a task and a passing leaf command, while several
tasks described multiple route adoptions and browser-only behavior. Manual browser
commands were recorded in plan metadata but never executed by the verifier.

## Why the normal master-plan workflow failed here

Goal pursuit was only the high-level continuation mechanism; it did not prove that
each promised interaction had been delivered. The implementation workflow also let a
child plan verify every one of its tasks in one bulk invocation. Combined with broad
tasks, one-of-many changed-path checking, and static-only leaf validations, this let
small edits satisfy a plan whose stated scope required multiple route adoptions and
browser behavior. The product capability inventory did not contain an active,
redesign-specific atomic queue, so its master-coverage audit could not report the
missing promised redesign slices.

The failure was therefore in decomposition and enforcement, not in a missing CSS
technique or a lack of an existing component: a detached preview primitive and partial
shell styling were permitted to stand in for full user workflows.

## Recovery decision

The former frontend and behavior masters are superseded by one strict master. It binds
workspace UI, backend contracts, Vision handoff, screenshot evidence, browser traces,
and closeout into one dependency graph. No task may close solely because a component,
CSS selector, or static contract exists.

## New proof standard

- Exact required code/docs paths all change.
- Named real routes adopt each component; detached primitives fail the slice.
- Visual tasks attach current desktop and narrow screenshots.
- Runtime tasks attach current trace metadata and HAR from an intentionally started stack.
- Existing user processes are never claimed as new agent evidence and are never stopped.
- The master verifies only strict children with strict evidence records.
- The execution inventory orders 30 atomic items; only one can be started or closed
  at a time, and each verification requires the named paths to change after its
  verifier-recorded start snapshot.

## Product split

Workspace routes follow a dense, keyboard-first product application model. Vision remains
a backend-orchestrated, voice-ready adaptive conversation; it shares route handoffs,
evidence, and recovery language but does not inherit a permanent workspace rail or
command palette. Personal context, intent assessment, and decision execution stay
backend-owned, consent-bound, and explicitly confirmed.

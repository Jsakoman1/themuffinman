# Business-parity human usability preflight

## Purpose

This is the execution-ready follow-up to the verified human-first all-surfaces
baseline. Its only purpose is to close the remaining gap between ordinary
authenticated screens and the calm, task-led usability standard already set by
Business owner and public booking flows.

## Locked scope

The program may simplify the first view, defer advanced controls, make the
next action explicit, and make a completed action understandable on these
surfaces: Profile, location/privacy settings, Circles, Work, Rides, Home, and
Chat.

Business owner and public booking flows are a reference baseline, not a second
implementation scope. This program does not change business rules, backend
permissions, routes, API contracts, database schema, or the shared app shell
unless an approved task explicitly adds the affected path and documents why.

## User-standard for every implementation task

Each changed first view must answer, without requiring product knowledge:

1. What am I looking at?
2. What needs my attention, if anything?
3. What is the one sensible next action?
4. What changed after I acted?

Advanced setup, optional preferences, and technical wording belong behind an
intentional secondary action. A screen must not force a user to choose a role,
configuration, or data model before they need it.

## Evidence contract

The existing authenticated route-family baseline is
`docs/runtime-evidence/human-first-all-surfaces-browser-2026-07-29.json`.
It is useful as context only; it cannot be reused as completion evidence for
this program.

Every changed route requires fresh evidence from a controlled local `make dev`
stack after its implementation validation. Runtime evidence must identify the
route, viewer state, visible primary action, and observed action outcome.
Desktop closeout covers Profile, Circles, Work, Rides, Home, and Chat. Mobile
closeout covers the same route families at a narrow viewport, including empty,
recovery, or first-use states where they are relevant. Store the resulting
trace and screenshots under `docs/runtime-evidence/` and record their exact
paths in the relevant plan evidence before verification.

## Execution gates

1. Start only `harden-business-parity-inventory` first with `make work-start`.
2. During that task, re-run atomic hardening against this master and inventory,
   confirm no active inventory conflict, and record any necessary bounded plan
   correction before the verifier closes it.
3. Run exactly one inventory item at a time, in its declared sequence.
4. For each UI implementation, run `npm run type-check` and `npm run build`
   in `apps/themuffinman/frontend`, then capture its fresh runtime evidence.
5. Do not run the desktop, mobile, or closeout audits before all applicable
   implementation evidence exists.

## Ready-state assessment

The plans name existing source files, use serial dependencies, isolate one
human outcome per task, and have a dedicated hardening gate. This preflight is
prepared for goal activation, but it is intentionally not marked verified: the
first goal task must verifier-verify the inventory against the exact workspace
state at the moment execution begins.

## Goal-activation hardening record

At goal activation on 2026-07-29, control-source validation, the targeted
atomic-task hardening audit, plan-coverage audit, and work-plan recursion audit
all passed. The inventory has no competing open plan and remains serial with
thirteen pending items. The first implementation task may start only after the
task verifier records this hardening task as verified.

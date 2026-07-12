# Program Planning Model

This document defines the durable planning model for broad implementation work in this repository.

## Core Terms

- Master plan: the coordination document for one broad initiative or program.
- Plan: one executable slice under a master plan.
- Checkbox: one concrete implementation step that can be completed and checked off.
- Analysis record: a temporary working note that captures the reasoning, local audit findings, and design choices before implementation begins.
- Temporary work product: a machine-readable artifact under `.agents/tmp/` that belongs to one plan or master plan while work is active.

## How The Model Works

1. Start with analysis.
2. Draft the master plan with the problem statement, shared context, scope boundaries, plan inventory, ordering, and final review gate.
3. Use local audits, documentation, and current source-of-truth surfaces to derive the concrete plans.
4. Expand each plan into checkbox-based implementation steps with the specific tools, guardrails, and evidence that matter for that slice.
5. Review the full master plan and all plan files together before implementation starts.
6. Execute the plans in order without asking for confirmation between safe slices.
7. Close each implementation plan only when all checkboxes are complete, the required evidence is recorded, and `make audit-plan-completion plan=<plan-file> [manifest=<manifest-file>]` passes.
8. Close the master plan only when every explicitly listed child plan passes the same audit and the final consistency review is satisfied.

## Master Plan Responsibilities

- Hold the shared context that applies to every plan in the program.
- List all plan files explicitly.
- Describe ordering, dependencies, and any cross-plan constraints.
- Record the final consistency review before implementation begins.
- Stay open until all plan files are complete and have evidence-backed completion audits.

## Plan Responsibilities

- Cover one bounded implementation or analysis slice.
- Keep its own local context, tools, validation, and evidence.
- Use checkboxes for the actual work.
- Mark each checkbox complete as soon as the step is implemented and verified.
- Stay open until all checkboxes are done, the plan evidence is no longer placeholder text, and its baseline-backed implementation evidence passes the completion audit.

## Planning Rules

- Broad work should not start from a flat mega-plan.
- Broad work should start with a master plan and then split into narrower plans.
- Each plan should be detailed enough that another session can continue without guessing.
- A version 2 implementation plan records the Git baseline that predates the implementation and concrete runtime or tooling code paths changed after that baseline.
- A master plan must name every child plan as an explicit `.agents/<child>-plan.md` path; a checked-off prose label is not child completion evidence.
- `make implementation-batch` prepares routing and discovery only. It may run closeout only when `closeout=true` is explicitly supplied with both plan and manifest paths.
- The final review should look for missing checks, overlap, conflicting assumptions, and simpler implementation options.
- Archive-only planning history is traceability, not live operational state.

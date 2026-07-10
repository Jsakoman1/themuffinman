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
7. Close each plan only when all checkboxes are complete and the required evidence is recorded.
8. Close the master plan only when every plan is complete and the final consistency review is satisfied.

## Master Plan Responsibilities

- Hold the shared context that applies to every plan in the program.
- List all plan files explicitly.
- Describe ordering, dependencies, and any cross-plan constraints.
- Record the final consistency review before implementation begins.
- Stay open until all plan files are complete.

## Plan Responsibilities

- Cover one bounded implementation or analysis slice.
- Keep its own local context, tools, validation, and evidence.
- Use checkboxes for the actual work.
- Mark each checkbox complete as soon as the step is implemented and verified.
- Stay open until all checkboxes are done and the plan evidence is no longer placeholder text.

## Planning Rules

- Broad work should not start from a flat mega-plan.
- Broad work should start with a master plan and then split into narrower plans.
- Each plan should be detailed enough that another session can continue without guessing.
- The final review should look for missing checks, overlap, conflicting assumptions, and simpler implementation options.
- Archive-only planning history is traceability, not live operational state.

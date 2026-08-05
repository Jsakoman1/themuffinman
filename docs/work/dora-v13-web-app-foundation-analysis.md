# Dora v1.3 web-app foundation plan analysis

Status: planning analysis. This document prepares a later goal; it does not authorize implementation.

## Selected scope

The plan takes the narrow web-app-foundation option. It makes Dora ready to start a controlled new Spring Boot, Vue, and PostgreSQL application, but does not turn Dora into an autonomous application generator or a deployment platform.

## Requirement-to-workstream mapping

| Requirement | Workstream | Proof |
| --- | --- | --- |
| Beginner-friendly interview route | Interview and guidance | Contract and documentation fixtures preserve user provenance and open decisions. |
| Neutral Java/Vue/PostgreSQL foundation | Postgres starter | Fresh independent consumer validates declared setup, test, build, health, and Compose configuration. |
| First vertical capability from confirmed intent | Vertical slice | Proposal contract, missing-decision checks, and two-consumer proof. |
| No MuffinMan or DoomsDayStorage coupling | Proof and documentation | Consumer fixtures use neutral product names and assert no product leakage. |
| Clear limits for Codex | Every workstream | Commands and documents state that generation is proposed context, not implementation or production proof. |

## Principal risks and controls

- Docker or PostgreSQL availability differs by developer machine. The starter will declare prerequisites and configuration validation separately from product runtime acceptance.
- Code generation can overwrite product work. The vertical-slice output is a proposal artifact only; applying it remains a project-owned atomic task.
- A generic starter can accidentally gain business assumptions. Every starter test must assert the absence of product entities, roles, API resources, and seed data.
- The current documentation emphasizes an older `new --answers` route. A dedicated documentation task makes the v1.2 interview route primary before adding v1.3 additions.
- Data safety is important but product-specific. v1.3 records missing data-safety decisions; it does not select retention, backups, auth, or deployment on behalf of a product.

## Execution decision

The work is safe to prepare for strict serial execution. It should not start until the atomic hardening task has passed the normal work verifier. Release work remains outside this plan.

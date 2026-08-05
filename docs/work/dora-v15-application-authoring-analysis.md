# Dora v1.5 application-authoring analysis

## Objective

Move Dora from validated planning and technical foundations to a controlled route
from a user-confirmed idea to a reviewable first application feature. Dora remains
neutral: it may transform explicit declarations into technical scaffolding, but it
must not infer domain rules, access rules, workflows, data retention, or production
choices.

## Baseline

Dora v1.4.0 already provides a guided interview session, app readiness, a neutral
Spring/Vue/PostgreSQL/Flyway starter, read-only vertical-slice proposals, cited
decision records, a neutral Playwright profile, and verified independent-consumer
evidence. It does not yet join the interview session to create-app, generate the
first feature's source skeleton, or provide generic application safety profiles.

## v1.5 workstreams

1. Convert a complete, user-confirmed interview session into reviewed product brief,
   domain library, agent profile, and create-app input without hand-authored YAML.
2. Generate a reviewable first vertical feature skeleton only from a confirmed
   capability context and declared domain model.
3. Provide an opt-in neutral authentication, permission, ownership, and audit-trail
   starter without choosing product roles or policies.
4. Provide data-safety and demo-data separation profiles with declared backup,
   restore, export, retention, and audit boundaries.
5. Add static migration/API/workflow safety checks before implementation evidence is
   accepted.
6. Extend decision records into architecture decision records and ask the offline/
   sync question before a technical design is treated as ready.
7. Provide neutral UI and accessibility blueprints so generated frontend skeletons
   have consistent, reviewable interaction shapes without product copy or design.
8. Provide an opt-in operational readiness profile for environment variables,
   secrets references, logging, health, rate limits, dependency posture, and
   deployment documentation without automatic deployment.
9. Compile declared capability acceptance scenarios into static, test, and runtime
   evidence obligations so a generated skeleton cannot look complete without proof.

## Explicit exclusions

- No DoomsDayStorage-specific source, fields, workflows, UI, data, or deployment.
- No automatic publishing, deployment, secret creation, database access, or backup.
- No inferred user role, permission, data classification, retention duration, or
  offline requirement.
- No claim that generated skeletons are complete product implementation.

## Goal gate

Before implementation, split every workstream into serial atomic tasks with exact
paths, direct dependencies, leaf validations, evidence boundaries, and independent
consumer proof where reusable files are created. Browser, database, package download,
or deployment execution stays behind an explicit approval boundary.

## Added scope rationale

UI blueprints, operational readiness, and acceptance evidence are separate from the
feature generator because they have distinct owners and proof boundaries. A feature
generator creates reviewable code structure; it does not decide interaction design,
production operations, or claim that an acceptance scenario has passed.

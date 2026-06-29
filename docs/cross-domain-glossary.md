# Cross-Domain Glossary

Use this glossary when adding or changing shared product concepts across modules.

The goal is to keep terminology stable before reading the larger business and technical docs.

## Core Actors

- `user`: a real platform account represented by `AppUser`; users can own resources, apply for work, belong to social relationships, and chat when eligible.
- `actor`: the authenticated user context performing an action; backend services must derive authority from the actor, not from frontend state.
- `admin`: a user with elevated platform authority; admin flows may inspect or manage broader data but still use explicit backend service rules.
- `owner`: the user who owns a resource such as a quest, business profile, thing listing, ride offer, circle group, or profile location rule.

## Social Graph

- `circle relation`: the relationship state between two users, such as pending, accepted, cancelled, or blocked.
- `circle group`: an owner-defined group used to organize accepted contacts and scope visibility.
- `circle membership`: the assignment of an accepted contact into one of the owner's circle groups.
- `contact`: a user connected to the actor through a current accepted circle relation.
- `blocked user`: a user blocked by the actor; blocked relationships must not be treated as contacts.

## Visibility And Consent

- `visibility`: backend-enforced rules that decide who can see a resource or location detail.
- `circle-scoped visibility`: access limited through selected owner circles or accepted circle relationships.
- `exact-location visibility`: the rule controlling who may see exact address or coordinate detail.
- `consent`: a real user's explicit or workflow-derived permission to participate; automation must not invent consent for another actor.
- `synthetic data`: admin or developer-created data for sandbox/testing; it must stay separate from production-like user action semantics.

## Workmarket

- `quest`: a task posted by one user for others to apply to, work on, and complete.
- `quest owner`: the creator or authorized admin context managing a quest.
- `application`: a worker-side request to perform a quest; applications belong to one applicant and one quest.
- `applicant`: the user who applies to a quest.
- `approved worker`: an applicant whose application was approved and who may participate in quest execution.
- `review`: post-completion feedback from one quest participant about another participant.
- `quest news`: backend-created notification/read-model entries about quest or application events.

## Scheduling And Bookings

- `scheduling window`: reusable start/end time shape with shared validation for past starts, missing starts, and invalid ranges.
- `booking`: a reserved time or appointment slot; current booking references are planned or future-facing unless a module explicitly implements them.
- `appointment`: a business-facing booking concept that should reuse scheduling-window and actor authority rules when implemented.

## Sharing Modules

- `business profile`: owner-managed business mini-site/profile data with active-directory and slug visibility rules.
- `thing listing`: an owner-created lending item that other users can request to borrow.
- `borrow request`: a request from another user for a thing listing; current workflow is intentionally narrower than future handoff/return flows.
- `ride offer`: a driver-owned voluntary ride entry with optional circle-scoped visibility.

## Messaging

- `conversation`: a normalized one-to-one chat thread between two eligible users.
- `message`: text or image content sent inside an eligible conversation.
- `presence`: short-lived online/activity state used by chat read models.
- `chat eligibility`: current accepted circle relationship requirement for opening or continuing direct chat access.

## Agent And Documentation Concepts

- `read-before-write`: automation must resolve exact entities through documented read workflows before mutation.
- `clarification`: the required stop condition when natural-language targets are ambiguous.
- `destructive confirmation`: explicit confirmation required after exact target resolution for destructive actions.
- `feature manifest`: optional machine-readable closeout record for plan-driven feature work.
- `validation evidence`: optional machine-readable record for commands run, generated artifacts checked or refreshed, and skipped checks.

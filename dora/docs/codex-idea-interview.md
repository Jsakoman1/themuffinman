# Codex idea interview

Use this sequence before `dora create-app --interview <idea-interview.yaml> --source <bootstrap-source.yaml>`.

Ask the user, in ordinary language, for the intended users, first problem, first capability, domain concepts, permission intent, workflow intent, and forbidden outcomes. Ask whether any important decision is still open. Record each answer only after the user confirms it.

Create one `dora_idea_interview` artifact using these required answer ids:

```text
target_users
first_problem
first_capability
domain_concepts
permission_intent
workflow_intent
forbidden_outcomes
```

Each answer must retain `source: user` or `source: user_confirmed`. An unanswered decision needs its own id, question, and the same source provenance. Do not convert an assumption, suggestion, or inferred product rule into an answer.

The interview is declared starting context only. It does not approve implementation, choose data retention or authentication, create application code, validate runtime behavior, or grant release authority.

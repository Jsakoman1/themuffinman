# Change Impact Preflight

- Decision: `review`
- Why: guardrail warnings=3 files=220
- Next action: `./mvnw`, `build`, `generate-agent-artifacts`
- Evidence: docs=0, tests=0, generated=5

## Scope Guardrails

- `mixed_product_domains`: `ok`
- `runtime_plus_tooling_or_infrastructure`: `ok`
- `large_generated_report_churn`: `warn`

- `.agents/god-plans/vision-god-plan.md` `other` `unknown`
- `.agents/god-plans/vision-god-plan.yaml` `other` `unknown`
- `apps/themuffinman/frontend/src/components/app/useAppTopbarState.ts` `other` `useAppTopbarState.ts`
- `apps/themuffinman/frontend/src/modules/chat/README.md` `other` `unknown`
- `apps/themuffinman/frontend/src/modules/social/composables/useCirclesView.ts` `frontend_composable` `unknown`
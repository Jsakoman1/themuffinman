# Open Plan Quick Index

## Purpose

One fast lookup surface for currently open plan files outside the Vision God Plan.

## Fast Query

Use this to refresh the view:

```bash
rg -n "^(## Status|status:)" .agents --glob '*plan*.md' --glob '*.yaml' | grep -Ei 'active|in_progress|in progress'
```

## Open Master Plans Outside God Plan

- `legacy-frontend-decommission-master-plan.md` - `Active.`
- `legacy-purge-master-plan.md` - open by goal, but it has no explicit status block yet and should be treated as a manual-review master plan.

## Open Child Plans Outside God Plan

- none after closing `vision-detail-continuity-plan.md`

## Open Master Plans Under God Plan

- `vision-adaptive-architecture-master-plan.md` - `Active under .agents/god-plans/vision-god-plan.yaml.`

## Closed But Relevant

- `vision-memory-and-context-master-plan.md` - complete
- `vision-capability-parity-plan.md` - complete

# Round 29: Drift Detection and Documentation Maintenance Controls

Status: control-design analysis. Reviewed: 2026-07-22.

## Conclusion

The repository already controls work-plan completion, target/inventory structure,
runtime acceptance, generated frontend contracts, documentation assertions, and
several code diagnostics. The remaining drift problem is cross-cutting: no single
check currently proves that a changed endpoint, entity, workflow, client surface,
provider, or release input has refreshed every related registry and canonical source.

## Control design

| Control | Trigger | Required result | Current state |
|---|---|---|---|
| Truth registry link audit | canonical source or registry change | all paths exist; owner/evidence/review fields present | design only |
| Interface evidence audit | controller/DTO/client/route changes | endpoint consumer class and evidence rows refreshed | partial diagnostics exist |
| Data/workflow impact audit | entity/repository/migration/service changes | ownership and workflow rows reviewed | design only |
| Evidence freshness audit | capability, test, runtime matrix changes | status remains separated from evidence class | inventory audit exists; graph link pending |
| Generated artifact audit | supported DTO/generator changes | contract generator/check runs; output explained | current generator validation exists |
| Operations/delivery audit | config, provider, dependency, build changes | registry and external-proof gap reviewed | design only |
| Master closeout audit | every nontrivial verified plan | system-map and affected registries reviewed | manual rule; automation pending |

## Operating rule

A registry is a relationship index, not a second truth system. Audits must verify
links, required fields, freshness markers, and impact obligations. They must not
infer capability completion, runtime proof, or deployed behavior from static code.

## Deferred implementation slices

DRIFT-001 through DRIFT-005 are recorded in the implementation backlog. They are
the safe implementation sequence after this analysis program: schema/link audit,
endpoint/evidence reconciliation, data/workflow impact audit, evidence freshness
enforcement, and delivery provenance controls.

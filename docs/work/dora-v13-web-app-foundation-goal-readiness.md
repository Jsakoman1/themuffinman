# Dora v1.3 web-app foundation goal readiness

Decision: ready to create a goal after `harden-v13-program` passes `make work-verify`.

The execution inventory is globally serial. Each item has one mapped task, direct predecessor dependency, exact required paths, a leaf validation command, and an evidence boundary. The plan does not require external approval because it excludes release, deployment, pins, and destructive database operations.

Expected first action after goal creation:

```text
make work-start plan=docs/work/dora-v13-atomic-hardening.yaml task=harden-v13-program
```

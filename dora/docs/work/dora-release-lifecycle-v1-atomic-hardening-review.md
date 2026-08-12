# Dora release lifecycle v1 atomic hardening review

| Item | Observable outcome | Exact owned paths | Direct dependency | Leaf validation | Boundary |
| --- | --- | --- | --- | --- | --- |
| `release-lifecycle-preflight` | Records the false historical subtree assumption and the safe replacement boundary. | Two preflight records only. | None. | `ruby test/atomic_plan_contract_test.rb && bin/dora doctor .dora/project.yaml` | No runtime, consumer, release, or remote mutation. |
| `release-pin-persistence` | An approved package update persists the reviewed target ref/checksum. | `ProjectUpgrade` and two upgrade tests. | Preflight verified. | Upgrade apply tests only. | No real consumer update. No authority expansion. |
| `release-operations-contract` | Durable guide, template and owner-maintained registry validate. | Four named Dora documentation/test files. | Pin persistence verified. | Registry test and Doctor. | No consumer update, release tag, remote or status claim. |

The execution inventory maps exactly one atomic task per serial item. Consumer
migrations are deliberately outside this source-repository master: each is a
separate owner-approved consumer change after the repaired Dora release is
published. A known-consumer registry is coordination metadata only and cannot
discover repositories or declare consumer verification.

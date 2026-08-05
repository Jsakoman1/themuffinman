# Dora v0.7 complete product-boundary analysis

## Decision

Dora v0.6 is a released bootstrap and delivery-control package. MuffinMan still
contains generic glue around Dora plugins and generic report writing. v0.7 must
extract that reusable mechanism, not product rules or product evidence.

## Extraction catalogue

| Candidate | Decision | Reason |
|---|---|---|
| JSON/Markdown audit report writing | extract | Output mechanics do not depend on a product domain. |
| Static plugin invocation and structured findings | extract | Plugins, input roots, output paths, and completion boundaries are portable. |
| Configuration, mapper, HTTP, navigation, hygiene, and dependency wrappers | delegate | Their algorithms are Dora-owned; MuffinMan retains only declared inputs and report destinations. |
| Repository-fetch audit | retain | Its persistence conventions and domain read surfaces are MuffinMan-specific. |
| Interface-evidence reconciliation | retain | It owns MuffinMan endpoint/evidence registries and product evidence meaning. |
| Capability, runtime, business, and domain-documentation audits | retain | They define MuffinMan product truth, permissions, workflows, and runtime proof. |
| Buildable Spring/Vue technical starter | extract | It is a generic implementation starting point with no business behavior. |

## Success definition

An independent consumer can create a buildable technical project, configure and
run Dora plugins with portable reports, and use the same controls without any
MuffinMan paths. MuffinMan declares product inputs and retains product-only audits,
registries, runtime evidence, and business meaning.

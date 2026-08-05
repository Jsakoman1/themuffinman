SHELL := /bin/zsh

.PHONY: help dev dev-stop dev-doctor clean-generated control-check audit-atomic-task-hardening context-search repository-map change-validation workspace-change-report audit-tool-catalog audit-template-freshness audit-work-artifact-schema audit-work-artifact-retention audit-intellij-mcp-routing tool-self-test

help:
	ruby scripts/tool-help.rb

dev:
	$(MAKE) -C apps/themuffinman dev

dev-stop:
	$(MAKE) -C apps/themuffinman dev-stop

dev-doctor:
	$(MAKE) -C apps/themuffinman dev-doctor

backend-test:
	$(MAKE) -C apps/themuffinman backend-test

backend-package:
	$(MAKE) -C apps/themuffinman backend-package

frontend-type-check:
	npm --prefix apps/themuffinman/frontend run type-check

frontend-build:
	npm --prefix apps/themuffinman/frontend run build

generate-frontend-contracts:
	npm --prefix apps/themuffinman/frontend run generate:contracts

validate-frontend-contracts:
	npm --prefix apps/themuffinman/frontend run validate:contracts

context-search:
	@if [ -z "$(q)" ]; then echo 'usage: make context-search q="search phrase"'; exit 1; fi
	ruby scripts/context-search.rb $(if $(mode),--mode "$(mode)") --budget "$(or $(budget),12000)" --max-files "$(or $(files),12)" --max-lines "$(or $(lines),8)" "$(q)"

repository-map:
	@ruby scripts/repository-map.rb $(if $(q),--query "$(q)" --max-output "$(or $(budget),20000)",--check)

change-validation:
	ruby scripts/change-validation.rb $(paths)

workspace-change-report:
	ruby scripts/audits/audit-workspace-change-inventory.rb

audit-tool-catalog:
	ruby scripts/audits/audit-tool-catalog.rb --check

audit-template-freshness:
	ruby scripts/audits/audit-template-freshness.rb

audit-work-artifact-schema:
	ruby scripts/audits/audit-work-artifact-schema.rb

audit-work-artifact-retention:
	ruby scripts/audits/audit-work-artifact-retention.rb --check docs/work-artifact-retention-review-2026-07-31.yaml

audit-intellij-mcp-routing:
	ruby scripts/audits/audit-intellij-mcp-routing.rb

tool-self-test:
	ruby scripts/tool-self-test.rb

audit-todo:
	ruby scripts/todo-audit.rb

audit-agent-safety:
	$(MAKE) -C apps/themuffinman audit-agent-safety

audit-backend:
	ruby scripts/audits/audit-api-contract-drift.rb
	ruby scripts/audits/audit-read-surface-inventory.rb
	ruby scripts/audits/audit-repository-fetch.rb
	ruby scripts/audits/audit-mapper-usage.rb
	ruby scripts/audits/audit-mutation-safety.rb
	$(MAKE) audit-module-dependency-direction

audit-frontend:
	ruby scripts/audits/audit-frontend-interaction-contract.rb
	ruby scripts/audits/audit-ui-entrypoints.rb
	ruby scripts/audits/audit-endpoint-callsite-linker.rb
	ruby scripts/audits/audit-frontend-route-surfaces.rb
	ruby scripts/audits/audit-frontend-stale-surfaces.rb
	$(MAKE) audit-runtime-tools

audit-runtime-tools:
	ruby scripts/audits/audit-runtime-tools.rb

audit-main-surfaces-plan:
	ruby scripts/audits/audit-main-surfaces-plan-preflight.rb

audit-atomic-task-hardening:
	ruby scripts/audits/audit-atomic-task-hardening.rb

audit-docs: audit-work-plan-recursion
	ruby scripts/audits/audit-docs-as-tests.rb

audit-plan-coverage:
	ruby scripts/audits/audit-plan-coverage.rb

audit-target-capability-catalog:
	ruby scripts/audits/audit-target-capability-catalog.rb

audit-target-capability-coverage:
	@ruby scripts/audits/audit-target-capability-coverage.rb

audit-inventory-freshness:
	@ruby scripts/audits/audit-inventory-freshness.rb

audit-truth-registry:
	@ruby scripts/audits/audit-truth-registry-integrity.rb

audit-interface-evidence:
	@ruby scripts/audits/audit-interface-evidence-reconciliation.rb

audit-data-workflow-impact:
	@ruby scripts/audits/audit-data-workflow-impact.rb

audit-capability-evidence:
	@ruby scripts/audits/audit-capability-evidence-freshness.rb

audit-module-dependency-direction:
	@ruby scripts/audits/audit-module-dependency-direction.rb

audit-canonical-source-integrity:
	@ruby scripts/audits/audit-canonical-source-integrity.rb

capability-evidence-coverage:
	@ruby scripts/audits/generate-capability-evidence-coverage.rb

audit-configuration-environment-drift:
	@ruby scripts/audits/audit-configuration-environment-drift.rb

audit-delivery-provenance:
	@ruby scripts/audits/audit-delivery-provenance.rb

system-map-impact:
	@ruby scripts/audits/generate-system-map-change-impact.rb

generate-target-capability-slices:
	@ruby scripts/audits/generate-target-capability-slices.rb

audit-runtime-acceptance:
	ruby scripts/audits/audit-runtime-acceptance.rb

audit-native-client-handoff:
	ruby scripts/audits/audit-native-client-handoff.rb

audit-work-plan-recursion:
	ruby scripts/audits/audit-work-plan-recursion.rb

clean-generated:
	ruby scripts/clean-generated-artifacts.rb

control-check:
	ruby scripts/validate-control-sources.rb
	$(MAKE) repository-map
	$(MAKE) audit-tool-catalog
	$(MAKE) audit-plan-coverage
	$(MAKE) audit-docs
	$(MAKE) audit-inventory-freshness
	$(MAKE) audit-target-capability-catalog
	$(MAKE) audit-atomic-task-hardening
	$(MAKE) audit-truth-registry
	$(MAKE) audit-canonical-source-integrity
	$(MAKE) audit-capability-evidence
	$(MAKE) audit-runtime-acceptance
	$(MAKE) audit-main-surfaces-plan
	$(MAKE) clean-generated

audit-all: audit-backend audit-frontend audit-docs tool-self-test clean-generated

work-create:
	@if [ -z "$(id)" ] || [ -z "$(title)" ]; then echo 'usage: make work-create id=<id> title="<title>"'; exit 1; fi
	@mkdir -p docs/work
	@sed -e "s/replace-me/$(id)/" -e "s/Replace me/$(title)/g" docs/work-plan.template.yaml > docs/work/$(id).yaml

master-create:
	@if [ -z "$(id)" ] || [ -z "$(title)" ]; then echo 'usage: make master-create id=<id> title="<title>"'; exit 1; fi
	@mkdir -p docs/work
	@sed -e "s/replace-me/$(id)/" -e "s/Replace me/$(title)/g" docs/master-plan.template.yaml > docs/work/$(id).yaml

work-verify:
	@if [ -z "$(plan)" ]; then echo "usage: make work-verify plan=<work-plan>"; exit 1; fi
	@if [ -n "$(task)" ]; then dora/bin/dora work-verify .dora/project.yaml plan="$(plan)" task="$(task)"; else dora/bin/dora work-verify .dora/project.yaml plan="$(plan)"; fi

work-start:
	@if [ -z "$(plan)" ] || [ -z "$(task)" ]; then echo "usage: make work-start plan=<work-plan> task=<task-id>"; exit 1; fi
	dora/bin/dora work-start .dora/project.yaml plan="$(plan)" task="$(task)"

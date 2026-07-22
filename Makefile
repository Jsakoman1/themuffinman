SHELL := /bin/zsh

.PHONY: dev dev-stop dev-doctor

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
	ruby scripts/audits/audit-ui-entrypoints.rb
	ruby scripts/audits/audit-endpoint-callsite-linker.rb
	ruby scripts/audits/audit-frontend-route-surfaces.rb
	ruby scripts/audits/audit-frontend-stale-surfaces.rb
	ruby scripts/audits/audit-frontend-state-logic-duplication.rb
	ruby scripts/audits/audit-duplicate-logic.rb
	ruby scripts/audits/audit-permission-rule-duplication.rb

audit-main-surfaces-plan:
	ruby scripts/audits/audit-main-surfaces-plan-preflight.rb

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

audit-tests:
	ruby scripts/audits/audit-contract-test-gaps.rb
	ruby scripts/audits/audit-test-fixture-duplication.rb

audit-work-plan-recursion:
	ruby scripts/audits/audit-work-plan-recursion.rb

audit-impact:
	ruby scripts/audits/audit-change-impact-preflight.rb
	ruby scripts/audits/score-changeset-risk.rb

audit-all: audit-backend audit-frontend audit-docs audit-tests audit-impact

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
	@if [ -n "$(task)" ]; then ruby scripts/verify-work.rb plan="$(plan)" task="$(task)"; else ruby scripts/verify-work.rb plan="$(plan)"; fi

work-start:
	@if [ -z "$(plan)" ] || [ -z "$(task)" ]; then echo "usage: make work-start plan=<work-plan> task=<task-id>"; exit 1; fi
	ruby scripts/verify-work.rb action=start plan="$(plan)" task="$(task)"

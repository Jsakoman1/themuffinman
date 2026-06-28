SHELL := /bin/zsh

.PHONY: dev backend-dev backend-test backend-package backend-bootstrap-example audit-agent-safety bootstrap-feature-work feature-closeout-audit generate-agent-operating-model generate-agent-artifacts

dev:
	@set -a; \
	[[ -f .env.backend.dev ]] && source .env.backend.dev; \
	[[ -f .env.backend.dev.local ]] && source .env.backend.dev.local; \
	set +a; \
	export SPRING_PROFILES_ACTIVE="$${SPRING_PROFILES_ACTIVE:-dev}"; \
	export SIDEQUEST_JWT_SECRET="$${SIDEQUEST_JWT_SECRET:-sidequest-dev-secret-sidequest-dev-secret-123456}"; \
	export SIDEQUEST_CORS_ALLOWED_ORIGINS="$${SIDEQUEST_CORS_ALLOWED_ORIGINS:-http://localhost:5173}"; \
	trap 'kill 0' INT TERM EXIT; \
	if lsof -nP -iTCP:8080 -sTCP:LISTEN >/dev/null 2>&1; then \
		echo "[backend] port 8080 is already in use, skipping local backend start"; \
	else \
		( ./mvnw spring-boot:run ) & \
	fi; \
	( cd frontend && npm run dev ) & \
	wait

backend-dev:
	@set -a; \
	[[ -f .env.backend.dev ]] && source .env.backend.dev; \
	[[ -f .env.backend.dev.local ]] && source .env.backend.dev.local; \
	set +a; \
	export SPRING_PROFILES_ACTIVE="$${SPRING_PROFILES_ACTIVE:-dev}"; \
	export SIDEQUEST_JWT_SECRET="$${SIDEQUEST_JWT_SECRET:-sidequest-dev-secret-sidequest-dev-secret-123456}"; \
	export SIDEQUEST_CORS_ALLOWED_ORIGINS="$${SIDEQUEST_CORS_ALLOWED_ORIGINS:-http://localhost:5173}"; \
	./mvnw spring-boot:run

backend-test:
	./mvnw test

backend-package:
	./mvnw package

backend-bootstrap-example:
	@echo "Example production bootstrap command:"
	@echo "SIDEQUEST_JWT_SECRET=replace-with-long-random-secret \\"
	@echo "SIDEQUEST_CORS_ALLOWED_ORIGINS=https://your-app.example.com \\"
	@echo "SIDEQUEST_BOOTSTRAP_ADMIN_ENABLED=true \\"
	@echo "SIDEQUEST_BOOTSTRAP_ADMIN_EMAIL=owner@example.com \\"
	@echo "SIDEQUEST_BOOTSTRAP_ADMIN_USERNAME=owner \\"
	@echo "SIDEQUEST_BOOTSTRAP_ADMIN_PASSWORD=replace-this \\"
	@echo "./mvnw spring-boot:run"

audit-agent-safety:
	./mvnw -Dtest=AgentOperatingModelValidationTest,AdminAgentPlaygroundServiceTest,AdminAgentGoldenPromptMatrixTest,AdminAgentCapabilityBoundaryTest,AppUserMgrTest,ChatServiceTest,CircleServiceTest,QuestMgrTest,QuestNewsMgrTest,QuestApplicationServiceTest test
	cd frontend && npm run type-check
	cd frontend && npm run validate:admin-agent-ui
	cd frontend && npm run build

bootstrap-feature-work:
	@if [ -z "$(topic)" ]; then echo "usage: make bootstrap-feature-work topic=<short-feature-topic> [risk=<tier>] [mode=<small-change|major-change>] [impact=<cosmetic|contract-neutral-refactor|logic-drift>] [profiles=<csv>]"; exit 1; fi
	/bin/zsh ../../scripts/bootstrap-feature-work.sh "$(topic)" "$(if $(risk),$(risk),medium)" "$(if $(mode),$(mode),major-change)" "$(if $(impact),$(impact),logic-drift)" "$(if $(profiles),$(profiles),backend-logic)"

feature-closeout-audit:
	@if [ -z "$(manifest)" ]; then echo "usage: make feature-closeout-audit manifest=<manifest-file>"; exit 1; fi
	/bin/zsh ../../scripts/feature-closeout-audit.sh "$(manifest)"

generate-agent-operating-model:
	ruby ../../scripts/generate-agent-operating-model.rb

generate-agent-artifacts:
	ruby ../../scripts/generate-backend-audit-inventory.rb
	ruby ../../scripts/generate-agent-endpoint-inventory.rb
	ruby ../../scripts/generate-automation-read-model-inventory.rb
	ruby ../../scripts/generate-source-of-truth-audit.rb

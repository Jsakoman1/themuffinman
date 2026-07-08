SHELL := /bin/zsh

.PHONY: dev dev-doctor dev-storage dev-storage-down backend-dev backend-test backend-package backend-bootstrap-example audit-agent-safety bootstrap-feature-work feature-closeout-audit generate-agent-operating-model generate-agent-artifacts

dev:
	@set -a; \
	[[ -f .env.backend.dev ]] && source .env.backend.dev; \
	[[ -f .env.backend.dev.local ]] && source .env.backend.dev.local; \
	set +a; \
	export SPRING_PROFILES_ACTIVE="$${SPRING_PROFILES_ACTIVE:-dev}"; \
	export SIDEQUEST_JWT_SECRET="$${SIDEQUEST_JWT_SECRET:-sidequest-dev-secret-sidequest-dev-secret-123456}"; \
	export SIDEQUEST_CORS_ALLOWED_ORIGINS="$${SIDEQUEST_CORS_ALLOWED_ORIGINS:-http://localhost:5173}"; \
	echo "[dev] object storage enabled=$${SIDEQUEST_OBJECT_STORAGE_ENABLED:-false} provider=$${SIDEQUEST_OBJECT_STORAGE_PROVIDER:-local}"; \
	if [[ "$${SIDEQUEST_OBJECT_STORAGE_ENABLED:-false}" == "true" && "$${SIDEQUEST_OBJECT_STORAGE_PROVIDER:-local}" == "s3" ]]; then \
		echo "[dev] starting local MinIO because object storage provider is s3"; \
		$(MAKE) dev-storage; \
	elif [[ "$${SIDEQUEST_OBJECT_STORAGE_ENABLED:-false}" == "true" && "$${SIDEQUEST_OBJECT_STORAGE_PROVIDER:-local}" == "local" ]]; then \
		echo "[dev] using local-disk object storage; no MinIO container needed"; \
	else \
		echo "[dev] chat attachments storage is disabled"; \
	fi; \
	backend_pid=""; \
	frontend_pid=""; \
	cleanup() { \
		[[ -n "$$backend_pid" ]] && kill "$$backend_pid" >/dev/null 2>&1 || true; \
		[[ -n "$$frontend_pid" ]] && kill "$$frontend_pid" >/dev/null 2>&1 || true; \
	}; \
	trap cleanup INT TERM EXIT; \
	if lsof -nP -iTCP:8080 -sTCP:LISTEN >/dev/null 2>&1; then \
		echo "[backend] port 8080 is already in use, skipping local backend start"; \
	else \
		( ./mvnw spring-boot:run ) & backend_pid="$$!"; \
	fi; \
	if lsof -nP -iTCP:5173 -sTCP:LISTEN >/dev/null 2>&1; then \
		echo "[frontend] port 5173 is already in use, skipping local frontend start"; \
	else \
		( cd frontend && npm run dev ) & frontend_pid="$$!"; \
	fi; \
	wait

dev-doctor:
	@set -a; \
	[[ -f .env.backend.dev ]] && source .env.backend.dev; \
	[[ -f .env.backend.dev.local ]] && source .env.backend.dev.local; \
	set +a; \
	echo "SPRING_PROFILES_ACTIVE=$${SPRING_PROFILES_ACTIVE:-dev}"; \
	echo "SIDEQUEST_OBJECT_STORAGE_ENABLED=$${SIDEQUEST_OBJECT_STORAGE_ENABLED:-false}"; \
	echo "SIDEQUEST_OBJECT_STORAGE_PROVIDER=$${SIDEQUEST_OBJECT_STORAGE_PROVIDER:-local}"; \
	echo "SIDEQUEST_OBJECT_STORAGE_ENDPOINT=$${SIDEQUEST_OBJECT_STORAGE_ENDPOINT:-}"; \
	echo "SIDEQUEST_OBJECT_STORAGE_BUCKET=$${SIDEQUEST_OBJECT_STORAGE_BUCKET:-}"; \
	echo "SIDEQUEST_OBJECT_STORAGE_LOCAL_BASE_PATH=$${SIDEQUEST_OBJECT_STORAGE_LOCAL_BASE_PATH:-/tmp/themuffinman-object-storage}"; \
	if command -v docker >/dev/null 2>&1; then \
		echo "[doctor] docker: available"; \
	else \
		echo "[doctor] docker: missing"; \
	fi; \
	if docker compose version >/dev/null 2>&1; then \
		echo "[doctor] docker compose: available"; \
	else \
		echo "[doctor] docker compose: missing"; \
	fi; \
	if lsof -nP -iTCP:8080 -sTCP:LISTEN >/dev/null 2>&1; then \
		echo "[doctor] backend port 8080: occupied"; \
	else \
		echo "[doctor] backend port 8080: free"; \
	fi; \
	if lsof -nP -iTCP:5173 -sTCP:LISTEN >/dev/null 2>&1; then \
		echo "[doctor] frontend port 5173: occupied"; \
	else \
		echo "[doctor] frontend port 5173: free"; \
	fi; \
	if [[ "$${SIDEQUEST_OBJECT_STORAGE_ENABLED:-false}" == "true" && "$${SIDEQUEST_OBJECT_STORAGE_PROVIDER:-local}" == "s3" ]]; then \
		if lsof -nP -iTCP:9000 -sTCP:LISTEN >/dev/null 2>&1; then \
			echo "[doctor] minio api port 9000: occupied"; \
		else \
			echo "[doctor] minio api port 9000: free"; \
		fi; \
		if lsof -nP -iTCP:9001 -sTCP:LISTEN >/dev/null 2>&1; then \
			echo "[doctor] minio console port 9001: occupied"; \
		else \
			echo "[doctor] minio console port 9001: free"; \
		fi; \
	fi

dev-storage:
	docker compose -f docker-compose.object-storage.dev.yaml up -d

dev-storage-down:
	docker compose -f docker-compose.object-storage.dev.yaml down

backend-dev:
	@set -a; \
	[[ -f .env.backend.dev ]] && source .env.backend.dev; \
	[[ -f .env.backend.dev.local ]] && source .env.backend.dev.local; \
	set +a; \
	export SPRING_PROFILES_ACTIVE="$${SPRING_PROFILES_ACTIVE:-dev}"; \
	export SIDEQUEST_JWT_SECRET="$${SIDEQUEST_JWT_SECRET:-sidequest-dev-secret-sidequest-dev-secret-123456}"; \
	export SIDEQUEST_CORS_ALLOWED_ORIGINS="$${SIDEQUEST_CORS_ALLOWED_ORIGINS:-http://localhost:5173}"; \
	echo "[backend-dev] object storage enabled=$${SIDEQUEST_OBJECT_STORAGE_ENABLED:-false} provider=$${SIDEQUEST_OBJECT_STORAGE_PROVIDER:-local}"; \
	if [[ "$${SIDEQUEST_OBJECT_STORAGE_ENABLED:-false}" == "true" && "$${SIDEQUEST_OBJECT_STORAGE_PROVIDER:-local}" == "s3" ]]; then \
		echo "[backend-dev] starting local MinIO because object storage provider is s3"; \
		$(MAKE) dev-storage; \
	elif [[ "$${SIDEQUEST_OBJECT_STORAGE_ENABLED:-false}" == "true" && "$${SIDEQUEST_OBJECT_STORAGE_PROVIDER:-local}" == "local" ]]; then \
		echo "[backend-dev] using local-disk object storage; no MinIO container needed"; \
	else \
		echo "[backend-dev] chat attachments storage is disabled"; \
	fi; \
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

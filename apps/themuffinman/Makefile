SHELL := /bin/zsh

.PHONY: dev backend-dev backend-test backend-package backend-bootstrap-example

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

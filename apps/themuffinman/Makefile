SHELL := /bin/zsh

.PHONY: dev backend-dev backend-test backend-package backend-bootstrap-example

dev:
	@trap 'kill 0' INT TERM EXIT; \
	( SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run ) & \
	( cd frontend && npm run dev ) & \
	wait

backend-dev:
	SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run

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

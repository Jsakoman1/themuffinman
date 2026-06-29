# Validation Matrix

- Generated At: `2026-06-29T12:47:29Z`
## `categories`

- `{:category: "backend_controller", :commands: ["./mvnw test", "make generate-agent-artifacts", "make audit-api-contract-drift"]}`
- `{:category: "backend_service", :commands: ["./mvnw test", "make audit-read-surface-inventory", "make audit-repository-fetch"]}`
- `{:category: "backend_repository", :commands: ["./mvnw test", "make audit-repository-fetch"]}`
- `{:category: "backend_mapper", :commands: ["./mvnw test", "make audit-mapper-usage"]}`
- `{:category: "backend_dto", :commands: ["./mvnw test", "npm run type-check", "npm run build", "make generate-agent-artifacts"]}`
- `{:category: "backend_model", :commands: ["./mvnw test", "make audit-migration-entity-drift"]}`
- `{:category: "frontend_api", :commands: ["npm run type-check", "npm run build", "make audit-api-contract-drift"]}`
- `{:category: "frontend_view", :commands: ["npm run type-check", "npm run build", "make audit-frontend-route-surfaces"]}`
- `{:category: "frontend_composable", :commands: ["npm run type-check", "npm run build", "make audit-async-mutation-flow"]}`
- `{:category: "frontend_contract", :commands: ["npm run type-check", "npm run build"]}`
- `{:category: "docs", :commands: ["./mvnw test", "make audit-documentation", "make audit-doc-canonical-phrases"]}`
- `{:category: "script", :commands: ["ruby -c <script>", "make audit-summary-index"]}`


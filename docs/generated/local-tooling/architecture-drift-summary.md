# Architecture Drift Audit

- Generated At: `2026-07-03T09:17:30Z`
- Mode: `report_first`
- `thresholds`: `6` entries
## `backend_entries`

- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionConversationService.java", :kind: "service", :lines: 4077, :public_methods: 5, :responsibility_markers: ["query", "mutation", "policy"], :flags: ["oversized_service", "mixed_responsibilities"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionCapabilityPreviewService.java", :kind: "service", :lines: 1632, :public_methods: 44, :responsibility_markers: ["query", "mutation", "policy", "mapping"], :flags: ["oversized_service", "many_public_methods", "mixed_responsibilities"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionSlotService.java", :kind: "service", :lines: 796, :public_methods: 3, :responsibility_markers: ["query", "mutation", "policy"], :flags: ["oversized_service", "mixed_responsibilities"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionSemanticOrchestrationContextService.java", :kind: "service", :lines: 559, :public_methods: 5, :responsibility_markers: ["query", "mutation"], :flags: ["oversized_service"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionPromptUnderstandingService.java", :kind: "service", :lines: 553, :public_methods: 3, :responsibility_markers: ["query", "mutation"], :flags: ["oversized_service"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionScheduleParserService.java", :kind: "service", :lines: 550, :public_methods: 5, :responsibility_markers: ["query"], :flags: ["oversized_service"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionSemanticRouteCatalogService.java", :kind: "service", :lines: 445, :public_methods: 1, :responsibility_markers: ["mutation"], :flags: ["oversized_service"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/chat/service/ChatService.java", :kind: "service", :lines: 403, :public_methods: 8, :responsibility_markers: ["query", "mutation", "policy"], :flags: ["mixed_responsibilities"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleService.java", :kind: "service", :lines: 285, :public_methods: 33, :responsibility_markers: ["query", "mutation", "policy"], :flags: ["many_public_methods", "mixed_responsibilities"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/social/controller/CircleController.java", :kind: "controller", :lines: 261, :public_methods: 21, :responsibility_markers: ["query", "mutation", "policy"], :flags: ["oversized_controller", "many_public_methods", "mixed_responsibilities"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/QuestStateTransitionService.java", :kind: "service", :lines: 257, :public_methods: 10, :responsibility_markers: ["query", "mutation", "policy"], :flags: ["mixed_responsibilities"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/location/service/LocationLookupService.java", :kind: "service", :lines: 252, :public_methods: 4, :responsibility_markers: ["query", "mutation", "notification"], :flags: ["mixed_responsibilities"]}`

## `frontend_entries`


## `doc_entries`

- `{:file: "docs/agent-operating-model.md", :section: "Current Scope", :level: 2, :lines: 216, :flags: ["oversized_doc_section"]}`
- `{:file: "docs/domain-technical.md", :section: "Control-System Technical Notes", :level: 2, :lines: 142, :flags: ["oversized_doc_section"]}`
- `{:file: "docs/domain-technical.md", :section: "Domain Areas", :level: 2, :lines: 115, :flags: ["oversized_doc_section"]}`

- Total Findings: `33`

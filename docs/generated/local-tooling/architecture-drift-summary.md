# Architecture Drift Audit

- Generated At: `2026-07-01T14:49:32Z`
- Mode: `report_first`
- `thresholds`: `6` entries
## `backend_entries`

- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/service/AdminAgentPlaygroundService.java", :kind: "service", :lines: 1036, :public_methods: 3, :responsibility_markers: ["query", "mutation", "policy", "notification"], :flags: ["oversized_service", "mixed_responsibilities"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionConversationService.java", :kind: "service", :lines: 907, :public_methods: 5, :responsibility_markers: ["query", "mutation", "policy"], :flags: ["oversized_service", "mixed_responsibilities"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionSlotService.java", :kind: "service", :lines: 703, :public_methods: 3, :responsibility_markers: ["query", "mutation", "policy"], :flags: ["oversized_service", "mixed_responsibilities"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionScheduleParserService.java", :kind: "service", :lines: 441, :public_methods: 5, :responsibility_markers: ["query"], :flags: ["oversized_service"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/chat/service/ChatService.java", :kind: "service", :lines: 403, :public_methods: 8, :responsibility_markers: ["query", "mutation", "policy"], :flags: ["mixed_responsibilities"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionPromptUnderstandingService.java", :kind: "service", :lines: 292, :public_methods: 1, :responsibility_markers: ["query", "mutation", "policy"], :flags: ["mixed_responsibilities"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleService.java", :kind: "service", :lines: 285, :public_methods: 33, :responsibility_markers: ["query", "mutation", "policy"], :flags: ["many_public_methods", "mixed_responsibilities"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/social/controller/CircleController.java", :kind: "controller", :lines: 261, :public_methods: 21, :responsibility_markers: ["query", "mutation", "policy"], :flags: ["oversized_controller", "many_public_methods", "mixed_responsibilities"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/QuestStateTransitionService.java", :kind: "service", :lines: 257, :public_methods: 10, :responsibility_markers: ["query", "mutation", "policy"], :flags: ["mixed_responsibilities"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/location/service/LocationLookupService.java", :kind: "service", :lines: 252, :public_methods: 4, :responsibility_markers: ["query", "mutation", "notification"], :flags: ["mixed_responsibilities"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/QuestNewsService.java", :kind: "service", :lines: 223, :public_methods: 14, :responsibility_markers: ["query", "mutation"], :flags: ["many_public_methods"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/AppUserService.java", :kind: "service", :lines: 221, :public_methods: 7, :responsibility_markers: ["query", "mutation", "policy", "mapping"], :flags: ["mixed_responsibilities"]}`

## `frontend_entries`

- `{:file: "apps/themuffinman/frontend/src/modules/vision/views/VisionSurfaceModernView.vue", :kind: "vue_view", :lines: 729, :product_logic_hits: 8, :flags: ["oversized_vue_view"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/vision/views/VisionUserSettingsView.vue", :kind: "vue_view", :lines: 579, :product_logic_hits: 0, :flags: ["oversized_vue_view"]}`
- `{:file: "apps/themuffinman/frontend/src/modules/vision/views/VisionUserProfileView.vue", :kind: "vue_view", :lines: 407, :product_logic_hits: 2, :flags: ["oversized_vue_view"]}`

## `doc_entries`

- `{:file: "docs/agent-operating-model.md", :section: "Current Scope", :level: 2, :lines: 216, :flags: ["oversized_doc_section"]}`
- `{:file: "docs/domain-technical.md", :section: "Control-System Technical Notes", :level: 2, :lines: 141, :flags: ["oversized_doc_section"]}`

- Total Findings: `33`

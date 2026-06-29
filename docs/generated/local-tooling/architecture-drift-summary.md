# Architecture Drift Audit

- Generated At: `2026-06-29T17:54:45Z`
- Mode: `report_first`
- `thresholds`: `6` entries
## `backend_entries`

- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/service/AdminAgentPlaygroundService.java", :kind: "service", :lines: 971, :public_methods: 2, :responsibility_markers: ["query", "mutation", "policy", "notification"], :flags: ["oversized_service", "mixed_responsibilities"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/location/service/LocationSettingsService.java", :kind: "service", :lines: 561, :public_methods: 15, :responsibility_markers: ["query", "mutation", "policy", "mapping"], :flags: ["oversized_service", "many_public_methods", "mixed_responsibilities"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/chat/service/ChatService.java", :kind: "service", :lines: 403, :public_methods: 8, :responsibility_markers: ["query", "mutation", "policy"], :flags: ["mixed_responsibilities"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleService.java", :kind: "service", :lines: 389, :public_methods: 33, :responsibility_markers: ["query", "mutation", "policy", "mapping"], :flags: ["many_public_methods", "mixed_responsibilities"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestService.java", :kind: "service", :lines: 380, :public_methods: 17, :responsibility_markers: ["query", "mutation"], :flags: ["many_public_methods"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationService.java", :kind: "service", :lines: 353, :public_methods: 15, :responsibility_markers: ["query", "mutation", "policy"], :flags: ["many_public_methods", "mixed_responsibilities"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/social/controller/CircleController.java", :kind: "controller", :lines: 261, :public_methods: 21, :responsibility_markers: ["query", "mutation", "policy"], :flags: ["oversized_controller", "many_public_methods", "mixed_responsibilities"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestStateTransitionService.java", :kind: "service", :lines: 257, :public_methods: 10, :responsibility_markers: ["query", "mutation", "policy"], :flags: ["mixed_responsibilities"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/location/service/LocationLookupService.java", :kind: "service", :lines: 242, :public_methods: 4, :responsibility_markers: ["query", "mutation", "notification"], :flags: ["mixed_responsibilities"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardService.java", :kind: "service", :lines: 229, :public_methods: 2, :responsibility_markers: ["query", "mutation", "policy", "mapping"], :flags: ["mixed_responsibilities"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestNewsService.java", :kind: "service", :lines: 223, :public_methods: 14, :responsibility_markers: ["query", "mutation"], :flags: ["many_public_methods"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/AppUserService.java", :kind: "service", :lines: 221, :public_methods: 7, :responsibility_markers: ["query", "mutation", "policy", "mapping"], :flags: ["mixed_responsibilities"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestValidationService.java", :kind: "service", :lines: 205, :public_methods: 10, :responsibility_markers: ["query", "mutation", "policy"], :flags: ["mixed_responsibilities"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleRelationService.java", :kind: "service", :lines: 186, :public_methods: 7, :responsibility_markers: ["query", "mutation", "policy", "mapping"], :flags: ["mixed_responsibilities"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/UserReviewService.java", :kind: "service", :lines: 168, :public_methods: 3, :responsibility_markers: ["query", "mutation", "policy", "mapping"], :flags: ["mixed_responsibilities"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/QuestApplicationController.java", :kind: "controller", :lines: 135, :public_methods: 12, :responsibility_markers: ["query", "mutation", "policy"], :flags: ["mixed_responsibilities"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/business/service/BusinessProfileService.java", :kind: "service", :lines: 121, :public_methods: 4, :responsibility_markers: ["query", "mutation", "mapping"], :flags: ["mixed_responsibilities"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestExecutionPrimitiveService.java", :kind: "service", :lines: 106, :public_methods: 14, :responsibility_markers: ["query", "mutation", "policy"], :flags: ["many_public_methods", "mixed_responsibilities"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/identity/controller/AppUserController.java", :kind: "controller", :lines: 104, :public_methods: 10, :responsibility_markers: ["query", "mutation", "policy", "mapping"], :flags: ["mixed_responsibilities"]}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleMembershipService.java", :kind: "service", :lines: 102, :public_methods: 6, :responsibility_markers: ["query", "mutation", "policy"], :flags: ["mixed_responsibilities"]}`

## `frontend_entries`


## `doc_entries`

- `{:file: "docs/agent-operating-model.md", :section: "Current Scope", :level: 2, :lines: 203, :flags: ["oversized_doc_section"]}`
- `{:file: "docs/domain-technical.md", :section: "Control-System Technical Notes", :level: 2, :lines: 139, :flags: ["oversized_doc_section"]}`

- Total Findings: `25`

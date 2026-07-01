# Backend Dead Code Audit

- Generated At: `2026-07-01T14:49:30Z`
- Java Files Scanned: `391`
## `high_confidence`

- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/chat/service/ChatRetentionService.java", :class_name: "ChatRetentionService", :refs: 1, :confidence: "high_confidence"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/common/dto/LabelValueDTO.java", :class_name: "LabelValueDTO", :refs: 1, :confidence: "high_confidence"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/identity/security/RepositoryUserDetailsService.java", :class_name: "RepositoryUserDetailsService", :refs: 1, :confidence: "high_confidence"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/social/dto/CircleCandidateDTO.java", :class_name: "CircleCandidateDTO", :refs: 1, :confidence: "high_confidence"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/QuestNewsRetentionService.java", :class_name: "QuestNewsRetentionService", :refs: 1, :confidence: "high_confidence"}`

## `review_needed`

- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/common/event/SpringDomainEventPublisher.java", :class_name: "SpringDomainEventPublisher", :refs: 2, :confidence: "review_needed"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/vision/model/VisionSlotDataConverter.java", :class_name: "VisionSlotDataConverter", :refs: 2, :confidence: "review_needed"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionCanvasAssembler.java", :method: "formatScheduledAt", :confidence: "review_needed"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionScheduleParserService.java", :method: "containsTimeSignal", :confidence: "review_needed"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionSlotService.java", :method: "shouldAutoFillVisibility", :confidence: "review_needed"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionSlotService.java", :method: "shouldAutoFillSchedule", :confidence: "review_needed"}`


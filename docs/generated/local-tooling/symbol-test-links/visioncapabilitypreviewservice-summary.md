# Symbol Test Links VisionCapabilityPreviewService

- Next action: `cd apps/themuffinman && ./mvnw test -Dtest=VisionCapabilityPreviewServiceAliasResolutionTest,VisionCapabilityPreviewServiceTest,VisionConversationServiceTest,VisionConversationSnapshotSupportTest,VisionCapabilityPreviewSupportTest`, `cd apps/themuffinman && ./mvnw test -Dtest=VisionLearningEvaluationHarnessTest,VisionConversationServiceTest`

## Details

- Direct tests: apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionCapabilityPreviewServiceAliasResolutionTest.java | apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionCapabilityPreviewServiceTest.java | apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionConversationServiceTest.java
- Direct tests more: 1
- Nearby tests: apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionCapabilityPreviewSupportTest.java
- Scenario hits: id: vision-learning-eval-harness | domain: common | risk: medium | scenario: Preference confidence decay, learned-memory recall, and clarification wording should stay stable while weak ambiguous follow-ups keep the current topic thread. | test_files: apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionLearningEvaluationHarnessTest.java, apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionConversationServiceTest.java | commands: cd apps/themuffinman && ./mvnw test -Dtest=VisionLearningEvaluationHarnessTest,VisionConversationServiceTest
